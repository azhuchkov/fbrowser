package me.azhuchkov.fbrowser.repository.spi;

import me.azhuchkov.fbrowser.domain.FileObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * SPI to support extensibility of browsable types list.
 *
 * @author Andrey Zhuchkov
 *         Date: 25.11.14
 */
public interface ContainerBrowser {

    /**
     * Returns list of file types (extension) this browser able to handle.
     *
     * @return collection of file types.
     */
    Collection<String> getSupportedTypes();


    /**
     * Lists contents of a container located by the given path.
     *
     * @param container path to container.
     * @param internalPath    path inside the container.
     * @return list of file objects.
     * @throws java.io.IOException If something bad happens.
     */
    List<FileObject> list(File container, String internalPath) throws IOException;
}
