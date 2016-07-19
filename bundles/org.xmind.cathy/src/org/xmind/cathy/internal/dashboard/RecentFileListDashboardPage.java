package org.xmind.cathy.internal.dashboard;

import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;
import org.xmind.ui.internal.dashboard.pages.IDashboardContext;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

public class RecentFileListDashboardPage extends DashboardPage
        implements IAdaptable {

    private GalleryViewer viewer;

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);

        Composite titleBar = new Composite(composite, SWT.NONE);
//        titleBar.setBackground(composite.getBackground());
        titleBar.setBackground((Color) JFaceResources.getResources()
                .get(ColorUtils.toDescriptor("#ececec"))); //$NON-NLS-1$
        titleBar.setForeground(composite.getForeground());
        GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(titleBar);
        GridData titleBarData = new GridData(SWT.FILL, SWT.FILL, true, false);
        titleBarData.heightHint = 44;
        titleBar.setLayoutData(titleBarData);

        Label titleLabel = new Label(titleBar, SWT.NONE);
        titleLabel.setBackground(titleBar.getBackground());
//        titleLabel.setForeground(titleBar.getForeground());
        titleLabel.setForeground((Color) JFaceResources.getResources()
                .get(ColorUtils.toDescriptor("#000000"))); //$NON-NLS-1$
        titleLabel.setFont((Font) JFaceResources.getResources().get(
                JFaceResources.getHeaderFontDescriptor().increaseHeight(-1)));
        titleLabel.setText(WorkbenchMessages.DashboardRecentFiles_message);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, true).applyTo(titleLabel);

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        IDashboardContext context = getContext();
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_RECENTFILE_PIN);
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_RECENTFILE_UNPIN);
        context.registerAvailableCommandId(
                ICathyConstants.COMMAND_RECENTFILE_CLEAR);
        context.registerAvailableCommandId(
                IWorkbenchCommandConstants.EDIT_DELETE);
        Control container = createViewer(composite);
        context.registerContextMenu(container,
                ICathyConstants.POPUP_RECENTFILE);
        setControl(composite);
    }

    protected Control createViewer(Composite parent) {
        viewer = new RecentFileViewer(parent);
        Control control = viewer.getControl();
        GridData viewerLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                true);
        control.setLayoutData(viewerLayoutData);

        control.setBackground(parent.getBackground());
        control.setForeground(parent.getForeground());

        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    final Object element = ((IStructuredSelection) selection)
                            .getFirstElement();
                    if (element instanceof URI) {
                        handleOpenRecentFile((URI) element);
                    }
                }
            }
        });

        return control;
    }

    private void handleOpenRecentFile(URI uri) {
        IEditorInput input = MindMapUI.getEditorInputFactory()
                .createEditorInput(uri);
        getContext().openEditor(input, MindMapUI.MINDMAP_EDITOR_ID);
    }

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
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
