package org.xmind.ui.internal;

import java.util.HashMap;
import java.util.Map;

public final class ModelCacheManager {

    public static final String MODEL_CACHE_DELAYLAYOUT = "org.xmind.ui.delayLayout"; //$NON-NLS-1$

    private static class ModelKeyWrapper {
        private Object model;
        private String key;

        public ModelKeyWrapper(Object model, String key) {
            this.model = model;
            this.key = key;
        }

        @Override
        public int hashCode() {
            return model.hashCode() ^ key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (!(obj instanceof ModelKeyWrapper))
                return false;
            ModelKeyWrapper that = (ModelKeyWrapper) obj;
            return this.model.equals(that.model) && this.key.equals(that.key);
        }
    }

    private static ModelCacheManager INSTANCE;

    private Map<ModelKeyWrapper, Object> caches = null;

    public ModelCacheManager() {
    }

    public void flush(Object model, String key) {
        if (caches == null || model == null || key == null)
            return;
        caches.remove(new ModelKeyWrapper(model, key));
    }

    public Object getCache(Object model, String key) {
        if (caches == null || model == null || key == null)
            return null;
        return caches.get(new ModelKeyWrapper(model, key));
    }

    public void setCache(Object model, String key, Object cache) {
        if (model == null || key == null)
            return;
        if (caches == null)
            caches = new HashMap<ModelKeyWrapper, Object>();
        if (cache == null)
            caches.remove(new ModelKeyWrapper(model, key));
        else
            caches.put(new ModelKeyWrapper(model, key), cache);
    }

    public static ModelCacheManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ModelCacheManager();
        return INSTANCE;
    }
}
