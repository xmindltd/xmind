/**
 * 
 */
package org.xmind.core.net;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IDataStore {

    public static final IDataStore EMPTY = new IDataStore() {

        private final Map<Object, Object> noMap = Collections.emptyMap();

        private final List<IDataStore> noChildren = Collections.emptyList();

        public Map<Object, Object> toMap() {
            return noMap;
        }

        public boolean has(String key) {
            return false;
        }

        public String getString(String key) {
            return null;
        }

        public long getLong(String key) {
            return 0;
        }

        public int getInt(String key) {
            return 0;
        }

        public double getDouble(String key) {
            return 0;
        }

        public boolean getBoolean(String key) {
            return false;
        }

        public List<IDataStore> getChildren(String key) {
            return noChildren;
        }

    };

    boolean has(String key);

    String getString(String key);

    int getInt(String key);

    boolean getBoolean(String key);

    long getLong(String key);

    double getDouble(String key);

    List<IDataStore> getChildren(String key);

    Map<Object, Object> toMap();

}