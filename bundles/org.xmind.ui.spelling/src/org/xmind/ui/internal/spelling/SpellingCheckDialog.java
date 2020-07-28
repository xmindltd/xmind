package org.xmind.ui.internal.spelling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.IWordContext;
import org.xmind.ui.IWordContextProvider;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.PrefUtils;

import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

public class SpellingCheckDialog extends Dialog
        implements IJobChangeListener, IPartListener {

    private static final int OPTIONS_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;

    private static String preferenceID = "org.xmind.ui.SpellingCheckPrefPage"; //$NON-NLS-1$

    private static class SpellingViewContent {

        private static final SpellingViewContent instance = new SpellingViewContent();

        private IWordContextProvider input = null;

        private List<SpellingCheckDialog> dialogs = new ArrayList<SpellingCheckDialog>();

        private SpellingViewContent() {
        }

        public void addDialog(SpellingCheckDialog dialog) {
            this.dialogs.add(dialog);
        }

        public void setInput(IWordContextProvider input) {
            this.input = input;
            fireInputChanged();
        }

        private void fireInputChanged() {
            for (Object dialog : dialogs.toArray()) {
                ((SpellingCheckDialog) dialog).inputChanged(input);
            }
        }

        public static SpellingViewContent getInstance() {
            return instance;
        }

        public void removeDialog(SpellingCheckDialog spellingDialog) {
            this.dialogs.remove(spellingDialog);

        }

    }

    private static class SpellingCheckContentProvider
            implements ITreeContentProvider {

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                List list = (List) inputElement;
                if (!list.isEmpty())
                    return list.toArray();
            }
            return new Object[0];
        }

        public Object[] getChildren(Object parentElement) {
            return null;
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            return false;
        }

    }

    private static class WordItem {

        public IWordContext parent;

        public int start;

        public String invalidWord;

        public List<Object> suggestions;

        /**
         * 
         */
        @SuppressWarnings("unchecked")
        public WordItem(IWordContext parent, SpellCheckEvent range) {
            this.parent = parent;
            this.start = range.getWordContextPosition();
            this.invalidWord = range.getInvalidWord();
            this.suggestions = range.getSuggestions();
        }

    }

    private static class CheckSpellingJob extends Job
            implements SpellCheckListener {

        private static final CheckSpellingJob instance = new CheckSpellingJob();

        private IWordContextProvider input = null;

        private SpellChecker spellChecker = null;

        private IWordContext currentWordContextItem = null;

        /**
         */
        private CheckSpellingJob() {
            super(Messages.CheckSpellingJob_name);
        }

        public void setInput(IWordContextProvider input) {
            this.input = input;
        }

        /**
         * @return the input
         */
        public IWordContextProvider getInput() {
            return input;
        }

        /*
         * (non-Javadoc)
         * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            spellChecker = null;
            SpellCheckerAgent.visitSpellChecker(new ISpellCheckerVisitor() {
                public void handleWith(SpellChecker theSpellChecker) {
                    spellChecker = theSpellChecker;
                }
            });

            while (spellChecker == null) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                }
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            monitor.beginTask(Messages.CheckSpellingJob_task_Scanning,
                    input.getWordContexts().size());

            SpellChecker theSpellChecker = spellChecker;
            theSpellChecker.addSpellCheckListener(this);
            try {
                for (IWordContext context : input.getWordContexts()) {
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    monitor.subTask(context.getContent());
                    scan(new SubProgressMonitor(monitor, 1), context);
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    monitor.worked(1);
                }
            } finally {
                theSpellChecker.removeSpellCheckListener(this);
            }

            monitor.done();

            return Status.OK_STATUS;
        }

        private void scan(IProgressMonitor monitor, IWordContext context) {
            monitor.beginTask(null, 1);

            String content = context.getContent();
            if (monitor.isCanceled())
                return;

            currentWordContextItem = context;
            spellChecker.checkSpelling(new StringWordTokenizer(content));

            if (monitor.isCanceled())
                return;

            monitor.done();
        }

        /*
         * (non-Javadoc)
         * @see com.swabunga.spell.event.SpellCheckListener#spellingError(com.
         * swabunga .spell.event.SpellCheckEvent)
         */
        public void spellingError(SpellCheckEvent event) {
            errorList.add(new WordItem(currentWordContextItem, event));
        }

        public static CheckSpellingJob getInstance() {
            return instance;
        }

        public static void start(IWordContextProvider input) {
            instance.setInput(input);
            instance.schedule();
        }
    }

    private static class ReplaceAction extends Action {

        private WordItem item;

        private String suggestion;

        /**
         * 
         */
        public ReplaceAction(WordItem item, String suggestion) {
            this.item = item;
            this.suggestion = suggestion;
            setText(item.invalidWord + " -> " + suggestion); //$NON-NLS-1$
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            item.parent.replaceWord(item.start, item.invalidWord.length(),
                    suggestion);
        }
    }

    private static class AddToDictionaryAction extends Action {

        private WordItem item;

        /**
         * 
         */
        public AddToDictionaryAction(WordItem item) {
            this.item = item;
            setText(Messages.addToDictionary);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            SpellCheckerAgent.visitSpellChecker(new ISpellCheckerVisitor() {
                public void handleWith(SpellChecker spellChecker) {
                    spellChecker.addToDictionary(item.invalidWord);
                }
            });
        }
    }

    private static class IgnoredWordsStorage {
        private Map<IWordContextProvider, Set<String>> workbookIgnores = new HashMap<IWordContextProvider, Set<String>>();
        private Map<IWordContext, Set<String>> wordContextIgnores = new HashMap<IWordContext, Set<String>>();
        private static IgnoredWordsStorage instance;

        private IgnoredWordsStorage() {
        }

        public static IgnoredWordsStorage getInstance() {
            if (instance == null)
                instance = new IgnoredWordsStorage();
            return instance;
        }

        public void addIgnoreAllWord(WordItem wordItem,
                IWordContextProvider provider) {
            Set<String> ignoredWords = workbookIgnores.get(provider);
            if (ignoredWords == null)
                ignoredWords = new HashSet<String>(10);
            ignoredWords.add(wordItem.invalidWord);
            workbookIgnores.put(provider, ignoredWords);
        }

        public void addIgnoreWord(WordItem wordItem) {
            Set<String> ignoredWords = wordContextIgnores.get(wordItem.parent);
            if (ignoredWords == null)
                ignoredWords = new HashSet<String>(5);
            ignoredWords.add(wordItem.invalidWord);
            wordContextIgnores.put(wordItem.parent, ignoredWords);
        }

        public boolean isIgnored(WordItem wordItem,
                IWordContextProvider provider) {
            //first,check the word is ignored in workbook
            Set<String> ignoreWords = workbookIgnores.get(provider);
            if (ignoreWords != null
                    && ignoreWords.contains(wordItem.invalidWord))
                return true;

            //then,check the word is ignored in word context
            ignoreWords = wordContextIgnores.get(wordItem.parent);
            if (ignoreWords != null
                    && ignoreWords.contains(wordItem.invalidWord))
                return true;

            return false;
        }

    }

    private TreeViewer viewer;

    StyledText textWidget;

    private IWordContextProvider provider;

    private Button ignoreButton, ignoreAllButton, addToDictionaryButton,
            changeButton, changeAllButton;

    private Button scanWorkbookBt;

    private static List<WordItem> errorList = new LinkedList<WordItem>();

    private ResourceManager resources;

    public SpellingCheckDialog(Shell parentShell) {
        super(parentShell);

        setShellStyle(SWT.MODELESS | SWT.RESIZE | SWT.DIALOG_TRIM | SWT.MIN
                | SWT.MAX);
        setBlockOnOpen(false);

    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                scanWorkbook();
            }
        });

        newShell.setText(Messages.SpellingCheckDialog_title);
    }

    @Override
    protected void initializeBounds() {
        getShell().setBounds(300, 150, 516, 600);
        super.initializeBounds();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        SpellingPlugin.getDefault().getUsageDataCollector()
                .trackView(UserDataConstants.VIEW_SPELLING_CHECK);

        Composite composite = (Composite) super.createDialogArea(parent);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                composite);

        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createDescriptionArea(composite);
        createSeparator(composite);

        Composite content = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 14;
        layout.marginTop = 10;
        layout.marginBottom = 0;
        layout.verticalSpacing = 20;
        layout.horizontalSpacing = 0;
        content.setLayout(layout);
        content.setLayoutData(new GridData(GridData.FILL_BOTH));

        createResultComposite(content);
        createSeparator(composite);

