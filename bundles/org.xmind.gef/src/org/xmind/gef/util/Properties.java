/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Frank Shaka
 */
public class Properties implements Cloneable {

    private Object source;

    private Map<String, Object> contents = new HashMap<String, Object>();

    private PropertyChangeSupport eventSupport;

    public Properties() {
        this.source = this;
        this.eventSupport = new PropertyChangeSupport(this);
    }

    public Properties(Object source) {
        this.source = source;
        this.eventSupport = new PropertyChangeSupport(source);
    }

    public Properties(Properties another) {
        this();
        putAll(another);
    }

    public Properties(Object source, Properties another) {
        this(source);
        putAll(another);
    }

    /**
     * @return the delegate
     */
    protected PropertyChangeSupport getDelegate() {
        return eventSupport;
    }

    public void set(String key, boolean value) {
        set(key, Boolean.valueOf(value));
    }

    public void set(String key, int value) {
        set(key, Integer.valueOf(value));
    }

    public void set(String key, Object value) {
        Object oldValue = contents.get(key);
        if (value != null && !value.equals(oldValue)) {
            contents.put(key, value);
            eventSupport.firePropertyChange(key, oldValue, value);
        } else if (value == null && value != oldValue) {
            contents.remove(key);
            eventSupport.firePropertyChange(key, oldValue, value);
        }
    }

    public void putAll(Properties another) {
        if (another == null)
            return;

        for (Entry<String, Object> entry : another.contents.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public Collection<String> keySet() {
        return contents.keySet();
    }

    public void remove(String key) {
        set(key, null);
    }

    public Object get(String key) {
        return contents.get(key);
    }

    public Object get(String key, Object defaultValue) {
        Object value = contents.get(key);
        return value == null ? defaultValue : value;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = get(key);
        return value instanceof Boolean ? ((Boolean) value).booleanValue()
                : defaultValue;

    }

    public int getInteger(String key, int defaultValue) {
        Object value = get(key);
        return value instanceof Integer ? ((Integer) value).intValue()
                : defaultValue;
    }

    public String getString(String key, String defaultValue) {
        Object value = get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    public void clear() {
        Iterator<String> ite = contents.keySet().iterator();
        while (ite.hasNext()) {
            String key = ite.next();
            Object oldValue = contents.get(key);
            ite.remove();
            if (oldValue != null) {
                eventSupport.firePropertyChange(key, oldValue, null);
            }
        }
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public boolean hasKey(String key) {
        return contents.containsKey(key);
    }

    public boolean hasValue(Object value) {
        return contents.containsValue(value);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Properties clone() {
        Properties newInstance = source == this ? new Properties()
                : new Properties(source);
        newInstance.contents.putAll(this.contents);
        return newInstance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        eventSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        eventSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        eventSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        eventSupport.removePropertyChangeListener(propertyName, listener);
    }

}