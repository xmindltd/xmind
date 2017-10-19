/**
 * 
 */
package org.xmind.core.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class JSONStore implements IDataStore {

    private JSONObject json;

    public JSONStore(JSONObject json) {
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }

    public boolean has(String key) {
        return json.has(key);
    }

    public long getLong(String key) {
        return json.optLong(key, 0);
    }

    public boolean getBoolean(String key) {
        return json.optBoolean(key, false);
    }

    public int getInt(String key) {
        return json.optInt(key, 0);
    }

    public double getDouble(String key) {
        return json.optDouble(key, 0);
    }

    public String getString(String key) {
        Object o = json.opt(key);
        return o == null || JSONObject.NULL.equals(o) ? null : o.toString();
    }

    public Map<Object, Object> toMap() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        Iterator<?> keys = json.keys();
        while (keys.hasNext()) {
            Object key = keys.next();
            map.put(key, json.opt((String) key));
        }
        return map;
    }

    public List<IDataStore> getChildren(String key) {
        JSONArray array = json.optJSONArray(key);
        if (array != null) {
            List<IDataStore> children = new ArrayList<IDataStore>(
                    array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject child = array.optJSONObject(i);
                children.add(new JSONStore(child));
            }
            return children;
        }
        return EMPTY.getChildren(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof JSONStore))
            return false;
        JSONStore that = (JSONStore) obj;
        return this.json.equals(that.json);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return json.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return json.toString();
    }

}