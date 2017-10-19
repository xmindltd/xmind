package org.xmind.cathy.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.resources.ColorUtils;

public class LanguagePreferencePageSection
        extends PreferenceFieldEditorPageSection
        implements IWorkbenchPreferencePage, MouseListener {

    private static final String LANGUAGE_OSGI_NL_KEY = "osgi.nl"; //$NON-NLS-1$

    private static final String LOADED_LANGUAGES_URL = "platform:/plugin/org.xmind.cathy/resource/langs.properties"; //$NON-NLS-1$

    private Properties supportLanguageProperties;

    private String oldLangKey = null;

    private String newLangKey = null;

    private Properties configIniProperties;

    private Label langLabel;

    private LocalResourceManager resource;

    private static final String blue = "#0070D8";  //$NON-NLS-1$

    private static final String[] supportedLangsKey = { "en_US", "de", "fr", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "zh_CN", "zh_TW", "ja", "ko", "da", "es", "sl", "it" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$

    private Map<Composite, String> langMap = null;

    public void init(IWorkbench workbench) {
        configIniProperties = loadProperties(getConfigFile());
        supportLanguageProperties = loadProperties(getSupportLanguageFile());
        if (oldLangKey == null) {
            String languageKey = configIniProperties
                    .getProperty(LANGUAGE_OSGI_NL_KEY);
            oldLangKey = languageKey != null ? languageKey
                    : System.getProperty(LANGUAGE_OSGI_NL_KEY);
        }
    }

    @Override
    protected Control createContents(Composite parent) {

        if (null == resource)
            resource = new LocalResourceManager(JFaceResources.getResources(),
                    parent);

        Composite topContainer = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(topContainer);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(topContainer);

        Label describe = new Label(topContainer, SWT.NONE);
        describe.setText(WorkbenchMessages.ChangeLanguageTo_description);

        langLabel = new Label(topContainer, SWT.NONE);
        if (supportLanguageProperties == null)
            supportLanguageProperties = loadProperties(
                    getSupportLanguageFile());
        String langString = supportLanguageProperties.getProperty(oldLangKey);
        langString = langString != null ? langString : " "; //$NON-NLS-1$

        langLabel.setText(langString);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(langLabel);
        GridLayout parentLayout = (GridLayout) parent.getLayout();

        if (parentLayout != null) {
            parentLayout.verticalSpacing = 10;
        }

        Composite container = createLangContainer(parent, 3);

        //firstly init language map, and init langMap by OldLangKey.
        initLangMap(container);
        initByOldLangKey();

        return container;
    }

    private void initLangMap(Composite container) {
        if (null == langMap)
            langMap = new HashMap<Composite, String>();
        for (String lang : supportedLangsKey) {
            Composite langComposite = createLang(container,
                    supportLanguageProperties.getProperty(lang));
            langComposite.addMouseListener(this);
            Control[] children = langComposite.getChildren();
            for (Control child : children) {
                child.addMouseListener(this);
            }
            langMap.put(langComposite, lang);
        }
    }

    private void initByOldLangKey() {
        if (null != langMap && langMap.containsValue(oldLangKey)) {
            Iterator iterator = langMap.entrySet().iterator();
            while (iterator.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<Composite, String> item = (Entry<Composite, String>) iterator
                        .next();
                if (oldLangKey.equals(item.getValue())) {
                    Composite composite = item.getKey();
                    changeControlBack(composite,
                            (Color) resource.get(ColorUtils.toDescriptor(blue)),
                            ColorConstants.white);
                }
            }
        }
    }

    private Composite createLangContainer(Composite parent, int cols) {
        Composite container = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
        GridLayoutFactory.fillDefaults().equalWidth(true).spacing(8, 10)
                .numColumns(cols).applyTo(container);
        return container;
    }

    private Composite createLang(Composite parent, String lang) {
        Composite panel = new Composite(parent, SWT.BORDER);
        panel.setBackground(ColorConstants.white);
        GridLayoutFactory.fillDefaults().margins(5, 5).spacing(5, 5)
                .applyTo(panel);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Label langLabel = new Label(panel, SWT.NONE);
        langLabel.setText(lang);
        langLabel.setBackground(ColorConstants.white);
        return panel;
    }

    private File getSupportLanguageFile() {
        try {
            URL url = FileLocator.find(new URL(LOADED_LANGUAGES_URL));
            File supportLanguageFile = new File(
                    FileLocator.toFileURL(url).getPath());
            return supportLanguageFile;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Properties loadProperties(File file) {
        if (file != null && file.exists() && file.canRead()) {
            try {
                InputStream stream = new BufferedInputStream(
                        new FileInputStream(file), 1024);
                try {
                    Properties properties = new Properties();
                    properties.load(stream);
                    return properties;
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    private File getConfigFile() {
        URL configDir = Platform.getConfigurationLocation().getURL();
        try {
            URL configIni = new URL(configDir, "config.ini"); //$NON-NLS-1$
            File file = new File(configIni.getFile());
            return file;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void storeProperties(Properties properties, File file) {
        if (file != null && file.exists() && file.canWrite()) {
            try {
                OutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(file), 1024);
                try {
                    properties.store(stream, "Store properties into file."); //$NON-NLS-1$
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void performDefaults() {
        configIniProperties.put(LANGUAGE_OSGI_NL_KEY,
                System.getProperty(LANGUAGE_OSGI_NL_KEY));
        String lang = System.getProperty(LANGUAGE_OSGI_NL_KEY);
        if (null != langMap && langMap.containsValue(lang)) {
            clearLangSelection();
            newLangKey = lang;
            Iterator iterator = langMap.entrySet().iterator();
            while (iterator.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<Composite, String> item = (Entry<Composite, String>) iterator
                        .next();
                if (lang.equals(item.getValue())) {
                    Composite composite = item.getKey();
                    changeControlBack(composite,
                            (Color) resource.get(ColorUtils.toDescriptor(blue)),
                            ColorConstants.white);
                }
            }
        }
        storeProperties(configIniProperties, getConfigFile());
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        if (!super.performOk())
            return false;
        if (null != newLangKey && !oldLangKey.equals(newLangKey)) {
            String message = WorkbenchMessages.LanguagePrefPage_ConfirmToRestart2_description;
            MessageDialog confirmDialog = new MessageDialog(getShell(),
                    WorkbenchMessages.ConfirmToRestart_title, null, message,
                    MessageDialog.CONFIRM,
                    new String[] {
                            WorkbenchMessages.LanguagePrefPage_ConfirmToRestart2_defaultButton,
                            WorkbenchMessages.LanguagePrefPage_ConfirmToRestart_laterButton },
                    IDialogConstants.OK_ID);
            int restart = confirmDialog.open();
            if (restart == -1)
                return false;

            configIniProperties.put(LANGUAGE_OSGI_NL_KEY, newLangKey);
            storeProperties(configIniProperties, getConfigFile());

            if (restart == IDialogConstants.OK_ID) {
                PlatformUI.getWorkbench().restart();
            }
        }
        return true;
    }

    public void mouseDoubleClick(MouseEvent e) {
        mouseDown(e);
        this.performOk();
    }

    public void mouseDown(MouseEvent e) {
        Composite composite = null;
        Object object = e.getSource();
        if (null != object && object instanceof Control) {
            if (object instanceof Composite)
                composite = (Composite) object;
            else if (object instanceof Label)
                composite = ((Label) object).getParent();
        }
        changeLangSelection(composite);
    }

    public void mouseUp(MouseEvent e) {
    }

    private void changeLangSelection(Composite composite) {
        if (null != composite) {
            clearLangSelection();
            newLangKey = null;
            changeControlBack(composite,
                    (Color) resource.get(ColorUtils.toDescriptor(blue)),
                    ColorConstants.white);
            if (langMap.containsKey(composite)) {
                newLangKey = langMap.get(composite);
                langLabel.setText(
                        supportLanguageProperties.getProperty(newLangKey));
            }
        }
    }

    private void clearLangSelection() {
        Iterator iterator = langMap.entrySet().iterator();
        while (iterator.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Composite, String> it = (Entry<Composite, String>) iterator
                    .next();
            changeControlBack(it.getKey(), ColorConstants.white,
                    ColorConstants.black);
        }
    }

    private void changeControlBack(Composite composite, Color color,
            Color fontColor) {
        composite.setBackground(color);
        Control[] children = composite.getChildren();
        for (Control child : children) {
            child.setBackground(color);
            child.setForeground(fontColor);
        }
    }

    @Override
    protected void createFieldEditors() {
    }

}
