package org.xmind.cathy.internal.dashboard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.views.Page;

public class RecentFileBlankPage extends Page {

    private static final String COLOR_DESCRIPTION = "#9B9B9B"; //$NON-NLS-1$
    private LocalResourceManager resources;

    @Override
    protected Control doCreateControl(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);

        if (resources == null)
            resources = new LocalResourceManager(JFaceResources.getResources(),
                    composite);
        return createContent(composite);
    }

    private Control createContent(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setBackground(parent.getBackground());
        GridLayoutFactory.fillDefaults().spacing(0, 24)
                .extendedMargins(0, 0, 0, 100).applyTo(panel);
        GridDataFactory.fillDefaults().grab(true, true)
                .align(SWT.CENTER, SWT.CENTER).applyTo(panel);

        Label imageLabel = new Label(panel, SWT.NONE);
        imageLabel.setImage((Image) resources.get(
                CathyPlugin.imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                        "dashboard/recent/blank_recent.png"))); //$NON-NLS-1$
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER)
                .grab(true, false).applyTo(imageLabel);
        Point imageSize = imageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        Control textArea = createTextArea(panel);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
                .grab(true, false).hint(imageSize.x + 140, SWT.DEFAULT)
                .applyTo(textArea);

        return parent;
    }

    private Control createTextArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());
        GridLayoutFactory.fillDefaults().spacing(0, 24).applyTo(composite);

        Label descriptionLabel = new Label(composite, SWT.CENTER | SWT.WRAP);
        descriptionLabel.setForeground((Color) resources
                .get(ColorUtils.toDescriptor(COLOR_DESCRIPTION)));
        descriptionLabel.setFont(
                (Font) resources.get(JFaceResources.getDefaultFontDescriptor()
                        .setStyle(SWT.NORMAL).setHeight(12)));
        descriptionLabel
                .setText(WorkbenchMessages.RecentFileBlankPage_description);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
                .grab(true, false).applyTo(descriptionLabel);

        return composite;
    }
}
