package org.xmind.ui.internal.editor;

import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapEditorInput implements IEditorInput, IPersistableElement {

    private URI uri;

    private IWorkbookRef workbookRef;

    public MindMapEditorInput(URI uri) {
        this(uri, MindMapUIPlugin.getDefault().getWorkbookRefFactory()
                .createWorkbookRef(uri, null));
        Assert.isNotNull(uri);
    }

    public MindMapEditorInput(IWorkbookRef workbookRef) {
        this(workbookRef.getURI(), workbookRef);
        Assert.isNotNull(workbookRef);
    }

    private MindMapEditorInput(URI uri, IWorkbookRef workbookRef) {
        this.uri = uri;
        this.workbookRef = workbookRef;
    }

    public IWorkbookRef getWorkbookRef() {
        return this.workbookRef;
    }

    public URI getURI() {
        if (uri != null)
            return uri;
        if (workbookRef != null)
            return workbookRef.getURI();
        throw new IllegalStateException("URI and workbookRef are both null"); //$NON-NLS-1$
    }

    public void dispose() {
        workbookRef = null;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (URI.class.equals(adapter)) {
            return adapter.cast(getURI());
        } else if (IWorkbookRef.class.equals(adapter)) {
            return adapter.cast(getWorkbookRef());
        } else if (IWorkbook.class.equals(adapter)) {
            IWorkbookRef workbookRef = getWorkbookRef();
            if (workbookRef != null) {
                return adapter.cast(workbookRef.getWorkbook());
            }
        }
        return null;
    }

    public boolean exists() {
        IWorkbookRef wr = getWorkbookRef();
        if (wr != null)
            return wr.exists();
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return MindMapUI.getImages().get(IMindMapImages.XMIND_FILE_ICON);
    }

    public String getName() {
        IWorkbookRef wr = getWorkbookRef();
        if (wr != null) {
            String workbookName = wr.getName();
            if (workbookName == null)
                return MindMapMessages.MindMapEditorInput_Workbook_Untitled_title;
            return workbookName;
        }

        URI uri = getURI();
        if (uri != null) {
            String path = uri.getPath();
            if (path != null && path.length() > 0) {
                if (path.charAt(path.length() - 1) == '/') {
                    path = path.substring(0, path.length() - 1);
                }
                int sep = path.lastIndexOf('/');
                if (sep >= 0) {
                    return path.substring(sep + 1);
                }
            }
            return path;
        }

        return ""; //$NON-NLS-1$
    }

    public IPersistableElement getPersistable() {
        IWorkbookRef wr = getWorkbookRef();
        if (wr == null)
            return null;
        return this;
    }

    public String getFactoryId() {
        return MindMapEditorInputFactory.ID;
    }

    public void saveState(IMemento memento) {
        MindMapEditorInputFactory.saveState(this, memento);
    }

    public String getToolTipText() {
        IWorkbookRef wr = getWorkbookRef();
        if (wr != null)
            return wr.getDescription();
        URI uri = getURI();
        if (uri != null)
            return uri.toString();
        return ""; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        IWorkbookRef wr = getWorkbookRef();
        if (wr != null)
            return wr.hashCode();
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MindMapEditorInput))
            return false;
        MindMapEditorInput that = (MindMapEditorInput) obj;
        IWorkbookRef thisWR = this.workbookRef;
        IWorkbookRef thatWR = that.workbookRef;
        URI thisURI = this.uri;
        URI thatURI = that.uri;
        return (thisURI == thatURI
                || (thisURI != null && thisURI.equals(thatURI)))
                && (thisWR == thatWR
                        || (thisWR != null && thisWR.equals(thatWR)));
    }

    @Override
    public String toString() {
        IWorkbookRef wr = getWorkbookRef();
        if (wr != null)
            return wr.toString();
        return super.toString();
    }

}
