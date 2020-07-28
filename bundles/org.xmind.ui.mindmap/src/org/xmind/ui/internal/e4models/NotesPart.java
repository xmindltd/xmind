/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.e4models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.E4PartWrapper;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.INotes;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.EditDomain;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyNotesCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.actions.FindReplaceAction;
import org.xmind.ui.internal.actions.ShowAllNotesAction;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider;
import org.xmind.ui.internal.notes.INotesContentViewer;
import org.xmind.ui.internal.notes.NotesFindReplaceOperationProvider;
import org.xmind.ui.internal.notes.RichDocumentNotesAdapter;
import org.xmind.ui.internal.notes.SheetNotesViewer;
import org.xmind.ui.internal.notes.TopicNotesViewer;
import org.xmind.ui.internal.spelling.SpellingPlugin;
import org.xmind.ui.internal.spellsupport.SpellingSupport;
import org.xmind.ui.internal.views.NotesHyperlinkDialog;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.richtext.FullRichTextActionBarContributor;
import org.xmind.ui.richtext.Hyperlink;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.IRichDocumentListener;
import org.xmind.ui.richtext.IRichTextAction;
import org.xmind.ui.richtext.IRichTextEditViewer;
import org.xmind.ui.richtext.IRichTextRenderer;
import org.xmind.ui.richtext.ImagePlaceHolder;
import org.xmind.ui.richtext.LineStyle;
import org.xmind.ui.richtext.RichTextEditViewer;
import org.xmind.ui.richtext.RichTextUtils;
import org.xmind.ui.richtext.TextActionConstants;
import org.xmind.ui.tabfolder.DelegatedSelectionProvider;
import org.xmind.ui.texteditor.IMenuContributor;
import org.xmind.ui.texteditor.ISpellingActivation;
import org.xmind.ui.util.Logger;

