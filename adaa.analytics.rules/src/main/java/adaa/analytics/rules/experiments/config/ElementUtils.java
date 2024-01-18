package adaa.analytics.rules.experiments.config;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ElementUtils {
    public static String getXmlParameterValue(Element element, String name) {
        NodeList subnodes = element.getElementsByTagName(name);
        if (subnodes.getLength() > 0) {
            return subnodes.item(0).getTextContent();
        }

        return null;
    }
}
