package org.xmind.ui.internal.editor;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * 
 * @author Frank Shaka
 */
public class FileEditorInput implements IEditorInput, IPersistableElement {

    private File file;

    /**
     * @param file
     */
    public FileEditorInput(File file) {
        super();
        this.file = file;
    }

    public boolean exists() {
        return file.exists() && file.canRead();
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return file.getName();
    }

    public IPersistableElement getPersistable() {
        return this;
    }

    public String getToolTipText() {
        return file.getAbsolutePath();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == File.class)
            return file;
        if (adapter == IPersistableElement.class)
            return this;
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    public String getFactoryId() {
        return FileEditorInputFactory.getFactoryId();
    }

    public void saveState(IMemento memento) {
        FileEditorInputFactory.saveState(memento, this);
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof FileEditorInput))
            return false;
        FileEditorInput that = (FileEditorInput) obj;
        return this.file.equals(that.file);
    }

    public String toString() {
        return "FileEditorInput@" + file.getAbsolutePath(); //$NON-NLS-1$
    }

    public int hashCode() {
        return file.hashCode();
    }

}
