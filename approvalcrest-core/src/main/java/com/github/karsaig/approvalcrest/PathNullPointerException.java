package com.github.karsaig.approvalcrest;

public class PathNullPointerException extends NullPointerException {

    private static final long serialVersionUID = 4210770969303844915L;
    private final String path;

    PathNullPointerException(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}