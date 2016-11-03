package org.xmind.cathy.internal.dashboard;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.editor.IEditorHistory.IEditorHistoryListener;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;
import org.xmind.ui.internal.dashboard.pages.IDashboardContext;
import org.xmind.ui.views.IPage;
import org.xmind.ui.views.PageStack;

public class RecentFileGridDashboardPage extends DashboardPage
        implements IAdaptable {

    private static final String COMMAND_OPEN_SEAWIND_FILE_ID = "org.xmind.ui.seawind.command.openSeawindFile"; //$NON-NLS-1$

    private static final String COMMAND_OPEN_LOCAL_FILE_ID = "org.xmind.ui.mindmap.command.openLocalFile"; //$NON-NLS-1$

    private GalleryViewer viewer;

    private PageStack stack;

    IPage recentBlankPage;

    IPage recentFileGridPage;

    public void createControl(Composite parent) {

        stack = new PageStack();
        stack.createControl(parent);
        stack.getControl().setBackground(parent.getBackground());

        final IEditorHistory editorHistory = PlatformUI.getWorkbench()
                .getService(IEditorHistory.class);
        editorHistory.addEditorHistoryListener(new IEditorHistoryListener() {

            @Override
            public void editorHistoryChanged() {
                if (getControl() == null || getControl().isDisposed())
                    return;
                if (Display.getCurrent() == null)
                    return;
                showPage(editorHistory);
            }
        });

        //do chose which viewer will show;
        showPage(editorHistory);
    }

    private void showPage(final IEditorHistory editorHistory) {
        Composite composite = stack.getStackComposite();

        if (editorHistory.getAllInputURIs().length == 0) {
            if (recentBlankPage == null) {
                recentBlankPage = new RecentFileBlankPage();
                recentBlankPage.createControl(composite);
            }
            stack.showPage(recentBlankPage);
        } else {
            if (recentFileGridPage == null) {
                recentFileGridPage = new RecentFileGridPage();
                recentFileGridPage.createControl(composite);
                viewer = recentFileGridPage.getAdapter(GalleryViewer.class);
            }
            stack.showPage(recentFileGridPage);
        }
        setControl(stack.getControl());
        stack.setFocus();
    }

    @Override
    public void setContext(IDashboardContext context) {
        super.setContext(context);
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_RECENTFILE_PIN);
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_RECENTFILE_UNPIN);
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_RECENTFILE_CLEAR);
        context.registerAvailableCommandId(
                IWorkbenchCommandConstants.EDIT_DELETE);

        //register command in DashboardContext
        context.registerAvailableCommandId(COMMAND_OPEN_SEAWIND_FILE_ID);
        context.registerAvailableCommandId(COMMAND_OPEN_LOCAL_FILE_ID);
    }

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            getContext().registerContextMenu(viewer.getControl(),
                    ICathyConstants.POPUP_RECENTFILE);
        }
        if (stack != null && stack.getControl() != null
                && !stack.getControl().isDisposed()) {
            stack.setFocus();
        }
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (viewer != null) {
            if (adapter.isAssignableFrom(viewer.getClass()))
                return adapter.cast(viewer);
            T obj = viewer.getAdapter(adapter);
            if (obj != null)
                return obj;
        }
        return null;
    }

}
