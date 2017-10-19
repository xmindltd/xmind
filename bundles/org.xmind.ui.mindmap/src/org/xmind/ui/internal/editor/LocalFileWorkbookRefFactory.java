package org.xmind.ui.internal.editor;

import java.net.URI;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.ui.IMemento;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefFactory;

public class LocalFileWorkbookRefFactory implements IWorkbookRefFactory {

    private Map<IWorkbookRef, URI> reversedCache = new WeakHashMap<IWorkbookRef, URI>();

    public LocalFileWorkbookRefFactory() {
    }

    public IWorkbookRef createWorkbookRef(URI uri, IMemento state) {
        if (uri == null)
            return null;

        for (IWorkbookRef wr : reversedCache.keySet()) {
            if (uri.equals(wr.getURI()))
                return wr;
        }

        IWorkbookRef wr = new LocalFileWorkbookRef(uri, state);
        reversedCache.put(wr, uri);
        return wr;
    }

}
