package org.xmind.ui.internal.zen;

public class JsonUtils {

    public static String parseString(Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        return null;
    }

}
