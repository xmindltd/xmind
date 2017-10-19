package org.xmind.cathy.internal.renderer;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.utils.CommandUtils;
import org.xmind.ui.resources.ColorUtils;

public class XEditorStackRenderer extends StackRenderer {

    private ResourceManager resources;

    private Composite nullContent;

    @Override
    public Object createWidget(MUIElement element, Object parent) {
        final CTabFolder ctf = (CTabFolder) super.createWidget(element, parent);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                ctf);
        nullContent = createNullContentTipArea(ctf);
        nullContent.moveBelow(null);

        ctf.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                nullContent.moveBelow(null);

                /// Set focus with no edit content, let the dashboard lose focus (Shift + Command + C key binding )
                if (ctf.getItemCount() == 0)
                    ctf.setFocus();
            }
        });

        ctf.addControlListener(new ControlListener() {

            @Override
            public void controlResized(ControlEvent e) {
                nullContent.setBounds(ctf.getClientArea());
            }

            @Override
            public void controlMoved(ControlEvent e) {
                nullContent.setBounds(ctf.getClientArea());
            }
        });

        return ctf;
    }

    private Composite createNullContentTipArea(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        Composite centerArea = new Composite(composite, SWT.NONE);
        centerArea.setBackground(composite.getBackground());
        centerArea.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true));
        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.verticalSpacing = 40;
        centerArea.setLayout(layout2);

        createTopArea(centerArea);
        createBottomArea(centerArea);

        return composite;
    }

    private void createTopArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 20;
        composite.setLayout(layout);

        Label imageLabel = new Label(composite, SWT.NONE);
        imageLabel.setBackground(composite.getBackground());
        imageLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        imageLabel.setImage((Image) resources.get(
                CathyPlugin.imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                        "icons/views/null_editor_tip.png"))); //$NON-NLS-1$

    }

    private void createBottomArea(Composite parent) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(WorkbenchMessages.XStackRenderer_BottomArea_Add_button);
        GridData layoutData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        layoutData.widthHint = Math.max(128,
                button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 10);
        button.setLayoutData(layoutData);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CommandUtils.executeCommand("org.xmind.ui.command.newWorkbook", //$NON-NLS-1$
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            }
        });
    }

}
