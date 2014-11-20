package me.azhuchkov.fbrowser.repository;

import me.azhuchkov.fbrowser.domain.FileObject;
import me.azhuchkov.fbrowser.domain.FileType;
import me.azhuchkov.fbrowser.exception.UnsupportedArchiveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * FileSystem browser with ZIP archive browsing support.
 *
 * @author Andrey Zhuchkov
 *         Date: 20.11.14
 */
@Repository
public class FileSystem {
    /**
     * Prefix for all paths requested by clients to find real path.
     */
    @Value("${filesystem.base}")
    private File base;

    /**
     * Returns listing of a given node in filesystem hierarchy.
     * Node can be a directory or a ZIP archive file or a path inside archive.
     *
     * @param path path to browsing entry.
     * @return List of children for the given path.
     * @throws IOException if something bad and I/O-related occurs.
     */
    public List<FileObject> listFiles(String path) throws IOException {
        File resolved = new File(base, path);

        if (resolved.isDirectory())
            return listDirectory(resolved, path);
        else {
            while (!resolved.exists() && resolved.getParentFile() != null)
                resolved = resolved.getParentFile();

            if (resolved.exists() && !resolved.isDirectory())
                return listArchive(resolved, path);
            else
                throw new FileNotFoundException(path);
        }
    }

    /**
     * Returns listing of a ZIP archive.
     *
     * @param archivePath archive file to observe.
     * @param clientPath  path to file.
     * @return list of file objects that belong to requested path of an archive.
     * @throws IOException If something bad happens.
     */
    private List<FileObject> listArchive(File archivePath, String clientPath) throws IOException {
        Path internalPathBase =
                archivePath.toPath().relativize(new File(base, clientPath).toPath());

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(archivePath);
        } catch (ZipException e) {
            throw new UnsupportedArchiveException("unsupported: " + clientPath, e);
        }

        Set<FileObject> result = new TreeSet<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            Path entryPath = Paths.get(entry.getName().replaceFirst("^/", ""));

            if (internalPathBase.toString().isEmpty() || entryPath.startsWith(internalPathBase)) {
                Path relativePath = internalPathBase.relativize(entryPath);

                String path = Paths.get(clientPath).resolve(relativePath.getName(0)).toString();

                FileType type = entry.isDirectory() || relativePath.getNameCount() > 1 ?
                        FileType.DIRECTORY :
                        fileType(entry);

                result.add(new FileObject(path, type));
            }
        }

        if (result.isEmpty())
            throw new FileNotFoundException(clientPath);

        FileObject first = result.iterator().next();

        if (result.size() == 1 &&
                Paths.get(first.getPath()).equals(Paths.get(clientPath)) &&
                first.getType() != FileType.DIRECTORY) {
            throw new UnsupportedArchiveException(clientPath);
        }

        result.remove(new FileObject(Paths.get(clientPath).toString(), FileType.DIRECTORY));

        return new ArrayList<>(result);
    }

    /**
     * Returns listing of a directory.
     *
     * @param dir        directory to observe.
     * @param clientPath path to file.
     * @return list of file objects that belong to requested directory.
     * @throws IOException If something bad happens.
     */
    private List<FileObject> listDirectory(File dir, String clientPath) throws IOException {
        File[] children = dir.listFiles();

        if (children == null) {
            throw new IOException("Given path is not a directory: " + dir.getAbsolutePath());
        }

        List<FileObject> result = new ArrayList<>(children.length);

        for (File child : children) {
            result.add(new FileObject(new File(clientPath, child.getName()).getPath(), fileType(child)));
        }

        Collections.sort(result);

        return result;
    }

    /**
     * Returns type for given file.
     */
    private static FileType fileType(File file) throws IOException {
        if (file.isDirectory())
            return FileType.DIRECTORY;

        String contentType = Files.probeContentType(file.toPath());

        return contentType != null ? FileType.byMimeType(contentType) : FileType.OTHER;
    }

    /**
     * Returns type for ZIP entry.
     */
    private static FileType fileType(ZipEntry entry) throws IOException {
        if (entry.isDirectory())
            return FileType.DIRECTORY;

        String contentType = URLConnection.guessContentTypeFromName(entry.getName());

        return contentType != null ? FileType.byMimeType(contentType) : FileType.OTHER;
    }
}
