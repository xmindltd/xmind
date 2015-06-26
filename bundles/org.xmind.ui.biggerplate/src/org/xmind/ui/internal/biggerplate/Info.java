package org.xmind.ui.internal.biggerplate;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.xmind.core.IMeta;

public class Info {

    public static final String X = IMeta.THUMBNAIL + IMeta.SEP + "X"; //$NON-NLS-1$

    public static final String Y = IMeta.THUMBNAIL + IMeta.SEP + "Y"; //$NON-NLS-1$

    public static final String SCALE = IMeta.THUMBNAIL + IMeta.SEP + "Scale"; //$NON-NLS-1$

    public static final String TITLE = "Title"; //$NON-NLS-1$

    public static final String DESCRIPTION = IMeta.DESCRIPTION;

    public static final String FULL_IMAGE = "FullImage"; //$NON-NLS-1$

    public static final String WORKBOOK = "Workbook"; //$NON-NLS-1$

    public static final String FILE = "File"; //$NON-NLS-1$

    public static final String MULTISHEETS = "multiSheets"; //$NON-NLS-1$

    public static final String ACCESS_TOKEN = "accessToken"; //$NON-NLS-1$

    public static final String RESULT_URL = "resultUrl"; //$NON-NLS-1$

    public static final String CANCELED = "canceled"; //$NON-NLS-1$

    private Map<String, Object> properties = new HashMap<String, Object>();

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public boolean isProperty(String key, Object value) {
        Object property = getProperty(key);
        return property == value
                || (property != null && property.equals(value));
    }

    public void setProperty(String key, Object value) {
        Object oldValue = getProperty(key);
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
        Object newValue = getProperty(key);
        support.firePropertyChange(key, oldValue, newValue);
    }

    public void setInt(String key, int value) {
        setProperty(key, Integer.valueOf(value));
    }

    public void setDouble(String key, double value) {
        setProperty(key, Double.valueOf(value));
    }

    public void setBoolean(String key, boolean value) {
        setProperty(key, Boolean.valueOf(value));
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getString(String key, String defaultString) {
        Object property = getProperty(key);
        return property instanceof String ? (String) property : defaultString;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public int getInt(String key, int defaultInt) {
        Object property = getProperty(key);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        }
        return defaultInt;
    }

    public double getDouble(String key, double defaultValue) {
        Object property = getProperty(key);
        if (property instanceof Double) {
            return ((Double) property).doubleValue();
        }
        return defaultValue;
    }

    public boolean getBoolean(String key) {
        Object property = getProperty(key);
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue();
        return false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String key,
            PropertyChangeListener listener) {
        support.addPropertyChangeListener(key, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String key,
            PropertyChangeListener listener) {
        support.removePropertyChangeListener(key, listener);
    }

    @Override
    public String toString() {
        return properties.toString();
    }

}
