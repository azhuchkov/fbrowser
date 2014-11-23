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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static me.azhuchkov.fbrowser.domain.FileType.DIRECTORY;

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

        File resolved = new File(base, path);

        if (Files.isDirectory(resolved.toPath()))
            return listDirectory(resolved, path);
        else {
            List<String> archivePathNames = new ArrayList<>();

            while (!resolved.exists() && resolved.getParentFile() != null) {
                archivePathNames.add(resolved.getName());
                resolved = resolved.getParentFile();
            }

            if (resolved.exists() && !Files.isDirectory(resolved.toPath())) {
                StringBuilder builder = new StringBuilder();

                for (int i = archivePathNames.size() - 1; i >= 0; i--) {
                    builder.append(archivePathNames.get(i));

                    if (i > 0)
                        builder.append("/");
                }

                return listArchive(resolved, builder.toString(), path);
            } else
                throw new FileNotFoundException(path);
        }
    }

    /**
     * Returns listing of a ZIP archive.
     *
     * @param archivePath  archive file to observe.
     * @param internalPath path inside the archive. Must be relative.
     * @param clientPath   path to file.
     * @return list of file objects that belong to requested path of an archive.
     * @throws IOException If something bad happens.
     */
    private List<FileObject> listArchive(File archivePath, String internalPath, String clientPath) throws IOException {
        Set<FileObject> matchedEntries = new HashSet<>();

        try (ZipFile zipFile = new ZipFile(archivePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryPath = entry.getName().replaceFirst("^/", "");

                if (entryPath.equals(internalPath) && !entry.isDirectory())
                    throw new UnsupportedArchiveException(clientPath);

                if (!internalPath.isEmpty() && !entryPath.startsWith(internalPath + "/"))
                    continue;

                String relativeEntryPath =
                        entryPath.substring(internalPath.length()).replaceAll("(^/|/$)", "");

                int nameEndIdx = relativeEntryPath.indexOf("/");

                String name = nameEndIdx < 0 ?
                        relativeEntryPath :
                        relativeEntryPath.substring(0, nameEndIdx);

                FileType type = nameEndIdx >= 0 ? DIRECTORY : fileType(entry);

                matchedEntries.add(new FileObject(clientPath, name, type));
            }
        } catch (ZipException e) {
            throw new UnsupportedArchiveException("unsupported: " + clientPath, e);
        }

        if (matchedEntries.isEmpty())
            throw new FileNotFoundException(clientPath);

        matchedEntries.remove(new FileObject(clientPath, "", DIRECTORY));

        return new ArrayList<>(matchedEntries);
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
        if (!Files.isDirectory(dir.toPath())) {
            throw new IOException("Given path is not a directory: " + dir.getAbsolutePath());
        }

        List<FileObject> result = new ArrayList<>();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir.toPath())) {
            for (Path path : paths) {
                String name = path.getName(path.getNameCount() - 1).toString();
                result.add(new FileObject(clientPath, name, fileType(path)));
            }
        }

        return result;
    }

    /**
     * Returns type for given file.
     */
    private static FileType fileType(Path path) throws IOException {
        if (Files.isDirectory(path))
            return DIRECTORY;

        return guessContentType(path.getName(path.getNameCount() - 1).toString());
    }

    /**
     * Returns type for ZIP entry.
     */
    private static FileType fileType(ZipEntry entry) throws IOException {
        if (entry.isDirectory())
            return DIRECTORY;

        return guessContentType(entry.getName());
    }

    /**
     * Guess content type.
     */
    private static FileType guessContentType(String name) {
        // it seems Java is not so good in guessing MIME

        String contentType = name.toLowerCase().endsWith(".rar") ?
                "application/x-rar-compressed" :
                URLConnection.guessContentTypeFromName(name);

        return contentType != null ? FileType.byMimeType(contentType) : FileType.OTHER;
    }
}
