package com.github.karsaig.approvalcrest.matcher.file;

import java.nio.file.Path;
import java.util.Optional;

public class MatcherFile {

    private Path filePath;
    private Optional<String> content = Optional.empty();
    private String testName;
    private Optional<Path> sharedContent = Optional.empty();

    public Optional<String> getContent() {
        return content;
    }

    public void setContent(Optional<String> content) {
        this.content = content;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Optional<Path> getSharedContent() {
        return sharedContent;
    }

    public void setSharedContent(Optional<Path> sharedContent) {
        this.sharedContent = sharedContent;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
}
