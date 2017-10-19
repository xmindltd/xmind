package org.xmind.cathy.internal.dashboard;

import java.net.URI;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.views.Page;

public class RecentFileGridPage extends Page {

    private GalleryViewer viewer;

    private LocalResourceManager resources;

    @Override
    protected Control doCreateControl(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        if (null == resources)
            resources = new LocalResourceManager(JFaceResources.getResources(),
                    composite);
        Composite titleBar = new Composite(composite, SWT.NONE);
        titleBar.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ececec"))); //$NON-NLS-1$
        titleBar.setForeground(composite.getForeground());
        GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(titleBar);
        GridData titleBarData = new GridData(SWT.FILL, SWT.FILL, true, false);
        titleBarData.heightHint = 44;
        titleBar.setLayoutData(titleBarData);

        Label titleLabel = new Label(titleBar, SWT.NONE);
        titleLabel.setBackground(titleBar.getBackground());
        titleLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#000000"))); //$NON-NLS-1$

        titleLabel.setFont((Font) resources.get(
                JFaceResources.getHeaderFontDescriptor().increaseHeight(-1)));

        titleLabel.setText(WorkbenchMessages.DashboardRecentFiles_message);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
                .grab(true, true).applyTo(titleLabel);

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite panel = new Composite(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(panel);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        createViewer(panel);
        return composite;
    }

    private void createViewer(Composite parent) {
        viewer = new RecentFileViewer(parent);

        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                handleOpenRecentFile(event.getSelection());
            }
        });
    }

    private void handleOpenRecentFile(ISelection selection) {
        if (!(selection instanceof IStructuredSelection))
            return;

        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (!(element instanceof URI))
            return;

        URI uri = (URI) element;
        openFile(uri);
    }

    private void openFile(final URI uri) {
        executeCommand(MindMapCommandConstants.OPEN_WORKBOOK,
                MindMapCommandConstants.OPEN_WORKBOOK_PARAM_URI, uri);
    }

    private void executeCommand(String commandId, String parameter, URI uri) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return;

        final EHandlerService hs = window.getService(EHandlerService.class);
        final ECommandService cs = window.getService(ECommandService.class);

        if (hs == null || cs == null)
            return;

        final Command command = cs.getCommand(commandId);
        if (command == null || !command.isDefined())
            return;

        try {
            IParameter param = command.getParameter(parameter);
            if (param == null)
                return;

            ParameterizedCommand pc = new ParameterizedCommand(command,
                    new Parameterization[] {
                            new Parameterization(param, uri.toString()) });

            if (!hs.canExecute(pc))
                return;
            hs.executeHandler(pc);

        } catch (NotDefinedException e) {
            CathyPlugin.log(e, this.getClass().getName()
                    + "-->execute openLocalFileHandler or openCloudFileHandler"); //$NON-NLS-1$
        }
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (viewer != null) {
            if (adapter.isAssignableFrom(viewer.getClass()))
                return adapter.cast(viewer);
            T obj = viewer.getAdapter(adapter);
            if (obj != null)
                return obj;
        }
        return super.getAdapter(adapter);
    }

}
