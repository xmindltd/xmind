package org.xmind.ui.internal.editor;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;

public class MindMapEditorInputFactory implements IElementFactory {

    public static final String ID = "org.xmind.ui.MindMapEditorInputFactory"; //$NON-NLS-1$

    private static final String TAG_URI = "uri"; //$NON-NLS-1$
    private static final String TAG_STATE = "state"; //$NON-NLS-1$

    public IAdaptable createElement(IMemento memento) {
        String uriString = memento.getString(TAG_URI);
        if (uriString == null)
            return null;

        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            MindMapUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID, "Invalid URI: " + uriString, e)); //$NON-NLS-1$
            return null;
        }

        IMemento state = memento.getChild(TAG_STATE);
        if (state != null) {
            IWorkbookRef workbookRef = MindMapUIPlugin.getDefault()
                    .getWorkbookRefFactory().createWorkbookRef(uri, state);
            if (workbookRef != null) {
                return new MindMapEditorInput(workbookRef);
            }
        }
        return new MindMapEditorInput(uri);
    }

    public static void saveState(MindMapEditorInput input, IMemento memento) {
        URI uri = input.getURI();
        if (uri == null)
            return;

        String uriString = uri.toString();
        memento.putString(TAG_URI, uriString);

        IWorkbookRef workbookRef = input.getWorkbookRef();
        if (workbookRef != null) {
            IPersistable persistable = MindMapUIPlugin.getAdapter(workbookRef,
                    IPersistable.class);
            if (persistable != null) {
                IMemento state = memento.createChild(TAG_STATE);
                persistable.saveState(state);
            }
        }
    }

}
