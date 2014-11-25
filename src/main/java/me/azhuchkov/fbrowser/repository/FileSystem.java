package me.azhuchkov.fbrowser.repository;

import me.azhuchkov.fbrowser.domain.FileObject;
import me.azhuchkov.fbrowser.repository.spi.ContainerBrowser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FileSystem browser with support of archive viewing.
 *
 * @author Andrey Zhuchkov
 *         Date: 20.11.14
 */
@Repository
public class FileSystem {
    /**
     * Prefix for all paths requested by clients to find real path.
     */
    private final File base;

    /**
     * Map of browsers by supported container type.
     */
    private final Map<String, ContainerBrowser> browsers;

    @Autowired
    public FileSystem(@Value("${filesystem.base}") File base,
                      List<ContainerBrowser> browsers) {
        this.base = base;

        Map<String, ContainerBrowser> browserMap = new HashMap<>(browsers.size());

        for (ContainerBrowser browser : browsers) {
            for (String type : browser.getSupportedTypes()) {
                browserMap.put(type.toUpperCase(), browser);
            }
        }

        this.browsers = browserMap;
    }

    /**
     * Return base directory.
     *
     * @return Base directory.
     */
    public File getBase() {
        return base;
    }

    /**
     * Returns listing of a given node in filesystem hierarchy.
     * Node can be a directory or a ZIP archive file or a path inside archive.
     *
     * @param path path to browsing entry.
     * @return List of children for the given path.
     * @throws IOException                        if something bad and I/O-related occurs.
     * @throws java.lang.IllegalArgumentException if given path is not absolute.
     */
    public List<FileObject> listFiles(String path) throws IOException {
        if (!path.startsWith("/"))
            throw new IllegalArgumentException("Only absolute paths allowed");

        if (path.contains("./"))
            throw new IllegalArgumentException("Relative paths are not allowed");

        File clientFile = new File(path);
        File resolved = new File(base, path);

        List<String> archivePathNames = new ArrayList<>();

        while (!resolved.exists() && clientFile.getParentFile() != null) {
            archivePathNames.add(clientFile.getName());
            clientFile = clientFile.getParentFile();
            resolved = resolved.getParentFile();
        }

        if (Files.notExists(resolved.toPath()))
            throw new FileNotFoundException(path);

        ContainerBrowser browser =
                browsers.get(FileTypeDetector.fileType(resolved));

        if (browser == null)
            throw new UnsupportedOperationException("unsupported container: " + path);

        StringBuilder builder = new StringBuilder();

        for (int i = archivePathNames.size() - 1; i >= 0; i--) {
            builder.append(archivePathNames.get(i));

            if (i > 0)
                builder.append("/");
        }

        return browser.list(resolved, builder.toString());
    }
}
