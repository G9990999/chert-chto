package ru.mws.wiki.entity;

/**
 * User roles for role-based access control.
 *
 * <ul>
 *   <li>{@link #USER} — can view and edit own pages, read shared pages</li>
 *   <li>{@link #MANAGER} — can manage page sharing and view all pages in space</li>
 *   <li>{@link #ADMIN} — full access including user management</li>
 * </ul>
 */
public enum Role {
    USER,
    MANAGER,
    ADMIN
}
