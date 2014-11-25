package me.azhuchkov.fbrowser.repository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andrey Zhuchkov
 *         Date: 25.11.14
 */
public class FileTypeDetector {
    public static final String DIRECTORY_TYPE = "DIRECTORY";
    public static final String UNKNOWN_TYPE = "UNKNOWN";
    public static final String FILE_TYPE_PREFIX = "FILE_".intern(); // avoid constant inlining

    /**
     * Returns file type for given file.
     *
     * @param file file to determine type.
     * @return file's type.
     */
    public static String fileType(File file) {
        return fileType(file.toPath());
    }

    /**
     * Returns file type for given file.
     *
     * @param path file to determine type.
     * @return file's type.
     */
    public static String fileType(Path path) {
        if (Files.isDirectory(path))
            return DIRECTORY_TYPE;

        return guessTypeByName(path.getFileName().toString());
    }

    /**
     * Returns file type by its name.
     *
     * @param name name of a file (may include path).
     * @return file's type.
     */
    public static String guessTypeByName(String name) {
        int extDelimiterIdx = name.lastIndexOf(".");

        return extDelimiterIdx >= 0 && extDelimiterIdx < name.length() - 1 ?
                FILE_TYPE_PREFIX + name.substring(extDelimiterIdx + 1, name.length()).toUpperCase() :
                UNKNOWN_TYPE;
    }
}
