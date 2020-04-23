package utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestResourcePathFactory {

    public static Path get(String fileName) {
        String resourceFolderPath = TestResourcePathFactory.class.getClassLoader().getResource("").getFile();
        String pathString = resourceFolderPath + "../../../test/resources/" + fileName;
        if (pathString.startsWith("/")) {
            pathString = pathString.substring(1);
        }
        return Paths.get(pathString);
    }
}
