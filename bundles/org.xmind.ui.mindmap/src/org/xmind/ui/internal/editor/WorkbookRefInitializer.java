package org.xmind.ui.internal.editor;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.xmind.core.IWorkbook;

public class WorkbookRefInitializer {

    private static WorkbookRefInitializer instance = new WorkbookRefInitializer();

    public void initialize(WorkbookRef ref, Object refKey, Object referrer)
            throws CoreException {
        if (refKey instanceof WorkbookEditorInput) {
            if (initNonExistingWorkbookRef(ref, (WorkbookEditorInput) refKey))
                return;
        }

        if (initByFileStore(ref, refKey))
            return;

        if (initByResourceFile(ref, refKey))
            return;

        initByLoadedWorkbook(ref, refKey);
    }

    private boolean initNonExistingWorkbookRef(WorkbookRef ref,
            WorkbookEditorInput input) {
        if (input.getContents() != null) {
            ref.setWorkbookLoader(
                    new PreLoadedWorkbookLoader(input.getContents()));
        } else {
            ref.setWorkbookLoader(
                    new TemplatedWorkbookLoader(input.getTemplateStream()));
        }
        return true;
    }

    private boolean initByLoadedWorkbook(WorkbookRef ref, Object refKey) {
        IWorkbook workbook = (IWorkbook) MME.getAdapter(refKey,
                IWorkbook.class);
        if (workbook != null) {
            ref.setWorkbookLoader(new PreLoadedWorkbookLoader(workbook));
            return true;
        }
        return false;
    }

    private boolean initByResourceFile(WorkbookRef ref, Object refKey)
            throws CoreException {
        // TODO 
        return false;
    }

    private boolean initByFileStore(WorkbookRef ref, Object refKey)
            throws CoreException {
        IFileStore fileStore = MME.getFileStore(refKey);
        if (fileStore == null)
            return false;

        FileStoreWorkbookAdapter adapter = new FileStoreWorkbookAdapter(
                fileStore);
        ref.setWorkbookLoader(adapter);
        ref.setWorkbookSaver(adapter);
        return true;
    }

    public static WorkbookRefInitializer getInstance() {
        return instance;
    }

}
