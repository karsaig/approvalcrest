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
import java.util.Collections;
import java.util.EnumSet;
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

    public static final Object SEPARATOR = "-";
    private static final String APPROVED_NAME_PART = "approved";
    private static final String NOT_APPROVED_NAME_PART = "not-approved";
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
    public String createNotApproved(Path fileNameWithPath, String jsonObject, String comment)
            throws IOException {
        Path file = getFullFileName(fileNameWithPath, false);
        Path parent = file.getParent();
        if (isPosixCompatible(parent)) {
            Files.createDirectories(parent, PosixFilePermissions.asFileAttribute(APPROVED_DIRECTORY_PERMISSIONS));
        } else {
            Files.createDirectories(parent);
        }
        return writeToFile(file, jsonObject, comment);
    }

    public String overwriteApprovedFile(Path fileNameWithPath, String jsonObject, String comment) throws IOException {
        return writeToFile(getFullFileName(fileNameWithPath, true), jsonObject, comment);
    }

    private String writeToFile(Path file, String jsonObject, String comment) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, UTF_8)) {
            writer.write("/*" + comment + "*/");
            writer.write("\n");
            writer.write(jsonObject);
        }
        if (isPosixCompatible(file)) {
            Files.setPosixFilePermissions(file, APPROVED_FILE_PERMISSIONS);
        }
        return file.getFileName().toString();
    }

    public String readFile(Path file) throws IOException {
        String fileContent = new String(Files.readAllBytes(file), UTF_8);
        if (fileContent.startsWith("/*")) {
            int index = fileContent.indexOf("*/\n");
            if (-1 < index) {
                return fileContent.substring(index + 3);
            }
        }
        return fileContent;
    }

    /**
     * Gets file with '-approved' suffix and .json extension and returns it.
     *
     * @param fileNameWithPath the name of the file with full path (relative to project root)
     * @return the {@link Path} object
     */
    public Path getApproved(Path fileNameWithPath) {
        return getFullFileName(fileNameWithPath, true);
    }

    public Path getFullFileName(Path fileName, boolean approved) {
        return getFileNameWithExtension(fileName, approved);
    }

    private Path getFileNameWithExtension(Path fileName, boolean approved) {
        Path parent = fileName.getParent();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileName.getFileName().toString());
        stringBuilder.append(SEPARATOR);
        if (approved) {
            stringBuilder.append(APPROVED_NAME_PART);
        } else {
            stringBuilder.append(NOT_APPROVED_NAME_PART);
        }
        stringBuilder.append(fileExtension);

        if (parent == null) {
            return Paths.get(stringBuilder.toString());
        }
        return parent.resolve(stringBuilder.toString());
    }

    private boolean isPosixCompatible(Path path) {
        return path.getFileSystem().supportedFileAttributeViews().contains("posix");
    }
}
