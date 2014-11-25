package me.azhuchkov.fbrowser.repository.spi;

import me.azhuchkov.fbrowser.domain.FileObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static me.azhuchkov.fbrowser.repository.FileTypeDetector.DIRECTORY_TYPE;
import static me.azhuchkov.fbrowser.repository.FileTypeDetector.fileType;

/**
 * @author Andrey Zhuchkov
 *         Date: 25.11.14
 */
@Component
public class DirBrowserImpl implements ContainerBrowser {
    @Override
    public Collection<String> getSupportedTypes() {
        return Arrays.asList(DIRECTORY_TYPE);
    }

    @Override
    public List<FileObject> list(File dir, String internalPath) throws IOException {
        dir = new File(dir, internalPath);

        if (Files.notExists(dir.toPath()))
            throw new FileNotFoundException("Path doesn't exist: " + dir);

        List<FileObject> result = new ArrayList<>();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir.toPath())) {

            for (Path path : paths) {
                String name = path.getFileName().toString();
                String type = fileType(path);

                result.add(new FileObject(name, type));
            }

        }

        return result;
    }
}
