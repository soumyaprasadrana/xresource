package org.xresource.core.annotation;

public enum AccessLevel {
    NONE, READ, WRITE;

    public boolean canRead() {
        return this == READ || this == WRITE;
    }

    public boolean canWrite() {
        return this == WRITE;
    }
}
