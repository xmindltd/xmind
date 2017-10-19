package org.xmind.ui.internal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.xmind.ui.mindmap.MindMapUI;

public class SeparatorControlContribution
        extends WorkbenchWindowControlContribution {

    private static final String ICON_SPLINE = "line.png"; //$NON-NLS-1$

    private ResourceManager resources;

    public SeparatorControlContribution() {
        super("org.xmind.ui.separatorControlContribution"); //$NON-NLS-1$
    }

    @Override
    protected Control createControl(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                label);

        label.setImage((Image) resources
                .get(MindMapUI.getImages().get(ICON_SPLINE, true)));
        return label;
    }

}
