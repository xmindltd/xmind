package org.xmind.cathy.internal.dashboard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.dashboard.pages.DashboardPage;

public class RecentFileListDashboardPage extends DashboardPage {

    private GalleryViewer viewer;

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());
        GridLayoutFactory.fillDefaults().applyTo(composite);

        Composite titleBar = new Composite(composite, SWT.NONE);
        titleBar.setBackground(composite.getBackground());
        titleBar.setForeground(composite.getForeground());
        GridLayoutFactory.fillDefaults().margins(30, 20).applyTo(titleBar);

        Label titleLabel = new Label(titleBar, SWT.NONE);
        titleLabel.setBackground(titleBar.getBackground());
        titleLabel.setForeground(titleBar.getForeground());
        titleLabel.setText(WorkbenchMessages.DashboardRecentFiles_message);
        titleLabel.setFont((Font) JFaceResources.getResources()
                .get(JFaceResources.getHeaderFontDescriptor()
                        .increaseHeight(Util.isMac() ? 2 : 1)));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(titleLabel);

        createViewer(composite);

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

        return control;
    }

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

}
