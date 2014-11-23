package me.azhuchkov.fbrowser.repository;

import me.azhuchkov.fbrowser.Application;
import me.azhuchkov.fbrowser.domain.FileObject;
import me.azhuchkov.fbrowser.domain.FileType;
import me.azhuchkov.fbrowser.exception.UnsupportedArchiveException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileNotFoundException;
import java.util.*;

import static me.azhuchkov.fbrowser.domain.FileType.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Andrey Zhuchkov
 *         Date: 23.11.14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class FileSystemTest {

    @Autowired
    FileSystem fileSystem;

    @Test
    public void testBrowsing() throws Exception {
        assertThat(fileSystem.listFiles("/"), returns(
                listing("/",
                        item("dir1", DIRECTORY),
                        item("archive.zip", ARCHIVE)
                )));

        assertThat(fileSystem.listFiles("/dir1"), returns(
                listing("/dir1",
                        item("dir2", DIRECTORY),
                        item("doc.txt", OTHER),
                        item("image.tiff", IMAGE)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2"), returns(
                listing("/dir1/dir2",
                        item("bootstrap.zip", ARCHIVE),
                        item("archive.zip", ARCHIVE),
                        item("archive2.rar", ARCHIVE),
                        item("gradle.zip", ARCHIVE),
                        item("file.xml", OTHER),
                        item("photo.jpg", IMAGE)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/gradle.zip/fbrowser/gradle"), returns(
                listing("/dir1/dir2/gradle.zip/fbrowser/gradle",
                        item("wrapper", DIRECTORY)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/bootstrap.zip"), returns(
                listing("/dir1/dir2/bootstrap.zip",
                        item("js", DIRECTORY),
                        item("css", DIRECTORY),
                        item("img", DIRECTORY)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/bootstrap.zip/js/"), returns(
                listing("/dir1/dir2/bootstrap.zip/js/",
                        item("bootstrap.js", OTHER),
                        item("bootstrap.min.js", OTHER)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/bootstrap.zip/css"), returns(
                listing("/dir1/dir2/bootstrap.zip/css",
                        item("bootstrap.css", OTHER),
                        item("bootstrap.min.css", OTHER)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/archive.zip/tmp"), returns(
                listing("/dir1/dir2/archive.zip/tmp",
                        item("tmp.ukxkr06vVH", DIRECTORY)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/archive.zip/tmp/tmp.ukxkr06vVH"), returns(
                listing("/dir1/dir2/archive.zip/tmp/tmp.ukxkr06vVH")));

        assertThat(fileSystem.listFiles("/archive.zip"), returns(
                listing("/archive.zip",
                        item("tmp", DIRECTORY)
                )));

        assertThat(fileSystem.listFiles("/archive.zip/"), returns(
                listing("/archive.zip/",
                        item("tmp", DIRECTORY)
                )));

        assertThat(fileSystem.listFiles("/archive.zip/tmp"), returns(
                listing("/archive.zip/tmp",
                        item("tmp.ukxkr06vVH", DIRECTORY)
                )));

        assertThat(fileSystem.listFiles("/archive.zip/tmp/tmp.ukxkr06vVH"), returns(
                listing("/archive.zip/tmp/tmp.ukxkr06vVH")));
    }

    @Test
    @Ignore("Git doesn't allow to store empty directories")
    public void testEmptyDirBrowsing() throws Exception {
        assertThat(fileSystem.listFiles("/empty"), returns(listing("/empty")));

        assertThat(fileSystem.listFiles("/empty/"), returns(listing("/empty/")));

        assertThat(fileSystem.listFiles("/dir1/empty"), returns(listing("/dir1/empty")));

        assertThat(fileSystem.listFiles("/dir1/empty/"), returns(listing("/dir1/empty/")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativePath() throws Exception {
        fileSystem.listFiles("file.txt");
    }

    @Test(expected = FileNotFoundException.class)
    public void testNotFound() throws Exception {
        fileSystem.listFiles("/fake");
    }

    @Test(expected = FileNotFoundException.class)
    public void testNotFoundInsideArchive() throws Exception {
        fileSystem.listFiles("/archive.zip/fake");
    }

    @Test(expected = FileNotFoundException.class)
    public void testNotFoundInsideNestedArchive() throws Exception {
        fileSystem.listFiles("/dir1/dir2/bootstrap.zip/css/fake");
    }

    @Test(expected = UnsupportedArchiveException.class)
    public void testUnsupportedArchive() throws Exception {
        fileSystem.listFiles("/dir1/image.tiff");
    }

    @Test(expected = UnsupportedArchiveException.class)
    public void tesUnsupportedArchiveInsideArchive() throws Exception {
        fileSystem.listFiles("/dir1/dir2/bootstrap.zip/js/bootstrap.js");
    }

    /* Helpers */

    private static List<FileObject> listing(String parent, Item... items) {
        List<FileObject> result = new ArrayList<>(items.length);

        for (Item item : items) {
            result.add(new FileObject(parent, item.name, item.type));
        }

        return result;
    }

    private static Item item(String name, FileType type) {
        return new Item(name, type);
    }

    private static class Item {
        private final String name;
        private final FileType type;

        public Item(String name, FileType type) {
            this.name = name;
            this.type = type;
        }
    }

    private <T> Matcher<Collection<FileObject>> returns(Collection<FileObject> expected) {
        return new ListingMatcher(expected);
    }

    private class ListingMatcher extends TypeSafeMatcher<Collection<FileObject>> {
        private final Set<FileObject> expected;

        private ListingMatcher(Collection<FileObject> expected) {
            this.expected = new HashSet<>(expected);
        }

        @Override
        protected boolean matchesSafely(Collection<FileObject> actual) {
            return new HashSet<>(actual).equals(expected);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(expected);
        }
    }
}
