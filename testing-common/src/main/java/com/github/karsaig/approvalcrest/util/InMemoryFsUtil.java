package com.github.karsaig.approvalcrest.util;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

public class InMemoryFsUtil {

    private InMemoryFsUtil() {
    }

    public static void inMemoryUnixFs(BiConsumer<FileSystem, Path> test) {
        Configuration config = Configuration.unix()
                .toBuilder()
                .setAttributeViews("basic", "owner", "posix", "unix")
                .build();
        inMemoryFs(config, test);
    }

    public static void inMemoryWindowsFs(BiConsumer<FileSystem, Path> test) {
        Configuration config = Configuration.windows()
                .toBuilder()
                .setAttributeViews("basic", "owner", "posix", "unix")
                .build();
        inMemoryFs(config, test);
    }

    public static void inMemoryFs(Configuration config, BiConsumer<FileSystem, Path> test) {
        try (FileSystem fs = Jimfs.newFileSystem(config)) {
            Path testPath = fs.getPath("test", "path");
            Path pathWithDirs = Files.createDirectories(testPath);
            test.accept(fs, pathWithDirs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                    if (fullPath.startsWith("/work/test/path/")) {
                        fullPath = fullPath.substring(16);
                    } else if (fullPath.startsWith("C:\\work\\test\\path\\")) {
                        fullPath = fullPath.substring(18);
                    }
                    return new InMemoryFiles(fullPath, readFile(p));
                })
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
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
