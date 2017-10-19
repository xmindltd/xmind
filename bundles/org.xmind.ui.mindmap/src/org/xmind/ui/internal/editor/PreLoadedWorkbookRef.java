package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IMemento;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.ISerializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.CloneHandler;
import org.xmind.ui.mindmap.IWorkbookRef;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class PreLoadedWorkbookRef extends AbstractWorkbookRef {

    private static final String TAG_NAME = "name"; //$NON-NLS-1$

    private IWorkbook sourceWorkbook;

    private String name;

    private PreLoadedWorkbookRef(IMemento state, IWorkbook workbook,
            String name) {
        super(null, state);
        this.sourceWorkbook = workbook;
        this.name = name;
    }

    /**
     * Makes a clone of the source workbook to be the content workbook and
     * stores its content at the temp storage.
     */
    @Override
    protected IWorkbook doLoadWorkbookFromURI(IProgressMonitor monitor, URI uri)
            throws InterruptedException, InvocationTargetException {
        if (sourceWorkbook == null) {
            throw new IllegalStateException("No source workbook to load"); //$NON-NLS-1$
        }
        try {
            /// make a clone workbook from the source workbook as the loaded workbook
            IWorkbook workbook = Core.getWorkbookBuilder()
                    .createWorkbook(getTempStorage());

            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(workbook);
            serializer.setWorkbookStorageAsOutputTarget();
            serializer.setEntryStreamNormalizer(getEncryptionHandler());
            serializer.serialize(null);

            new CloneHandler().withWorkbooks(sourceWorkbook, workbook)
                    .copyWorkbookContents();
            return workbook;
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Loads the content workbook from the specified storage and makes a clone
     * of it to be the source workbook (stored at a temporary location).
     */
    @Override
    protected IWorkbook doLoadWorkbookFromTempStorage(IProgressMonitor monitor,
            IStorage tempStorage)
            throws InterruptedException, InvocationTargetException {
        IWorkbook workbook = super.doLoadWorkbookFromTempStorage(monitor,
                tempStorage);

        if (sourceWorkbook == null) {
            /// make a clone of the saved workbook as the source workbook
            String sourceStoragePath = Core.getWorkspace()
                    .getTempDir("preloaded/" + UUID.randomUUID().toString()); //$NON-NLS-1$
            File sourceStorageDir = new File(sourceStoragePath);
            sourceStorageDir.mkdirs();
            try {
                sourceWorkbook = Core.getWorkbookBuilder()
                        .createWorkbook(new DirectoryStorage(sourceStorageDir));
                new CloneHandler().withWorkbooks(workbook, sourceWorkbook)
                        .copyWorkbookContents();
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }
        }

        return workbook;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        IWorkbook w = getWorkbook();
        return w == null ? super.hashCode() : w.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof PreLoadedWorkbookRef))
            return false;
        PreLoadedWorkbookRef that = (PreLoadedWorkbookRef) obj;
        IWorkbook thisWorkbook = this.getWorkbook();
        IWorkbook thatWorkbook = that.getWorkbook();
        return thisWorkbook == thatWorkbook
                || (thisWorkbook != null && thisWorkbook.equals(thatWorkbook));
    }

    @Override
    public String toString() {
        IWorkbook w = getWorkbook();
        return w == null ? super.toString() : w.toString();
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
            memento.putString(TAG_NAME, name);
        }
        super.saveState(memento);
    }

    public static IWorkbookRef createFromSavedState(IMemento state) {
        Assert.isLegal(state != null);
        return new PreLoadedWorkbookRef(state, null, state.getString(TAG_NAME));
    }

    public static IWorkbookRef createFromLoadedWorkbook(IWorkbook workbook,
            String name) {
        Assert.isLegal(workbook != null);
        return new PreLoadedWorkbookRef(null, workbook, name);
    }

}
