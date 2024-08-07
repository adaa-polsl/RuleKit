package adaa.analytics.rules.consoles;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class VersionService {

    private Properties versionProperties;

    public VersionService() {
        versionProperties = new Properties();
        try {
            versionProperties.load(this.getClass().getResourceAsStream("/version.properties"));
        } catch (Exception e) {
            versionProperties = null;
        }
    }

    private String getProperty(String name, String defaultValue) {
        if (versionProperties != null)
            return versionProperties.getProperty(name);
        else
            return defaultValue;
    }

    public String getVersion() {
        return getProperty("version", "dev version");
    }

    public String getCommitHash() {
        return getProperty("commitHash", "no hash");
    }

    public String getCommitDate() {
        String date =  getProperty("commitDate", null);
        if (date != null) {
            return new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(Long.parseLong(date) * 1000));
        } else {
            return "no commit date";
        }
    }

    public String getSimpleHeader() {
        String version = getVersion();
        String commitHash = getCommitHash();
        String commitDate = getCommitDate();
        return "RuleKit " + version + " (build " + commitHash + " " + commitDate + ")\n";
    }

    public String getHeader() {
        int yearNumber = Calendar.getInstance().get(Calendar.YEAR);
        return getSimpleHeader() + "    RuleKit Development Team (c) "+Math.max(2024,yearNumber)+"\n\n";
    }

}