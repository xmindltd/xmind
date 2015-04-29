package net.xmind.share.dialog;

import java.util.HashMap;
import java.util.Map;

import net.xmind.share.Info;
import net.xmind.share.Messages;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.xmind.ui.resources.FontUtils;

public class PrivacyUploaderPage extends UploaderPage {

    private static boolean SmallFonts = Util.isMac()
            && System.getProperty("org.eclipse.swt.internal.carbon.smallFonts") != null; //$NON-NLS-1$

    private Map<Object, Button> optionButtons;

    private Button downloadCheck;

    public PrivacyUploaderPage() {
        setTitle(Messages.UploaderDialog_Privacy_title);
    }

    public void setFocus() {
        getControl().setFocus();
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout();
        layout.marginHeight = 15;
        layout.marginWidth = 15;
        composite.setLayout(layout);

        optionButtons = new HashMap<Object, Button>();
        Listener optionHandler = new Listener() {
            public void handleEvent(Event event) {
                handleOptionSelected((Button) event.widget);
            }
        };
        createOption(composite, Messages.UploaderDialog_Privacy_Public_title,
                Info.PRIVACY_PUBLIC, optionHandler);
        createDescription(composite,
                Messages.UploaderDialog_Privacy_Public_description);
        createOption(composite, Messages.UploaderDialog_Privacy_Unlisted_title,
                Info.PRIVACY_UNLISTED, optionHandler);
        createDescription(composite,
                Messages.UploaderDialog_Privacy_Unlisted_description);
        createOption(composite, Messages.UploaderDialog_Privacy_Private_title,
                Info.PRIVACY_PRIVATE, optionHandler);
        createDescription(composite,
                Messages.UploaderDialog_Privacy_Private_description);

        createDownloadCheck(composite);

        updateWidgets();
        setAccessibility(getAccessibility());
        setDownloadable(getDownloadable());

        setControl(composite);
    }

    private void createOption(Composite parent, String text, Object data,
            Listener optionHandler) {
        Button button = new Button(parent, SWT.RADIO);
        button.setBackground(parent.getBackground());
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        if (SmallFonts) {
            button.setFont(FontUtils.getBoldRelative(
                    JFaceResources.DEFAULT_FONT, 2));
        } else {
            button.setFont(FontUtils.getBoldRelative(
                    JFaceResources.DEFAULT_FONT, 1));
        }
        button.setText(text);
        button.setData(data);
        optionButtons.put(data, button);
        button.addListener(SWT.Selection, optionHandler);
    }

    private void createDescription(Composite parent, String description) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginBottom = 15;
        layout.marginLeft = 20;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setBackground(composite.getBackground());
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        if (!SmallFonts)
            label.setFont(FontUtils.getRelativeHeight(
                    JFaceResources.DEFAULT_FONT, -1));
        label.setText(description);
    }

    private void createDownloadCheck(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.marginTop = 10;
        composite.setLayout(layout);

        downloadCheck = new Button(composite, SWT.CHECK);
        downloadCheck.setBackground(parent.getBackground());
        downloadCheck
                .setText(Messages.UploaderDialog_Privacy_AllowDownload_text);
        downloadCheck.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        downloadCheck.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                setDownloadable(((Button) event.widget).getSelection());
            }
        });
    }

    private void handleOptionSelected(Button button) {
        Object data = button.getData();
        if (data == null)
            return;
        setAccessibility(data);
    }

    private void updateWidgets() {
        Object accessibility = getAccessibility();
        if (optionButtons != null) {
            Button selectedButton = optionButtons.get(accessibility);
            for (Button button : optionButtons.values()) {
                button.setSelection(button == selectedButton);
            }
        }
        if (downloadCheck != null && !downloadCheck.isDisposed()) {
            downloadCheck.setSelection(getDownloadable());
        }
    }

    private Object getAccessibility() {
        Object value = getInfo().getProperty(Info.PRIVACY);
        if (value == null) {
            value = getContainer().getDialogSettings().get(Info.PRIVACY);
            if (value == null) {
                value = Info.PRIVACY_PUBLIC;
            }
        }
        return value;
    }

    private boolean getDownloadable() {
        Object value = getInfo().getProperty(Info.DOWNLOADABLE);
        if (value == null) {
            value = getContainer().getDialogSettings().get(Info.DOWNLOADABLE);
            if (value == null) {
                value = Info.DOWNLOADABLE_YES;
            }
        }
        return !Info.DOWNLOADABLE_NO.equals(value);
    }

    private void setAccessibility(Object value) {
        if (value == null || !(value instanceof String))
            return;
        getInfo().setProperty(Info.PRIVACY, value);
    }

    private void setDownloadable(boolean value) {
        String data = value ? Info.DOWNLOADABLE_YES : Info.DOWNLOADABLE_NO;
        getInfo().setProperty(Info.DOWNLOADABLE, data);
    }

}
