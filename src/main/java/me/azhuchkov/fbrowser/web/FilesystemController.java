package me.azhuchkov.fbrowser.web;

import me.azhuchkov.fbrowser.domain.FileObject;
import me.azhuchkov.fbrowser.repository.FileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author Andrey Zhuchkov
 *         Date: 19.11.14
 */
@RestController
@RequestMapping("/fs/*")
public class FilesystemController {
    private final FileSystem fileSystem;

    @Autowired
    public FilesystemController(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @RequestMapping("/browse")
    public List<FileObject> browse(@RequestParam String path) throws IOException {
        return fileSystem.listFiles(path);
    }
}
