package me.azhuchkov.fbrowser.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author Andrey Zhuchkov
 *         Date: 19.11.14
 */
@RestController
@RequestMapping("/fs/*")
public class FilesystemController {

    @RequestMapping("/browse")
    public List<String> browse(@RequestParam String path) {
        return Arrays.asList(path);
    }

}
