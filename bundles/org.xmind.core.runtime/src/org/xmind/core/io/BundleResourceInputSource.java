package org.xmind.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class BundleResourceInputSource implements IInputSource {

    private BundleResource base;

    public BundleResourceInputSource(BundleResource baseResource) {
        Assert.isNotNull(baseResource);
        BundleResource resolved = baseResource.resolve();
        this.base = resolved == null ? baseResource : resolved;
    }

    public BundleResourceInputSource(String pluginId, String basePath) {
        this(new BundleResource(Platform.getBundle(pluginId),
                new Path(basePath)));
    }

    public BundleResourceInputSource(Bundle bundle, String basePath) {
        this(new BundleResource(bundle, new Path(basePath)));
    }

    private URL getEntry(String entryName) {
        BundleResource entryResource = new BundleResource(base.getBundle(),
                base.getPath().append(entryName)).resolve();
        if (entryResource == null)
            return null;
        return entryResource.toPlatformURL();
    }

    public boolean hasEntry(String entryName) {
        return getEntry(entryName) != null;
    }

    public Iterator<String> getEntries() {
        String basePath = base.getPath().toString();
        final String prefix = basePath.startsWith("/") ? basePath.substring(1) //$NON-NLS-1$
                : basePath;
        final Stack<Enumeration<String>> pathStack = new Stack<Enumeration<String>>();
        pathStack.push(base.getBundle().getEntryPaths(basePath));
        return new Iterator<String>() {

            private String nextPath = findNextPath();

            private String findNextPath() {
                if (pathStack.isEmpty())
                    return null;

                Enumeration<String> paths = pathStack.peek();

                if (!paths.hasMoreElements()) {
                    // reached end of current path list
                    pathStack.pop();
                    return findNextPath();
                }

                String path = paths.nextElement();

                if (path.endsWith("/")) { //$NON-NLS-1$
                    // directory path
                    // add sub path list
                    pathStack.push(base.getBundle().getEntryPaths(path));
                    return findNextPath();
                }

                if (path.startsWith(prefix))
                    return path.substring(prefix.length());

                return findNextPath();
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