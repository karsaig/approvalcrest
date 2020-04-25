package com.github.karsaig.approvalcrest.util;

import java.util.Objects;

public class InMemoryFiles {
    private final String path;
    private final String content;

    public InMemoryFiles(String path, String content) {
        this.path = path;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryFiles that = (InMemoryFiles) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, content);
    }
}
