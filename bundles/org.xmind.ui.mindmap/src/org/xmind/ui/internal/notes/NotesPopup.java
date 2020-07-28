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
package org.xmind.ui.internal.notes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.keys.IBindingService;
import org.xmind.core.Core;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyNotesCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.editor.MindMapEditor;
import org.xmind.ui.internal.spellsupport.SpellingSupport;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.richtext.Hyperlink;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.IRichDocumentListener;
import org.xmind.ui.richtext.IRichTextAction;
import org.xmind.ui.richtext.IRichTextEditViewer;
import org.xmind.ui.richtext.ImagePlaceHolder;
import org.xmind.ui.richtext.LineStyle;
import org.xmind.ui.richtext.SimpleRichTextActionBarContributor;
import org.xmind.ui.richtext.TextActionConstants;
import org.xmind.ui.texteditor.IMenuContributor;
import org.xmind.ui.texteditor.ISpellingActivation;

public class NotesPopup extends PopupDialog implements IDocumentListener,
        IRichDocumentListener, ISelectionChangedListener {

    private static final String CONTEXT_ID = "org.xmind.ui.context.notesPopup"; //$NON-NLS-1$

    private static final String CMD_GOTO_NOTES_VIEW = "org.xmind.ui.command.gotoNotesView"; //$NON-NLS-1$

    private static final String CMD_COMMIT_NOTES = "org.xmind.ui.command.commitNotes"; //$NON-NLS-1$

    private class TextAction extends Action {
        private int op;

        private TextViewer textViewer;

        public TextAction(int op) {
            this.op = op;
        }

        public void run() {
            if (textViewer == null) {
                textViewer = notesViewer.getImplementation().getTextViewer();
            }
            if (textViewer != null) {
                if (textViewer.canDoOperation(op)) {
                    textViewer.doOperation(op);
                }
            }
        }

        public void update(TextViewer textViewer) {
            setEnabled(textViewer.canDoOperation(op));
        }
    }

    private class NotesPopupActionBarContributor
            extends SimpleRichTextActionBarContributor {

        private Map<String, TextAction> textActions = new HashMap<String, TextAction>(
                10);

        private Map<String, IAction> actionHandlers = new HashMap<String, IAction>(
                10);

        private Collection<String> textCommandIds = new HashSet<String>(10);

        private class GotoNotesPartAction extends Action {
            public GotoNotesPartAction() {
                super(MindMapMessages.EditInNotesView_text);
                setToolTipText(MindMapMessages.EditInNotesView_toolTip);
                setImageDescriptor(
                        MindMapUI.getImages().get("notes_part.png", true)); //$NON-NLS-1$
                setDisabledImageDescriptor(
                        MindMapUI.getImages().get("notes_part.png", false)); //$NON-NLS-1$
            }

            public void run() {
                gotoNotesPart();
            }
        }

        protected void makeActions(IRichTextEditViewer viewer) {
            super.makeActions(viewer);

            addWorkbenchAction(ActionFactory.UNDO, ITextOperationTarget.UNDO);
            addWorkbenchAction(ActionFactory.REDO, ITextOperationTarget.REDO);
            addWorkbenchAction(ActionFactory.CUT, ITextOperationTarget.CUT);
            addWorkbenchAction(ActionFactory.COPY, ITextOperationTarget.COPY);
            addWorkbenchAction(ActionFactory.PASTE, ITextOperationTarget.PASTE);
            addWorkbenchAction(ActionFactory.SELECT_ALL,
                    ITextOperationTarget.SELECT_ALL);

            registerTextCommand(TextActionConstants.BOLD_ID,
                    "org.xmind.ui.command.text.bold"); //$NON-NLS-1$
            registerTextCommand(TextActionConstants.ITALIC_ID,
                    "org.xmind.ui.command.text.italic"); //$NON-NLS-1$
            registerTextCommand(TextActionConstants.UNDERLINE_ID,
                    "org.xmind.ui.command.text.underline"); //$NON-NLS-1$
            registerTextCommand(TextActionConstants.LEFT_ALIGN_ID,
                    "org.xmind.ui.command.text.leftAlign"); //$NON-NLS-1$
            registerTextCommand(TextActionConstants.CENTER_ALIGN_ID,
                    "org.xmind.ui.command.text.centerAlign"); //$NON-NLS-1$
            registerTextCommand(TextActionConstants.RIGHT_ALIGN_ID,
                    "org.xmind.ui.command.text.rightAlign"); //$NON-NLS-1$
        }

        private void addWorkbenchAction(ActionFactory factory, int textOp) {
            IWorkbenchAction action = factory.create(window);
            TextAction textAction = new TextAction(textOp);
            textAction.setId(action.getId());
            textAction.setActionDefinitionId(action.getActionDefinitionId());
            textAction.setText(action.getText());
            textAction.setToolTipText(action.getToolTipText());
            textAction.setDescription(action.getDescription());
            textAction.setImageDescriptor(action.getImageDescriptor());
            textAction.setDisabledImageDescriptor(
                    action.getDisabledImageDescriptor());
            textAction
                    .setHoverImageDescriptor(action.getHoverImageDescriptor());
            action.dispose();
            actionHandlers.put(action.getActionDefinitionId(), textAction);
            textActions.put(textAction.getId(), textAction);
        }

        private void registerTextCommand(String actionId, String commandId) {
            IRichTextAction action = getRichTextAction(actionId);
            if (action != null) {
                action.setActionDefinitionId(commandId);
                actionHandlers.put(commandId, action);
                textCommandIds.add(commandId);
            }
        }

        public void fillToolBar(IToolBarManager toolbar) {
            super.fillToolBar(toolbar);
            if (showGotoNotesPart) {
                toolbar.add(new Separator());
                toolbar.add(new GotoNotesPartAction());
            }
        }

        public void fillContextMenu(IMenuManager menu) {
            menu.add(getTextAction(ActionFactory.UNDO.getId()));
            menu.add(getTextAction(ActionFactory.REDO.getId()));
            menu.add(new Separator());
            menu.add(getTextAction(ActionFactory.CUT.getId()));
            menu.add(getTextAction(ActionFactory.COPY.getId()));
            menu.add(getTextAction(ActionFactory.PASTE.getId()));
            menu.add(new Separator());
            menu.add(getTextAction(ActionFactory.SELECT_ALL.getId()));
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
        public void dispose() {
            actionHandlers.clear();
            textActions.clear();
            super.dispose();
        }

        public void update(TextViewer textViewer) {
            for (TextAction action : textActions.values()) {
                action.update(textViewer);
            }
        }

        public IAction getActionHandler(String commandId) {
            return actionHandlers.get(commandId);
        }

        public IAction getTextAction(String actionId) {
            return textActions.get(actionId);
        }

        public Collection<String> getTextCommandIds() {
            return textCommandIds;
        }

    }

    private class PopupKeyboardListener implements Listener {

        private List<TriggerSequence> currentSequences = null;

        private int nextKeyIndex = -1;

        public void hook(Control control) {
            control.getDisplay().addFilter(SWT.KeyDown, this);
            control.getShell().addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    if (!e.display.isDisposed()) {
                        e.display.removeFilter(SWT.KeyDown,
                                PopupKeyboardListener.this);
                    }
                }
            });
        }

        public void handleEvent(Event event) {
            if (event.type == SWT.KeyDown) {
                handleKeyDown(event);
            }
            update();
        }

        private void handleKeyDown(Event event) {
            if (triggerableCommands.isEmpty())
                return;

            List<KeyStroke> keys = generateKeyStrokes(event);
            if (currentSequences == null) {
                nextKeyIndex = -1;
                for (TriggerSequence ts : triggerableCommands.keySet()) {
                    if (matches(keys, ts.getTriggers()[0])) {
                        if (currentSequences == null)
                            currentSequences = new ArrayList<TriggerSequence>(
                                    triggerableCommands.size());
                        currentSequences.add(ts);
                    }
                }
                if (currentSequences == null)
                    return;
            }

            if (nextKeyIndex < 0)
                nextKeyIndex = 0;
            Iterator<TriggerSequence> it = currentSequences.iterator();
            while (it.hasNext()) {
                TriggerSequence ts = it.next();
                Trigger[] triggers = ts.getTriggers();
                if (nextKeyIndex >= triggers.length) {
                    it.remove();
                } else {
                    if (matches(keys, triggers[nextKeyIndex])) {
                        if (nextKeyIndex == triggers.length - 1) {
                            if (triggerFound(ts)) {
                                event.doit = false;
                            }
                            return;
                        }
                    } else {
                        it.remove();
                    }
                }
            }
            if (currentSequences != null && currentSequences.isEmpty()) {
                nextKeyIndex++;
            } else {
                currentSequences = null;
                nextKeyIndex = -1;
            }
        }

        private boolean triggerFound(TriggerSequence triggerSequence) {
            currentSequences = null;
            nextKeyIndex = -1;
            String commandId = triggerableCommands.get(triggerSequence);
            if (commandId != null) {
                return handleCommand(commandId);
            }
            return false;
        }

        private boolean matches(List<KeyStroke> keys, Trigger expected) {
            for (KeyStroke key : keys) {
                if (key.equals(expected))
                    return true;
            }
            return false;
        }

        private List<KeyStroke> generateKeyStrokes(Event event) {
            final List<KeyStroke> keyStrokes = new ArrayList<KeyStroke>(3);

            /*
             * If this is not a keyboard event, then there are no key strokes.
             * This can happen if we are listening to focus traversal events.
             */
            if ((event.stateMask == 0) && (event.keyCode == 0)
                    && (event.character == 0)) {
                return keyStrokes;
            }

            // Add each unique key stroke to the list for consideration.
            final int firstAccelerator = SWTKeySupport
                    .convertEventToUnmodifiedAccelerator(event);
            keyStrokes.add(SWTKeySupport
                    .convertAcceleratorToKeyStroke(firstAccelerator));

            // We shouldn't allow delete to undergo shift resolution.
            if (event.character == SWT.DEL) {
                return keyStrokes;
            }

            final int secondAccelerator = SWTKeySupport
                    .convertEventToUnshiftedModifiedAccelerator(event);
            if (secondAccelerator != firstAccelerator) {
                keyStrokes.add(SWTKeySupport
                        .convertAcceleratorToKeyStroke(secondAccelerator));
            }

            final int thirdAccelerator = SWTKeySupport
                    .convertEventToModifiedAccelerator(event);
            if ((thirdAccelerator != secondAccelerator)
                    && (thirdAccelerator != firstAccelerator)) {
                keyStrokes.add(SWTKeySupport
                        .convertAcceleratorToKeyStroke(thirdAccelerator));
            }

            return keyStrokes;
        }

    }

    private IWorkbenchWindow window;

    private ITopicPart topicPart;

    private TopicNotesViewer notesViewer;

    private NotesPopupActionBarContributor contributor;

    private RichDocumentNotesAdapter notesAdapter;

    private Map<TriggerSequence, String> triggerableCommands = new HashMap<TriggerSequence, String>(
            3);

    private IContextActivation contextActivation;

    private IContextService contextService;

    private IBindingService bindingService;

    private boolean showGotoNotesPart;

    private boolean editable;

    private boolean updating = false;

    private ISpellingActivation spellingActivation;

    private ICoreEventRegistration saveNotesReg;

    public NotesPopup(IWorkbenchWindow window, ITopicPart topicPart,
            boolean editable, boolean showGotoNotesView) {
        super(window.getShell(), SWT.RESIZE, true, true, true, false, false,
                null, showGotoNotesView ? "" : null); //$NON-NLS-1$
        this.window = window;
        this.topicPart = topicPart;
        this.showGotoNotesPart = showGotoNotesView;
        this.editable = editable;
    }

    public NotesPopup(Shell parentShell, ITopicPart topicPart,
            boolean editable) {
        super(parentShell, SWT.Resize, true, true, true, false, false, null,
                null);
        this.topicPart = topicPart;
        this.window = null;
        this.showGotoNotesPart = false;
        this.editable = editable;
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        notesViewer = new TopicNotesViewer();
        if (editable) {
            notesViewer.setContributor(
                    contributor = new NotesPopupActionBarContributor());
        }
        int style = IRichTextEditViewer.DEFAULT_CONTROL_STYLE;
        if (!editable) {
            style |= SWT.READ_ONLY;
        }
        notesViewer.createControl(composite, style);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 400;
        notesViewer.getControl().setLayoutData(gridData);

        ITopic topic = topicPart.getTopic();

        notesAdapter = new RichDocumentNotesAdapter(topic);

        notesViewer.setInput(notesAdapter);

        notesViewer.getControl().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (notesAdapter != null) {
                    notesAdapter.dispose();
                    notesAdapter = null;
                }
            }
        });
        notesViewer.getImplementation().addSelectionChangedListener(this);
        notesViewer.getImplementation().getDocument().addDocumentListener(this);
        notesViewer.getImplementation().getDocument()
                .addRichDocumentListener(this);
        new PopupKeyboardListener()
                .hook(notesViewer.getImplementation().getFocusControl());
        update();
        addSpellCheck();
        return composite;
    }

    private void addSpellCheck() {
        spellingActivation = SpellingSupport.getInstance().activateSpelling(
                notesViewer.getImplementation().getTextViewer());
    }

    public TopicNotesViewer getNotesViewer() {
        return notesViewer;
    }

    public RichDocumentNotesAdapter getNotesAdapter() {
        return notesAdapter;
    }

    public ITopicPart getTopicPart() {
        return topicPart;
    }

    protected Control getFocusControl() {
        return notesViewer.getImplementation().getFocusControl();
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        IViewer viewer = topicPart.getSite().getViewer();
        Rectangle bounds = topicPart.getFigure().getBounds().getCopy();
        return calcInitialLocation((IGraphicalViewer) viewer, bounds);
    }

    private Point calcInitialLocation(IGraphicalViewer viewer,
            Rectangle bounds) {
        ZoomManager zoom = viewer.getZoomManager();
        bounds = bounds.scale(zoom.getScale()).expand(1, 1)
                .translate(viewer.getScrollPosition().getNegated());
        return viewer.getControl().toDisplay(bounds.x,
                bounds.y + bounds.height);
    }

    @SuppressWarnings("unchecked")
    protected List getBackgroundColorExclusions() {
        List list = super.getBackgroundColorExclusions();
        collectBackgroundColorExclusions(notesViewer.getControl(), list);
        return list;
    }

    @SuppressWarnings("unchecked")
    private void collectBackgroundColorExclusions(Control control, List list) {
        list.add(control);
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                collectBackgroundColorExclusions(child, list);
            }
        }
    }

    protected IDialogSettings getDialogSettings() {
        return MindMapUIPlugin.getDefault()
                .getDialogSettings(MindMapUI.POPUP_DIALOG_SETTINGS_ID);
    }

    public int open() {
        IWorkbench workbench = window.getWorkbench();
        bindingService = (IBindingService) workbench
                .getAdapter(IBindingService.class);
        contextService = (IContextService) workbench
                .getAdapter(IContextService.class);
        if (bindingService != null) {
            registerWorkbenchCommands();
        }
        int ret = super.open();
        if (ret == OK) {
            if (contextService != null) {
                contextActivation = contextService.activateContext(CONTEXT_ID);
            }
            if (bindingService != null) {
                registerDialogCommands();
            }
        }
        activateJob();

        return ret;
    }

    private void activateJob() {
        if (saveNotesReg != null && saveNotesReg.isValid())
            return;

        saveNotesReg = null;
        if (window != null) {
            IEditorPart editorPart = window.getActivePage().getActiveEditor();
            if (editorPart != null && editorPart instanceof MindMapEditor) {
                IWorkbook workbook = ((MindMapEditor) editorPart).getWorkbook();
                if (workbook instanceof ICoreEventSource2) {
                    saveNotesReg = ((ICoreEventSource2) workbook)
                            .registerOnceCoreEventListener(
                                    Core.WorkbookPreSaveOnce,
                                    ICoreEventListener.NULL);
                }
            }

        }
    }

    protected void registerDialogCommands() {
        if (showGotoNotesPart) {
            TriggerSequence key = registerCommand(CMD_GOTO_NOTES_VIEW);
            if (key != null) {
                setInfoText(
                        NLS.bind(DialogMessages.NotesPopup_GotoNotesView_text,
                                key.format()));
            }
        }
        registerCommand(CMD_COMMIT_NOTES);
        for (String commandId : contributor.getTextCommandIds()) {
            registerCommand(commandId);
        }
    }

    protected void registerWorkbenchCommands() {
        registerCommand(IWorkbenchCommandConstants.FILE_SAVE);
        registerCommand(IWorkbenchCommandConstants.EDIT_UNDO);
        registerCommand(IWorkbenchCommandConstants.EDIT_REDO);
        registerCommand(IWorkbenchCommandConstants.EDIT_CUT);
        registerCommand(IWorkbenchCommandConstants.EDIT_COPY);
        registerCommand(IWorkbenchCommandConstants.EDIT_PASTE);
        registerCommand(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
    }

    protected TriggerSequence registerCommand(String commandId) {
        if (bindingService == null)
            return null;
        TriggerSequence key = bindingService.getBestActiveBindingFor(commandId);
        if (key != null) {
            triggerableCommands.put(key, commandId);
        }
        return key;
    }

    protected boolean handleCommand(String commandId) {
        if (CMD_GOTO_NOTES_VIEW.equals(commandId)) {
            if (showGotoNotesPart) {
                gotoNotesPart();
            }
            return true;
        } else if (CMD_COMMIT_NOTES.equals(commandId)) {
//            Display.getCurrent().asyncExec(new Runnable() {
//                public void run() {
            setReturnCode(OK);
            close();
//                }
//            });
            return true;
        } else if (IWorkbenchCommandConstants.FILE_SAVE.equals(commandId)) {
            saveNotes();
            return true;
        }
        IAction action = contributor.getActionHandler(commandId);
        if (action != null && action.isEnabled()) {
            if (action.getStyle() == IAction.AS_CHECK_BOX) {
                action.setChecked(!action.isChecked());
            }
            action.run();
            return true;
        }
        return false;
    }

    public boolean close() {
        if (contextActivation != null && contextService != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
        deactivateJob();
        if (getReturnCode() == OK)
            saveNotes();
        return super.close();
    }

    private void deactivateJob() {
        if (saveNotesReg != null) {
            saveNotesReg.unregister();
            saveNotesReg = null;
        }
    }

    private void saveNotes() {
        if (notesAdapter == null || notesViewer == null
                || notesViewer.getControl().isDisposed()
                || !notesViewer.hasModified())
            return;

        doSaveNotes();
        notesViewer.resetModified();
    }

    private void doSaveNotes() {
        INotesContent html = notesAdapter.makeNewHtmlContent();
        INotesContent plain = notesAdapter.makeNewPlainContent();
        ITopic topic = topicPart.getTopic();
        EditDomain domain = topicPart.getSite().getViewer().getEditDomain();
        if (domain != null) {
            ICommandStack cs = domain.getCommandStack();
            if (cs != null) {
                ModifyNotesCommand modifyHtml = new ModifyNotesCommand(topic,
                        html, INotes.HTML);
                ModifyNotesCommand modifyPlain = new ModifyNotesCommand(topic,
                        plain, INotes.PLAIN);
                CompoundCommand cmd = new CompoundCommand(modifyHtml,
                        modifyPlain);
                cmd.setLabel(CommandMessages.Command_ModifyNotes);
                cs.execute(cmd);
                return;
            }
        }

        INotes notes = topic.getNotes();
        notes.setContent(INotes.HTML, html);
        notes.setContent(INotes.PLAIN, plain);
    }

    private void gotoNotesPart() {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (window == null) {
                    return;
                }
                close();

                E4Utils.showPart(IModelConstants.COMMAND_SHOW_MODEL_PART,
                        window, IModelConstants.PART_ID_NOTES, null,
                        IModelConstants.PART_STACK_ID_RIGHT);
            }
        });
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    public void documentChanged(DocumentEvent event) {
        update();
    }

    public void hyperlinkChanged(IRichDocument document,
            Hyperlink[] oldHyperlinks, Hyperlink[] newHyperlinks) {
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

    public void selectionChanged(SelectionChangedEvent event) {
        update();
    }

    private void update() {
        if (updating)
            return;
        updating = true;
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                updateTextActions();
                updating = false;
            }

        });
        doSaveNotes();
    }

    private void updateTextActions() {
        if (notesViewer == null || notesViewer.getControl().isDisposed()
                || contributor == null)
            return;
        TextViewer textViewer = notesViewer.getImplementation().getTextViewer();
        if (textViewer != null) {
            contributor.update(textViewer);
        }
    }
}
