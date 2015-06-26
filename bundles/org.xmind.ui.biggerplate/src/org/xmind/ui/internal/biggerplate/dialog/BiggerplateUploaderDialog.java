package org.xmind.ui.internal.biggerplate.dialog;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.biggerplate.BiggerplateMessages;
import org.xmind.ui.biggerplate.BiggerplatePlugin;
import org.xmind.ui.internal.biggerplate.Info;
import org.xmind.ui.resources.FontUtils;

public class BiggerplateUploaderDialog extends TitleAreaDialog {

    private static final int PREVIEW_IMAGE_WIDTH = 400;

    private static final int PREVIEW_IMAGE_HEIGHT = 180;

    private static final int DESCRIPTION_TEXT_MIN_HEIGHT = 60;

    private static enum DescriptionTextStatus {
        UNUSED, //
        USED;
    }

    private Info info;

    private Text titleText;

    private Text descriptionText;

    private CutPreviewViewer viewer;

    public BiggerplateUploaderDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE
                | SWT.APPLICATION_MODAL);
        setBlockOnOpen(true);
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    @Override
    public void create() {
        super.create();
        setFocus();
    }

    @Override
    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        if (id == OK) {
            label = BiggerplateMessages.UploaderDialog_UploadButton_text;
        }
        return super.createButton(parent, id, label, defaultButton);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(BiggerplateMessages.UploaderDialog_title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 5;
        composite2.setLayout(layout2);

        Composite container = new Composite(composite2, SWT.BORDER);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 15;
        layout.marginWidth = 15;
        layout.horizontalSpacing = 20;
        layout.verticalSpacing = 15;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        createEditTitleArea(container);
        createPreviewArea(container);
        createDescriptionArea(container);
//        createTagArea(container);
//        createCopyrightArea(container);

        setTitle(BiggerplateMessages.UploaderDialog_Upload_title);
        setMessage(BiggerplateMessages.UploaderDialog_Upload_description);
        if (getInfo().getBoolean(Info.MULTISHEETS)) {
            setMessage(BiggerplateMessages.UploaderDialog_uploadOneSheet_message);
        }

        return composite;
    }

    private void createEditTitleArea(Composite parent) {
        createLabel(parent, BiggerplateMessages.UploaderDialog_TitleLabel_text);
        titleText = createText(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        titleText.setText(info.getString(Info.TITLE));
    }

    private void createPreviewArea(Composite parent) {
        createLabel(parent,
                BiggerplateMessages.UploaderDialog_PreviewLabel_text);

        if (viewer == null) {
            viewer = new CutPreviewViewer();
            viewer.setInfo(info);
            viewer.setPrefHeight(PREVIEW_IMAGE_HEIGHT);
            viewer.setPrefWidth(PREVIEW_IMAGE_WIDTH - 2);
        }
        viewer.createControl(parent);
        viewer.setBackgroundColor(parent.getBackground());
        viewer.getControl().setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));
        viewer.getControl().addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    getShell().traverse(e.detail);
                }
            }
        });

        double widthRatio = (double) PREVIEW_IMAGE_WIDTH
                / viewer.getImage().getBounds().width;
        double heightRatio = (double) PREVIEW_IMAGE_HEIGHT
                / viewer.getImage().getBounds().height;
        double ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        viewer.setRatio(ratio);
    }

    private void createDescriptionArea(Composite parent) {
        createLabel(parent,
                BiggerplateMessages.UploaderDialog_DescriptionLabel_text);

        descriptionText = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.LEFT
                | SWT.BORDER);
        descriptionText
                .setText(BiggerplateMessages.UploaderDialog_Description_Initial_text);
        final GridData data = new GridData(SWT.LEFT, SWT.FILL, false, true);
//        data.widthHint = PREVIEW_IMAGE_WIDTH - 10;
        data.heightHint = DESCRIPTION_TEXT_MIN_HEIGHT;
        descriptionText.setLayoutData(data);
        descriptionText.setData(DescriptionTextStatus.UNUSED);

        final Font font = descriptionText.getFont();
        descriptionText.setFont(FontUtils.getItalic(font));
        final Color foreground = descriptionText.getForeground();
        descriptionText.setForeground(parent.getDisplay().getSystemColor(
                SWT.COLOR_GRAY));

        descriptionText.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event event) {
                descriptionText.setSize(PREVIEW_IMAGE_WIDTH, descriptionText
                        .computeSize(data.widthHint, data.heightHint, true).y);
            }
        });

        descriptionText.addFocusListener(new FocusListener() {

            public void focusLost(FocusEvent e) {
                descriptionText.removeFocusListener(this);
            }

            public void focusGained(FocusEvent e) {
                descriptionText.setData(DescriptionTextStatus.USED);
                descriptionText.setText(""); //$NON-NLS-1$
                descriptionText.setFont(font);
                descriptionText.setForeground(foreground);
            }
        });
    }

//    private void createTagArea(Composite parent) {
//        createLabel(parent, "Tag:");
//        final Text tagText = createText(parent, SWT.SINGLE | SWT.LEFT
//                | SWT.BORDER);
//        tagText.setText("Tag1, Tag2, ...");
//        ((GridData) tagText.getLayoutData()).widthHint = PREVIEW_IMAGE_WIDTH - 11;
//
//        final Font font = tagText.getFont();
//        tagText.setFont(FontUtils.getItalic(font));
//        final Color foreground = tagText.getForeground();
//        tagText.setForeground(parent.getDisplay()
//                .getSystemColor(SWT.COLOR_GRAY));
//
//        tagText.addFocusListener(new FocusListener() {
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                tagText.removeFocusListener(this);
//            }
//
//            @Override
//            public void focusGained(FocusEvent e) {
//                tagText.setText("");
//                tagText.setFont(font);
//                tagText.setForeground(foreground);
//            }
//        });
//    }

//    private void createCopyrightArea(Composite parent) {
//        createLabel(parent, "Copyright:");
//
//        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY
//                | SWT.SIMPLE);
//        combo.setItems(new String[] { "All Rights Reserved",
//                "Some Rights Reserved", "No Rights Reserved" });
//        combo.select(0);
//        GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
//        data.widthHint = PREVIEW_IMAGE_WIDTH - 24;
//        combo.setLayoutData(data);
//    }

    private Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setBackground(parent.getBackground());
        GridData data = new GridData(SWT.RIGHT, SWT.TOP, false, false);
        data.verticalIndent = 3;
        label.setLayoutData(data);
        label.setText(text);
        return label;
    }

    private Text createText(Composite parent, int style) {
        final Text text = new Text(parent, style);
        final GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        data.widthHint = PREVIEW_IMAGE_WIDTH - 7;
        text.setLayoutData(data);

        text.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event event) {
                text.setSize(
                        PREVIEW_IMAGE_WIDTH,
                        text.computeSize(data.widthHint, data.heightHint, true).y);
            }
        });

        return text;
    }

    private void setFocus() {
        getButtonBar().setFocus();
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings global = BiggerplatePlugin.getDefault()
                .getDialogSettings();
        String sectionId = getClass().getName();
        IDialogSettings settings = global.getSection(sectionId);
        if (settings == null) {
            settings = global.addNewSection(sectionId);
        }
        return settings;
    }

    @Override
    protected void okPressed() {
        String title = titleText.getText();
        if (title != null && !title.equals("")) { //$NON-NLS-1$
            info.setProperty(Info.TITLE, title);
        }

        if (descriptionText.getData().equals(DescriptionTextStatus.USED)) {
            info.setProperty(Info.DESCRIPTION, descriptionText.getText());
        }

        super.okPressed();
    }

}