//        if (SpellingViewContent.getInstance().getInput() != null) {
//            viewer.setInput(SpellingViewContent.getInstance().getInput());
//        }
        SpellingViewContent.getInstance().addDialog(this);
        CheckSpellingJob.getInstance().addJobChangeListener(this);

        return composite;
    }

    private void createDescriptionArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 14;
        gridLayout.marginHeight = 21;
        gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);

        Label discriptionLabel = new Label(composite, SWT.WRAP);
        GridData discriptionLabelData = new GridData(SWT.FILL, SWT.CENTER, true,
                true);
        discriptionLabel.setLayoutData(discriptionLabelData);
        discriptionLabel.setAlignment(SWT.LEFT);
        discriptionLabel.setText(Messages.SpellingCheckDialog_description);
    }

    private void createSeparator(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#cfcfcf"))); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 1;
        composite.setLayoutData(gridData);
        composite.setLayout(new GridLayout(1, false));
    }

    private void createResultComposite(Composite parent) {

        Composite incorrectWordComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 0;
        incorrectWordComposite.setLayout(layout);
        incorrectWordComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label label = new Label(incorrectWordComposite, SWT.NONE);
        label.setBackground(incorrectWordComposite.getBackground());
        label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        label.setText(Messages.SpellingCheckDialog_Result_NoDictionary_label);

        Composite textComposite = new Composite(incorrectWordComposite,
                SWT.NONE);
        textComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        textComposite.setBackground(incorrectWordComposite.getBackground());

        GridLayout layout2 = new GridLayout(1, true);
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.verticalSpacing = 0;
        layout2.horizontalSpacing = 0;
        textComposite.setLayout(layout2);

        textWidget = new StyledText(textComposite,
                SWT.WRAP | SWT.MULTI | SWT.BORDER);
        textWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        textWidget.setEditable(false);
        textWidget.setEnabled(false);

        Composite buttonBar = new Composite(incorrectWordComposite, SWT.NONE);
        buttonBar.setBackground(incorrectWordComposite.getBackground());
        buttonBar.setLayoutData(
                new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));

        GridLayout buttonLayout = new GridLayout(3, false);
        buttonLayout.marginHeight = 0;
        buttonLayout.marginTop = 5;
        buttonLayout.marginWidth = 0;
        buttonLayout.verticalSpacing = 0;
        buttonLayout.horizontalSpacing = 20;
        buttonBar.setLayout(buttonLayout);

        ignoreButton = new Button(buttonBar, SWT.PUSH);
        setButtonLayoutData(ignoreButton);
        ignoreButton.setText(Messages.SpellingCheckDialog_Result_Ignore_button);
        ignoreButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ignore();
            }
        });

        ignoreAllButton = new Button(buttonBar, SWT.PUSH);
        setButtonLayoutData(ignoreAllButton);
        ignoreAllButton
                .setText(Messages.SpellingCheckDialog_Result_IgnoreAll_button);
        ignoreAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ignoreAll();
            }
        });

        addToDictionaryButton = new Button(buttonBar, SWT.PUSH);
        addToDictionaryButton.setText(
                Messages.SpellingCheckDialog_Result_AddToDictionary_button);
        addToDictionaryButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addToDictionary();
            }
        });

        //
        Composite suggestionComposite = new Composite(parent, SWT.NONE);
        suggestionComposite.setBackground(parent.getBackground());
        suggestionComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout3 = new GridLayout(1, true);
        layout3.marginHeight = 0;
        layout3.marginWidth = 0;
        layout3.marginBottom = 20;
        layout3.verticalSpacing = 10;
        layout3.horizontalSpacing = 0;
        suggestionComposite.setLayout(layout3);

        Label suggestionLabel = new Label(suggestionComposite, SWT.NONE);
        suggestionLabel
                .setText(Messages.SpellingCheckDialog_Result_Suggestions_label);

        viewer = new TreeViewer(suggestionComposite,
                SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        viewer.getTree()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.getTree().setLinesVisible(false);
        viewer.getTree().setHeaderVisible(false);
        viewer.setContentProvider(new SpellingCheckContentProvider());

        TreeColumn col1 = new TreeColumn(viewer.getTree(), SWT.RIGHT);

        col1.setWidth(300);
        viewer.setAutoExpandLevel(1);

        Composite suggestionButtonBar = new Composite(suggestionComposite,
                SWT.NONE);
        suggestionButtonBar.setBackground(suggestionComposite.getBackground());
        suggestionButtonBar.setLayoutData(
                new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));

        GridLayout buttonLayout2 = new GridLayout(2, false);
        buttonLayout2.marginHeight = 0;
        buttonLayout2.marginTop = 5;
        buttonLayout2.marginWidth = 0;
        buttonLayout2.verticalSpacing = 0;
        buttonLayout2.horizontalSpacing = 20;
        suggestionButtonBar.setLayout(buttonLayout2);

        changeButton = new Button(suggestionButtonBar, SWT.PUSH);
        setButtonLayoutData(changeButton);
        changeButton.setText(Messages.SpellingCheckDialog_Result_Change_button);
        changeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = viewer.getSelection();
                if (errorList != null && !errorList.isEmpty()
                        && selection instanceof StructuredSelection) {
                    StructuredSelection st = (StructuredSelection) selection;
                    ReplaceAction action = new ReplaceAction(
                            errorList.remove(0),
                            st.getFirstElement().toString());
                    action.run();
                }
                refresh();

            }
        });

        changeAllButton = new Button(suggestionButtonBar, SWT.PUSH);
        setButtonLayoutData(changeAllButton);
        changeAllButton
                .setText(Messages.SpellingCheckDialog_Result_ChangeAll_button);
        changeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = viewer.getSelection();
                if (errorList != null && !errorList.isEmpty()
                        && selection instanceof StructuredSelection) {

                    List<WordItem> waitDelete = new ArrayList<WordItem>();

                    StructuredSelection st = (StructuredSelection) selection;
                    String suggestion = st.getFirstElement().toString();
                    String invalidWord = errorList.get(0).invalidWord;
                    for (WordItem item : errorList)
                        if (invalidWord.equals(item.invalidWord)) {
                            waitDelete.add(item);
                            ReplaceAction action = new ReplaceAction(item,
                                    suggestion);
                            action.run();
                        }

                    for (WordItem item : waitDelete)
                        errorList.remove(item);
                }
                refresh();
            }
        });

    }

    private void ignore() {
        if (errorList.isEmpty())
            return;
        WordItem item = errorList.remove(0);
        IgnoredWordsStorage ignoreInstance = IgnoredWordsStorage.getInstance();
        ignoreInstance.addIgnoreWord(item);
        refresh();
    }

    private void ignoreAll() {
        if (errorList.isEmpty())
            return;
        WordItem item = errorList.get(0);
        IgnoredWordsStorage ignoreInstance = IgnoredWordsStorage.getInstance();
        ignoreInstance.addIgnoreAllWord(item, provider);
        scanWorkbook();
    }

    private void addToDictionary() {
        if (errorList.isEmpty())
            return;
        AddToDictionaryAction addToDic = new AddToDictionaryAction(
                errorList.get(0));
        addToDic.run();

        scanWorkbook();
    }

    public void scanWorkbook() {
        errorList.clear();

        IEditorPart editor = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor == null) {
            MessageDialog.openInformation(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                    Messages.SpellingCheckView_dialogTitle,
                    Messages.SpellingCheckView_NoEditors_message);
            return;
        }

        provider = (IWordContextProvider) editor
                .getAdapter(IWordContextProvider.class);
        if (provider == null) {
            MessageDialog.openInformation(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                    Messages.SpellingCheckView_dialogTitle,
                    Messages.SpellingCheckView_NoProviders_message);
            return;
        } else {
            CheckSpellingJob.start(provider);
        }
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 13;
        layout.marginHeight = 23;
        composite.setLayout(layout);

        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        createOptionsButton(composite);

        Composite buttonBar = new Composite(composite, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout2 = new GridLayout();
        layout2.numColumns = 0; // this is incremented by createButton
        layout2.makeColumnsEqualWidth = false;
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_SPACING);
        layout2.verticalSpacing = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_SPACING);
        buttonBar.setLayout(layout2);
        buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));
        buttonBar.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(buttonBar);
        return buttonBar;
    }

    private void createOptionsButton(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(gridLayout);

        createButton(composite, OPTIONS_BUTTON_ID,
                Messages.SpellingCheckDialog_ButtonBar_Options_button, false);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.CLOSE_ID == buttonId)
            close();
        else if (OPTIONS_BUTTON_ID == buttonId) {
            PrefUtils.openPrefDialog(getParentShell(), preferenceID);
        }
    }

    @Override
    public int open() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .addPartListener(this);
        return super.open();
    }

    @Override
    public boolean close() {
        SpellingViewContent.getInstance().removeDialog(this);
        CheckSpellingJob.getInstance().removeJobChangeListener(this);
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .removePartListener(this);
        return super.close();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse
     * .core.runtime.jobs.IJobChangeEvent)
     */
    public void aboutToRun(IJobChangeEvent event) {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core
     * .runtime.jobs.IJobChangeEvent)
     */
    public void awake(IJobChangeEvent event) {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core
     * .runtime.jobs.IJobChangeEvent)
     */
    public void done(final IJobChangeEvent event) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (event.getResult().isOK()) {
                    SpellingViewContent.getInstance().setInput(
                            ((CheckSpellingJob) event.getJob()).getInput());
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.
     * core.runtime.jobs.IJobChangeEvent)
     */
    public void running(IJobChangeEvent event) {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse
     * .core.runtime.jobs.IJobChangeEvent)
     */
    public void scheduled(IJobChangeEvent event) {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse
     * .core.runtime.jobs.IJobChangeEvent)
     */
    public void sleeping(IJobChangeEvent event) {
    }

    public void inputChanged(IWordContextProvider input) {

        clearIgnoredWord();

        refresh();
    }

    private void clearIgnoredWord() {
        if (errorList != null) {
            IgnoredWordsStorage ignoreInstance = IgnoredWordsStorage
                    .getInstance();
            List<WordItem> waitToDelete = new ArrayList<WordItem>();
            for (WordItem item : errorList)
                if (ignoreInstance.isIgnored(item, provider))
                    waitToDelete.add(item);
            for (WordItem item : waitToDelete)
                errorList.remove(item);
        }

    }

    private void refresh() {

        if (!errorList.isEmpty()) {
            IWordContext wordContext = errorList.get(0).parent;
            wordContext.reveal();
            textWidget.setText(wordContext.getContent());
            List<Object> suggestions = errorList.get(0).suggestions;
            viewer.setInput(suggestions);

            if (suggestions != null && !suggestions.isEmpty()) {
                viewer.setSelection(new StructuredSelection(
                        errorList.get(0).suggestions.get(0)));
                changeButton.setEnabled(true);
                changeAllButton.setEnabled(true);
            } else {
                changeButton.setEnabled(false);
                changeAllButton.setEnabled(false);
            }

            String fullText = errorList.get(0).parent.getContent();
            String errorText = errorList.get(0).invalidWord;
            StyleRange range = new StyleRange();
            range.fontStyle = SWT.BOLD;
            range.foreground = ColorUtils.getColor(0xff, 0, 0);
            range.start = fullText.indexOf(errorText);
            range.length = errorText.length();
            StyleRange[] styleRanges = textWidget.getStyleRanges();
            List<StyleRange> ranges = new ArrayList<StyleRange>(
                    styleRanges.length + 1);
            for (StyleRange ran : styleRanges)
                ranges.add(ran);
            ranges.add(range);

            styleRanges = new StyleRange[ranges.size()];
            ranges.toArray(styleRanges);
            textWidget.setStyleRanges(styleRanges);

            ignoreButton.setEnabled(true);
            ignoreAllButton.setEnabled(true);
            addToDictionaryButton.setEnabled(true);
        } else {
            textWidget
                    .setText(Messages.SpellingCheckDialog_Result_NoError_label);
            textWidget.setStyleRanges(new StyleRange[] {});
            ignoreButton.setEnabled(false);
            ignoreAllButton.setEnabled(false);
            addToDictionaryButton.setEnabled(false);

            viewer.setInput(Collections.emptyList());
            changeButton.setEnabled(false);
            changeAllButton.setEnabled(false);
        }

        if (scanWorkbookBt != null && !scanWorkbookBt.isDisposed())
            scanWorkbookBt.setEnabled(!errorList.isEmpty());
    }

    public void partActivated(IWorkbenchPart part) {
        if (part instanceof IEditorPart)
            scanWorkbook();
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (part instanceof IEditorPart)
            scanWorkbook();
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

}
