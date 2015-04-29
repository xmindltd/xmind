package org.xmind.ui.internal.evernote.export;

import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.INCLUDE_FILE;
import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.INCLUDE_IMAGE;
import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.INCLUDE_TEXT;
import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.NOTEBOOK;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.xmind.ui.evernote.EvernotePlugin;
import org.xmind.ui.internal.evernote.EvernoteMessages;
import org.xmind.ui.resources.FontUtils;

import com.evernote.auth.EvernoteService;
import com.evernote.edam.type.Notebook;

/**
 * @author Jason Wong
 */
public class EvernoteExportDialog extends Dialog {

    private class WidgetListener implements Listener {
        public void handleEvent(Event event) {
            handleWidgetEvent(event);
        }
    }

    private static final String SECTION_NAME = "org.xmind.ui.evernote.export"; //$NON-NLS-1$

    private static final String EVERNOTE_LOGO = "icons/evernote_logo.png"; //$NON-NLS-1$

    private static final String YINXIANG_LOGO = "icons/yinxiang_logo.png"; //$NON-NLS-1$

    private static final String PROPERTY_NAME = "PROPERTY_NAME"; //$NON-NLS-1$

    private final Map<String, Widget> widgets = new HashMap<String, Widget>();

    private Map<Integer, Notebook> notebookMaps = new HashMap<Integer, Notebook>();

    private Set<String> propertyNames;

    private Listener widgetListener = null;

    private List<Notebook> notebooks;

    private EvernoteService evernoteService;

    private Combo notebookCombo;

    public EvernoteExportDialog(Shell parentShell, List<Notebook> notebooks) {
        super(parentShell);
        this.notebooks = notebooks;
    }

    public EvernoteExportDialog(Shell parentShell, List<Notebook> notebooks,
            EvernoteService evernoteService) {
        this(parentShell, notebooks);
        this.evernoteService = evernoteService;
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = (Composite) super.createContents(parent);
        updateButtons();
        return composite;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(EvernoteMessages.EvernoteExportDialog_title);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite buttonBar = (Composite) super.createButtonBar(parent);
        GridLayout layout = (GridLayout) buttonBar.getLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        layout.marginTop = 0;
        layout.marginBottom = 3;

        Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText(EvernoteMessages.EvernoteExportDialog_SaveButton_label);
        okButton.setEnabled(false);

        return buttonBar;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        layout.marginBottom = -10;

        createSettingArea(composite);
        return composite;
    }

    private void createSettingArea(Composite parent) {
        Composite setting = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.marginBottom = 0;
        setting.setLayout(layout);
        setting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createContentArea(setting);
        createAdditionArea(setting);
    }

    private void createContentArea(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginRight = 25;
        layout.verticalSpacing = 2;
        content.setLayout(layout);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createOptionArea(content);

        if (EvernoteService.YINXIANG.name().equals(evernoteService.name())) {
            createImageLabel(content, YINXIANG_LOGO);
        } else {
            createImageLabel(content, EVERNOTE_LOGO);
        }
    }

    private void createOptionArea(Composite parent) {
        propertyNames = new HashSet<String>();
        Composite option = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginRight = 5;
        option.setLayout(layout);

        createLabel(option, EvernoteMessages.EvernoteExportDialog_Content_label);
        createCheckButton(option, INCLUDE_IMAGE);
        createCheckButton(option, INCLUDE_FILE);
        createCheckButton(option, INCLUDE_TEXT);

        setDefaultCheckButton();
    }

