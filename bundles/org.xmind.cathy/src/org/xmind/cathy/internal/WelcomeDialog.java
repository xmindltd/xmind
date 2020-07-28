package org.xmind.cathy.internal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

/**
 * @author Shawn Liu
 * @since 3.6.50
 */
public class WelcomeDialog extends Dialog {

    private ResourceManager resources;

    public WelcomeDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.NO_TRIM | SWT.TOOL | SWT.APPLICATION_MODAL
                | SWT.NO_REDRAW_RESIZE);
        setBlockOnOpen(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                newShell);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#cccccc"))); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 640;
        gridData.heightHint = 410;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 1;
        gridLayout.marginHeight = 1;
        composite.setLayout(gridLayout);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        GridData gridData2 = new GridData(GridData.FILL_BOTH);
        composite2.setLayoutData(gridData2);

        GridLayout gridLayout2 = new GridLayout(1, false);
        gridLayout2.marginWidth = 0;
        gridLayout2.marginHeight = 0;
        gridLayout2.verticalSpacing = 0;
        composite2.setLayout(gridLayout2);

        createTopSection(composite2);
        createBottomSection(composite2);

        return composite;
    }

    private void createTopSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        layoutData.widthHint = 637;
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        createHeaderSection(composite);
        createTitleSection(composite);
        createPlaceholderComposite(composite);
    }

    private void createPlaceholderComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        layoutData.heightHint = 33;
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);
    }

    private void createHeaderSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 10;
        layout.marginRight = 10;
        composite.setLayout(layout);

        createCloseButtonSection(composite);

        //add drag function
        Listener controlMovedListener = createControlMovedListener(composite);
        composite.addListener(SWT.MouseDown, controlMovedListener);
        composite.addListener(SWT.MouseMove, controlMovedListener);
        composite.addListener(SWT.MouseUp, controlMovedListener);
    }

    private void createCloseButtonSection(Composite parent) {
        Composite composite2 = new Composite(parent, SWT.NONE);
        composite2.setBackground(composite2.getParent().getBackground());
        GridData layoutData2 = new GridData(SWT.RIGHT, SWT.TOP, true, true);
        composite2.setLayoutData(layoutData2);

        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 5;
        layout2.marginHeight = 5;
        composite2.setLayout(layout2);

        final Label close = new Label(composite2, SWT.RIGHT);
        close.setBackground(composite2.getBackground());
        close.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        final Image focusedImage = (Image) resources.get(
                CathyPlugin.imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                        "icons/welcome/close.png")); //$NON-NLS-1$
        final Image noFocusedImage = (Image) resources.get(
                CathyPlugin.imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                        "icons/welcome/close-d.png")) //$NON-NLS-1$
        ;

        close.setImage(noFocusedImage);

        MouseTrackListener mouseTrackListener = new MouseTrackListener() {

            public void mouseHover(MouseEvent e) {
            }

            public void mouseExit(MouseEvent e) {
                close.setImage(noFocusedImage);
            }

            public void mouseEnter(MouseEvent e) {
                close.setImage(focusedImage);
            }
        };

        composite2.addMouseTrackListener(mouseTrackListener);
        close.addMouseTrackListener(mouseTrackListener);

        MouseListener mouseListener = new MouseListener() {

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                close();
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
        };

        composite2.addMouseListener(mouseListener);
        close.addMouseListener(mouseListener);
    }

    private Listener createControlMovedListener(final Control header) {
        Listener listener = new Listener() {
            Point point = null;
            Point startLocation = null;

            public void handleEvent(Event event) {
                Shell shell = getShell();

                switch (event.type) {
                case SWT.MouseDown:
                    if (getShell().isDisposed()) {
                        return;
                    }
                    point = header.toDisplay(event.x, event.y);
                    startLocation = shell.getLocation();
                    break;
                case SWT.MouseMove:
                    if (point == null)
                        return;
                    Point p2 = header.toDisplay(event.x, event.y);
                    int deltaX = p2.x - point.x;
                    int deltaY = p2.y - point.y;
                    Rectangle rect = shell.getBounds();
                    rect.x = startLocation.x + deltaX;
                    rect.y = startLocation.y + deltaY;
                    shell.setLocation(rect.x, rect.y);
                    break;
                case SWT.MouseUp:
                    point = null;
                    break;
                }
            }
        };
        return listener;
    }

    private void createTitleSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 31;
        layout.marginLeft = 47;
        composite.setLayout(layout);

        Label titleLabel = new Label(composite, SWT.CENTER);
        titleLabel.setBackground(composite.getBackground());
        titleLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#4a4a4a"))); //$NON-NLS-1$
        titleLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, true));

        FontData[] fontData = Display.getDefault().getSystemFont()
                .getFontData();
        titleLabel.setFont(
                (Font) resources.get(FontDescriptor.createFrom(FontUtils.bold(
                        (FontUtils.relativeHeight(fontData, 18)), true))));
        titleLabel.setText(WorkbenchMessages.WelcomDialog_Welcom_title);

        Label title2 = new Label(composite, SWT.BOTTOM);
        title2.setBackground(composite.getBackground());
        title2.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#4a4a4a"))); //$NON-NLS-1$
        GridData layoutData = new GridData(SWT.CENTER, SWT.BOTTOM, false, true);
        title2.setLayoutData(layoutData);

        title2.setFont((Font) resources.get(FontDescriptor
                .createFrom(FontUtils.relativeHeight(fontData, 4))));
        title2.setText(WorkbenchMessages.WelcomDialog_WhatIsNew_title);
    }

    private void createBottomSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);

        GridLayoutFactory.fillDefaults().applyTo(composite);

        createNewFeatureItems(composite);
        createButtonSection(composite);
    }

    private void createNewFeatureItems(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(3, true);
        layout.marginWidth = 50;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 10;
        composite.setLayout(layout);

        createImageItem(composite,
                (Image) resources.get(CathyPlugin.imageDescriptorFromPlugin(
                        CathyPlugin.PLUGIN_ID, "icons/welcome/slide.png")) //$NON-NLS-1$
                , WorkbenchMessages.WelcomeDialog_item_slide_title,
                WorkbenchMessages.WelcomeDialog_item_slide_description,
                new Runnable() {

                    public void run() {
                        // TODO launch permalink
                    }
                });

        createImageItem(composite,
                (Image) resources.get(CathyPlugin.imageDescriptorFromPlugin(
                        CathyPlugin.PLUGIN_ID,
                        "icons/welcome/new_workspace.png")) //$NON-NLS-1$
                , WorkbenchMessages.WelcomeDialog_item_workspace_title,
                WorkbenchMessages.WelcomeDialog_item_workspace_description,
                new Runnable() {

                    public void run() {
                        // TODO launch permalink
                    }
                });

        createImageItem(composite,
                (Image) resources.get(CathyPlugin.imageDescriptorFromPlugin(
                        CathyPlugin.PLUGIN_ID, "icons/welcome/high_dpi.png")) //$NON-NLS-1$
                , WorkbenchMessages.WelcomeDialog_item_high_dpi_title,
                WorkbenchMessages.WelcomeDialog_item_high_dpi_description,

                new Runnable() {

                    public void run() {
                        // TODO launch permalink
                    }
                });
    }

    private void createImageItem(Composite parent, Image image, String title,
            String description, final Runnable action) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(layoutData);

        GridLayoutFactory.fillDefaults().spacing(0, 10).applyTo(composite);

        Label imageLabel = new Label(composite, SWT.CENTER);
        imageLabel.setBackground(composite.getBackground());
        GridData gridData2 = new GridData(SWT.CENTER, SWT.TOP, true, false);
        imageLabel.setLayoutData(gridData2);
        imageLabel.setImage(image);
