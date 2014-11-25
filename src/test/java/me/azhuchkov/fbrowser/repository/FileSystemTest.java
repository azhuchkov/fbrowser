package me.azhuchkov.fbrowser.repository;

import me.azhuchkov.fbrowser.Application;
import me.azhuchkov.fbrowser.domain.FileObject;
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
                listing(
                        item("dir1", FileTypeDetector.DIRECTORY_TYPE),
                        item("archive.zip", "FILE_ZIP")
                )));

        assertThat(fileSystem.listFiles("/dir1"), returns(
                listing(
                        item("dir2", FileTypeDetector.DIRECTORY_TYPE),
                        item("doc.txt", "FILE_TXT"),
                        item("image.tiff", "FILE_TIFF")
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2"), returns(
                listing(
                        item("bootstrap.zip", "FILE_ZIP"),
                        item("archive.zip", "FILE_ZIP"),
                        item("archive2.rar", "FILE_RAR"),
                        item("gradle.zip", "FILE_ZIP"),
                        item("file.xml", "FILE_XML"),
                        item("photo.jpg", "FILE_JPG")
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/gradle.zip/fbrowser/gradle"), returns(
                listing(
                        item("wrapper", FileTypeDetector.DIRECTORY_TYPE)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/bootstrap.zip"), returns(
                listing(
                        item("js", FileTypeDetector.DIRECTORY_TYPE),
                        item("css", FileTypeDetector.DIRECTORY_TYPE),
                        item("img", FileTypeDetector.DIRECTORY_TYPE)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/bootstrap.zip/js/"), returns(
                listing(
                        item("bootstrap.js", "FILE_JS"),
                        item("bootstrap.min.js", "FILE_JS")
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/bootstrap.zip/css"), returns(
                listing(
                        item("bootstrap.css", "FILE_CSS"),
                        item("bootstrap.min.css", "FILE_CSS")
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/archive.zip/tmp"), returns(
                listing(
                        item("tmp.ukxkr06vVH", FileTypeDetector.DIRECTORY_TYPE)
                )));

        assertThat(fileSystem.listFiles("/dir1/dir2/archive.zip/tmp/tmp.ukxkr06vVH"), returns(listing()));

        assertThat(fileSystem.listFiles("/archive.zip"), returns(
                listing(
                        item("tmp", FileTypeDetector.DIRECTORY_TYPE)
                )));

        assertThat(fileSystem.listFiles("/archive.zip/"), returns(
                listing(
                        item("tmp", FileTypeDetector.DIRECTORY_TYPE)
                )));

        assertThat(fileSystem.listFiles("/archive.zip/tmp"), returns(
                listing(
                        item("tmp.ukxkr06vVH", FileTypeDetector.DIRECTORY_TYPE)
                )));

        assertThat(fileSystem.listFiles("/archive.zip/tmp/tmp.ukxkr06vVH"), returns(listing()));
    }

    @Test
    @Ignore("Git doesn't allow to store empty directories")
    public void testEmptyDirBrowsing() throws Exception {
        assertThat(fileSystem.listFiles("/empty"), returns(listing()));

        assertThat(fileSystem.listFiles("/empty/"), returns(listing()));

        assertThat(fileSystem.listFiles("/dir1/empty"), returns(listing()));

        assertThat(fileSystem.listFiles("/dir1/empty/"), returns(listing()));
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

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedArchive() throws Exception {
        fileSystem.listFiles("/dir1/image.tiff");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void tesUnsupportedArchiveInsideArchive() throws Exception {
        fileSystem.listFiles("/dir1/dir2/bootstrap.zip/js/bootstrap.js");
    }

    /* Helpers */

    private static List<FileObject> listing(Item... items) {
        List<FileObject> result = new ArrayList<>(items.length);

        for (Item item : items) {
            result.add(new FileObject(item.name, item.type));
        }

        return result;
    }

    private static Item item(String name, String type) {
        return new Item(name, type);
    }

    private static class Item {
        private final String name;
        private final String type;

        public Item(String name, String type) {
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
