package com.github.karsaig.approvalcrest.matcher.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import com.github.karsaig.approvalcrest.FileMatcherConfig;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;

/**
 * Utility class with methods for creating the JSON files for
 * {@link JsonMatcher}.
 *
 * @author Andras_Gyuro
 */
public class FileStoreMatcherUtils {

    public static final char SEPARATOR = '-';
    private static final String APPROVED_NAME_PART = "approved";
    private static final String NOT_APPROVED_NAME_PART = "not-approved";
    private static final String POINTER_PREFIX = "/*pointer:";
    private static final int MAX_POINTER_DEPTH = 5;
    private static final Set<PosixFilePermission> APPROVED_FILE_PERMISSIONS = Collections.unmodifiableSet(EnumSet.of(OTHERS_READ, OTHERS_WRITE, GROUP_READ, GROUP_WRITE, OWNER_READ, OWNER_WRITE));
    private static final Set<PosixFilePermission> APPROVED_DIRECTORY_PERMISSIONS = Collections.unmodifiableSet(EnumSet.allOf(PosixFilePermission.class));
    private final String fileType;
    private final String fileExtension;
    private final FileMatcherConfig fileMatcherConfig;

    public FileStoreMatcherUtils(String fileType, FileMatcherConfig fileMatcherConfig) {
        this.fileType = fileType;
        this.fileExtension = "." + fileType;
        this.fileMatcherConfig = fileMatcherConfig;
    }

    /**
     * Creates file with '-not-approved' suffix and .json extension and writes
     * the jsonObject in it.
     *
     * @param fileNameWithPath specifies the name of the file with full path (relative to
     *                         project root)
     * @param jsonObject       the file's content
     * @param comment          the first line of file
     * @return the filename
     * @throws IOException exception thrown when failed to create the file
     */
    public CreatedFile createNotApproved(Path fileNameWithPath, String fileNameWithRelativePath, String jsonObject, String comment)
            throws IOException {
        CreatedFile fileAndInfo = getFullFileName(fileNameWithPath, fileNameWithRelativePath, false);
        Path parent = fileAndInfo.getFileName().getParent();
        if (isPosixCompatible(parent)) {
            Files.createDirectories(parent, PosixFilePermissions.asFileAttribute(APPROVED_DIRECTORY_PERMISSIONS));
        } else {
            Files.createDirectories(parent);
        }
        writeToFile(fileAndInfo.getFileName(), jsonObject, comment);
        return fileAndInfo;
    }

    /**
     * Creates a not-approved pointer file referencing the given canonical path.
     */
    public CreatedFile createNotApprovedPointer(Path fileNameWithPath, String fileNameWithRelativePath, String comment, String canonicalRelativePath)
            throws IOException {
        CreatedFile fileAndInfo = getFullFileName(fileNameWithPath, fileNameWithRelativePath, false);
        Path parent = fileAndInfo.getFileName().getParent();
        if (isPosixCompatible(parent)) {
            Files.createDirectories(parent, PosixFilePermissions.asFileAttribute(APPROVED_DIRECTORY_PERMISSIONS));
        } else {
            Files.createDirectories(parent);
        }
        writePointerToFile(fileAndInfo.getFileName(), comment, canonicalRelativePath);
        return fileAndInfo;
    }

    public Path overwriteApprovedFile(Path fileNameWithPath, String fileNameWithRelativePath, String jsonObject, String comment) throws IOException {
        CreatedFile fileAndInfo = getFullFileName(fileNameWithPath, fileNameWithRelativePath, true);
        return writeToFile(fileAndInfo.getFileName(), jsonObject, comment);
    }

    public Path writePointerFile(Path pointerPath, String comment, String relativeTarget) throws IOException {
        return writePointerToFile(pointerPath, comment, relativeTarget);
    }

    /**
     * Writes standalone content (not a pointer) to an existing approved file path, preserving the comment header.
     * Used by the dedup tool when reinstating pointer files back to standalone content.
     */
    public Path writeStandaloneFile(Path file, String content, String comment) throws IOException {
        return writeToFile(file, content, comment);
    }

