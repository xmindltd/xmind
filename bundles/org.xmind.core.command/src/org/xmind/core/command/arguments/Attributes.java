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
package org.xmind.core.command.arguments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Attributes {

    private Map<String, String> map = new HashMap<String, String>();

    public Attributes() {
    }

    public Attributes(Map<?, ?> initials) {
        for (Object key : initials.keySet()) {
            this.map.put((String) key, (String) initials.get(key));
        }
    }

    public Attributes with(String key, String value) {
        map.put(key, value);
        return this;
    }

    public Attributes with(String key, int value) {
        map.put(key, String.valueOf(value));
        return this;
    }

    public Attributes with(String key, long value) {
        map.put(key, String.valueOf(value));
        return this;
    }

    public Attributes with(String key, boolean value) {
        map.put(key, String.valueOf(value));
        return this;
    }

    public Attributes with(String key, float value) {
        map.put(key, String.valueOf(value));
        return this;
    }

    public Attributes with(String key, double value) {
        map.put(key, String.valueOf(value));
        return this;
    }

    public String get(String key) {
        return map.get(key);
    }

    public String getString(String key, String defaultValue) {
        String value = map.get(key);
        return value == null ? defaultValue : value;
    }

    public int getInt(String key, int defaultValue) {
        String value = map.get(key);
        if (value == null || "".equals(value)) //$NON-NLS-1$
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = map.get(key);
        if (value == null || "".equals(value)) //$NON-NLS-1$
            return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = map.get(key);
        if (value == null || "".equals(value)) //$NON-NLS-1$
            return defaultValue;
        if ("true".equalsIgnoreCase(value)) //$NON-NLS-1$
            return true;
        if ("false".equalsIgnoreCase(value)) //$NON-NLS-1$
            return false;
        return defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        String value = map.get(key);
        if (value == null || "".equals(value)) //$NON-NLS-1$
            return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        String value = map.get(key);
        if (value == null || "".equals(value)) //$NON-NLS-1$
            return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Iterator<String> keys() {
        return map.keySet().iterator();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public Map<String, String> getRawMap() {
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof Attributes))
            return false;
        Attributes that = (Attributes) obj;
        return this.map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return "Attributes" + map.toString(); //$NON-NLS-1$
    }

}