@SuppressWarnings("restriction")
public class NotesPart extends ViewModelPart
        implements IPartListener, ICoreEventListener, IDocumentListener,
        IRichDocumentListener, IContributedContentsView,
        ISelectionChangedListener, IPropertyChangeListener {

    private static final String NOTES_EDIT_CONTEXT_ID = "org.xmind.ui.context.notes.edit"; //$NON-NLS-1$

    private static boolean DEBUG = false;

    private static class EActionHandler {

        private IAction action;

        public EActionHandler(IAction action) {
            this.action = action;
        }

        @Execute
        public void execute() {
            if (action.getStyle() == IAction.AS_CHECK_BOX
                    || action.getStyle() == IAction.AS_RADIO_BUTTON) {
                action.setChecked(!action.isChecked());
            }
            action.run();
        }

        @CanExecute
        public boolean canExecute() {
            return action.isEnabled();
        }
    }

    private class ContextActivator implements FocusListener, DisposeListener {

        public ContextActivator(Control control) {
            control.addFocusListener(this);
            control.addDisposeListener(this);
        }

        public void focusGained(FocusEvent e) {
            activateContext();
        }

        public void focusLost(FocusEvent e) {
            deactivateContext();
        }

        public void widgetDisposed(DisposeEvent e) {
            deactivateContext();
        }

        private void deactivateContext() {
            contextService.deactivateContext(NOTES_EDIT_CONTEXT_ID);
        }

        private void activateContext() {
            contextService.activateContext(NOTES_EDIT_CONTEXT_ID);
        }
    }

    private class InsertImageAction extends Action implements IRichTextAction {

        private IRichTextEditViewer viewer;

        public InsertImageAction(IRichTextEditViewer viewer) {
            super(MindMapMessages.InsertImage_text, MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_IMAGE, true));
            this.viewer = viewer;
            setToolTipText(MindMapMessages.NotesView_InsertImage_toolTip);
            setDisabledImageDescriptor(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_IMAGE, false));
        }

        public void run() {
            if (!(viewer instanceof RichTextEditViewer)
                    || viewer.getControl().isDisposed() || adapter == null)
                return;

            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_NOTES,
                    UserDataConstants.NOTES_INSERT_IMAGE);

            String path = getPath();
            if (path == null)
                return;

            Image image = adapter.createImageFromFile(path);
            if (image == null)
                return;

            viewer.getRenderer().insertImage(image);
        }

        private String getPath() {
            FileDialog fd = new FileDialog(workbenchWindow.getShell(),
                    SWT.OPEN);
            DialogUtils.makeDefaultImageSelectorDialog(fd, true);
            return fd.open();
        }

        public void dispose() {
            viewer = null;
        }

        public void selectionChanged(IRichTextEditViewer viewer,
                ISelection selection) {
        }
    }

    private class InsertHyperlinkAction extends Action
            implements IRichTextAction {

        private IRichTextEditViewer viewer;

        public InsertHyperlinkAction(IRichTextEditViewer viewer) {
            super(MindMapMessages.InsertHyperlinkAction_text,
                    MindMapUI.getImages().get("notes_hyperlink.png", true)); //$NON-NLS-1$
            setToolTipText(MindMapMessages.InserthyperlinkAction_toolTip);
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get("notes_hyperlink.png", false)); //$NON-NLS-1$
            this.viewer = viewer;
        }

        public void run() {
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_NOTES,
                    UserDataConstants.NOTES_INSERT_HYPERLINK);

            IRichTextRenderer renderer = viewer.getRenderer();
            ITextSelection selection = (ITextSelection) viewer.getSelection();
            String oldText = selection.getText();

            int start = selection.getOffset();
            int end = start + selection.getLength();

            Hyperlink[] oldHyperlinks = renderer.getSelectionHyperlinks();
            String oldHref = null;
            Hyperlink oldHyperlink = null;
            if (oldHyperlinks.length == 1) {
                Hyperlink link = oldHyperlinks[0];
                if (link.start <= selection.getOffset()
                        && link.end() >= selection.getOffset()
                                + selection.getLength()) {
                    // selection within the hyperlink
                    oldHyperlink = link;
                    oldHref = link.href;
                    try {
                        oldText = viewer.getDocument().get(link.start,
                                link.length);
                        start = link.start;
                        end = start + link.length;

                    } catch (BadLocationException e) {
                        String message = String.format(
                                "Unexpected hyperlink range: start=%d, length=%d", //$NON-NLS-1$
                                link.start, link.length);
                        Logger.log(e, message);
                    }
                }
            }

            ImagePlaceHolder[] images = viewer.getDocument().getImages();
            int temp = -1;
            for (int i = 0; i < images.length; i++) {
                ImagePlaceHolder image = images[i];
                if (image.offset >= end)
                    break;
                if (image.offset >= start && image.offset <= end) {
                    temp++;
                    int offset = image.offset - start;
                    oldText = oldText.substring(0, offset - temp)
                            + oldText.substring(offset + 1 - temp);
                }
            }

            NotesHyperlinkDialog dialog = new NotesHyperlinkDialog(
                    workbenchWindow.getShell(), oldHref, oldText);
            int ret = dialog.open();
            if (ret == NotesHyperlinkDialog.OK) {
                String newText = dialog.getDisplayText();
                String newHref = dialog.getHref();
                if (oldHyperlink != null && newText.equals(oldText)) {
                    if (!oldHyperlink.href.equals(newHref)) {
                        RichTextUtils.replaceHyperlinkHref(viewer.getDocument(),
                                oldHyperlink, newHref);
                    }
                } else {
                    if ("".equals(newText)) { //$NON-NLS-1$
                        newText = newHref;
                    }
                    renderer.insertHyperlink(newHref, newText);
                }
            }
        }

        public void dispose() {
            viewer = null;
        }

        public void selectionChanged(IRichTextEditViewer viewer,
                ISelection selection) {
        }
    }

    private class TextAction extends Action {

        private int op;

        public TextAction(int op) {
            this.op = op;
        }

        public void run() {
            if (!(viewer instanceof TopicNotesViewer)
                    || viewer.getControl().isDisposed())
                return;

            TextViewer textViewer = ((TopicNotesViewer) viewer)
                    .getImplementation().getTextViewer();
            if (textViewer.canDoOperation(op)) {
                textViewer.doOperation(op);
            }
        }

        public void update(TextViewer textViewer) {
            setEnabled(textViewer.canDoOperation(op));
        }
    }

    private class NotesPartRichTextActionBarContributor
            extends FullRichTextActionBarContributor {

        private IRichTextAction insertImageAction;
        private IRichTextAction insertHyperlinkAction;

        private IAction showAllNotesAction;

        protected void makeActions(IRichTextEditViewer viewer) {
            super.makeActions(viewer);

            insertImageAction = new InsertImageAction(viewer);
            addRichTextAction(insertImageAction);

            insertHyperlinkAction = new InsertHyperlinkAction(viewer);
            addRichTextAction(insertHyperlinkAction);

            showAllNotesAction = new ShowAllNotesAction(NotesPart.this);
        }

        public void fillMenu(IMenuManager menu) {
        }

        public void fillToolBar(IToolBarManager toolbar) {
            super.fillToolBar(toolbar);
            toolbar.add(new Separator());
            toolbar.add(insertImageAction);
            toolbar.add(insertHyperlinkAction);
            toolbar.add(showAllNotesAction);
        }

        @Override
        public void fillContextMenu(IMenuManager menu) {
            menu.add(getGlobalAction(ActionFactory.UNDO.getId()));
            menu.add(getGlobalAction(ActionFactory.REDO.getId()));
            menu.add(new Separator());
            menu.add(getGlobalAction(ActionFactory.CUT.getId()));
            menu.add(getGlobalAction(ActionFactory.COPY.getId()));
            menu.add(getGlobalAction(ActionFactory.PASTE.getId()));
            menu.add(new Separator());
            menu.add(getGlobalAction(ActionFactory.SELECT_ALL.getId()));
            menu.add(new Separator());
            super.fillContextMenu(menu);
            if (spellingActivation != null) {
                IMenuContributor contributor = (IMenuContributor) spellingActivation
                        .getAdapter(IMenuContributor.class);
                if (contributor != null) {
                    menu.add(new Separator());
                    contributor.fillMenu(menu);
                }
            }
        }

        @Override
        protected void handleFontSelectionChanged(SelectionChangedEvent event) {
            super.handleFontSelectionChanged(event);
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_NOTES,
                    UserDataConstants.NOTES_CHANGE_FONT);
        }
    }

    private class CommitNotesHandler {

        @Execute
        public Object execute() {
            saveNotes();
            IWorkbenchPage page = workbenchWindow.getActivePage();
            if (page != null && contributingEditor != null
                    && page == contributingEditor.getSite().getPage()) {
                page.activate(contributingEditor);
            }
            return null;
        }
    }

    private class SaveNotesJob implements ICoreEventListener {

        public void handleCoreEvent(CoreEvent event) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run() {
                    saveNotes();
                }
            });
        }
    }

    @Inject
    private IWorkbenchWindow workbenchWindow;

    @Inject
    private EContextService contextService;

    @Inject
    private EHandlerService handlerService;

    private IGraphicalEditor contributingEditor;

    private ISelection currentSelection;

    private ITopicPart currentTopicPart;

    private INotesContentViewer viewer;

    private RichDocumentNotesAdapter adapter;

    private NotesPartRichTextActionBarContributor topicViewerContributor;

    private ICoreEventRegister eventRegister;

    private Map<String, Object> handlers = new HashMap<String, Object>();

    private ISpellingActivation spellingActivation;

    private boolean savingNotes;

    private NotesFindReplaceOperationProvider notesOperationProvider = null;

    private ICoreEventRegistration saveNotesReg = null;

    private Map<String, IAction> globalActions = new HashMap<String, IAction>(
            7);

    private List<TextAction> textActions = new ArrayList<TextAction>(7);

    private IWorkbenchAction findReplaceAction;

    private IPreferenceStore spellingPreferences;

    private boolean updating;

    private Composite contentArea;

    private ISelectionChangedListener listener;

    private CommitNotesHandler commitNotesHandler = new CommitNotesHandler();

    @Override
    protected Control doCreateContent(Composite parent) {
        contentArea = createComposite(parent);

        topicViewerContributor = new NotesPartRichTextActionBarContributor();
        workbenchWindow.getActivePage().addPartListener(this);
        showBootstrapContent();
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_NOTES, UserDataConstants.USE_NOTES);
        return contentArea;
    }

    private Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setEnabled(false);

        return composite;
    }

    protected boolean postConfiguration(IWorkbenchPart workbenchPart,
            MPart part) {
        super.postConfiguration(workbenchPart, part);
        IWorkbenchPartSite site = workbenchPart.getSite();
        if (site instanceof IViewSite) {
            IActionBars actionBars = ((IViewSite) site).getActionBars();
            if (actionBars == null) {
                return false;
            }
            IServiceLocator serviceLocator = actionBars.getServiceLocator();
            if (serviceLocator == null)
                return false;
            IEclipseContext eclipseContext = serviceLocator
                    .getService(IEclipseContext.class);
            eclipseContext.set(ECommandService.class,
                    serviceLocator.getService(ECommandService.class));
            eclipseContext.set(EHandlerService.class,
                    serviceLocator.getService(EHandlerService.class));

            createActions(actionBars);
            return true;
        }
        return false;
    }

    private void createActions(IActionBars actionBars) {
        registerGlobalTextActionHandlers();
        registerRichTextActionHandlers();
    }

    private void registerGlobalTextActionHandlers() {
        activateGlobalTextHandler(ActionFactory.UNDO,
                ITextOperationTarget.UNDO);
        activateGlobalTextHandler(ActionFactory.REDO,
                ITextOperationTarget.REDO);
        activateGlobalTextHandler(ActionFactory.CUT, ITextOperationTarget.CUT);
        activateGlobalTextHandler(ActionFactory.COPY,
                ITextOperationTarget.COPY);
        activateGlobalTextHandler(ActionFactory.PASTE,
                ITextOperationTarget.PASTE);
        activateGlobalTextHandler(ActionFactory.SELECT_ALL,
                ITextOperationTarget.SELECT_ALL);

        //activate find action.
        findReplaceAction = new FindReplaceAction(workbenchWindow);
        findReplaceAction
                .setActionDefinitionId(ActionFactory.FIND.getCommandId());
        Object handler = new EActionHandler(findReplaceAction);
        handlerService.activateHandler(ActionFactory.FIND.getCommandId(),
                handler);
        handlers.put(ActionFactory.FIND.getCommandId(), handler);
    }

    private void activateGlobalTextHandler(ActionFactory actionFactory,
            int textOp) {
        TextAction textAction = new TextAction(textOp);
        String commandId = actionFactory.getCommandId();
        textAction.setActionDefinitionId(commandId);
        Object handler = new EActionHandler(textAction);
        handlerService.activateHandler(commandId, handler);
        handlers.put(commandId, handler);

        textAction.setId(actionFactory.getId());
        IWorkbenchAction workbenchAction = actionFactory
                .create(workbenchWindow);
        textAction.setText(workbenchAction.getText());
        workbenchAction.dispose();
        textActions.add(textAction);
        globalActions.put(actionFactory.getId(), textAction);
    }

    private void registerRichTextActionHandlers() {
        activateRichTextHandler(TextActionConstants.FONT_ID,
                "org.xmind.ui.command.text.font"); //$NON-NLS-1$
        activateRichTextHandler(TextActionConstants.BOLD_ID,
                "org.xmind.ui.command.text.bold"); //$NON-NLS-1$
        activateRichTextHandler(TextActionConstants.ITALIC_ID,
                "org.xmind.ui.command.text.italic"); //$NON-NLS-1$
        activateRichTextHandler(TextActionConstants.UNDERLINE_ID,
                "org.xmind.ui.command.text.underline"); //$NON-NLS-1$
        activateRichTextHandler(TextActionConstants.LEFT_ALIGN_ID,
                "org.xmind.ui.command.text.leftAlign"); //$NON-NLS-1$
        activateRichTextHandler(TextActionConstants.CENTER_ALIGN_ID,
                "org.xmind.ui.command.text.centerAlign"); //$NON-NLS-1$
        activateRichTextHandler(TextActionConstants.RIGHT_ALIGN_ID,
                "org.xmind.ui.command.text.rightAlign"); //$NON-NLS-1$
    }

    private void activateRichTextHandler(String actionId, String commandId) {
        IRichTextAction action = topicViewerContributor
                .getRichTextAction(actionId);
        if (action != null) {
            action.setActionDefinitionId(commandId);
            Object handler = new EActionHandler(action);
            handlerService.activateHandler(commandId, handler);
            handlers.put(commandId, handler);
        }
    }

    private void unregisterTextActionHandlers() {
        if (handlerService != null) {
            for (Entry<String, Object> entry : handlers.entrySet()) {
                handlerService.deactivateHandler(entry.getKey(),
                        entry.getValue());
            }
            handlers.clear();
        }
    }

    private IAction getGlobalAction(String actionId) {
        return globalActions == null ? null : globalActions.get(actionId);
    }

    private void showBootstrapContent() {
        IEditorPart activeEditor = workbenchWindow.getActivePage()
                .getActiveEditor();
        if (activeEditor instanceof IGraphicalEditor) {
            setContributingEditor((IGraphicalEditor) activeEditor);
        } else {
            editorSelectionChanged(StructuredSelection.EMPTY);
        }
    }

    private void setContributingEditor(IGraphicalEditor editor) {
        if (editor == contributingEditor) {
            return;
        }

        if (contributingEditor != null) {
            DelegatedSelectionProvider selectionProvider = (DelegatedSelectionProvider) contributingEditor
                    .getSite().getSelectionProvider();
            if (selectionProvider != null)
                selectionProvider.removeAsyncSelectionChangedListener(
                        getSelectionChangedListener());
        }

        contributingEditor = editor;

        ISelection newSelection = null;

        if (contributingEditor != null) {
            DelegatedSelectionProvider selectionProvider = (DelegatedSelectionProvider) contributingEditor
                    .getSite().getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.addAsyncSelectionChangedListener(
                        getSelectionChangedListener());
                newSelection = selectionProvider.getSelection();
            }
        }
        if (newSelection == null) {
            newSelection = StructuredSelection.EMPTY;
        }

        if (contentArea.isDisposed()) {
            return;
        }

        editorSelectionChanged(newSelection);
    }

    private void editorSelectionChanged(ISelection selection) {
        if (selection == currentSelection
                || (selection != null && selection.equals(currentSelection)))
            return;

        currentSelection = selection;
        setCurrentTopicPart(findSelectedTopicPart());

        if (currentSelection instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection) currentSelection)
                    .getFirstElement();
            updateViewer(obj);
        } else {
            updateViewer(null);
        }
    }

    private void setCurrentTopicPart(ITopicPart topicPart) {
        if (topicPart == currentTopicPart)
            return;

        unhookTopic();
        saveNotes();
        this.currentTopicPart = topicPart;
//        forceRefreshViewer();
        hookTopic();
    }

    private ITopicPart findSelectedTopicPart() {
        if (contributingEditor == null)
            return null;

        if (currentSelection == null || currentSelection.isEmpty()
                || !(currentSelection instanceof IStructuredSelection))
            return null;

        Object o = ((IStructuredSelection) currentSelection).getFirstElement();

        IGraphicalEditorPage page = contributingEditor.getActivePageInstance();
        if (page == null)
            return null;

        IPart part = page.getViewer().findPart(o);
        if (part instanceof ITopicPart)
            return (ITopicPart) part;

        return null;
    }

    private ISelectionChangedListener getSelectionChangedListener() {
        if (listener == null) {
            listener = new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    editorSelectionChanged(event.getSelection());
                }
            };
        }
        return listener;
    }

    private void hookTopic() {
        if (currentTopicPart != null) {
            ITopic topic = currentTopicPart.getTopic();
            if (eventRegister == null)
                eventRegister = new CoreEventRegister(topic, this);
            eventRegister.register(Core.TopicNotes);
            if (DEBUG)
                System.out.println("Model listeners installed"); //$NON-NLS-1$
        }
    }

    private void unhookTopic() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            if (DEBUG)
                System.out.println("Model listeners uninstalled"); //$NON-NLS-1$
            eventRegister = null;
        }
    }

    private void saveNotes() {
        if (adapter == null || currentTopicPart == null
                || !(viewer instanceof TopicNotesViewer)
                || viewer.getControl().isDisposed()
                || !((TopicNotesViewer) viewer).hasModified()) {
            deactivateJob();
            return;
        }

        if (DEBUG)
            System.out.println("Start saving notes"); //$NON-NLS-1$

        savingNotes = true;
        ITopic topic = currentTopicPart.getTopic();
        ICommandStack cs = getCommandStack();
        if (cs != null) {
            doSaveNotes(topic, cs);
        } else {
            forceSaveNotes(topic);
        }
        ((TopicNotesViewer) viewer).resetModified();
        savingNotes = false;

        deactivateJob();

        if (DEBUG)
            System.out.println("End saving notes"); //$NON-NLS-1$
    }

    private ICommandStack getCommandStack() {
        EditDomain domain = currentTopicPart.getSite().getViewer()
                .getEditDomain();
        if (domain != null) {
            return domain.getCommandStack();
        }
        return null;
    }

    private void doSaveNotes(ITopic topic, ICommandStack cs) {
        ModifyNotesCommand modifyHtml = new ModifyNotesCommand(topic,
                adapter.makeNewHtmlContent(), INotes.HTML);
        ModifyNotesCommand modifyPlain = new ModifyNotesCommand(topic,
                adapter.makeNewPlainContent(), INotes.PLAIN);
        CompoundCommand cmd = new CompoundCommand(modifyHtml, modifyPlain);
        cmd.setLabel(CommandMessages.Command_ModifyNotes);
        cs.execute(cmd);
    }

    private void forceSaveNotes(ITopic topic) {
        INotes notes = topic.getNotes();
        notes.setContent(INotes.HTML, adapter.makeNewHtmlContent());
        notes.setContent(INotes.PLAIN, adapter.makeNewPlainContent());
    }

    protected void setFocus() {
        super.setFocus();
        if (viewer instanceof TopicNotesViewer) {
            ((TopicNotesViewer) viewer).getImplementation().getFocusControl()
                    .setFocus();
        } else if (viewer instanceof SheetNotesViewer) {
            viewer.getControl().setFocus();
        }
    }

    private void updateViewer(Object input) {
        if (contentArea == null || contentArea.isDisposed()) {
            return;
        }
        contentArea.setRedraw(false);
        contentArea.setEnabled(true);

        RichDocumentNotesAdapter oldAdapter = this.adapter;
        if (viewer instanceof TopicNotesViewer && input instanceof ITopic) {
            unhookDocument();
            this.adapter = createNotesAdapter();
            viewer.setInput(adapter);
            hookDocument();
        } else if (viewer instanceof SheetNotesViewer
                && (input instanceof ISheet || input instanceof IRelationship
                        || input instanceof IBoundary)) {
            viewer.setInput(input);
            ((SheetNotesViewer) viewer).setEditor(contributingEditor);
        } else {
            if (viewer instanceof TopicNotesViewer) {
                removeSpellChecker();
                deactivateHandlers();
                if (((TopicNotesViewer) viewer).getImplementation() != null) {
                    ((TopicNotesViewer) viewer).getImplementation()
                            .removePostSelectionChangedListener(this);
                }
                unhookDocument();
            }
            if (viewer instanceof SheetNotesViewer) {
                ((SheetNotesViewer) viewer).setEditor(null);
            }
            if (viewer != null) {
                viewer.dispose();
                viewer = null;
            }

            if (input instanceof ITopic) {
                viewer = new TopicNotesViewer(topicViewerContributor);
                viewer.createControl(contentArea);
                this.adapter = createNotesAdapter();
                viewer.setInput(adapter);
                addSpellChecker();
                activateHandlers();
                ((TopicNotesViewer) viewer).getImplementation()
                        .addPostSelectionChangedListener(this);
                hookDocument();

                new ContextActivator(((TopicNotesViewer) viewer)
                        .getImplementation().getFocusControl());

            } else if (input instanceof ISheet || input instanceof IBoundary
                    || input instanceof IRelationship) {
                viewer = new SheetNotesViewer(contributingEditor);
                viewer.createControl(contentArea);
                viewer.setInput(input);
            } else {
                contentArea.setEnabled(false);
            }
        }
        contentArea.setRedraw(true);
        contentArea.layout(true, true);

        if (oldAdapter != null) {
            oldAdapter.dispose();
        }
        update();

        unregisterTextActionHandlers();
        registerGlobalTextActionHandlers();
        registerRichTextActionHandlers();
    }

    private void hookDocument() {
        IRichDocument document = ((TopicNotesViewer) viewer).getImplementation()
                .getDocument();
        if (document != null) {
            document.addDocumentListener(this);
            document.addRichDocumentListener(this);
            if (DEBUG)
                System.out.println("Document hooked"); //$NON-NLS-1$
        }
    }

    private void unhookDocument() {
        IRichDocument document = ((TopicNotesViewer) viewer).getImplementation()
                .getDocument();
        if (document != null) {
            document.removeDocumentListener(this);
            document.removeRichDocumentListener(this);
            if (DEBUG)
                System.out.println("Document unhooked"); //$NON-NLS-1$
        }
    }

    private void addSpellChecker() {
        spellingActivation = SpellingSupport.getInstance()
                .activateSpelling(((TopicNotesViewer) viewer)
                        .getImplementation().getTextViewer());
        spellingPreferences = SpellingPlugin.getDefault().getPreferenceStore();
        if (spellingPreferences != null) {
            spellingPreferences.addPropertyChangeListener(this);
        }
    }

    private void removeSpellChecker() {
        if (spellingPreferences != null) {
            spellingPreferences.removePropertyChangeListener(this);
            spellingPreferences = null;
        }
        if (spellingActivation != null) {
            spellingActivation.getSpellingSupport()
                    .deactivateSpelling(spellingActivation);
            spellingActivation = null;
        }
    }

    private void activateHandlers() {
        handlerService.activateHandler("org.xmind.ui.command.commitNotes", //$NON-NLS-1$
                commitNotesHandler);
    }

    private void deactivateHandlers() {
        handlerService.deactivateHandler("org.xmind.ui.command.commitNotes", //$NON-NLS-1$
                commitNotesHandler);
    }

    private void update() {
        if (updating)
            return;

        updating = true;
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                updateJob();
                updateTextActions();
                updating = false;
            }

        });
    }

    private void updateJob() {
        if (!(viewer instanceof TopicNotesViewer)
                || viewer.getControl().isDisposed())
            return;

        if (((TopicNotesViewer) viewer).hasModified()) {
            activateJob();
        } else {
            deactivateJob();
        }
    }

    private void activateJob() {
        if (saveNotesReg != null && saveNotesReg.isValid())
            return;

        saveNotesReg = null;
        IWorkbook workbook = (IWorkbook) contributingEditor
                .getAdapter(IWorkbook.class);
        if (workbook instanceof ICoreEventSource2) {
            saveNotesReg = ((ICoreEventSource2) workbook)
                    .registerOnceCoreEventListener(Core.WorkbookPreSaveOnce,
                            new SaveNotesJob());
            if (DEBUG)
                System.out.println("Job acitvated"); //$NON-NLS-1$
        }
    }

    private void deactivateJob() {
        if (saveNotesReg != null) {
            saveNotesReg.unregister();
            saveNotesReg = null;
        }
    }

    private void updateTextActions() {
        if (!(viewer instanceof TopicNotesViewer)
                || viewer.getControl().isDisposed() || textActions == null
                || textActions.isEmpty())
            return;
        TextViewer textViewer = ((TopicNotesViewer) viewer).getImplementation()
                .getTextViewer();
        if (textViewer != null) {
            for (TextAction action : textActions) {
                action.update(textViewer);
            }
        }
    }

    private void forceRefreshViewer() {
        RichDocumentNotesAdapter oldAdapter = this.adapter;
        if (viewer != null && !viewer.getControl().isDisposed()) {
            this.adapter = createNotesAdapter();
            if (DEBUG)
                if (adapter != null)
                    System.out.println("New adapter created"); //$NON-NLS-1$
            unhookDocument();
            viewer.setInput(adapter);
            if (adapter != null) {
                hookDocument();
            }
        } else {
            this.adapter = null;
        }
        if (oldAdapter != null) {
            oldAdapter.dispose();
            if (DEBUG)
                System.out.println("Old adapter disposed"); //$NON-NLS-1$
        }
        update();
    }

    private RichDocumentNotesAdapter createNotesAdapter() {
        if (currentTopicPart == null)
            return null;
        ITopic topic = currentTopicPart.getTopic();
        return new RichDocumentNotesAdapter(topic);
    }

    public IWorkbenchPart getContributingPart() {
        return contributingEditor;
    }

    public void handleCoreEvent(final CoreEvent event) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                String eventType = event.getType();
                if (Core.TopicNotes.equals(eventType)) {
                    handleNotesChanged();
                }
            }
        });
    }

    private void handleNotesChanged() {
        if (savingNotes)
            return;

        forceRefreshViewer();
    }

    public void partActivated(IWorkbenchPart part) {
        if (DEBUG)
            System.out.println("Part activated: " + part); //$NON-NLS-1$
        MPart modelPart = (MPart) getAdapter(MPart.class);
        Object e4Wrapper = modelPart.getTransientData()
                .get(E4PartWrapper.E4_WRAPPER_KEY);
        if (part == e4Wrapper || !(part instanceof IEditorPart))
            return;

        if (part instanceof IGraphicalEditor) {
            setContributingEditor((IGraphicalEditor) part);
        }
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (DEBUG)
            System.out.println("Part closed: " + part); //$NON-NLS-1$
        if (part == this.contributingEditor) {
            setContributingEditor(null);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
        if (DEBUG)
            System.out.println("Part deactivated: " + part); //$NON-NLS-1$
        MPart modelPart = (MPart) getAdapter(MPart.class);
        Object e4Wrapper = modelPart.getTransientData()
                .get(E4PartWrapper.E4_WRAPPER_KEY);
        if (part == e4Wrapper) {
            saveNotes();
        }
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    public void documentChanged(DocumentEvent event) {
        update();
    }

    public void selectionChanged(SelectionChangedEvent event) {
        update();
    }

    public void imageChanged(IRichDocument document,
            ImagePlaceHolder[] oldImages, ImagePlaceHolder[] newImages) {
        update();
    }

    public void lineStyleChanged(IRichDocument document,
            LineStyle[] oldLineStyles, LineStyle[] newLineStyles) {
        update();
    }

    public void textStyleChanged(IRichDocument document,
            StyleRange[] oldTextStyles, StyleRange[] newTextStyles) {
        update();
    }

    public void hyperlinkChanged(IRichDocument document,
            Hyperlink[] oldHyperlinks, Hyperlink[] newHyperlinks) {
        update();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
     * .jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (SpellingPlugin.SPELLING_CHECK_ENABLED.equals(event.getProperty())) {
            if (spellingActivation != null) {
                spellingActivation.getSpellingSupport()
                        .deactivateSpelling(spellingActivation);
                spellingActivation = null;
            }
            if (spellingPreferences
                    .getBoolean(SpellingPlugin.SPELLING_CHECK_ENABLED)) {
                spellingActivation = SpellingSupport.getInstance()
                        .activateSpelling(((TopicNotesViewer) viewer)
                                .getImplementation().getTextViewer());
            }
        }
    }

    public void dispose() {
        deactivateHandlers();
        removeSpellChecker();

        setCurrentTopicPart(null);
        workbenchWindow.getActivePage().removePartListener(this);
        setContributingEditor(null);
        unregisterTextActionHandlers();

        handlerService = null;
        super.dispose();

        if (findReplaceAction != null) {
            findReplaceAction.dispose();
            findReplaceAction = null;
        }
        textActions = null;
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        if (viewer != null) {
            viewer.dispose();
            viewer = null;
        }
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        } else if (adapter == IFindReplaceOperationProvider.class) {
            if (notesOperationProvider == null) {
                notesOperationProvider = new NotesFindReplaceOperationProvider(
                        this);
            }
            return notesOperationProvider;
        } else if (adapter == IFindReplaceTarget.class) {
            if (viewer instanceof TopicNotesViewer) {
                IRichTextEditViewer rtViewer = ((TopicNotesViewer) viewer)
                        .getImplementation();
                if (rtViewer != null) {
                    TextViewer textViewer = rtViewer.getTextViewer();
                    if (textViewer != null)
                        return textViewer.getFindReplaceTarget();
                }
            }
        } else if (adapter == ITextViewer.class) {
            if (viewer instanceof TopicNotesViewer) {
                IRichTextEditViewer rtViewer = ((TopicNotesViewer) viewer)
                        .getImplementation();
                if (rtViewer != null)
                    return rtViewer.getTextViewer();
            }
        } else if (adapter == IRichTextEditViewer.class) {
            if (viewer instanceof TopicNotesViewer) {
                return ((TopicNotesViewer) viewer).getImplementation();
            }
        } else if (adapter == ITopicPart.class) {
            return currentTopicPart;
        } else if (adapter == ITopic.class) {
            return currentTopicPart == null ? null
                    : currentTopicPart.getTopic();
        }
        return super.getAdapter(adapter);
    }

}
