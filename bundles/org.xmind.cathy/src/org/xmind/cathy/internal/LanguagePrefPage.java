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
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class LanguagePrefPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    private static final String LANGUAGE_OSGI_NL_KEY = "osgi.nl"; //$NON-NLS-1$
    private static final String LOADED_LANGUAGES_URL = "platform:/plugin/org.xmind.cathy/resource/langs.properties"; //$NON-NLS-1$
    @SuppressWarnings("nls")
    private static final String[] supportedLangsKey = { "en_US", "de", "fr",
            "zh_CN", "zh_TW", "ja", "ko", "da", "ru", "it", "sl", "ar", "es",
            "pt_BR" };

    private class LanguageLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof String) {
                StringBuffer buffer = new StringBuffer(3);

                String langKey = (String) element;
                String supportLang = supportLanguageProperties
                        .getProperty(langKey);
                buffer.append(supportLang);
                buffer.append(" - "); //$NON-NLS-1$
                buffer.append(supportLangsMap.get(langKey));
                return buffer.toString();
            }
            return super.getText(element);
        }
    }

    private static String oldLangKey = null;

    private Properties configIniProperties;

    private Properties supportLanguageProperties;

    private HashMap<String, String> supportLangsMap;

    private ListViewer langsViewer;

    private Label changingToLangText;

    private Button changeButton;

    public LanguagePrefPage() {
        super(WorkbenchMessages.LanguagePrefPage_title, FLAT);
        configIniProperties = loadProperties(getConfigFile());
        supportLanguageProperties = loadProperties(getSupportLanguageFile());
        initSupportLanguageMap();
        if (oldLangKey == null) {
            String languageKey = configIniProperties
                    .getProperty(LANGUAGE_OSGI_NL_KEY);
            oldLangKey = languageKey != null ? languageKey : System
                    .getProperty(LANGUAGE_OSGI_NL_KEY);
        }
    }

    private void initSupportLanguageMap() {
        if (supportLangsMap != null)
            return;
        supportLangsMap = new HashMap<String, String>();
        supportLangsMap.put(
                "en_US", WorkbenchMessages.SupportLanguageName_English); //$NON-NLS-1$
        supportLangsMap.put("ar", WorkbenchMessages.SupportLanguageName_Arabic); //$NON-NLS-1$
        supportLangsMap.put("da", WorkbenchMessages.SupportLanguageName_Danish); //$NON-NLS-1$
        supportLangsMap.put("de", WorkbenchMessages.SupportLanguageName_German); //$NON-NLS-1$
        supportLangsMap.put(
                "es", WorkbenchMessages.SupportLanguageName_Spanish); //$NON-NLS-1$
        supportLangsMap.put("fr", WorkbenchMessages.SupportLanguageName_French); //$NON-NLS-1$
        supportLangsMap
                .put("it", WorkbenchMessages.SupportLanguageName_Italian); //$NON-NLS-1$
        supportLangsMap.put(
                "ja", WorkbenchMessages.SupportLanguageName_Japanese); //$NON-NLS-1$
        supportLangsMap.put("ko", WorkbenchMessages.SupportLanguageName_Korean); //$NON-NLS-1$
        supportLangsMap
                .put("pt_BR", WorkbenchMessages.SupportLanguageName_Portuguese_Brazilian); //$NON-NLS-1$
        supportLangsMap
                .put("ru", WorkbenchMessages.SupportLanguageName_Russian); //$NON-NLS-1$
        supportLangsMap.put(
                "sl", WorkbenchMessages.SupportLanguageName_Slovenian); //$NON-NLS-1$
        supportLangsMap.put(
                "zh_CN", WorkbenchMessages.SupportLanguageName_SimplifiedCN); //$NON-NLS-1$
        supportLangsMap.put(
                "zh_TW", WorkbenchMessages.SupportLanguageName_TraditionalCN); //$NON-NLS-1$
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

    private File getSupportLanguageFile() {
        try {
            URL url = FileLocator.find(new URL(LOADED_LANGUAGES_URL));
            File supportLanguageFile = new File(FileLocator.toFileURL(url)
                    .getPath());
            return supportLanguageFile;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        addLanguageGroup();
    }

    private void addLanguageGroup() {
        Composite parent = getFieldEditorParent();
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        parent.setLayout(gridLayout);
        addChangeLanguageField(parent);
    }

    private void addChangeLanguageField(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        composite.setLayout(new GridLayout());

        createLangsDescription(composite);
        createChangeLanguageControl(parent);
    }

    private void createChangeLanguageControl(Composite parent) {
        Composite line = new Composite(parent, SWT.NONE);
        line.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        line.setLayout(new GridLayout(2, false));

        int flags = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;
        langsViewer = new ListViewer(line, flags);
        GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        langsViewer.getControl().setLayoutData(layoutData);
        langsViewer.setContentProvider(new ArrayContentProvider());
        langsViewer.setLabelProvider(new LanguageLabelProvider());
        langsViewer.setInput(supportedLangsKey);

        String languageKey = configIniProperties
                .getProperty(LANGUAGE_OSGI_NL_KEY);
        String currentLanguageKey = languageKey != null ? languageKey
                : oldLangKey;

        langsViewer.setSelection(new StructuredSelection(currentLanguageKey));
        langsViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        ISelection langElementSelection = event.getSelection();
                        if (langElementSelection instanceof StructuredSelection) {
                            Object langKey = ((StructuredSelection) langElementSelection)
                                    .getFirstElement();

                            boolean isChanged = !oldLangKey.equals(langKey);
                            if (isChanged) {
                                String langSelected = ((LanguageLabelProvider) langsViewer
                                        .getLabelProvider()).getText(langKey);
                                changingToLangText.setText(langSelected);
                            } else {
                                changingToLangText.setText(""); //$NON-NLS-1$
                            }
                            changeButton.setEnabled(isChanged);
                        }
                    }
                });

        changeButton = new Button(line, SWT.NONE);
        changeButton.setText(WorkbenchMessages.ChangeLanguage_button);
        changeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
                false));
        changeButton.setEnabled(!currentLanguageKey.equals(oldLangKey));
        changeButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                ISelection langElementSelection = langsViewer.getSelection();
                if (langElementSelection instanceof StructuredSelection) {
                    Object langKey = ((StructuredSelection) langElementSelection)
                            .getFirstElement();
                    String langSelected = ((LanguageLabelProvider) langsViewer
                            .getLabelProvider()).getText(langKey);
                    String message = NLS.bind(
                            WorkbenchMessages.ConfirmToRestart_description,
                            langSelected);
                    MessageDialog confirmDialog = new MessageDialog(
                            getShell(),
                            WorkbenchMessages.ConfirmToRestart_title,
                            null,
                            message,
                            MessageDialog.CONFIRM,
                            new String[] {
                                    WorkbenchMessages.ConfirmToRestart_defaultButton,
                                    WorkbenchMessages.ConfirmToRestart_cancelButton },
                            IDialogConstants.OK_ID);
                    int restart = confirmDialog.open();
                    configIniProperties.put(LANGUAGE_OSGI_NL_KEY, langKey);
                    storeProperties(configIniProperties, getConfigFile());
                    if (restart == 0) {
                        PlatformUI.getWorkbench().restart();
                    }
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    private void createLangsDescription(Composite composite) {
        Composite descriptionComposite = new Composite(composite, SWT.NONE);
        descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));
        GridLayout descriptionLayout = new GridLayout(2, false);
        descriptionLayout.marginHeight = 0;
        descriptionLayout.marginWidth = 0;
        descriptionComposite.setLayout(descriptionLayout);

        Label langToChangeDescription = new Label(descriptionComposite,
                SWT.NONE);
        langToChangeDescription
                .setText(WorkbenchMessages.ChangeLanguageTo_description);
        langToChangeDescription.setLayoutData(new GridData(SWT.FILL));

        changingToLangText = new Label(descriptionComposite, SWT.READ_ONLY
                | SWT.SINGLE | SWT.BORDER | SWT.SHADOW_IN | SWT.LEFT);
        changingToLangText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                true, true));

        String languageKey = configIniProperties
                .getProperty(LANGUAGE_OSGI_NL_KEY);
        String supportLang = supportLangsMap.get(languageKey);
        if (supportLang != null)
            changingToLangText.setText(supportLang);
    }

    @Override
    protected void performDefaults() {
        configIniProperties.put(LANGUAGE_OSGI_NL_KEY,
                System.getProperty(LANGUAGE_OSGI_NL_KEY));
        langsViewer.setSelection(
                new StructuredSelection(System
                        .getProperty(LANGUAGE_OSGI_NL_KEY)), true);
        storeProperties(configIniProperties, getConfigFile());
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        if (!super.performOk())
            return false;

        ISelection langElementSelection = langsViewer.getSelection();
        if (langElementSelection instanceof StructuredSelection) {
            Object langKey = ((StructuredSelection) langElementSelection)
                    .getFirstElement();
            if (!oldLangKey.equals(langKey)) {
                String message = WorkbenchMessages.LanguagePrefPage_ConfirmToRestart2_description;
                MessageDialog confirmDialog = new MessageDialog(
                        getShell(),
                        WorkbenchMessages.ConfirmToRestart_title,
                        null,
                        message,
                        MessageDialog.CONFIRM,
                        new String[] {
                                WorkbenchMessages.LanguagePrefPage_ConfirmToRestart2_defaultButton,
                                WorkbenchMessages.LanguagePrefPage_ConfirmToRestart_laterButton },
                        IDialogConstants.OK_ID);
                int restart = confirmDialog.open();
                if (restart == -1)
                    return false;

                configIniProperties.put(LANGUAGE_OSGI_NL_KEY, langKey);
                storeProperties(configIniProperties, getConfigFile());
                if (restart == IDialogConstants.OK_ID) {
                    PlatformUI.getWorkbench().restart();
                }
            }
        }

        return true;
    }

}
