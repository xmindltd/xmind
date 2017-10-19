package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.ui.IMemento;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IDeserializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.BundleResource;
import org.xmind.core.io.BundleResourceInputSource;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.util.ProgressReporter;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class URLWorkbookRef extends AbstractWorkbookRef {

    private static final String SCHEME_PLATFORM = "platform"; //$NON-NLS-1$

    private static final String ATT_NAME = "name"; //$NON-NLS-1$

    private static Map<URLWorkbookRef, URI> cache = new WeakHashMap<URLWorkbookRef, URI>();

    private String name;

    private URLWorkbookRef(URI uri, String name, IMemento state) {
        super(uri, state);
        Assert.isNotNull(uri);
        this.name = name == null
                ? (state == null ? null : state.getString(ATT_NAME)) : name;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.ui.editor.Editable#getName()
     */
    @Override
    public String getName() {
        if (name != null)
            return name;
        return super.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.editor.AbstractWorkbookRef#saveState(org.eclipse.ui
     * .IMemento)
     */
    @Override
    protected void saveState(IMemento memento) {
        if (name != null) {
            memento.putString(ATT_NAME, name);
        }
        super.saveState(memento);
    }

    @Override
    public int hashCode() {
        return getURI().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof URLWorkbookRef))
            return false;
        URLWorkbookRef that = (URLWorkbookRef) obj;
        URI thisURI = this.getURI();
        URI thatURI = that.getURI();
        return thisURI == thatURI
                || (thisURI != null && thisURI.equals(thatURI));
    }

    @Override
    public String toString() {
        return getURI().toString();
    }

    public static URLWorkbookRef create(URI uri, IMemento state) {
        if (uri == null)
            return null;

        if (state != null)
            return new URLWorkbookRef(uri, null, state);

        for (URLWorkbookRef wr : cache.keySet()) {
            if (uri.equals(wr.getURI()))
                return wr;
        }
        URLWorkbookRef wr = new URLWorkbookRef(uri, null, null);
        cache.put(wr, uri);
        return wr;
    }

    public static URLWorkbookRef create(URI uri, String name) {
        if (uri == null)
            return null;

        for (URLWorkbookRef wr : cache.keySet()) {
            if (uri.equals(wr.getURI()))
                return wr;
        }
        URLWorkbookRef wr = new URLWorkbookRef(uri, name, null);
        cache.put(wr, uri);
        return wr;
    }

    @Override
    protected IWorkbook doLoadWorkbookFromURI(IProgressMonitor monitor, URI uri)
            throws InterruptedException, InvocationTargetException {
        try {
            if (URIUtil.isFileURI(uri)) {
                return loadFromFileURI(monitor, uri);
            } else if (SCHEME_PLATFORM.equalsIgnoreCase(uri.getScheme())) {
                return loadFromPlatformURI(monitor, uri);
            } else {
                return loadFromGenericURI(monitor, uri);
            }
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }
    }

    private IWorkbook loadFromGenericURI(IProgressMonitor monitor, URI uri)
            throws IOException, CoreException {
        URL url = uri.toURL();
        InputStream stream = url.openStream();
        try {
            IDeserializer deserializer = Core.getWorkbookBuilder()
                    .newDeserializer();
            deserializer.setWorkbookStorage(getTempStorage());
            deserializer.setEntryStreamNormalizer(getEncryptionHandler());
            deserializer.setInputStream(stream);
            ProgressReporter reporter = new ProgressReporter(monitor);
//            deserializer.deserializeManifest(reporter);
//            String passwordHint = deserializer.getManifest().getPasswordHint();
//            getEncryptable().setPasswordHint(passwordHint);
            deserializer.deserialize(reporter);
            return deserializer.getWorkbook();
        } finally {
            stream.close();
        }
    }

    private IWorkbook loadFromFileURI(IProgressMonitor monitor, URI uri)
            throws IOException, CoreException {
        File file = URIUtil.toFile(uri);
        if (file.isDirectory()) {
            IDeserializer deserializer = Core.getWorkbookBuilder()
                    .newDeserializer();
            deserializer.setWorkbookStorage(getTempStorage());
            deserializer.setInputSource(new DirectoryInputSource(file));
            deserializer.setEntryStreamNormalizer(getEncryptionHandler());
            deserializer.deserialize(new ProgressReporter(monitor));
            return deserializer.getWorkbook();
        }
        return loadFromGenericURI(monitor, uri);
    }

    private IWorkbook loadFromPlatformURI(IProgressMonitor monitor, URI uri)
            throws IOException, CoreException {
        // use decoded path, or the file with '%20' in path will not be found
        URL url = new URL(uri.getScheme(), uri.getAuthority(), uri.getPath());

        BundleResource resource;
        try {
            resource = new BundleResource(url);
        } catch (IllegalArgumentException e) {
            resource = null;
        }
        if (resource != null) {
            BundleResource resolvedResource = resource.resolve();
            if (resolvedResource != null
                    && resolvedResource.getPath().hasTrailingSeparator()) {
                // the workbook URI represents a directory
                IDeserializer deserializer = Core.getWorkbookBuilder()
                        .newDeserializer();
                deserializer.setWorkbookStorage(getTempStorage());
                deserializer.setInputSource(
                        new BundleResourceInputSource(resolvedResource));
                deserializer.setEntryStreamNormalizer(getEncryptionHandler());
                deserializer.deserialize(new ProgressReporter(monitor));
                return deserializer.getWorkbook();
            }
        }

        URL locatedURL = FileLocator.find(url);
        if (locatedURL != null) {
            url = locatedURL;
        }
        InputStream stream = url.openStream();
        try {
            IDeserializer deserializer = Core.getWorkbookBuilder()
                    .newDeserializer();
            deserializer.setWorkbookStorage(getTempStorage());
            deserializer.setEntryStreamNormalizer(getEncryptionHandler());
            deserializer.setInputStream(stream);
            deserializer.deserialize(new ProgressReporter(monitor));
            return deserializer.getWorkbook();
        } finally {
            stream.close();
        }
    }

}
