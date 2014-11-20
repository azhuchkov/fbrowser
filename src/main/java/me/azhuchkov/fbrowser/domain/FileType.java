package me.azhuchkov.fbrowser.domain;

/**
 * File Type.
 *
 * @author Andrey Zhuchkov
 *         Date: 20.11.14
 */
public enum FileType {
    DIRECTORY, IMAGE, ARCHIVE, OTHER;

    /**
     * Guesses appropriate type by the given MIME type.
     *
     * @param mimeType MIME type of a object.
     * @return file type.
     * @throws java.lang.NullPointerException if given MIME type is <code>null</code>.
     */
    public static FileType byMimeType(String mimeType) {
        String normalizedMime = mimeType.toLowerCase();

        switch (normalizedMime) {
            case "inode/directory":
                return DIRECTORY;
            case "application/zip":
                return ARCHIVE;
            case "application/x-rar-compressed":
                return ARCHIVE;
            case "application/x-tar":
                return ARCHIVE;
            case "application/x-compressed-tar":
                return ARCHIVE;
            case "application/x-bzip-compressed-tar":
                return ARCHIVE;
            default:
                return normalizedMime.startsWith("image") ? IMAGE : OTHER;
        }
    }
}
