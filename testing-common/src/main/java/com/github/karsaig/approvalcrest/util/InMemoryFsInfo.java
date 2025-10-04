package com.github.karsaig.approvalcrest.util;

import java.nio.file.FileSystem;
import java.nio.file.Path;

public class InMemoryFsInfo {

    private final FileSystem inMemoryFileSystem;
    private final Path testPath;
    private final Path resourcePath;
    private final Path workingDirectory;

    public InMemoryFsInfo(FileSystem inMemoryFileSystem, Path testPath, Path resourcePath,Path workingDirectory) {
        this.inMemoryFileSystem = inMemoryFileSystem;
        this.testPath = testPath;
        this.resourcePath = resourcePath;
        this.workingDirectory = workingDirectory;
    }

    public FileSystem getInMemoryFileSystem() {
        return inMemoryFileSystem;
    }

    public Path getResourcePath() {
        return resourcePath;
    }

    public Path getTestPath() {
        return testPath;
    }

    public Path getWorkingDirectory() {
        return workingDirectory;
    }
}