//        imageLabel.setCursor(
//                parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        imageLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                action.run();
            }
        });

        //
        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setBackground(composite.getBackground());
        GridData layoutData2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite2.setLayoutData(layoutData2);

        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.verticalSpacing = 10;
        composite2.setLayout(layout2);

        FontData[] fontData = Display.getDefault().getSystemFont()
                .getFontData();

        Label titleLabel = new Label(composite2, SWT.NONE);
        titleLabel.setBackground(composite2.getBackground());
        titleLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#4a4a4a"))); //$NON-NLS-1$
        titleLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        titleLabel.setText(title);
        titleLabel.setFont((Font) resources.get(FontDescriptor.createFrom(
                FontUtils.bold(FontUtils.relativeHeight(fontData, 2), true))));

        Label descriptionLabel = new Label(composite2, SWT.WRAP);
        descriptionLabel.setBackground(composite2.getBackground());
        descriptionLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#4a4a4a"))); //$NON-NLS-1$
        descriptionLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        descriptionLabel.setAlignment(SWT.CENTER);
        descriptionLabel.setText(description);
        descriptionLabel.setFont((Font) resources
                .get(FontDescriptor.createFrom(FontUtils.relativeHeight(
                        descriptionLabel.getFont().getFontData(), 0))));
    }

    private void createButtonSection(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().applyTo(container);
        GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(container);
        container.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#e8e8e8"))); //$NON-NLS-1$

        Composite composite = new Composite(container, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#fcfcfc"))); //$NON-NLS-1$
        GridData layoutData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(0, false);
        layout.marginWidth = 27;
        layout.marginHeight = 10;
        composite.setLayout(layout);

        createOkButton(composite);
    }

    private void createOkButton(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;

        final Button okButton = new Button(parent, SWT.PUSH);
        okButton.setBackground(parent.getBackground());

        GridData gridData = null;
        if (((GridLayout) parent.getLayout()).numColumns > 1) {
            gridData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        } else {
            gridData = new GridData(SWT.RIGHT, SWT.CENTER, true, true);
        }
        gridData.widthHint = 92;
        okButton.setLayoutData(gridData);
        okButton.setText(WorkbenchMessages.WelcomeDialog_okButton_text);
        okButton.setFont(
                (Font) resources.get(FontDescriptor.createFrom(FontUtils
                        .relativeHeight(okButton.getFont().getFontData(), 1))));

        okButton.addMouseListener(new MouseListener() {

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                close();
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
        });
    }

}
