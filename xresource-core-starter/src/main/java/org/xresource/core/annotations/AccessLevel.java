package org.xresource.core.annotations;

/**
 * Enumeration representing different levels of access control
 * used in XResource authorization metadata.
 * <p>
 * This enum is typically used to define field-level or resource-level access
 * permissions,
 * especially in conjunction with annotations such as {@code @XFieldAccess} or
 * {@code @XResourceAccess}. These access levels control what operations a user
 * or role
 * can perform on a particular field or resource.
 *
 * <ul>
 * <li>{@link #NONE} - No access; the field is not visible or modifiable.</li>
 * <li>{@link #READ} - Read-only access; the field is visible but cannot be
 * modified.</li>
 * <li>{@link #WRITE} - Full access; the field is both visible and
 * modifiable.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * 
 * <pre>{@code
 * @XFieldAccess(readRoles = { "USER" }, writeRoles = { "ADMIN" })
 * private String status;
 * }</pre>
 *
 * <p>
 * Utility methods {@link #canRead()} and {@link #canWrite()} are provided
 * for easy programmatic evaluation of access rights.
 * </p>
 *
 * @apiNote Use this enum in conjunction with field/resource access control
 *          annotations
 *          to implement fine-grained role-based authorization in your
 *          XResource-based APIs.
 *
 * @see org.xresource.core.annotations.XFieldAccess
 * @see org.xresource.core.annotations.XResourceAccess
 * @since xresource-core 0.1
 */
public enum AccessLevel {
    /**
     * No access granted (neither read nor write).
     */
    NONE,

    /**
     * Read-only access granted.
     */
    READ,

    /**
     * Read and write access granted.
     */
    WRITE;

    /**
     * Determines whether this access level permits reading.
     *
     * @return {@code true} if read access is allowed, {@code false} otherwise
     */
    public boolean canRead() {
        return this == READ || this == WRITE;
    }

    /**
     * Determines whether this access level permits writing.
     *
     * @return {@code true} if write access is allowed, {@code false} otherwise
     */
    public boolean canWrite() {
        return this == WRITE;
    }
}
