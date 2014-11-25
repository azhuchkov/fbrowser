package me.azhuchkov.fbrowser.repository;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Andrey Zhuchkov
 *         Date: 25.11.14
 */
public class FileTypeDetectorTest {
    @Test
    public void testRegularExtension() throws Exception {
        assertThat(FileTypeDetector.guessTypeByName("photo.jpg"), equalTo("FILE_JPG"));
    }

    @Test
    public void testRegularExtensionWithPath() throws Exception {
        assertThat(FileTypeDetector.guessTypeByName("/path/to/photo.jpg"), equalTo("FILE_JPG"));
    }

    @Test
    public void testEmptyExtension() throws Exception {
        assertThat(FileTypeDetector.guessTypeByName("file."), equalTo(FileTypeDetector.UNKNOWN_TYPE));
    }

    @Test
    public void testExtensionLess() throws Exception {
        assertThat(FileTypeDetector.guessTypeByName("file"), equalTo(FileTypeDetector.UNKNOWN_TYPE));
    }
}
