package com.github.karsaig.approvalcrest.util;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.Set;

public class InMemoryPermissions {

    private final String path;
    private final Set<PosixFilePermission> permissions;

    public InMemoryPermissions(String path, Set<PosixFilePermission> permissions) {
        this.path = path;
        this.permissions = permissions;
    }

    public String getPath() {
        return path;
    }

    public Set<PosixFilePermission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryPermissions that = (InMemoryPermissions) o;
        return path.equals(that.path) &&
                permissions.equals(that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, permissions);
    }
}
