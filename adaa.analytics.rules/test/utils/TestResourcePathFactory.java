package utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class TestResourcePathFactory {

    public static Path get(String fileName) {
        String resourceFolderPath = TestResourcePathFactory.class.getClassLoader().getResource("").getFile();
        String[] split = resourceFolderPath.replace('/', '\\').split(Pattern.quote("\\"));
        String pathString;

        if (split.length > 0 && split[split.length - 1].equals("test")) {
            // przy wywoływaniu testów z poziomu gradlew scieżka zmienia się
            pathString = resourceFolderPath + "../../../../test/resources/" + fileName;
        } else {
            pathString = resourceFolderPath + "../../../test/resources/" + fileName;
        }
        if (pathString.startsWith("/")) {
            pathString = pathString.substring(1);
        }
        return Paths.get(pathString);
    }
}
