package net.xmind.signin.internal;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmind.core.net.IDataStore;
import org.xmind.core.net.internal.JSONStore;

import net.xmind.signin.IXMindNetCommand;

public class XMindNetCommand implements IXMindNetCommand {

    public static final String STATUS = "xmind_status"; //$NON-NLS-1$

    public static final String JSON = "xmind_json"; //$NON-NLS-1$

    public static final String COMMAND = "xmind_cmd"; //$NON-NLS-1$

    private Properties properties;

    private IDataStore json = null;

    private XMindNetCommand(Properties properties) {
        this.properties = properties;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String getCode() {
        return get(STATUS);
    }

    public String getContentString() {
        return get(JSON);
    }

    public IDataStore getContent() {
        if (json == null)
            json = createJSON();
        return json;
    }

    private IDataStore createJSON() {
        String json = getContentString();
        if (json != null) {
            try {
                return new JSONStore(new JSONObject(json));
            } catch (JSONException e) {
            }
        }
        return null;
    }

    public String getCommandName() {
        return get(COMMAND);
    }

    public static XMindNetCommand createFromText(String text) {
        if (!text.startsWith("xmind_")) //$NON-NLS-1$
            return null;

        Properties properties = new Properties();
        String[] parts = text.split(";"); //$NON-NLS-1$
        for (String part : parts) {
            String[] kv = part.split("="); //$NON-NLS-1$
            if (kv.length == 2) {
                properties.setProperty(kv[0], kv[1]);
            }
        }
        return new XMindNetCommand(properties);
    }
}
