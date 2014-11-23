package me.azhuchkov.fbrowser.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entry of a filesystem hierarchy.
 *
 * @author Andrey Zhuchkov
 *         Date: 20.11.14
 */
public class FileObject {
    /** Path to parent file object. */
    private final String parent;

    /** File name. */
    private final String name;

    /** Type. */
    private final FileType type;

    /**
     * Creates new instance.
     *
     * @param parent parent's path.
     * @param name filename.
     * @param type object type.
     * @throws java.lang.IllegalArgumentException if path or type is <code>null</code>.
     */
    public FileObject(String parent, String name, FileType type) {
        if (parent == null)
            throw new IllegalArgumentException("parent cannot be null");

        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

        if (type == null)
            throw new IllegalArgumentException("type cannot be null");

        this.parent = parent;
        this.name = name;
        this.type = type;
    }

    @JsonIgnore
    public String getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public boolean isExpandable() {
        return type == FileType.DIRECTORY || type == FileType.ARCHIVE;
    }

    public FileType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileObject that = (FileObject) o;

        if (!name.equals(that.name)) return false;
        if (!parent.equals(that.parent)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FileObject{" +
                "parent='" + parent + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
