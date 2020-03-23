package bma.photo.uploader.google;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class GooglePhotosUploaderTest {

//    private static final Pattern ALBUM_NAME_MASK = Pattern.compile("\\S/(\\d{4}-\\d{2}-\\d{2}.*|\\d{4}_\\d{2}_\\d{2}.*)/\\S");
    private static final Pattern ALBUM_NAME_MASK = Pattern.compile("\\S*/(\\d{4}-\\d{2}-\\d{2}.*|\\d{4}_\\d{2}_\\d{2}.*)/\\S*");

    @ParameterizedTest
    @CsvSource({
            "/foo/2020-01-02/, 2020-01-02",
            "/foo/2020_01_02/, 2020_01_02",
            "/foo/2020-01-02-aaa/, 2020-01-02-aaa",
            "/foo/2020_01_02 aaa/jpeg, 2020_01_02 aaa",
    })
    public void albumNameMatching(String folderPath, String albumName) {
        Matcher matcher = ALBUM_NAME_MASK.matcher(folderPath);
        Assertions.assertTrue(matcher.matches());
        Assertions.assertEquals(albumName, matcher.group(1));
    }
}