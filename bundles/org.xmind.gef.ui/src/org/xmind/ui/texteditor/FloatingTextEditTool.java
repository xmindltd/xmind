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
package org.xmind.ui.texteditor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CommandStackBase;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStack2;
import org.xmind.gef.command.ICommandStackDelegate;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.EditTool;
import org.xmind.gef.tool.PartTextSelection;
import org.xmind.ui.viewers.SWTUtils;

public abstract class FloatingTextEditTool extends EditTool {

    private static final boolean DEBUG = false;

    private class TextCommandStackDelegate extends CommandStackBase
            implements ICommandStackDelegate {

        public boolean canExecute(Command command) {
            return false;
        }

        public boolean canRedo() {
            return FloatingTextEditTool.this.canRedo();
        }

        public boolean canUndo() {
            //return FloatingTextEditTool.this.canUndo();
            return true;
        }

        public void clear() {
        }

        public void execute(Command command) {
        }

        public String getRedoLabel() {
            return FloatingTextEditTool.this.getRedoLabel();
        }

        public String getUndoLabel() {
            return FloatingTextEditTool.this.getUndoLabel();
        }

        public void redo() {
            FloatingTextEditTool.this.redo();
        }

        public void undo() {
            FloatingTextEditTool.this.undo();
        }

        public void fireUpdate() {
            fireEvent(GEF.CS_UPDATED);
        }

    }