    private void createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
    }

    private void createCheckButton(Composite parent, String propertyName) {
        createCheckButton(parent, propertyName, getDefaultLabel(propertyName));
    }

    private void createCheckButton(Composite parent, String propertyName,
            String text) {
        Button check = new Button(parent, SWT.CHECK);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalIndent = 16;
        check.setLayoutData(data);
        check.setText(text);

        registerPropertyWidget(propertyName, check);
        check.setSelection(getBoolean(propertyName));
        hookWidget(check, SWT.Selection);
    }

    private String getDefaultLabel(String propertyName) {
        if (INCLUDE_IMAGE.equals(propertyName))
            return EvernoteMessages.EvernoteExportDialog_IncludeImage;
        if (INCLUDE_FILE.equals(propertyName))
            return EvernoteMessages.EvernoteExportDialog_IncludeFile;
        if (INCLUDE_TEXT.equals(propertyName))
            return EvernoteMessages.EvernoteExportDialog_IncludeText;
        return ""; //$NON-NLS-1$
    }

    private void createImageLabel(Composite parent, String imagePath) {
        Label image = new Label(parent, SWT.NONE);
        URL url = Platform.getBundle(EvernotePlugin.PLUGIN_ID).getEntry(
                imagePath);
        if (url != null) {
            try {
                image.setImage(new Image(null, url.openStream()));
            } catch (IOException e) {
            }
        }
    }

    private void createAdditionArea(Composite parent) {
        Composite addition = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginTop = 10;
        layout.marginBottom = 0;
        addition.setLayout(layout);
        addition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createNotebookArea(addition);
        // createTagArea(addition);
    }

    private void createNotebookArea(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        label.setText(EvernoteMessages.EvernoteExportDialog_Notebook_label);

        notebookCombo = new Combo(parent, SWT.BORDER | SWT.SINGLE
                | SWT.READ_ONLY);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 160;
        notebookCombo.setLayoutData(data);
        notebookCombo.setFont(FontUtils.getRelativeHeight(
                JFaceResources.DEFAULT_FONT, -1));
        notebookCombo.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Notebook notebook = notebookMaps.get(notebookCombo
                        .getSelectionIndex());
                setValue(NOTEBOOK, notebook.getName());
            }
        });

        // String xmindNotebook =
        // EvernoteMessages.EvernoteExporter_DefaultNotebook_text;
        // boolean hasXMindNotebook = false;
        for (Notebook notebook : notebooks) {
            String name = notebook.getName();
            if (name != null) {
                int index = notebookCombo.getItemCount();
                notebookCombo.add(notebook.getName());
                notebookMaps.put(index, notebook);

                if (notebook.isDefaultNotebook()) {
                    notebookCombo.select(index);
                    setValue(NOTEBOOK, notebook.getName());
                }
            }

            // if (xmindNotebook.equals(name))
            // hasXMindNotebook = true;

        }

        // if (!hasXMindNotebook) {
        // Notebook notebook = new Notebook();
        // notebook.setName(xmindNotebook);
        //
        // int index = notebookCombo.getItemCount();
        // notebookCombo.add(xmindNotebook);
        // notebooksMap.put(index, notebook);
        // }

    }

    // private void createTagArea(Composite parent) {
    // Label label = new Label(parent, SWT.NONE);
    // label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
    // label.setText(EvernoteMessages.EvernoteExportDialog_Tag_label);
    //
    // Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
    // GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
    // data.widthHint = 160;
    // text.setLayoutData(data);
    // text.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
    // -1));
    // }

    private void registerPropertyWidget(String propertyName, Widget widget) {
        widget.setData(PROPERTY_NAME, propertyName);
        widgets.put(propertyName, widget);
        if (propertyNames != null) {
            propertyNames.add(propertyName);
        }
    }

    private void hookWidget(Widget widget, int eventType) {
        if (widgetListener == null) {
            widgetListener = new WidgetListener();
        }
        widget.addListener(eventType, widgetListener);
    }

    private void handleWidgetEvent(Event event) {
        Object propertyName = event.widget.getData(PROPERTY_NAME);
        if (propertyName instanceof String) {
            if (event.widget instanceof Button) {
                setValue((String) propertyName,
                        ((Button) event.widget).getSelection());
            }
        }
    }

    private void setValue(String propertyName, boolean value) {
        IDialogSettings dialogSettings = getDialogSettings();
        dialogSettings.put(propertyName, value);
        updateButtons();
    }

    private void setValue(String propertyName, String value) {
        IDialogSettings dialogSettings = getDialogSettings();
        dialogSettings.put(propertyName, value);
    }

    public IDialogSettings getDialogSettings() {
        return EvernotePlugin.getDialogSettings(SECTION_NAME);
    }

    private boolean getBoolean(String propertyName) {
        IDialogSettings dialogSettings = getDialogSettings();
        return dialogSettings.getBoolean(propertyName);
    }

    private void updateButtons() {
        if (getBoolean(INCLUDE_IMAGE) || getBoolean(INCLUDE_FILE)
                || getBoolean(INCLUDE_TEXT)) {
            setOKEnabled(true);
        } else {
            setOKEnabled(false);
        }
    }

    private void setOKEnabled(boolean enabled) {
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed()) {
            button.setEnabled(enabled);
        }
    }

    private void setDefaultCheckButton() {
        if (!getBoolean(INCLUDE_IMAGE) && !getBoolean(INCLUDE_FILE)
                && !getBoolean(INCLUDE_TEXT)) {
            Button btImg = ((Button) widgets.get(INCLUDE_IMAGE));
            btImg.setSelection(true);
            setValue(INCLUDE_IMAGE, btImg.getSelection());

            Button btFile = ((Button) widgets.get(INCLUDE_FILE));
            btFile.setSelection(true);
            setValue(INCLUDE_FILE, btFile.getSelection());
        }
    }

}
