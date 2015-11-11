/**
 * 
 */
package org.xmind.core.net.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmind.core.net.IDataStore;

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

}