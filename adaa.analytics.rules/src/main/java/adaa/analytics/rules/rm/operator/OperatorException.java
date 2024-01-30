package adaa.analytics.rules.rm.operator;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class OperatorException extends Exception {
    private static final long serialVersionUID = 3626738574540303240L;
//    private static ResourceBundle messages = I18N.getUserErrorMessagesBundle();
    private static final MessageFormat formatter = new MessageFormat("");

    public OperatorException(String message) {
        super(message);
    }

    public OperatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperatorException(String errorKey, Throwable cause, Object... arguments) {
        super(getErrorMessage(errorKey, arguments), cause);
    }

    public static String getErrorMessage(String identifier, Object[] arguments) {
        String message = getResourceString(identifier, "short", "No message.");

        try {
            formatter.applyPattern(message);
            String formatted = formatter.format(arguments);
            return formatted;
        } catch (Throwable var4) {
            return message;
        }
    }

//    public static String getResourceString(String id, String key, String deflt) {
//        if (messages == null) {
//            return deflt;
//        } else {
//            try {
//                return messages.getString("error." + id + "." + key);
//            } catch (MissingResourceException var4) {
//                return deflt;
//            }
//        }
//    }

    /*
    TODO: Update method to default implementation
     */
    public static String getResourceString(String id, String key, String deflt) {
        return "";
    }
}
