package com.github.karsaig.approvalcrest;

public class PathNullPointerException extends NullPointerException {

    private final String path;

    public PathNullPointerException(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}