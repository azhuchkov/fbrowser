package me.azhuchkov.fbrowser.repository.spi;

import me.azhuchkov.fbrowser.domain.FileObject;
import me.azhuchkov.fbrowser.repository.FileTypeDetector;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static me.azhuchkov.fbrowser.repository.FileTypeDetector.DIRECTORY_TYPE;
import static me.azhuchkov.fbrowser.repository.FileTypeDetector.FILE_TYPE_PREFIX;

/**
 * @author Andrey Zhuchkov
 *         Date: 25.11.14
 */
@Component
public class ZipBrowserImpl implements ContainerBrowser {
    @Override
    public Collection<String> getSupportedTypes() {
        return Arrays.asList(FILE_TYPE_PREFIX + "ZIP", FILE_TYPE_PREFIX + "JAR");
    }

    @Override
    public List<FileObject> list(File container, String internalPath) throws IOException {
        if (Files.notExists(container.toPath()))
            throw new FileNotFoundException("Path doesn't exist " + container);

        Set<FileObject> matchedEntries = new HashSet<>();

        try (ZipFile zipFile = new ZipFile(container)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryPath = entry.getName().replaceFirst("^/", "");

                if (entryPath.equals(internalPath) && !entry.isDirectory())
                    throw new UnsupportedOperationException("Cannot open compressed " + internalPath);

                if (!internalPath.isEmpty() && !entryPath.startsWith(internalPath + "/"))
                    continue;

                String relativeEntryPath =
                        entryPath.substring(internalPath.length()).replaceAll("(^/|/$)", "");

                int nameEndIdx = relativeEntryPath.indexOf("/");

                String name = nameEndIdx < 0 ?
                        relativeEntryPath :
                        relativeEntryPath.substring(0, nameEndIdx);

                String type = nameEndIdx >= 0 ?
                        DIRECTORY_TYPE :
                        entry.isDirectory() ?
                                DIRECTORY_TYPE :
                                FileTypeDetector.guessTypeByName(name);

                matchedEntries.add(new FileObject(name, type));
            }
        }

        if (matchedEntries.isEmpty())
            throw new FileNotFoundException(internalPath);

        matchedEntries.remove(new FileObject("", DIRECTORY_TYPE));

        return new ArrayList<>(matchedEntries);
    }
}
