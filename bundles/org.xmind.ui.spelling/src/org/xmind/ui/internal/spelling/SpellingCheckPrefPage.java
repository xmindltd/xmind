/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.spelling;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.viewers.CheckListViewer;

import com.swabunga.spell.engine.Configuration;

public class SpellingCheckPrefPage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private static final String SPELLING_HELP_URL = "https://xmind.desk.com/customer/portal/articles/690243"; //$NON-NLS-1$

    private static final Object DEFAULT_PLACEHOLDER = Messages.defaultDictionary;

    private static class Element {

        private String name;

        private boolean enabled;

        private ISpellCheckerDescriptor descriptor;

        private String path;

        public Element(String name, boolean enabled,
                ISpellCheckerDescriptor descriptor, String path) {
            this.name = name;
            this.enabled = enabled;
            this.descriptor = descriptor;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public ISpellCheckerDescriptor getDescriptor() {
            return descriptor;
        }

        public void setDescriptor(ISpellCheckerDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }

    private static class DictionaryLabelProvider extends LabelProvider {

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            if (element instanceof Element)
                return ((Element) element).getName();
            return super.getText(element);
        }
    }

    private static class DictionaryComparator extends ViewerComparator {

        /**
         * 
         */
        public DictionaryComparator() {
            super(new Comparator<String>() {
                public int compare(String n1, String n2) {
                    // Compare no-extension name:
                    int s1 = n1.lastIndexOf('.');
                    int s2 = n2.lastIndexOf('.');
                    String p1 = s1 < 0 ? n1 : n1.substring(0, s1);
                    String p2 = s2 < 0 ? n2 : n2.substring(0, s2);
                    int c = p1.compareTo(p2);
                    if (c != 0)
                        return c;

                    // Compare full name:
                    return n1.compareTo(n2);
                }
            });
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
         */
        @Override
        public int category(Object element) {
            if (element == DEFAULT_PLACEHOLDER)
                return 0;
            return 1;
        }
    }

    private class DictionarySelectionListener
            implements ISelectionChangedListener {

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged
         * (org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event) {
            updateDictionaryControls();
        }

    }

    private class DictionaryOpenHandler implements IOpenListener {
        public void open(OpenEvent event) {
            selectSingleDictionary(event.getSelection());
        }
    }

    private List<FieldEditor> settingFields = new ArrayList<FieldEditor>();

    private Composite settingsParent;

    private ResourceManager resources;

    private CheckListViewer dictionaryViewer;

    private Button addButton;

    private Button removeButton;

    private List<Element> addElementReferences = new ArrayList<Element>();

    private List<ISpellCheckerDescriptor> removeDescriptorReferences = new ArrayList<ISpellCheckerDescriptor>();

    public SpellingCheckPrefPage() {
        super(Messages.SpellingPrefPage_title, FLAT);
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(SpellingPlugin.getDefault().getPreferenceStore());
    }

    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(SpellingPlugin.SPELLING_CHECK_ENABLED,
                Messages.enableSpellCheck, getFieldEditorParent()));
        addSpellingSettings(getFieldEditorParent());
        addDictionariesPanel(getFieldEditorParent());

        updateOptions(SpellingPlugin.isSpellingCheckEnabled());
        updateDictionaryControls();
    }

    private void addSpellingSettings(Composite composite) {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                composite);

        settingsParent = createSettingsParent(composite);
        addSettingField(Configuration.SPELL_IGNOREUPPERCASE,
                Messages.ignoreAllCapital);
        addSettingField(Configuration.SPELL_IGNOREMIXEDCASE,
                Messages.ignoreMultiCapital);
        addSettingField(Configuration.SPELL_IGNOREINTERNETADDRESSES,
                Messages.ignoreWebAddress);
        addSettingField(Configuration.SPELL_IGNOREDIGITWORDS,
                Messages.ignoreNumberousAppendix);
        addSettingField(Configuration.SPELL_IGNORESENTENCECAPITALIZATION,
                Messages.ignoreFirstLowercaseSentences);
    }

    private void addSettingField(String name, String label) {
        FieldEditor field = new BooleanFieldEditor(name, label, settingsParent);
        addField(field);
        settingFields.add(field);
    }

    private Composite createSettingsParent(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 7;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        parent.setLayout(layout);

        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.options);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout groupLayout = new GridLayout(1, false);
        groupLayout.marginWidth = 5;
        groupLayout.marginHeight = 5;
        groupLayout.verticalSpacing = 5;
        groupLayout.horizontalSpacing = 0;
        group.setLayout(groupLayout);

        return group;
    }

    private void updateOptions(boolean enabled) {
        settingsParent.setEnabled(enabled);
        for (FieldEditor field : settingFields) {
            field.setEnabled(enabled, settingsParent);
        }
    }

    private void addDictionariesPanel(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 7;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.dictionaries);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout groupLayout = new GridLayout(2, false);
        groupLayout.marginWidth = 5;
        groupLayout.marginHeight = 5;
        groupLayout.verticalSpacing = 5;
        groupLayout.horizontalSpacing = 5;
        group.setLayout(groupLayout);

        createDictionaryViewer(group);
        createDictionaryControls(group);
        createDetailsLink(group);
    }

    private void createDictionaryViewer(Composite parent) {
        dictionaryViewer = new CheckListViewer(parent, SWT.BORDER);
        dictionaryViewer.getControl().setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        dictionaryViewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        dictionaryViewer.setContentProvider(new ArrayContentProvider());
        dictionaryViewer.setLabelProvider(new DictionaryLabelProvider());
        dictionaryViewer.setComparator(new DictionaryComparator());
        dictionaryViewer
                .addSelectionChangedListener(new DictionarySelectionListener());
        dictionaryViewer.addOpenListener(new DictionaryOpenHandler());

        dictionaryViewer.setInput(getInput());
        initCheckStates();
    }

    private Object getInput() {
        List<Object> objects = new ArrayList<Object>();

        if (!getPreferenceStore().getBoolean(
                SpellingPlugin.DEFAULT_SPELLING_CHECKER_INVISIBLE)) {
            objects.add(DEFAULT_PLACEHOLDER);
        }

        List<ISpellCheckerDescriptor> descriptors = SpellCheckerRegistry
                .getInstance().getDescriptors();
        for (ISpellCheckerDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            boolean enabled = descriptor.isEnabled();
            objects.add(new Element(name, enabled, descriptor, null));
        }

        return objects;
    }

    private void initCheckStates() {
        if (dictionaryViewer == null || dictionaryViewer.getControl() == null
                || dictionaryViewer.getControl().isDisposed()) {
            return;
        }

        Object[] elements = ((IStructuredContentProvider) dictionaryViewer
                .getContentProvider()).getElements(dictionaryViewer.getInput());
        for (Object element : elements) {
            boolean enabled = false;
            if (element instanceof Element) {
                enabled = ((Element) element).isEnabled();
            } else if (element == DEFAULT_PLACEHOLDER) {
                enabled = !getPreferenceStore().getBoolean(
                        SpellingPlugin.DEFAULT_SPELLING_CHECKER_DISABLED);
            }

            dictionaryViewer.setChecked(element, enabled);
        }
    }

    private void createDictionaryControls(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        Composite buttonBar = new Composite(composite, SWT.NONE);
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        GridLayout buttonBarLayout = new GridLayout(1, true);
        buttonBarLayout.marginWidth = 0;
        buttonBarLayout.marginHeight = 0;
        buttonBarLayout.verticalSpacing = 10;
        buttonBarLayout.horizontalSpacing = 0;
        buttonBar.setLayout(buttonBarLayout);

        createAddDictionaryButton(buttonBar);
        createRemoveDictionaryButton(buttonBar);
        //createDictionaryInfoPanel(composite);
    }

    private void createDetailsLink(Composite parent) {
        Hyperlink hyperlink = new Hyperlink(parent, SWT.SINGLE);
        hyperlink
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) hyperlink.getLayoutData()).horizontalSpan = 2;
        hyperlink.setText(Messages.detailsLink_text);
        hyperlink.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#006CF9"))); //$NON-NLS-1$
        hyperlink.addHyperlinkListener(new IHyperlinkListener() {
            public void linkExited(HyperlinkEvent e) {
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                final IBrowser browser;
                browser = BrowserSupport.getInstance()
                        .createBrowser(IBrowserSupport.AS_EXTERNAL);
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        browser.openURL(SPELLING_HELP_URL);
                    }
                });
            }
        });
    }

    private void createAddDictionaryButton(Composite parent) {
        addButton = new Button(parent, SWT.PUSH);
        addButton.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, false, false));
        addButton.setText(Messages.dictionaries_add);
        addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                addDictionaryReference();
            }
        });
    }

    private void createRemoveDictionaryButton(Composite parent) {
        removeButton = new Button(parent, SWT.PUSH);
        removeButton.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, false, false));
        removeButton.setText(Messages.dictionaries_remove);
        removeButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                removeSelectedDictionaryReference();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void addDictionaryReference() {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.dic;*.dict;*.txt;*.*" }); //$NON-NLS-1$
        final String path = dialog.open();
        if (path == null)
            return;

        List<String> nameExclusions = new ArrayList<String>();
        if (dictionaryViewer.getInput() instanceof List<?>) {
            for (Object obj : (List<?>) dictionaryViewer.getInput()) {
                if (obj instanceof Element) {
                    nameExclusions.add(((Element) obj).getName());
                }
            }
        }

        String addedDictionaryName = SpellCheckerRegistry.getInstance()
                .getImportableDictFileName(new File(path), nameExclusions);
        Element addedElement = new Element(addedDictionaryName, false, null,
                path);

        if (dictionaryViewer.getInput() instanceof List<?>) {
            ((List<Object>) dictionaryViewer.getInput()).add(addedElement);
        }

        if (!addElementReferences.contains(addedElement)) {
            addElementReferences.add(addedElement);
        }

        dictionaryViewer.refresh();
    }

    private void removeSelectedDictionaryReference() {
        Object selection = ((IStructuredSelection) dictionaryViewer
                .getSelection()).getFirstElement();
        if (selection == null)
            return;

        // Confirm remove
        String name = ((ILabelProvider) dictionaryViewer.getLabelProvider())
                .getText(selection);
        if (!MessageDialog.openConfirm(getShell(),
                Messages.dictionaries_remove_confirm_title,
                NLS.bind(Messages.dictionaries_remove_confirm_message, name)))
            return;

        removeDictionaryReference(selection);

        dictionaryViewer.refresh();
    }

    @SuppressWarnings("unchecked")
    private void removeDictionaryReference(Object obj) {
        if (dictionaryViewer.getInput() instanceof List<?>) {
            ((List<Object>) dictionaryViewer.getInput()).remove(obj);
        }

        if (obj instanceof Element) {
            Element element = (Element) obj;
            if (element.getDescriptor() != null) {
                //remove old element
                removeDescriptorReferences.add(element.getDescriptor());
            } else if (element.getPath() != null) {
                //remove new element
                addElementReferences.remove(element);
            }
        }
    }

    private void updateDictionaryControls() {
        ISelection selection = dictionaryViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            boolean containDefault = ((IStructuredSelection) selection).toList()
                    .contains(DEFAULT_PLACEHOLDER);
            removeButton.setEnabled(!dictionaryViewer.getSelection().isEmpty()
                    && !containDefault);
        } else {
            removeButton.setEnabled(!dictionaryViewer.getSelection().isEmpty());
        }
    }

