package me.azhuchkov.fbrowser.domain;

/**
 * Entry of a filesystem hierarchy.
 *
 * @author Andrey Zhuchkov
 *         Date: 20.11.14
 */
public class FileObject {
    /** File name. */
    private final String name;

    /** Type. */
    private final String type;

    /**
     * Creates new instance.
     *
     * @param name filename.
     * @param type object type.
     * @throws java.lang.IllegalArgumentException if path or type is <code>null</code>.
     */
    public FileObject(String name, String type) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

        if (type == null)
            throw new IllegalArgumentException("type cannot be null");

        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileObject that = (FileObject) o;

        if (!name.equals(that.name)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FileObject{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
