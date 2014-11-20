package me.azhuchkov.fbrowser.domain;

/**
 * Entry of a filesystem hierarchy.
 *
 * @author Andrey Zhuchkov
 *         Date: 20.11.14
 */
public class FileObject implements Comparable<FileObject> {
    /** Path to file object. */
    private final String path;

    /** Type. */
    private final FileType type;

    /**
     * Creates new instance.
     *
     * @param path path to object.
     * @param type object type.
     * @throws java.lang.IllegalArgumentException if path or type is <code>null</code>.
     */
    public FileObject(String path, FileType type) {
        if (path == null)
            throw new IllegalArgumentException("path cannot be null");

        if (type == null)
            throw new IllegalArgumentException("type cannot be null");

        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public boolean isExpandable() {
        return type == FileType.DIRECTORY || type == FileType.ARCHIVE;
    }

    public FileType getType() {
        return type;
    }

    @Override
    public int compareTo(FileObject o) {
        return path.compareTo(o.path);
    }

    @Override
    public String toString() {
        return "FileObject{" +
                "path='" + path + '\'' +
                ", type=" + type +
                '}';
    }
}