//    /**
//     * @param composite
//     */
//    private void createDictionaryInfoPanel(Composite composite) {
//
//    }

    public void propertyChange(PropertyChangeEvent event) {
        FieldEditor field = (FieldEditor) event.getSource();
        if (SpellingPlugin.SPELLING_CHECK_ENABLED
                .equals(field.getPreferenceName())) {
            updateOptions(((BooleanFieldEditor) field).getBooleanValue());
        }
        super.propertyChange(event);
    }

    public void selectSingleDictionary(ISelection selection) {
        if (dictionaryViewer == null || dictionaryViewer.getControl() == null
                || dictionaryViewer.getControl().isDisposed()) {
            return;
        }

        if (selection instanceof IStructuredSelection) {
            Object selectedElement = ((IStructuredSelection) selection)
                    .getFirstElement();

            //disabled other element
            Object[] checkedElements = dictionaryViewer.getCheckedElements();
            for (Object checkedElement : checkedElements) {
                if (selectedElement != checkedElement) {
                    dictionaryViewer.setChecked(checkedElement, false);
                }
            }

            //enabled selected element
            dictionaryViewer.setChecked(selectedElement, true);
        }
    }

    @Override
    public boolean performOk() {
        List<Object> oldEnabledDictionaries = getEnabledDictionaries();

        removeReferenceDictionarys();
        addReferenceDictionarys();

        //manage Default element
        getPreferenceStore().setValue(
                SpellingPlugin.DEFAULT_SPELLING_CHECKER_INVISIBLE,
                !((List<?>) dictionaryViewer.getInput())
                        .contains(DEFAULT_PLACEHOLDER));

        for (int i = 0; i < dictionaryViewer.getItemCount(); i++) {
            Object element = dictionaryViewer.getElementAt(i);
            boolean enabled = dictionaryViewer.getChecked(element);

            if (element instanceof Element) {
                ((Element) element).getDescriptor().setEnabled(enabled);
            } else if (element == DEFAULT_PLACEHOLDER) {
                getPreferenceStore().setValue(
                        SpellingPlugin.DEFAULT_SPELLING_CHECKER_DISABLED,
                        !enabled);
            }
        }

        List<Object> newEnabledDictionaries = getEnabledDictionaries();
        boolean needUpdated = !oldEnabledDictionaries
                .equals(newEnabledDictionaries);
        if (needUpdated) {
            SpellCheckerAgent.updateSpellChecker();
        }

        return super.performOk();
    }

    private List<Object> getEnabledDictionaries() {
        List<Object> objects = new ArrayList<Object>();

        if (!getPreferenceStore()
                .getBoolean(SpellingPlugin.DEFAULT_SPELLING_CHECKER_INVISIBLE)
                && !getPreferenceStore().getBoolean(
                        SpellingPlugin.DEFAULT_SPELLING_CHECKER_DISABLED)) {
            objects.add(DEFAULT_PLACEHOLDER);
        }

        List<ISpellCheckerDescriptor> descriptors = SpellCheckerRegistry
                .getInstance().getDescriptors();
        for (ISpellCheckerDescriptor descriptor : descriptors) {
            if (descriptor.isEnabled()) {
                objects.add(descriptor);
            }
        }

        return objects;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void performDefaults() {
        if (!((List<?>) dictionaryViewer.getInput())
                .contains(DEFAULT_PLACEHOLDER)) {
            ((List<Object>) dictionaryViewer.getInput()).add(0,
                    DEFAULT_PLACEHOLDER);
            dictionaryViewer.refresh();
        }

        for (int i = 0; i < dictionaryViewer.getItemCount(); i++) {
            Object element = dictionaryViewer.getElementAt(i);

            if (element instanceof Element) {
                removeDictionaryReference(element);
            } else if (element == DEFAULT_PLACEHOLDER) {
                dictionaryViewer.setChecked(element, true);
            }
        }
        super.performDefaults();

        dictionaryViewer.refresh();
    }

    private void addReferenceDictionarys() {
        for (Element element : addElementReferences) {
            addDictionary(element);
        }
        addElementReferences.clear();
    }

    private void removeReferenceDictionarys() {
        for (ISpellCheckerDescriptor descriptor : removeDescriptorReferences) {
            removeDictionary(descriptor);
        }
        removeDescriptorReferences.clear();
    }

    private void addDictionary(final Element element) {
        final String path = element.getPath();
        if (path == null)
            return;

        try {
            ProgressMonitorDialog progress = new ProgressMonitorDialog(
                    getShell());
            progress.setOpenOnRun(false);
            progress.run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.addingDictionary, 1);
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            ISpellCheckerDescriptor descriptor = SpellCheckerRegistry
                                    .getInstance().importDictFile(
                                            new File(path), element.getName());
                            element.setPath(null);
                            element.setDescriptor(descriptor);
                        }
                    });
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
    }

    private void removeDictionary(final ISpellCheckerDescriptor descriptor) {
        // Remove dictionary descriptor and local file
        try {
            ProgressMonitorDialog progress = new ProgressMonitorDialog(
                    getShell());
            progress.setOpenOnRun(false);
            progress.run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.removingDictionary, 1);
                    SafeRunner.run(new SafeRunnable() {
                        public void run() throws Exception {
                            SpellCheckerRegistry.getInstance()
                                    .removeDictionary(descriptor);
                        }
                    });
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void dispose() {
        if (addElementReferences != null) {
            addElementReferences.clear();
            addElementReferences = null;
        }
        if (removeDescriptorReferences != null) {
            removeDescriptorReferences.clear();
            removeDescriptorReferences = null;
        }

        super.dispose();
    }

}