    private class EditorListener extends IFloatingTextEditorListener.Stub
            implements Listener {

        public void editingCanceled(TextEvent e) {
            if (closingFromTool)
                return;
            closingFromEditor = true;
            cancelEditing();
            closingFromEditor = false;
        }

        public void editingFinished(TextEvent e) {
            if (closingFromTool)
                return;
            closingFromEditor = true;
            finishEditing();
            closingFromEditor = false;
        }

        public void textChanged(TextEvent e) {
            Display.getCurrent().asyncExec(new Runnable() {

                public void run() {
                    if (!getStatus().isStatus(GEF.ST_ACTIVE))
                        return;
                    updateCommandActions();
                }
            });
        }

        public void handleEvent(Event event) {
            if (event.type == SWT.Dispose) {
                uninstallCommandStackDelegate();
            } else if (event.type == SWT.FocusOut) {
                final Shell oldShell = getTargetViewer().getControl()
                        .getShell();
                final Display display = event.display;
                display.asyncExec(new Runnable() {

                    public void run() {
                        if (!getStatus().isStatus(GEF.ST_ACTIVE)
                                || oldShell.isDisposed())
                            return;

                        if (!display.isDisposed()) {
                            display.asyncExec(new Runnable() {

                                public void run() {
                                    if (display.isDisposed()
                                            || oldShell.isDisposed()
                                            || !getStatus()
                                                    .isStatus(GEF.ST_ACTIVE))
                                        return;

                                    Shell newShell = display.getActiveShell();
                                    if (newShell != null
                                            && !newShell.isDisposed()) {
                                        finishEditing();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private class EditorSelectionChangedListener
            implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            notifySelectionChange();
        }

    }

    private FloatingTextEditor editor = null;

    private boolean closingFromEditor = false;

    private boolean closingFromTool = false;

    private EditorListener editorListener = null;

    private ISelectionChangedListener editorSelectionChangedListener;

    private boolean notifyingSelectionChange = false;

    private TextCommandStackDelegate delegate = null;

    private ICommandStackDelegate oldDelegate = null;

    private boolean focusOnRequest = true;

    public FloatingTextEditTool() {
        this(false);
    }

    public FloatingTextEditTool(boolean listensToSelectionChange) {
        if (listensToSelectionChange) {
            editorSelectionChangedListener = new EditorSelectionChangedListener();
        } else {
            editorSelectionChangedListener = null;
        }
    }

    public FloatingTextEditor getEditor() {
        return editor;
    }

    public ITextSelection getTextSelection() {
        ISelection editorSelection = editor == null ? null
                : editor.getSelection();
        if (editorSelection instanceof ITextSelection) {
            ITextSelection s = (ITextSelection) editorSelection;
            PartTextSelection realSelection = new PartTextSelection(getSource(),
                    (IDocument) editor.getInput(), s.getOffset(),
                    s.getLength());
            return realSelection;
        }
        return null;
    }

    public void setTextSelection(ITextSelection selection) {
        if (notifyingSelectionChange)
            return;

        if (selection != null) {
            editor.setSelection(selection, true);
        } else {
            cancelEditing();
        }
    }

    protected void notifySelectionChange() {
        if (editorSelectionChangedListener == null)
            return;
        notifyingSelectionChange = true;
        getTargetViewer().setSelection(getTextSelection(), false);
        notifyingSelectionChange = false;
    }

    @Override
    protected void handleEditRequest(Request request) {
        focusOnRequest = !request.hasParameter(GEF.PARAM_FOCUS)
                || request.isParameter(GEF.PARAM_FOCUS);
        super.handleEditRequest(request);
        if (getDomain().getActiveTool() == this) {
            Object text = request.getParameter(GEF.PARAM_TEXT);
            if (text instanceof String) {
                if (editor != null && !editor.isClosed()) {
                    editor.replaceText((String) text);
                }
            }
            Object selection = request.getParameter(GEF.PARAM_TEXT_SELECTION);
            if (selection instanceof ITextSelection) {
                setTextSelection((ITextSelection) selection);
            }
            if (focusOnRequest && editor != null && !editor.isClosed()) {
                editor.setFocus();
            }
        }
    }

    protected boolean startEditing(IGraphicalEditPart source) {
        IDocument document = getTextContents(source);
        if (document == null)
            return false;

        if (editor == null) {
            editor = createEditor();
        }
        if (editor == null)
            return false;

        hookEditor(editor);
        boolean started = openEditor(editor, document);
        if (started) {
            notifySelectionChange();
        }
        return started;
    }

    protected void installCommandStackDelegate() {
        ICommandStack cs = getDomain().getCommandStack();
        if (cs != null && cs instanceof ICommandStack2) {
            delegate = new TextCommandStackDelegate();
            ICommandStack2 cs2 = (ICommandStack2) cs;
            oldDelegate = cs2.getDelegate();
            cs2.setDelegate(delegate);
        }
    }

    protected void uninstallCommandStackDelegate() {
        if (delegate != null) {
            ICommandStack cs = getDomain().getCommandStack();
            if (cs != null && cs instanceof ICommandStack2) {
                ICommandStack2 cs2 = (ICommandStack2) cs;
                cs2.setDelegate(oldDelegate);
            }
            oldDelegate = null;
            delegate = null;
        }
    }

    protected void updateCommandActions() {
        if (delegate != null) {
            delegate.fireUpdate();
        }
    }

    protected boolean openEditor(FloatingTextEditor editor,
            IDocument document) {
        boolean wasOpen = !editor.isClosed();
        editor.setInput(document);
        boolean isOpen = editor.open(false);
        if (isOpen) {
            if (editor.canDoOperation(ITextOperationTarget.SELECT_ALL)) {
                editor.doOperation(ITextOperationTarget.SELECT_ALL);
            }
            if (!wasOpen) {
                hookEditorControl(editor, editor.getTextViewer());
            }
        }
        return isOpen;
    }

    protected void hookEditorControl(FloatingTextEditor editor,
            ITextViewer textViewer) {
        installCommandStackDelegate();
        textViewer.getTextWidget().addListener(SWT.FocusOut,
                getEditorListener());
        textViewer.getTextWidget().addListener(SWT.Dispose,
                getEditorListener());
    }

    protected void cancelEditing() {
        if (editor != null) {
            if (closingFromEditor) {
                unhookEditor(editor);
            } else if (!editor.isClosed()) {
                closingFromTool = true;
                closeEditor(editor, false);
                closingFromTool = false;
            }
            editor = null;
        }
        super.cancelEditing();
        notifySelectionChange();
    }

    protected void finishEditing() {
        if (DEBUG)
            System.out.println("Finish Editing"); //$NON-NLS-1$
        if (editor != null) {
            if (closingFromEditor) {
                unhookEditor(editor);
            } else if (!editor.isClosed()) {
                closingFromTool = true;
                if (DEBUG)
                    System.out.println("Close editor"); //$NON-NLS-1$
                closeEditor(editor, true);
                closingFromTool = false;
            }
            Object input = editor.getInput();
            if (input instanceof IDocument) {
                IDocument document = (IDocument) input;
                if (DEBUG)
                    System.out.println("Perform text modification"); //$NON-NLS-1$
                handleTextModified(getSource(), document);
            }
            editor = null;
        }
        super.finishEditing();
        notifySelectionChange();
    }

    protected void closeEditor(FloatingTextEditor editor, boolean finish) {
        unhookEditor(editor);
        editor.close(finish);
    }

    protected abstract IDocument getTextContents(IPart source);

    protected abstract void handleTextModified(IPart source,
            IDocument document);

    protected FloatingTextEditor createEditor() {
        int style = SWT.BORDER | SWT.V_SCROLL
                | (isMultilineAllowed() ? SWT.MULTI : SWT.SINGLE);
        if (isWrapAllowed()) {
            style |= SWT.WRAP;
        } else {
            style |= SWT.H_SCROLL;
        }
        FloatingTextEditor editor = new FloatingTextEditor(
                getTargetViewer().getCanvas(), style);
        return editor;
    }

    protected void hookEditor(FloatingTextEditor editor) {
        editor.addFloatingTextEditorListener(getEditorListener());
        if (editorSelectionChangedListener != null)
            editor.addSelectionChangedListener(editorSelectionChangedListener);
    }

    protected void unhookEditor(FloatingTextEditor editor) {
        if (editorSelectionChangedListener != null)
            editor.removeSelectionChangedListener(
                    editorSelectionChangedListener);
        editor.removeFloatingTextEditorListener(getEditorListener());
        Display.getCurrent().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (focusOnRequest) {
                    restoreFocusControl();
                }
            }
        });
    }

    private EditorListener getEditorListener() {
        if (editorListener == null)
            editorListener = new EditorListener();
        return editorListener;
    }

    protected boolean isMultilineAllowed() {
        return false;
    }

    protected boolean isWrapAllowed() {
        return false;
    }

    protected void selectAll() {
        if (editor != null
                && editor.canDoOperation(FloatingTextEditor.SELECT_ALL)) {
            editor.doOperation(FloatingTextEditor.SELECT_ALL);
        }
    }

    protected void copy() {
        if (editor != null && editor.canDoOperation(FloatingTextEditor.COPY)) {
            editor.doOperation(FloatingTextEditor.COPY);
        }
    }

    protected void cut() {
        if (editor != null && editor.canDoOperation(FloatingTextEditor.CUT)) {
            editor.doOperation(FloatingTextEditor.CUT);
        }
    }

    protected void delete() {
        if (editor != null
                && editor.canDoOperation(FloatingTextEditor.DELETE)) {
            editor.doOperation(FloatingTextEditor.DELETE);
        }
    }

    protected void paste() {
        if (editor != null && editor.canDoOperation(FloatingTextEditor.PASTE)) {
            editor.doOperation(FloatingTextEditor.PASTE);
        }
    }

    protected void undo() {
        if (editor != null && editor.canDoOperation(FloatingTextEditor.UNDO))
            editor.doOperation(FloatingTextEditor.UNDO);
    }

    protected void redo() {
        if (editor != null && editor.canDoOperation(FloatingTextEditor.REDO))
            editor.doOperation(FloatingTextEditor.REDO);
    }

    public boolean canUndo() {
        return editor != null && editor.canDoOperation(FloatingTextEditor.UNDO);
    }

    public boolean canRedo() {
        return editor != null && editor.canDoOperation(FloatingTextEditor.REDO);
    }

    protected abstract String getUndoLabel();

    protected abstract String getRedoLabel();

    protected boolean shouldFinish(KeyEvent ke) {
        return SWTUtils.matchKey(ke.getState(), ke.keyCode, 0, SWT.CR);
    }

    protected boolean shouldCancel(KeyEvent ke) {
        return SWTUtils.matchKey(ke.getState(), ke.keyCode, 0, SWT.ESC);
    }

    protected void restoreFocusControl() {
        IGraphicalViewer viewer = getTargetViewer();
        if (viewer == null)
            return;

        Control control = viewer.getControl();
        if (control == null || control.isDisposed())
            return;

        control.setFocus();
    }
}