    private Path writeToFile(Path file, String jsonObject, String comment) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            writer.write("/*" + comment + "*/");
            writer.write("\n");
            writer.write(jsonObject);
        }
        if (isPosixCompatible(file)) {
            Files.setPosixFilePermissions(file, APPROVED_FILE_PERMISSIONS);
        }
        return file;
    }

    private Path writePointerToFile(Path file, String comment, String relativeTarget) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            writer.write("/*" + comment + "*/");
            writer.write("\n");
            writer.write(POINTER_PREFIX + relativeTarget + "*/");
        }
        if (isPosixCompatible(file)) {
            Files.setPosixFilePermissions(file, APPROVED_FILE_PERMISSIONS);
        }
        return file;
    }

    public String readFile(Path file) throws IOException {
        return readFile(file, null);
    }

    public String readFile(Path file, Path workingDirectory) throws IOException {
        return readFile(file, workingDirectory, 0);
    }

    private String readFile(Path file, Path workingDirectory, int depth) throws IOException {
        if (depth > MAX_POINTER_DEPTH) {
            throw new IllegalStateException("Pointer chain depth exceeded " + MAX_POINTER_DEPTH + " hops, possible cycle at: " + file);
        }
        String fileContent = new String(Files.readAllBytes(file), UTF_8);
        if (fileContent.startsWith("/*")) {
            int index = fileContent.indexOf("*/\n");
            if (-1 < index) {
                String content = fileContent.substring(index + 3);
                if (content.startsWith(POINTER_PREFIX)) {
                    String target = content.substring(POINTER_PREFIX.length(), content.indexOf("*/")).trim();
                    if (workingDirectory == null) {
                        throw new IllegalStateException("Cannot follow pointer in file " + file + ": working directory is required to resolve relative path '" + target + "'");
                    }
                    Path targetPath = workingDirectory.resolve(target);
                    return readFile(targetPath, workingDirectory, depth + 1);
                }
                return content;
            }
        }
        return fileContent;
    }

    /**
     * Returns true if the file contains a pointer reference (after stripping the comment header).
     */
    public boolean isPointerFile(Path file) throws IOException {
        String fileContent = new String(Files.readAllBytes(file), UTF_8);
        if (fileContent.startsWith("/*")) {
            int index = fileContent.indexOf("*/\n");
            if (-1 < index) {
                String content = fileContent.substring(index + 3);
                return content.startsWith(POINTER_PREFIX);
            }
        }
        return false;
    }

    /**
     * Returns the comment text (between the opening {@code /*} and closing {@code *}{@code /}) from the header line,
     * or an empty string if no header is present.
     */
    public String readComment(Path file) throws IOException {
        String fileContent = new String(Files.readAllBytes(file), UTF_8);
        if (fileContent.startsWith("/*")) {
            int index = fileContent.indexOf("*/");
            if (-1 < index) {
                return fileContent.substring(2, index);
            }
        }
        return "";
    }

    /**
     * Returns the relative target path embedded in a pointer file, or empty if not a pointer.
     */
    public Optional<String> readPointerTarget(Path file) throws IOException {
        String fileContent = new String(Files.readAllBytes(file), UTF_8);
        if (fileContent.startsWith("/*")) {
            int index = fileContent.indexOf("*/\n");
            if (-1 < index) {
                String content = fileContent.substring(index + 3);
                if (content.startsWith(POINTER_PREFIX)) {
                    String target = content.substring(POINTER_PREFIX.length(), content.indexOf("*/")).trim();
                    return Optional.of(target);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks whether the pointer target (if any) is within the configured shared directory.
     * Returns empty if the file is not a pointer or the target is within the shared dir.
     * Returns the target path string if the pointer is stale (outside shared dir).
     */
    public Optional<String> findStalePointerTarget(Path file, Path workingDirectory, String sharedApprovalDirectory) throws IOException {
        Optional<String> target = readPointerTarget(file);
        if (!target.isPresent()) {
            return Optional.empty();
        }
        String targetPath = target.get();
        Path normalizedSharedDir = workingDirectory.resolve(sharedApprovalDirectory).normalize();
        Path normalizedTarget = workingDirectory.resolve(targetPath).normalize();
        if (!normalizedTarget.startsWith(normalizedSharedDir)) {
            return Optional.of(targetPath);
        }
        return Optional.empty();
    }

    /**
     * Looks up a canonical file in the shared directory whose content matches the given string.
     * Uses a direct filename lookup based on SHA-256 hash prefix and content size (no directory scan).
     *
     * @return the relative path from workingDirectory to the canonical file, or empty if no match
     */
    public Optional<String> findMatchingCanonical(String content, Path workingDirectory, String sharedApprovalDirectory, int bucketDepth) {
        String key = computeContentKey(content);
        String bucket = key.substring(0, bucketDepth);
        Path sharedDir = workingDirectory.resolve(sharedApprovalDirectory);
        Path canonicalPath = sharedDir.resolve(bucket).resolve(key + SEPARATOR + APPROVED_NAME_PART + fileExtension);
        if (Files.exists(canonicalPath)) {
            Path relativePath = workingDirectory.relativize(canonicalPath);
            return Optional.of(relativePath.toString().replace('\\', '/'));
        }
        return Optional.empty();
    }

    /**
     * Writes a new canonical file to the shared directory for the given content.
     * Returns the relative path from workingDirectory to the newly created canonical.
     */
    public String writeCanonical(String content, String comment, Path workingDirectory, String sharedApprovalDirectory, int bucketDepth) throws IOException {
        String key = computeContentKey(content);
        String bucket = key.substring(0, bucketDepth);
        Path sharedDir = workingDirectory.resolve(sharedApprovalDirectory);
        Path bucketDir = sharedDir.resolve(bucket);
        if (isPosixCompatible(bucketDir)) {
            Files.createDirectories(bucketDir, PosixFilePermissions.asFileAttribute(APPROVED_DIRECTORY_PERMISSIONS));
        } else {
            Files.createDirectories(bucketDir);
        }
        Path canonicalPath = bucketDir.resolve(key + SEPARATOR + APPROVED_NAME_PART + fileExtension);
        if (!Files.exists(canonicalPath)) {
            writeToFile(canonicalPath, content, comment);
        }
        return workingDirectory.relativize(canonicalPath).toString().replace('\\', '/');
    }

    /**
     * Computes the canonical filename key: sha256prefix-contentSizeInBytes.
     */
    public String computeContentKey(String content) {
        byte[] bytes = content.getBytes(UTF_8);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().substring(0, 12) + SEPARATOR + bytes.length;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Gets file with '-approved' suffix and .json extension and returns it.
     *
     * @param fileNameWithPath the name of the file with full path (relative to project root)
     * @return the {@link Path} object
     */
    public CreatedFile getApproved(Path fileNameWithPath, String fileNameWithRelativePath) {
        return getFullFileName(fileNameWithPath, fileNameWithRelativePath, true);
    }

    public CreatedFile getFullFileName(Path fileName, String fileNameWithRelativePath, boolean approved) {
        return getFileNameWithExtension(fileName, fileNameWithRelativePath, approved);
    }

    private CreatedFile getFileNameWithExtension(Path fileName, String fileNameWithRelativePath, boolean approved) {
        Path parent = fileName.getParent();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileName.getFileName().toString());

        StringBuilder relativeFileNameBuilder = new StringBuilder();
        relativeFileNameBuilder.append(fileNameWithRelativePath);
        if (stringBuilder.charAt(stringBuilder.length() - 1) != SEPARATOR) {
            stringBuilder.append(SEPARATOR);
            relativeFileNameBuilder.append(SEPARATOR);
        }
        if (approved) {
            stringBuilder.append(APPROVED_NAME_PART);
            relativeFileNameBuilder.append(APPROVED_NAME_PART);
        } else {
            stringBuilder.append(NOT_APPROVED_NAME_PART);
            relativeFileNameBuilder.append(NOT_APPROVED_NAME_PART);
        }
        stringBuilder.append(fileExtension);
        relativeFileNameBuilder.append(fileExtension);

        if (parent == null) {
            return new CreatedFile(Paths.get(stringBuilder.toString()), relativeFileNameBuilder.toString());
        }
        return new CreatedFile(parent.resolve(stringBuilder.toString()), relativeFileNameBuilder.toString());
    }

    private boolean isPosixCompatible(Path path) {
        return path.getFileSystem().supportedFileAttributeViews().contains("posix");
    }

    public static class CreatedFile {
        Path fileName;
        String fileNameWithRelativePath;

        public CreatedFile(Path fileName, String fileNameWithRelativePath) {
            this.fileName = fileName;
            this.fileNameWithRelativePath = fileNameWithRelativePath;
        }

        public Path getFileName() {
            return fileName;
        }

        public String getFileNameWithRelativePath() {
            return fileNameWithRelativePath;
        }
    }
}
