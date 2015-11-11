/**
 * 
 */
package net.xmind.signin.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xmind.core.net.IDataStore;

public class PropertyStore implements IDataStore {

    private Properties properties;

    public PropertyStore(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean has(String key) {
        return properties.containsKey(key);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public long getLong(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public int getInt(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public double getDouble(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public Map<Object, Object> toMap() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object key : properties.keySet()) {
            map.put(key, properties.get(key));
        }
        return map;
    }

    public List<IDataStore> getChildren(String key) {
        return EMPTY.getChildren(key);
    }

}