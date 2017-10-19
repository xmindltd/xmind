package org.xmind.ui.internal.editor;

import java.net.URI;

import org.eclipse.ui.IMemento;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefFactory;

public class TempWorkbookRefFactory implements IWorkbookRefFactory {

    public static final String URI_SCHEME = "xmind-temp"; //$NON-NLS-1$

    public TempWorkbookRefFactory() {
    }

    public IWorkbookRef createWorkbookRef(URI uri, IMemento state) {
        String path = uri.getPath();
        if (path == null)
            return null;

        if (CreatedWorkbookRef.URI_PATH.equals(path)) {
            return CreatedWorkbookRef.create(uri, state);
        }

        if (ClonedWorkbookRef.URI_PATH.equals(path)) {
            return ClonedWorkbookRef.create(uri, state);
        }

        return null;
    }

}
