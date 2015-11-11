package org.xmind.cathy.internal;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.xmind.ui.internal.editor.WorkbookRefManager;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class CheckRecoverFilesProcess extends AbstractCheckFilesProcess {

    private class ListLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof IEditorInput)
                return ((IEditorInput) element).getName();
            return null;
        }

        public Image getImage(Object element) {
            if (element instanceof IEditorInput) {
                ImageDescriptor image = MindMapUI.getImages()
                        .get(IMindMapImages.XMIND_ICON);
                if (image != null)
                    return image.createImage();
            }
            return null;
        }
    }

    private List<IEditorInput> loadedFiles;

    public CheckRecoverFilesProcess(IWorkbench workbench) {
        super(workbench);
    }

    public void checkAndRecoverFiles() {
        loadFilesToRecover();

        if (loadedFiles != null && !loadedFiles.isEmpty()) {
            filterFiles();
            openEditors(false);
        }

        WorkbookRefManager.getInstance().clearLastSession();
    }

    private void loadFilesToRecover() {
        loadedFiles = WorkbookRefManager.getInstance().loadLastSession();
    }

    private void filterFiles() {
        getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                ListSelectionDialog dialog = new ListSelectionDialog(null,
                        loadedFiles, new ArrayContentProvider(),
                        new ListLabelProvider(),
                        WorkbenchMessages.appWindow_ListSelectionDialog_Text);
                dialog.setTitle(
                        WorkbenchMessages.appWindow_ListSelectionDialog_Title);
                dialog.setInitialElementSelections(loadedFiles);
                int ret = dialog.open();
                if (ret == ListSelectionDialog.CANCEL)
                    return;
                Object[] result = dialog.getResult();
                for (Object input : result) {
                    addEditorToOpen((IEditorInput) input);
                }
            }
        });
    }

}
