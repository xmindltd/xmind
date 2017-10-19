package org.xmind.ui.internal;

import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.xmind.ui.internal.editor.ClonedWorkbookRef;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.IWorkbookRef;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class ClonedTemplate implements ITemplate {

    private URI sourceWorkbookURI;

    private String name;

    public ClonedTemplate(URI sourceWorkbookURI, String name) {
        super();
        Assert.isNotNull(sourceWorkbookURI);
        this.sourceWorkbookURI = sourceWorkbookURI;
        this.name = name;
    }

    public String getName() {
        if (this.name != null)
            return this.name;
        IWorkbookRef sourceWorkbookRef = MindMapUIPlugin.getDefault()
                .getWorkbookRefFactory()
                .createWorkbookRef(sourceWorkbookURI, null);
        if (sourceWorkbookRef != null)
            return sourceWorkbookRef.getName();
        return ""; //$NON-NLS-1$
    }

    @Override
    public IWorkbookRef createWorkbookRef() {
        return ClonedWorkbookRef
                .createFromSourceWorkbookURI(this.sourceWorkbookURI, name);
    }

    public URI getSourceWorkbookURI() {
        return this.sourceWorkbookURI;
    }

    @Override
    public int hashCode() {
        int h = 37 ^ sourceWorkbookURI.hashCode();
        if (name != null) {
            h = h ^ name.hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ClonedTemplate))
            return false;
        ClonedTemplate that = (ClonedTemplate) obj;
        return this.sourceWorkbookURI.equals(that.sourceWorkbookURI)
                && (this.name == that.name
                        || (this.name != null && this.name.equals(that.name)));
    }

}
