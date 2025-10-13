package com.github.karsaig.approvalcrest.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableSet;

public class InMemoryFsUtil {

    public static final Set<PosixFilePermission> FILE_CREATE_PERMISSONS = unmodifiableSet(EnumSet.of(PosixFilePermission.GROUP_WRITE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.OTHERS_WRITE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_READ));
    public static final Set<PosixFilePermission> DIRECTORY_CREATE_PERMISSONS = unmodifiableSet(EnumSet.of(PosixFilePermission.GROUP_WRITE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_WRITE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_EXECUTE,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_EXECUTE));
    public static final Set<PosixFilePermission> DEFAULT_JIMFS_PERMISSIONS = unmodifiableSet(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ));
    private static final Set<InMemoryPermissions> PRE_EXISTING_PATHS_WITH_PERMISSIONS;

    static {

        Set<InMemoryPermissions> preExisting = new HashSet<>();
        preExisting.add(new InMemoryPermissions("/", DEFAULT_JIMFS_PERMISSIONS));
        preExisting.add(new InMemoryPermissions("/work", DEFAULT_JIMFS_PERMISSIONS));
        preExisting.add(new InMemoryPermissions("/work/resources", DEFAULT_JIMFS_PERMISSIONS));
        preExisting.add(new InMemoryPermissions("/work/test", DEFAULT_JIMFS_PERMISSIONS));
        preExisting.add(new InMemoryPermissions("/work/test/path", DEFAULT_JIMFS_PERMISSIONS));
        PRE_EXISTING_PATHS_WITH_PERMISSIONS = unmodifiableSet(preExisting);
    }


    private InMemoryFsUtil() {
    }

    public static Configuration JIMFS_UNIX_CONFIG = Configuration.unix()
            .toBuilder()
            .setAttributeViews("basic", "owner", "posix", "unix")
            .build();

    public static void inMemoryUnixFsWithFileAttributeSupport(Consumer<InMemoryFsInfo> test) {
        inMemoryFs(JIMFS_UNIX_CONFIG, test);
    }

    public static Configuration JIMFS_WINDOWS_CONFIG = Configuration.windows()
            .toBuilder()
                .build();

    public static Collection<Configuration> TESTED_OS_CONFIGS = getTestedOsConfigs();

    private static Collection<Configuration> getTestedOsConfigs(){
        List<Configuration> testedOsConfigs = new ArrayList<>();
        testedOsConfigs.add(JIMFS_WINDOWS_CONFIG);
        testedOsConfigs.add(JIMFS_UNIX_CONFIG);
        return Collections.unmodifiableList(testedOsConfigs);
    }

    public static void inMemoryWindowsFs(Consumer<InMemoryFsInfo> test) {
        inMemoryFs(JIMFS_WINDOWS_CONFIG, test);
    }

    public static void inMemoryFs(Configuration config, Consumer<InMemoryFsInfo> test) {
        try (FileSystem fs = Jimfs.newFileSystem(config)) {
            Path testPath = fs.getPath("test", "path");
            Path resourcePath = fs.getPath("resources");
            Path pathWithDirs = Files.createDirectories(testPath);
            resourcePath = Files.createDirectories(resourcePath);
            Path workdir = fs.getPath("work");
            List<InMemoryFiles> stateBefore = Collections.emptyList();
            try {
                stateBefore = getFiles(fs);
                test.accept(new InMemoryFsInfo(fs, pathWithDirs, resourcePath, workdir));
            } catch (Error e) {
                List<InMemoryFiles> state = getFiles(fs);
                System.out.println("In memory fs content:");
                state.forEach(System.out::println);
                System.out.println();
                System.out.println("before test:");
                stateBefore.forEach(System.out::println);
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<InMemoryFiles> getFiles(InMemoryFsInfo imfsi) {
        return getFiles(imfsi.getInMemoryFileSystem());
    }

    public static List<InMemoryFiles> getFiles(FileSystem fs) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap(rp -> {
                    try {
                        return Files.find(rp, Integer.MAX_VALUE, (p, fa) -> Files.isRegularFile(p));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(p -> {
                    String fullPath = p.toString();
                    String originalPath = fullPath;
                        if (fullPath.startsWith("/work/test/path/")) {
                            fullPath = fullPath.substring(16);
                        } else if (fullPath.startsWith("C:\\work\\test\\path\\")) {
                            fullPath = fullPath.substring(18);
                        }

                    return new InMemoryFiles(fullPath, readFile(p),originalPath);
                })
                .collect(Collectors.toList());
    }

    public static List<InMemoryPermissions> getPermissons(InMemoryFsInfo imfsi) {
        return getPermissons(imfsi.getInMemoryFileSystem());
    }

    public static List<InMemoryPermissions> getPermissons(FileSystem fs) {
        return StreamSupport.stream(fs.getRootDirectories().spliterator(), false)
                .flatMap(rp -> {
                    try {
                        return Files.walk(rp);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(p -> {
                    String fullPath = p.toString();
                    if (fullPath.startsWith("/work/test/path/")) {
                        fullPath = fullPath.substring(16);
                    } else if (fullPath.startsWith("C:\\work\\test\\path\\")) {
                        fullPath = fullPath.substring(18);
                    }
                    try {
                        PosixFileAttributeView attributeView = Files.getFileAttributeView(p, PosixFileAttributeView.class);
                        if (attributeView != null) {
                            PosixFileAttributes attributes = attributeView.readAttributes();
                            return new InMemoryPermissions(fullPath, attributes.permissions());
                        } else {
                            return new InMemoryPermissions(fullPath, Collections.emptySet());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(imp -> !PRE_EXISTING_PATHS_WITH_PERMISSIONS.contains(imp))
                .collect(Collectors.toList());
    }


    public static String readFile(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void writeFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            if (content == null) {
                Files.createFile(path);
            } else {
                Files.write(path, content.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
