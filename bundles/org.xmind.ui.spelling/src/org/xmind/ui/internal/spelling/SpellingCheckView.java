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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.IWordContext;
import org.xmind.ui.IWordContextProvider;

import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

/**
 * @author Frank Shaka
 */
@Deprecated
public class SpellingCheckView extends ViewPart
        implements IJobChangeListener, IPartListener {

    private static class SpellingViewContent {

        private static final SpellingViewContent instance = new SpellingViewContent();

        private SpellingCheckViewerInput input = null;

        private List<SpellingCheckView> views = new ArrayList<SpellingCheckView>();

        private SpellingViewContent() {
        }

        public void addView(SpellingCheckView view) {
            this.views.add(view);
        }

        public void removeView(SpellingCheckView view) {
            this.views.remove(view);
        }

        public void setInput(SpellingCheckViewerInput input) {
            this.input = input;
            fireInputChanged();
        }

        public SpellingCheckViewerInput getInput() {
            return this.input;
        }

        private void fireInputChanged() {
            for (Object view : views.toArray()) {
                ((SpellingCheckView) view).inputChanged(input);
            }
        }

        public void update(Object element) {
            for (Object view : views.toArray()) {
                ((SpellingCheckView) view).update(element);
            }
        }

        public static SpellingViewContent getInstance() {
            return instance;
        }

    }

    private static class SpellingCheckViewerInput {

        public List<WorkbookItem> elements = new ArrayList<WorkbookItem>();

    }

    private static class WorkbookItem {

        public SpellingCheckViewerInput parent;

        public IEditorPart editor;

        public IWordContextProvider provider;

        public List<WordContextItem> children = new ArrayList<WordContextItem>();

        /**
         * 
         */
        public WorkbookItem(SpellingCheckViewerInput parent, IEditorPart editor,
                IWordContextProvider provider) {
            this.parent = parent;
            this.editor = editor;
            this.provider = provider;
        }

    }

    private static class WordContextItem {

        public WorkbookItem parent;

        public IWordContext context;

        public List<WordItem> children = new ArrayList<WordItem>();

        /**
         * 
         */
        public WordContextItem(WorkbookItem parent, IWordContext context) {
            this.parent = parent;
            this.context = context;
        }

    }

    private static class WordItem {

        public WordContextItem parent;

        public int start;

        public String invalidWord;

        public List suggestions;

        /**
         * 
         */
        public WordItem(WordContextItem parent, SpellCheckEvent range) {
            this.parent = parent;
            this.start = range.getWordContextPosition();
            this.invalidWord = range.getInvalidWord();
            this.suggestions = range.getSuggestions();
        }

        public void reveal() {
            parent.context.revealWord(start, invalidWord.length());
        }

    }

    private static class NoSpellingErrorItem {

        public WorkbookItem parent;

        public NoSpellingErrorItem(WorkbookItem parent) {
            this.parent = parent;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (!(obj instanceof NoSpellingErrorItem))
                return false;
            return ((NoSpellingErrorItem) obj).parent == this.parent;
        }
    }

    private static class SpellingCheckContentProvider
            implements ITreeContentProvider {

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang
         * .Object)
         */
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof WorkbookItem) {
                WorkbookItem item = (WorkbookItem) parentElement;
                if (item.children.isEmpty()) {
                    return new Object[] { new NoSpellingErrorItem(item) };
                }
                return item.children.toArray();
            } else if (parentElement instanceof WordContextItem) {
                return ((WordContextItem) parentElement).children.toArray();
            }
            return new Object[0];
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang
         * .Object)
         */
        public Object getParent(Object element) {
            if (element instanceof WordItem) {
                return ((WordItem) element).parent;
            } else if (element instanceof WordContextItem) {
                return ((WordContextItem) element).parent;
            } else if (element instanceof WorkbookItem) {
                return ((WorkbookItem) element).parent;
            } else if (element instanceof NoSpellingErrorItem) {
                return ((NoSpellingErrorItem) element).parent;
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang
         * .Object)
         */
        public boolean hasChildren(Object element) {
            if (element instanceof WorkbookItem) {
                return true;
            } else if (element instanceof WordContextItem) {
                return !((WordContextItem) element).children.isEmpty();
            } else if (element instanceof SpellingCheckViewerInput) {
                return !((SpellingCheckViewerInput) element).elements.isEmpty();
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(
         * java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof SpellingCheckViewerInput) {
                return ((SpellingCheckViewerInput) inputElement).elements
                        .toArray();
            }
            return new Object[0];
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
         * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

    }

    private static class SpellingCheckLabelProvider extends LabelProvider
            implements ITableLabelProvider {

        private Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
         * .lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof WordContextItem) {
                if (columnIndex == 0) {
                    return getImageFor(
                            ((WordContextItem) element).context.getIcon());
                }
            } else if (element instanceof WorkbookItem) {
                if (columnIndex == 0) {
                    return ((WorkbookItem) element).editor.getTitleImage();
                }
            }
            return null;
        }

        private Image getImageFor(ImageDescriptor icon) {
            if (icon == null)
                return null;
            Image image = images.get(icon);
            if (image == null) {
                image = icon.createImage();
                images.put(icon, image);
            }
            return image;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            for (Image image : images.values()) {
                image.dispose();
            }
            images.clear();
            super.dispose();
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
         * lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof WordItem) {
                WordItem item = (WordItem) element;
                if (columnIndex == 0) {
                    return item.invalidWord;
                }
                StringBuffer sb = new StringBuffer(
                        item.suggestions.size() * 10);
                for (Object s : item.suggestions) {
                    if (sb.length() > 0) {
                        sb.append(", "); //$NON-NLS-1$
                    }
                    sb.append(s.toString());
                }
                return sb.toString();
            } else if (element instanceof WordContextItem) {
                if (columnIndex == 0) {
                    return ((WordContextItem) element).context.getName();
                }
            } else if (element instanceof WorkbookItem) {
                if (columnIndex == 0) {
                    return ((WorkbookItem) element).editor.getTitle();
                }
            } else if (element instanceof NoSpellingErrorItem) {
                if (columnIndex == 0) {
                    return Messages.SpellingCheckView_NoSpellingError_text;
                }
            }
            return ""; //$NON-NLS-1$
        }

    }

    private static class WordDoubleClickListener
            implements IDoubleClickListener {

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.
         * eclipse .jface.viewers.DoubleClickEvent)
         */
        public void doubleClick(DoubleClickEvent event) {
            if (event.getSelection() instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) event.getSelection())
                        .getFirstElement();
                if (element instanceof WordItem) {
                    ((WordItem) element).reveal();
                } else if (element instanceof WordContextItem) {
                    ((WordContextItem) element).context.reveal();
                } else if (element instanceof WorkbookItem) {
                    IEditorPart editor = ((WorkbookItem) element).editor;
                    editor.getSite().getPage().activate(editor);
                }
            }
        }

    }

    private static class CheckSpellingJob extends Job
            implements SpellCheckListener {

        private static final CheckSpellingJob instance = new CheckSpellingJob();

        private SpellingCheckViewerInput input = null;

        private SpellChecker spellChecker = null;

        private WordContextItem currentWordContextItem = null;

        /**
         */
        private CheckSpellingJob() {
            super(Messages.CheckSpellingJob_name);
        }

        public void setInput(SpellingCheckViewerInput input) {
            this.input = input;
        }

        /**
         * @return the input
         */
        public SpellingCheckViewerInput getInput() {
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
                    input.elements.size());

            SpellChecker theSpellChecker = spellChecker;
            theSpellChecker.addSpellCheckListener(this);
            try {
                for (WorkbookItem item : input.elements) {
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    monitor.subTask(item.editor.getTitle());
                    scan(new SubProgressMonitor(monitor, 1), item);
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    monitor.worked(1);
                }
            } finally {
                theSpellChecker.removeSpellCheckListener(this);
            }

            monitor.done();

            SpellingPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_SPELLING,
                    UserDataConstants.CHECK_SPELLING);

            return Status.OK_STATUS;
        }

        private void scan(IProgressMonitor monitor, WorkbookItem parent) {
            List<IWordContext> contexts = parent.provider.getWordContexts();
            monitor.beginTask(null, contexts.size());
            for (IWordContext context : contexts) {
                if (monitor.isCanceled())
                    return;
                WordContextItem item = new WordContextItem(parent, context);
                monitor.subTask(parent.editor.getTitle() + " - " //$NON-NLS-1$
                        + item.context.getName());
                scan(new SubProgressMonitor(monitor, 1), item);
                if (monitor.isCanceled())
                    return;

                if (!item.children.isEmpty()) {
                    parent.children.add(item);
                }
                monitor.worked(1);
            }
            monitor.done();
        }

        private void scan(IProgressMonitor monitor, WordContextItem parent) {
            monitor.beginTask(null, 1);

            String content = parent.context.getContent();
            if (monitor.isCanceled())
                return;

            currentWordContextItem = parent;
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
            currentWordContextItem.children
                    .add(new WordItem(currentWordContextItem, event));
        }

        public static CheckSpellingJob getInstance() {
            return instance;
        }

        public static void start(SpellingCheckViewerInput input) {
            instance.setInput(input);
            instance.schedule();
        }

        public static boolean isRunning() {
            return instance.getState() == Job.RUNNING;
        }

    }

    private static class SuggestionMenu
            implements DisposeListener, IMenuListener {

        private TreeViewer viewer;

        private MenuManager menu;

        /**
         * 
         */
        public SuggestionMenu(TreeViewer viewer) {
            this.viewer = viewer;
            this.menu = new MenuManager();
            this.menu.addMenuListener(this);
            this.menu.setRemoveAllWhenShown(true);
            viewer.getTree()
                    .setMenu(this.menu.createContextMenu(viewer.getTree()));
            viewer.getTree().addDisposeListener(this);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse
         * .jface.action.IMenuManager)
         */
        public void menuAboutToShow(IMenuManager manager) {
            if (manager != this.menu)
                return;

            ISelection selection = this.viewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                Object o = ((IStructuredSelection) selection).getFirstElement();
                if (o != null && o instanceof WordItem) {
                    fillMenu(this.menu, (WordItem) o);
                }
            }
        }

        private void fillMenu(IMenuManager menu, WordItem item) {
            for (Object suggestion : item.suggestions) {
                menu.add(new ReplaceAction(item, suggestion.toString()));
            }
            menu.add(new Separator());
            menu.add(new AddToDictionaryAction(item));
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse
         * .swt.events.DisposeEvent)
         */
        public void widgetDisposed(DisposeEvent e) {
            this.menu.dispose();
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
            if (item.parent.context.replaceWord(item.start,
                    item.invalidWord.length(), suggestion)) {
                Object toUpdate = item.parent;
                item.parent.children.remove(item);
                if (item.parent.children.isEmpty()) {
                    toUpdate = item.parent.parent;
                    item.parent.parent.children.remove(item.parent);
                } else {
                    int offset = suggestion.length()
                            - item.invalidWord.length();
                    if (offset != 0) {
                        for (WordItem sibling : item.parent.children) {
                            if (sibling.start > item.start) {
                                sibling.start += offset;
                            }
                        }
                    }
                }
                SpellingViewContent.getInstance().update(toUpdate);
            }
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
                    Object toUpdate = item.parent;
                    item.parent.children.remove(item);
                    if (item.parent.children.isEmpty()) {
                        toUpdate = item.parent.parent;
                        item.parent.parent.children.remove(item.parent);
                    }
                    SpellingViewContent.getInstance().update(toUpdate);
                }
            });
        }
    }

    private Composite composite;

    private PageBook bannerBar;

    private Control buttonBar;

    private Control statusBar;

    private TreeViewer viewer;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.
     * widgets .Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        createBannerBar(composite);
        createResultViewer(composite);

        if (SpellingViewContent.getInstance().getInput() != null) {
            viewer.setInput(SpellingViewContent.getInstance().getInput());
        }
        SpellingViewContent.getInstance().addView(this);

        if (CheckSpellingJob.isRunning()) {
            showScanning();
        } else {
            showButtons();
        }
        CheckSpellingJob.getInstance().addJobChangeListener(this);
        getSite().getPage().addPartListener(this);
    }

    private void createBannerBar(Composite parent) {
        bannerBar = new PageBook(parent, SWT.NONE);
        bannerBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createButtonBar(bannerBar);
        createStatusBar(bannerBar);
    }

    private void createButtonBar(Composite parent) {
        Composite buttonBar = new Composite(parent, SWT.NONE);
        this.buttonBar = buttonBar;
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 5;
        buttonBar.setLayout(layout);

        Button scanAllButton = new Button(buttonBar, SWT.PUSH);
        scanAllButton
                .setText(Messages.SpellingCheckView_button_ScanAllWorkbooks);
        scanAllButton.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));
        scanAllButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                scanAll();
            }
        });

        Button scanButton = new Button(buttonBar, SWT.PUSH);
        scanButton.setText(Messages.SpellingCheckView_button_ScanWorkbook);
        scanButton.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));
        scanButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                scanWorkbook();
            }
        });
    }

    private void createStatusBar(Composite parent) {
        Composite statusBar = new Composite(parent, SWT.NONE);
        this.statusBar = statusBar;
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 3;
        layout.marginHeight = 3;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        statusBar.setLayout(layout);

        Label label = new Label(statusBar, SWT.CENTER);
        label.setText(Messages.SpellingCheckView_Scanning);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    }

    private void createResultViewer(Composite parent) {
        viewer = new TreeViewer(parent,
                SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
        viewer.getTree()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.getTree().setLinesVisible(true);
        viewer.getTree().setHeaderVisible(true);

        TreeColumn col1 = new TreeColumn(viewer.getTree(), SWT.NONE);
        col1.setText(Messages.SpellingCheckView_column_Word);
        col1.setWidth(150);

        TreeColumn col2 = new TreeColumn(viewer.getTree(), SWT.NONE);
        col2.setText(Messages.SpellingCheckView_column_Suggestions);
        col2.setWidth(220);

        viewer.setContentProvider(new SpellingCheckContentProvider());
        viewer.setLabelProvider(new SpellingCheckLabelProvider());
        viewer.addDoubleClickListener(new WordDoubleClickListener());
        viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        new SuggestionMenu(viewer);
    }

    private void showScanning() {
        bannerBar.showPage(statusBar);
    }

    private void showButtons() {
        bannerBar.showPage(buttonBar);
    }

    public void scanAll() {
        IEditorReference[] ers = getSite().getPage().getEditorReferences();
        if (ers.length == 0) {
            MessageDialog.openInformation(getSite().getShell(),
                    Messages.SpellingCheckView_dialogTitle,
                    Messages.SpellingCheckView_NoEditors_message);
            return;
        }

        SpellingCheckViewerInput input = new SpellingCheckViewerInput();
        for (IEditorReference er : ers) {
            IEditorPart editor = er.getEditor(false);
            if (editor != null) {
                IWordContextProvider provider = (IWordContextProvider) editor
                        .getAdapter(IWordContextProvider.class);
                if (provider != null) {
                    input.elements
                            .add(new WorkbookItem(input, editor, provider));
                }
            }
        }
        if (input.elements.isEmpty()) {
            MessageDialog.openInformation(getSite().getShell(),
                    Messages.SpellingCheckView_dialogTitle,
                    Messages.SpellingCheckView_NoProviders_message);
        } else {
            CheckSpellingJob.start(input);
        }
    }

    public void scanWorkbook() {
        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor == null) {
            MessageDialog.openInformation(getSite().getShell(),
                    Messages.SpellingCheckView_dialogTitle,
                    Messages.SpellingCheckView_NoEditors_message);
            return;
        }

        IWordContextProvider provider = (IWordContextProvider) editor
                .getAdapter(IWordContextProvider.class);
        if (provider == null) {
            MessageDialog.openInformation(getSite().getShell(),
                    Messages.SpellingCheckView_dialogTitle,
                    Messages.SpellingCheckView_NoProviders_message);
        } else {
            SpellingCheckViewerInput input = new SpellingCheckViewerInput();
            input.elements.add(new WorkbookItem(input, editor, provider));
            CheckSpellingJob.start(input);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        getSite().getPage().removePartListener(this);
        SpellingViewContent.getInstance().removeView(this);
        CheckSpellingJob.getInstance().removeJobChangeListener(this);
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse
     * .core.runtime.jobs.IJobChangeEvent)
     */
    public void aboutToRun(IJobChangeEvent event) {
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                showScanning();
            }
        });
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
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                showButtons();
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

    public void inputChanged(SpellingCheckViewerInput input) {
        this.viewer.setInput(input);
    }

    public void update(Object element) {
        this.viewer.refresh(element);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.
     * IWorkbenchPart )
     */
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
        SpellingCheckViewerInput input = SpellingViewContent.getInstance()
                .getInput();
        if (input != null) {
            Object[] workbookItems = input.elements.toArray();
            for (Object workbookItem : workbookItems) {
                WorkbookItem item = (WorkbookItem) workbookItem;
                if (item.editor == part) {
                    input.elements.remove(item);
                    SpellingViewContent.getInstance().update(input);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.
     * IWorkbenchPart )
     */
    public void partDeactivated(IWorkbenchPart part) {
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
    }

}
