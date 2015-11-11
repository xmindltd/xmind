package org.xmind.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class BundleResourceInputSource implements IInputSource {

    private Bundle bundle;

    private String basePath;

    public BundleResourceInputSource(String pluginId, String basePath) {
        this(Platform.getBundle(pluginId), basePath);
    }

    public BundleResourceInputSource(Bundle bundle, String basePath) {
        Assert.isNotNull(bundle);
        this.bundle = bundle;
        this.basePath = basePath;
    }

    private URL getEntry(String entryName) {
        if (bundle == null)
            return null;
        return bundle.getEntry(basePath + "/" + entryName); //$NON-NLS-1$
    }

    public boolean hasEntry(String entryName) {
        return getEntry(entryName) != null;
    }

    public Iterator<String> getEntries() {
        final Enumeration<String> paths = bundle.getEntryPaths(basePath);
        return new Iterator<String>() {

            private String nextPath = findNextPath();

            private String findNextPath() {
                if (paths.hasMoreElements()) {
                    String p = paths.nextElement();
                    if (!p.endsWith("/")) //$NON-NLS-1$
                        return p;
                }
                return null;
            }

            public void remove() {
            }

            public String next() {
                String p = nextPath;
                nextPath = findNextPath();
                return p;
            }

            public boolean hasNext() {
                return nextPath != null;
            }
        };
    }

    public boolean isEntryAvailable(String entryName) {
        return getEntry(entryName) != null;
    }

    public InputStream getEntryStream(String entryName) {
        try {
            return openEntryStream(entryName);
        } catch (IOException e) {
            return null;
        }
    }

    public InputStream openEntryStream(String entryName) throws IOException {
        URL entry = getEntry(entryName);
        if (entry == null)
            throw new FileNotFoundException();
        return entry.openStream();
    }

    public long getEntrySize(String entryName) {
        URL entry = getEntry(entryName);
        if (entry == null)
            return 0;
        URLConnection conn;
        try {
            conn = entry.openConnection();
        } catch (IOException e) {
            return 0;
        }
        return conn.getContentLength();
    }

    public long getEntryTime(String entryName) {
        URL entry = getEntry(entryName);
        if (entry == null)
            return 0;
        URLConnection conn;
        try {
            conn = entry.openConnection();
        } catch (IOException e) {
            return 0;
        }
        return conn.getLastModified();
    }

}