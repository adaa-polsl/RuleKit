package adaa.analytics.rules.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class VersionService {

    private Properties versionProperties;

    public VersionService() {
        versionProperties = new Properties();
        try {
            versionProperties.load(this.getClass().getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            versionProperties = null;
        }
    }

    public String getVersion() {
        if (versionProperties != null)
            return versionProperties.getProperty("version");
        else
            return "dev version";
    }

    public String getBuildDate() {
        if (versionProperties != null)
            return versionProperties.getProperty("date");
        else
            return "no date" ;//new SimpleDateFormat().format(new java.util.Date());
    }
}