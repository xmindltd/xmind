/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.richtext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IContextReplacingOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.text.undo.IDocumentUndoListener;

/**
 * @author Frank Shaka
 */
public class RichDocumentUndoManager implements IRichDocumentUndoManager {

    /**
     * Represents an undo-able text change, described as the replacement of some
     * preserved text with new text.
     * <p>
     * Based on the DefaultUndoManager.TextCommand from R3.1.
     * </p>
     */
    private static class UndoableRichTextChange extends AbstractOperation {

        /** The start index of the replaced text. */
        protected int fStart = -1;

        /** The end index of the replaced text. */
        protected int fEnd = -1;

        /** The newly inserted text. */
        protected String fText;

        /** The replaced text. */
        protected String fPreservedText;

        /** The undo modification stamp. */
        protected long fUndoModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

        /** The redo modification stamp. */
        protected long fRedoModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

        /** The undo manager that generated the change. */
        protected RichDocumentUndoManager fDocumentUndoManager;

        protected StyleRange[] oldTextStyles;

        protected StyleRange[] newTextStyles;

        protected LineStyle[] oldLineStyles;

        protected LineStyle[] newLineStyles;

        protected ImagePlaceHolder[] oldImages;

        protected ImagePlaceHolder[] newImages;

        protected Hyperlink[] oldHyperlinks;

        protected Hyperlink[] newHyperlinks;

        /**
         * Creates a new text change.
         * 
         * @param manager
         *            the undo manager for this change
         */
        UndoableRichTextChange(RichDocumentUndoManager manager) {
            super(""); //$NON-NLS-1$
            this.fDocumentUndoManager = manager;
            addContext(manager.getUndoContext());
        }

        /**
         * Re-initializes this text change.
         */
        protected void reinitialize() {
            reinitializeTextChange();
            oldTextStyles = null;
            newTextStyles = null;
            oldLineStyles = null;
            newLineStyles = null;
            oldImages = null;
            newImages = null;

            oldHyperlinks = null;
            newHyperlinks = null;
        }

        protected void reinitializeTextChange() {
            fStart = fEnd = -1;
            fText = fPreservedText = null;
            fUndoModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
            fRedoModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
        }

        /**
         * Sets the start and the end index of this change.
         * 
         * @param start
         *            the start index
         * @param end
         *            the end index
         */
        protected void set(int start, int end) {
            fStart = start;
            fEnd = end;
            fText = null;
            fPreservedText = null;
        }

        /*
         * @see
         * org.eclipse.core.commands.operations.IUndoableOperation#dispose()
         */
        public void dispose() {
            reinitialize();
        }

        /**
         * Undo the change described by this change.
         */
        protected void undoTextChange() {
            if (fStart < 0 || fEnd < 0)
                return;
            try {
                if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4)
                    ((IDocumentExtension4) fDocumentUndoManager.fDocument)
                            .replace(fStart, fText.length(), fPreservedText,
                                    fUndoModificationStamp);
                else
                    fDocumentUndoManager.fDocument.replace(fStart, fText
                            .length(), fPreservedText);
            } catch (BadLocationException x) {
            }
        }

        protected void undoRichTextChange() {
//            fDocumentUndoManager.ignoreDocumentChange = true;
            if (oldTextStyles != null)
                fDocumentUndoManager.fDocument.setTextStyles(oldTextStyles);
            if (oldLineStyles != null)
                fDocumentUndoManager.fDocument.setLineStyles(oldLineStyles);
            if (oldImages != null)
                fDocumentUndoManager.fDocument.setImages(oldImages);
            if (oldHyperlinks != null)
                fDocumentUndoManager.fDocument.setHyperlinks(oldHyperlinks);
//            fDocumentUndoManager.ignoreDocumentChange = false;
        }

        /*
         * @see
         * org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
         */
        public boolean canUndo() {
            if (isValid()) {
                if (fStart >= 0 && fEnd >= 0 && fText != null) {
                    if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4) {
                        long docStamp = ((IDocumentExtension4) fDocumentUndoManager.fDocument)
                                .getModificationStamp();

                        // Normal case: an undo is valid if its redo will restore
                        // document to its current modification stamp
                        boolean canUndo = docStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP
                                || docStamp == getRedoModificationStamp();

                        /*
                         * Special case to check if the answer is false. If the
                         * last document change was empty, then the document's
                         * modification stamp was incremented but nothing was
                         * committed. The operation being queried has an older
                         * stamp. In this case only, the comparison is
                         * different. A sequence of document changes that
                         * include an empty change is handled correctly when a
                         * valid commit follows the empty change, but when
                         * #canUndo() is queried just after an empty change, we
                         * must special case the check. The check is very
                         * specific to prevent false positives. see
                         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98245
                         */
                        if (!canUndo
                                && this == fDocumentUndoManager.fHistory
                                        .getUndoOperation(fDocumentUndoManager.fUndoContext)
                                // this is the latest operation
                                && this != fDocumentUndoManager.fCurrent
                                // there is a more current operation not on the stack
                                && !fDocumentUndoManager.fCurrent.isValid()
                                // the current operation is not a valid document
                                // modification
                                && fDocumentUndoManager.fCurrent.fUndoModificationStamp !=
                                // the invalid current operation has a document stamp
                                IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
                            canUndo = fDocumentUndoManager.fCurrent.fRedoModificationStamp == docStamp;
                        }
                        /*
                         * When the composite is the current operation, it may
                         * hold the timestamp of a no-op change. We check this
                         * here rather than in an override of canUndo() in
                         * UndoableCompoundTextChange simply to keep all the
                         * special case checks in one place.
                         */
                        if (!canUndo
                                && this == fDocumentUndoManager.fHistory
                                        .getUndoOperation(fDocumentUndoManager.fUndoContext)
                                && // this is the latest operation
                                this instanceof UndoableCompoundRichTextChange
                                && this == fDocumentUndoManager.fCurrent && // this is the current operation
                                this.fStart == -1 && // the current operation text is not valid
                                fDocumentUndoManager.fCurrent.fRedoModificationStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
                            // but it has a redo stamp
                            canUndo = fDocumentUndoManager.fCurrent.fRedoModificationStamp == docStamp;
                        }

                    }
                }
                // if there is no timestamp to check, simply return true per the
                // 3.0.1 behavior
                return true;
            }
            return false;
        }

        /*
         * @see
         * org.eclipse.core.commands.operations.IUndoableOperation#canRedo()
         */
        public boolean canRedo() {
            if (isValid()) {
                if (fStart >= 0 && fEnd >= 0 && fText != null) {
                    if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4) {
                        long docStamp = ((IDocumentExtension4) fDocumentUndoManager.fDocument)
                                .getModificationStamp();
                        return docStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP
                                || docStamp == getUndoModificationStamp();
                    }
                }
                // if there is no timestamp to check, simply return true per the
                // 3.0.1 behavior
                return true;
            }
            return false;
        }

        /*
         * @see
         * org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
         */
        public boolean canExecute() {
            return fDocumentUndoManager.isConnected();
        }

        /*
         * @seeorg.eclipse.core.commands.operations.IUndoableOperation.
         * IUndoableOperation#execute(IProgressMonitor, IAdaptable)
         */
        public IStatus execute(IProgressMonitor monitor, IAdaptable uiInfo) {
            // Text changes execute as they are typed, so executing one has no
            // effect.
            return Status.OK_STATUS;
        }

        /**
         * {@inheritDoc} Notifies clients about the undo.
         */
        public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {
            if (isValid()) {
                fDocumentUndoManager.fireDocumentUndo(fStart, fPreservedText,
                        fText, uiInfo, RichDocumentUndoEvent.ABOUT_TO_UNDO,
                        false);
                undoTextChange();
                undoRichTextChange();
                fDocumentUndoManager.resetProcessChangeState();
                fDocumentUndoManager.fireDocumentUndo(fStart, fPreservedText,
                        fText, uiInfo, RichDocumentUndoEvent.UNDONE, false);
                return Status.OK_STATUS;
            }
            return IOperationHistory.OPERATION_INVALID_STATUS;
        }

        /**
         * Re-applies the change described by this change.
         */
        protected void redoTextChange() {
            if (fStart < 0 || fEnd < 0)
                return;
            try {
                if (fDocumentUndoManager.fDocument instanceof IDocumentExtension4)
                    ((IDocumentExtension4) fDocumentUndoManager.fDocument)
                            .replace(fStart, fEnd - fStart, fText,
                                    fRedoModificationStamp);
                else
                    fDocumentUndoManager.fDocument.replace(fStart, fEnd
                            - fStart, fText);
            } catch (BadLocationException x) {
            }
        }

        protected void redoRichTextChange() {
//            fDocumentUndoManager.ignoreDocumentChange = true;
            if (newTextStyles != null)
                fDocumentUndoManager.fDocument.setTextStyles(newTextStyles);
            if (newLineStyles != null)
                fDocumentUndoManager.fDocument.setLineStyles(newLineStyles);
            if (newImages != null)
                fDocumentUndoManager.fDocument.setImages(newImages);
            if (newHyperlinks != null)
                fDocumentUndoManager.fDocument.setHyperlinks(newHyperlinks);
//            fDocumentUndoManager.ignoreDocumentChange = false;
        }

        /**
         * Re-applies the change described by this change that was previously
         * undone. Also notifies clients about the redo.
         * 
         * @param monitor
         *            the progress monitor to use if necessary
         * @param uiInfo
         *            an adaptable that can provide UI info if needed
         * @return the status
         */
        public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {
            if (isValid()) {
                redoTextChange();
                redoRichTextChange();
                fDocumentUndoManager.resetProcessChangeState();
                fDocumentUndoManager.fireDocumentUndo(fStart, fText,
                        fPreservedText, uiInfo, RichDocumentUndoEvent.REDONE,
                        false);
                return Status.OK_STATUS;
            }
            return IOperationHistory.OPERATION_INVALID_STATUS;
        }

        /**
         * Update the change in response to a commit.
         */

        protected void updateTextChange() {
            fText = fDocumentUndoManager.fTextBuffer.toString();
            fDocumentUndoManager.fTextBuffer.setLength(0);
            fPreservedText = fDocumentUndoManager.fPreservedTextBuffer
                    .toString();
            fDocumentUndoManager.fPreservedTextBuffer.setLength(0);
        }

        /**
         * Creates a new uncommitted text change depending on whether a compound
         * change is currently being executed.
         * 
         * @return a new, uncommitted text change or a compound text change
         */
        protected UndoableRichTextChange createCurrent() {
            if (fDocumentUndoManager.fFoldingIntoCompoundChange)
                return new UndoableCompoundRichTextChange(fDocumentUndoManager);
            return new UndoableRichTextChange(fDocumentUndoManager);
        }

        /**
         * Commits the current change into this one.
         */
        protected void commit() {
            if (!isValid()) {//fStart < 0 ) {
                if (fDocumentUndoManager.fFoldingIntoCompoundChange) {
                    fDocumentUndoManager.fCurrent = createCurrent();
                } else {
                    reinitialize();
                }
            } else {
                updateTextChange();
                fDocumentUndoManager.fCurrent = createCurrent();
            }
            fDocumentUndoManager.resetProcessChangeState();
        }

        /**
         * Updates the text from the buffers without resetting the buffers or
         * adding anything to the stack.
         */
        protected void pretendCommit() {
            if (fStart > -1) {
                fText = fDocumentUndoManager.fTextBuffer.toString();
                fPreservedText = fDocumentUndoManager.fPreservedTextBuffer
                        .toString();
            }
        }

        /**
         * Attempt a commit of this change and answer true if a new fCurrent was
         * created as a result of the commit.
         * 
         * @return <code>true</code> if the change was committed and created a
         *         new <code>fCurrent</code>, <code>false</code> if not
         */
        protected boolean attemptCommit() {
            pretendCommit();
            if (isValid()) {
                fDocumentUndoManager.commit();
                return true;
            }
            return false;
        }

        /**
         * Checks whether this text change is valid for undo or redo.
         * 
         * @return <code>true</code> if the change is valid for undo or redo
         */
        protected boolean isValid() {
            return (fStart > -1 && fEnd > -1 && fText != null)
                    || (oldImages != null && newImages != null)
                    || (oldLineStyles != null && newLineStyles != null)
                    || (oldTextStyles != null && newTextStyles != null)
                    || (oldHyperlinks != null && newHyperlinks != null);
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString() {
            String delimiter = ", "; //$NON-NLS-1$
            StringBuffer text = new StringBuffer(super.toString());
            text.append("\n"); //$NON-NLS-1$
            text.append(this.getClass().getName());
            text.append(" undo modification stamp: "); //$NON-NLS-1$
            text.append(fUndoModificationStamp);
            text.append(" redo modification stamp: "); //$NON-NLS-1$
            text.append(fRedoModificationStamp);
            text.append(" start: "); //$NON-NLS-1$
            text.append(fStart);
            text.append(delimiter);
            text.append("end: "); //$NON-NLS-1$
            text.append(fEnd);
            text.append(delimiter);
            text.append("text: '"); //$NON-NLS-1$
            text.append(fText);
            text.append('\'');
            text.append(delimiter);
            text.append("preservedText: '"); //$NON-NLS-1$
            text.append(fPreservedText);
            text.append('\'');
            return text.toString();
        }

        /**
         * Return the undo modification stamp
         * 
         * @return the undo modification stamp for this change
         */
        protected long getUndoModificationStamp() {
            return fUndoModificationStamp;
        }

        /**
         * Return the redo modification stamp
         * 
         * @return the redo modification stamp for this change
         */
        protected long getRedoModificationStamp() {
            return fRedoModificationStamp;
        }
    }

    /**
     * Represents an undo-able text change consisting of several individual
     * changes.
     */
    private static class UndoableCompoundRichTextChange extends
            UndoableRichTextChange {

        /** The list of individual changes */
        private List<UndoableRichTextChange> fChanges = new ArrayList<UndoableRichTextChange>();

        /**
         * Creates a new compound text change.
         * 
         * @param manager
         *            the undo manager for this change
         */
        UndoableCompoundRichTextChange(RichDocumentUndoManager manager) {
            super(manager);
        }

        /**
         * Adds a new individual change to this compound change.
         * 
         * @param change
         *            the change to be added
         */
        protected void add(UndoableRichTextChange change) {
            fChanges.add(change);
        }

        /*
         * @see
         * org.eclipse.text.undo.UndoableTextChange#undo(org.eclipse.core.runtime
         * .IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
         */
        public IStatus undo(IProgressMonitor monitor, IAdaptable uiInfo) {

            int size = fChanges.size();
            if (size > 0) {
                UndoableRichTextChange c;

                c = (UndoableRichTextChange) fChanges.get(0);
                fDocumentUndoManager.fireDocumentUndo(c.fStart,
                        c.fPreservedText, c.fText, uiInfo,
                        RichDocumentUndoEvent.ABOUT_TO_UNDO, true);

                for (int i = size - 1; i >= 0; --i) {
                    c = (UndoableRichTextChange) fChanges.get(i);
                    c.undoTextChange();
                }
                fDocumentUndoManager.resetProcessChangeState();
                fDocumentUndoManager.fireDocumentUndo(c.fStart,
                        c.fPreservedText, c.fText, uiInfo,
                        RichDocumentUndoEvent.UNDONE, true);
            }
            undoRichTextChange();
            return Status.OK_STATUS;
        }

        /*
         * @see
         * org.eclipse.text.undo.UndoableTextChange#redo(org.eclipse.core.runtime
         * .IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
         */
        public IStatus redo(IProgressMonitor monitor, IAdaptable uiInfo) {

            int size = fChanges.size();
            if (size > 0) {

                UndoableRichTextChange c;
                c = (UndoableRichTextChange) fChanges.get(size - 1);
                fDocumentUndoManager.fireDocumentUndo(c.fStart, c.fText,
                        c.fPreservedText, uiInfo,
                        RichDocumentUndoEvent.ABOUT_TO_REDO, true);

                for (int i = 0; i <= size - 1; ++i) {
                    c = (UndoableRichTextChange) fChanges.get(i);
                    c.redoTextChange();
                }
                fDocumentUndoManager.resetProcessChangeState();
                fDocumentUndoManager.fireDocumentUndo(c.fStart, c.fText,
                        c.fPreservedText, uiInfo, RichDocumentUndoEvent.REDONE,
                        true);
            }
            redoRichTextChange();

            return Status.OK_STATUS;
        }

        /*
         * @see org.eclipse.text.undo.UndoableTextChange#updateTextChange()
         */
        protected void updateTextChange() {
            // first gather the data from the buffers
            super.updateTextChange();

            // the result of the update is stored as a child change
            UndoableRichTextChange c = new UndoableRichTextChange(
                    fDocumentUndoManager);
            c.fStart = fStart;
            c.fEnd = fEnd;
            c.fText = fText;
            c.fPreservedText = fPreservedText;
            c.fUndoModificationStamp = fUndoModificationStamp;
            c.fRedoModificationStamp = fRedoModificationStamp;
//            c.oldImages = oldImages;
//            c.newImages = newImages;
//            c.oldLineStyles = oldLineStyles;
//            c.newLineStyles = newLineStyles;
//            c.oldTextStyles = oldTextStyles;
//            c.newTextStyles = newTextStyles;
            add(c);

            // clear out all indexes now that the child is added
            reinitializeTextChange();
        }

        /*
         * @see org.eclipse.text.undo.UndoableTextChange#createCurrent()
         */
        protected UndoableRichTextChange createCurrent() {

            if (!fDocumentUndoManager.fFoldingIntoCompoundChange)
                return new UndoableRichTextChange(fDocumentUndoManager);

            reinitialize();
            return this;
        }

        /*
         * @see org.eclipse.text.undo.UndoableTextChange#commit()
         */
        protected void commit() {
            // if there is pending data, update the text change
            if (fStart > -1)
                updateTextChange();
            fDocumentUndoManager.fCurrent = createCurrent();
            fDocumentUndoManager.resetProcessChangeState();
        }

        /*
         * @see org.eclipse.text.undo.UndoableTextChange#isValid()
         */
        protected boolean isValid() {
            return fStart > -1 || fChanges.size() > 0
                    || (oldImages != null && newImages != null)
                    || (oldLineStyles != null && newLineStyles != null)
                    || (oldTextStyles != null && newTextStyles != null)
                    || (oldHyperlinks != null && newHyperlinks != null);
        }

        /*
         * @see
         * org.eclipse.text.undo.UndoableTextChange#getUndoModificationStamp()
         */
        protected long getUndoModificationStamp() {
            if (fStart > -1)
                return super.getUndoModificationStamp();
            else if (fChanges.size() > 0)
                return ((UndoableRichTextChange) fChanges.get(0))
                        .getUndoModificationStamp();

            return fUndoModificationStamp;
        }

        /*
         * @see
         * org.eclipse.text.undo.UndoableTextChange#getRedoModificationStamp()
         */
        protected long getRedoModificationStamp() {
            if (fStart > -1)
                return super.getRedoModificationStamp();
            else if (fChanges.size() > 0)
                return ((UndoableRichTextChange) fChanges
                        .get(fChanges.size() - 1)).getRedoModificationStamp();

            return fRedoModificationStamp;
        }
    }

    private class RichDocumentListener implements IRichDocumentListener {

        public void imageChanged(IRichDocument document,
                ImagePlaceHolder[] oldImages, ImagePlaceHolder[] newImages) {
            handleRichDocumentEvent(null, null, null, null, oldImages,
                    newImages, null, null);
        }

        public void lineStyleChanged(IRichDocument document,
                LineStyle[] oldLineStyles, LineStyle[] newLineStyles) {
            handleRichDocumentEvent(null, null, oldLineStyles, newLineStyles,
                    null, null, null, null);
        }

        public void textStyleChanged(IRichDocument document,
                StyleRange[] oldTextStyles, StyleRange[] newTextStyles) {
            handleRichDocumentEvent(oldTextStyles, newTextStyles, null, null,
                    null, null, null, null);
        }

        public void hyperlinkChanged(IRichDocument document,
                Hyperlink[] oldHyperlinks, Hyperlink[] newHyperlinks) {
            handleRichDocumentEvent(null, null, null, null, null, null,
                    oldHyperlinks, newHyperlinks);
        }

        private void handleRichDocumentEvent(StyleRange[] oldStyles,
                StyleRange[] newStyles, LineStyle[] oldLineStyles,
                LineStyle[] newLineStyle, ImagePlaceHolder[] oldImages,
                ImagePlaceHolder[] newImages, Hyperlink[] oldHyperlinks,
                Hyperlink[] newHyperlinks) {
            if (ignoreDocumentChange)
                return;

            IUndoableOperation op = fHistory.getUndoOperation(fUndoContext);
            boolean wasValid = false;
            if (op != null)
                wasValid = op.canUndo();

//            if ( !foldingRichTextChange ) {
//                fCurrent.attemptCommit();
//            }
            processRichChange(fCurrent, oldStyles, newStyles, oldLineStyles,
                    newLineStyle, oldImages, newImages, oldHyperlinks,
                    newHyperlinks);

            if (op == fCurrent) {
                // if the document change did not cause a new fCurrent to be
                // created, then we should
                // notify the history that the current operation changed if its
                // validity has changed.
                if (wasValid != fCurrent.isValid()) {
                    fHistory.operationChanged(op);
                } else {

                }
            } else {
                // if the change created a new fCurrent that we did not yet add
                // to the
                // stack, do so if it's valid and we are not in the middle of a
                // compound change.
                if (fCurrent != fLastAddedTextEdit && fCurrent.isValid()) {
                    addToOperationHistory(fCurrent);
                }
            }
        }
    }

    private void processRichChange(UndoableRichTextChange edit,
            StyleRange[] oldTextStyles, StyleRange[] newTextStyle,
            LineStyle[] oldLineStyles, LineStyle[] newLineStyle,
            ImagePlaceHolder[] oldImages, ImagePlaceHolder[] newImages,
            Hyperlink[] oldHyperlinks, Hyperlink[] newHyperlinks) {

        if (oldTextStyles != null && edit.oldTextStyles == null)
            edit.oldTextStyles = oldTextStyles;
        if (newTextStyle != null)
            edit.newTextStyles = newTextStyle;

        if (oldLineStyles != null && edit.oldLineStyles == null)
            edit.oldLineStyles = oldLineStyles;
        if (newLineStyle != null)
            edit.newLineStyles = newLineStyle;

        if (oldImages != null && edit.oldImages == null)
            edit.oldImages = oldImages;
        if (newImages != null)
            edit.newImages = newImages;

        if (oldHyperlinks != null && edit.oldHyperlinks == null)
            edit.oldHyperlinks = oldHyperlinks;
        if (newHyperlinks != null)
            edit.newHyperlinks = newHyperlinks;

    }

    /**
     * Internal listener to document changes.
     */
    private class DocumentListener implements IDocumentListener {

        private String fReplacedText;

        /*
         * @see
         * org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged
         * (org.eclipse.jface.text.DocumentEvent)
         */
        public void documentAboutToBeChanged(DocumentEvent event) {
            if (ignoreDocumentChange)
                return;

            try {
                fReplacedText = event.getDocument().get(event.getOffset(),
                        event.getLength());
                fPreservedUndoModificationStamp = event.getModificationStamp();
            } catch (BadLocationException x) {
                fReplacedText = null;
            }
        }

        /*
         * @see
         * org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse
         * .jface.text.DocumentEvent)
         */
        public void documentChanged(DocumentEvent event) {
            if (ignoreDocumentChange)
                return;

            fPreservedRedoModificationStamp = event.getModificationStamp();

            // record the current valid state for the top operation in case it
            // remains the
            // top operation but changes state.
            IUndoableOperation op = fHistory.getUndoOperation(fUndoContext);
            boolean wasValid = false;
            if (op != null)
                wasValid = op.canUndo();
            // Process the change, providing the before and after timestamps
            processChange(event.getOffset(), event.getOffset()
                    + event.getLength(), event.getText(), fReplacedText,
                    fPreservedUndoModificationStamp,
                    fPreservedRedoModificationStamp);

            // now update fCurrent with the latest buffers from the document
            // change.
            fCurrent.pretendCommit();

            if (op == fCurrent) {
                // if the document change did not cause a new fCurrent to be
                // created, then we should
                // notify the history that the current operation changed if its
                // validity has changed.
                if (wasValid != fCurrent.isValid())
                    fHistory.operationChanged(op);
            } else {
                // if the change created a new fCurrent that we did not yet add
                // to the
                // stack, do so if it's valid and we are not in the middle of a
                // compound change.
                if (fCurrent != fLastAddedTextEdit && fCurrent.isValid()) {
                    addToOperationHistory(fCurrent);
                }
            }
        }
    }

    /*
     * @see IOperationHistoryListener
     */
    private class HistoryListener implements IOperationHistoryListener {

        private IUndoableOperation fOperation;

        public void historyNotification(final OperationHistoryEvent event) {
            final int type = event.getEventType();
            switch (type) {
            case OperationHistoryEvent.ABOUT_TO_UNDO:
            case OperationHistoryEvent.ABOUT_TO_REDO:
                // if this is one of our operations
                if (event.getOperation().hasContext(fUndoContext)) {
                    // if we are undoing/redoing an operation we generated, then
                    // ignore
                    // the document changes associated with this undo or redo.
                    if (event.getOperation() instanceof UndoableRichTextChange) {
//                            listenToTextChanges( false );
                        ignoreDocumentChange = true;

                        // in the undo case only, make sure compounds are closed
                        if (type == OperationHistoryEvent.ABOUT_TO_UNDO) {
                            if (fFoldingIntoCompoundChange) {
                                endCompoundChange();
                            }
                        }
                    } else {
                        // the undo or redo has our context, but it is not one
                        // of our edits. We will listen to the changes, but will
                        // reset the state that tracks the undo/redo history.
                        commit();
                        fLastAddedTextEdit = null;
                    }
                    fOperation = event.getOperation();
                }
                break;
            case OperationHistoryEvent.UNDONE:
            case OperationHistoryEvent.REDONE:
            case OperationHistoryEvent.OPERATION_NOT_OK:
                if (event.getOperation() == fOperation) {
//                        listenToTextChanges( true );
                    ignoreDocumentChange = false;
                    fOperation = null;
                }
                break;
            }
        }

    }

    /**
     * The undo context for this document undo manager.
     */
    private ObjectUndoContext fUndoContext;

    /**
     * The document whose changes are being tracked.
     */
    private IRichDocument fDocument;

    /**
     * The currently constructed edit.
     */
    private UndoableRichTextChange fCurrent;

    /**
     * The internal document listener.
     */
    private DocumentListener fDocumentListener;

    private RichDocumentListener fRichDocumentListener;

    /**
     * Indicates whether the current change belongs to a compound change.
     */
    private boolean fFoldingIntoCompoundChange = false;

    /**
     * The operation history being used to store the undo history.
     */
    private IOperationHistory fHistory;

    /**
     * The operation history listener used for managing undo and redo before and
     * after the individual edits are performed.
     */
    private IOperationHistoryListener fHistoryListener;

    /**
     * The text edit last added to the operation history. This must be tracked
     * internally instead of asking the history, since outside parties may be
     * placing items on our undo/redo history.
     */
    private UndoableRichTextChange fLastAddedTextEdit = null;

    /**
     * The document modification stamp for redo.
     */
    private long fPreservedRedoModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

    /**
     * Text buffer to collect viewer content which has been replaced
     */
    private StringBuffer fPreservedTextBuffer;

    /**
     * The document modification stamp for undo.
     */
    private long fPreservedUndoModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

    /**
     * The last delete text edit.
     */
    private UndoableRichTextChange fPreviousDelete;

    /**
     * Text buffer to collect text which is inserted into the viewer
     */
    private StringBuffer fTextBuffer;

    /** Indicates inserting state. */
    private boolean fInserting = false;

    /** Indicates overwriting state. */
    private boolean fOverwriting = false;

    /** The registered document listeners. */
    private ListenerList fDocumentUndoListeners;

    /** The list of clients connected. */
    private List<Object> fConnected;

    private boolean ignoreDocumentChange = false;

//    private boolean foldingRichTextChange = false;
//    
//    public void beginCompoundRichTextChange() {
//        if ( isConnected() ) {
//            this.foldingRichTextChange = true;
//        }
//    }
//    
//    public void endCompoundRichTextChange() {
//        if ( isConnected() ) {
//            this.foldingRichTextChange = false;
//        }
//    }

    /**
     * Create a DocumentUndoManager for the given document.
     * 
     * @param document
     *            the document whose undo history is being managed.
     */
    public RichDocumentUndoManager(IRichDocument document) {
        super();
        Assert.isNotNull(document);
        fDocument = document;
        fHistory = OperationHistoryFactory.getOperationHistory();
        fUndoContext = new ObjectUndoContext(fDocument);
        fConnected = new ArrayList<Object>();
        fDocumentUndoListeners = new ListenerList();
    }

    /*
     * @see
     * org.eclipse.jface.text.IDocumentUndoManager#addDocumentUndoListener(org
     * .eclipse.jface.text.IDocumentUndoListener)
     */
    public void addDocumentUndoListener(IRichDocumentUndoListener listener) {
        fDocumentUndoListeners.add(listener);
    }

    /*
     * @see
     * org.eclipse.jface.text.IDocumentUndoManager#removeDocumentUndoListener
     * (org.eclipse.jface.text.IDocumentUndoListener)
     */
    public void removeDocumentUndoListener(IRichDocumentUndoListener listener) {
        fDocumentUndoListeners.remove(listener);
    }

    /*
     * @see org.eclipse.jface.text.IDocumentUndoManager#getUndoContext()
     */
    public IUndoContext getUndoContext() {
        return fUndoContext;
    }

    /*
     * @see org.eclipse.jface.text.IDocumentUndoManager#commit()
     */
    public void commit() {
        // if fCurrent has never been placed on the history, do so now.
        // this can happen when there are multiple programmatically commits in a
        // single document change.
        if (fLastAddedTextEdit != fCurrent) {
            fCurrent.pretendCommit();
            if (fCurrent.isValid())
                addToOperationHistory(fCurrent);
        }
        fCurrent.commit();
    }

    /*
     * @see org.eclipse.text.undo.IDocumentUndoManager#reset()
     */
    public void reset() {
        if (isConnected()) {
            shutdown();
            initialize();
        }
    }

    /*
     * @see org.eclipse.text.undo.IDocumentUndoManager#redoable()
     */
    public boolean redoable() {
        return OperationHistoryFactory.getOperationHistory().canRedo(
                fUndoContext);
    }

    /*
     * @see org.eclipse.text.undo.IDocumentUndoManager#undoable()
     */
    public boolean undoable() {
        return OperationHistoryFactory.getOperationHistory().canUndo(
                fUndoContext);
    }

    /*
     * @see org.eclipse.text.undo.IDocumentUndoManager#undo()
     */
    public void redo() throws ExecutionException {
        if (isConnected() && redoable())
            OperationHistoryFactory.getOperationHistory().redo(
                    getUndoContext(), null, null);
    }

    /*
     * @see org.eclipse.text.undo.IDocumentUndoManager#undo()
     */
    public void undo() throws ExecutionException {
        if (undoable())
            OperationHistoryFactory.getOperationHistory().undo(fUndoContext,
                    null, null);
    }

    /*
     * @see
     * org.eclipse.jface.text.IDocumentUndoManager#connect(java.lang.Object)
     */
    public void connect(Object client) {
        if (!isConnected()) {
            initialize();
        }
        if (!fConnected.contains(client))
            fConnected.add(client);
    }

    /*
     * @see
     * org.eclipse.jface.text.IDocumentUndoManager#disconnect(java.lang.Object)
     */
    public void disconnect(Object client) {
        fConnected.remove(client);
        if (!isConnected()) {
            shutdown();
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocumentUndoManager#beginCompoundChange()
     */
    public void beginCompoundChange() {
        if (isConnected()) {
            fFoldingIntoCompoundChange = true;
            commit();
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocumentUndoManager#endCompoundChange()
     */
    public void endCompoundChange() {
        if (isConnected()) {
            fFoldingIntoCompoundChange = false;
            commit();
        }
    }

    /*
     * @see org.eclipse.jface.text.IDocumentUndoManager#setUndoLimit(int)
     */
    public void setMaximalUndoLevel(int undoLimit) {
        fHistory.setLimit(fUndoContext, undoLimit);
    }

    /**
     * Fires a document undo event to all registered document undo listeners.
     * Uses a robust iterator.
     * 
     * @param offset
     *            the document offset
     * @param text
     *            the text that was inserted
     * @param preservedText
     *            the text being replaced
     * @param source
     *            the source which triggered the event
     * @param eventType
     *            the type of event causing the change
     * @param isCompound
     *            a flag indicating whether the change is a compound change
     * @see IDocumentUndoListener
     */
    void fireDocumentUndo(int offset, String text, String preservedText,
            Object source, int eventType, boolean isCompound) {
        if (offset < 0)
            return;
        eventType = isCompound ? eventType | RichDocumentUndoEvent.COMPOUND
                : eventType;
        RichDocumentUndoEvent event = new RichDocumentUndoEvent(fDocument,
                offset, text, preservedText, eventType, source);
        Object[] listeners = fDocumentUndoListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IRichDocumentUndoListener) listeners[i])
                    .documentUndoNotification(event);
        }
    }

    /**
     * Adds any listeners needed to track the document and the operations
     * history.
     */
    private void addListeners() {
        fHistoryListener = new HistoryListener();
        fHistory.addOperationHistoryListener(fHistoryListener);
//        listenToTextChanges( true );
        if (fDocumentListener == null && fDocument != null) {
            fDocumentListener = new DocumentListener();
            fDocument.addDocumentListener(fDocumentListener);
        }
        if (fRichDocumentListener == null && fDocument != null) {
            fRichDocumentListener = new RichDocumentListener();
            fDocument.addRichDocumentListener(fRichDocumentListener);
        }
    }

    /**
     * Removes any listeners that were installed by the document.
     */
    private void removeListeners() {
        if (fRichDocumentListener != null && fDocument != null) {
            fDocument.removeRichDocumentListener(fRichDocumentListener);
            fRichDocumentListener = null;
        }
        if (fDocumentListener != null && fDocument != null) {
            fDocument.removeDocumentListener(fDocumentListener);
            fDocumentListener = null;
        }
//        listenToTextChanges( false );
        fHistory.removeOperationHistoryListener(fHistoryListener);
        fHistoryListener = null;
    }

    /**
     * Adds the given text edit to the operation history if it is not part of a
     * compound change.
     * 
     * @param edit
     *            the edit to be added
     */
    private void addToOperationHistory(UndoableRichTextChange edit) {
        if (!fFoldingIntoCompoundChange
                || edit instanceof UndoableCompoundRichTextChange) {
            fHistory.add(edit);
            fLastAddedTextEdit = edit;
        }
    }

    /**
     * Disposes the undo history.
     */
    private void disposeUndoHistory() {
        fHistory.dispose(fUndoContext, true, true, true);
    }

    /**
     * Initializes the undo history.
     */
    private void initializeUndoHistory() {
        if (fHistory != null && fUndoContext != null)
            fHistory.dispose(fUndoContext, true, true, false);

    }

    /**
     * Checks whether the given text starts with a line delimiter and
     * subsequently contains a white space only.
     * 
     * @param text
     *            the text to check
     * @return <code>true</code> if the text is a line delimiter followed by
     *         whitespace, <code>false</code> otherwise
     */
    private boolean isWhitespaceText(String text) {

        if (text == null || text.length() == 0)
            return false;

        String[] delimiters = fDocument.getLegalLineDelimiters();
        int index = TextUtilities.startsWith(delimiters, text);
        if (index > -1) {
            char c;
            int length = text.length();
            for (int i = delimiters[index].length(); i < length; i++) {
                c = text.charAt(i);
                if (c != ' ' && c != '\t')
                    return false;
            }
            return true;
        }

        return false;
    }

//    /**
//     * Switches the state of whether there is a text listener or not.
//     * 
//     * @param listen the state which should be established
//     */
//    private void listenToTextChanges( boolean listen ) {
//        if ( listen ) {
//            if ( fDocumentListener == null && fDocument != null ) {
//                fDocumentListener = new DocumentListener();
//                fDocument.addDocumentListener( fDocumentListener );
//            }
//            if ( fRichDocumentListener == null && fDocument != null ) {
//                fRichDocumentListener = new RichDocumentListener();
//                fDocument.addRichDocumentListener( fRichDocumentListener );
//            }
//        }else if ( !listen ) {
//            if ( fRichDocumentListener != null && fDocument != null ) {
//                fDocument.removeRichDocumentListener( fRichDocumentListener );
//                fRichDocumentListener = null;
//            }
//            if ( fDocumentListener != null && fDocument != null ) {
//                fDocument.removeDocumentListener( fDocumentListener );
//                fDocumentListener = null;
//            }
//        }
//    }

    private void processChange(int modelStart, int modelEnd,
            String insertedText, String replacedText,
            long beforeChangeModificationStamp,
            long afterChangeModificationStamp) {

        if (insertedText == null)
            insertedText = ""; //$NON-NLS-1$

        if (replacedText == null)
            replacedText = ""; //$NON-NLS-1$

        int length = insertedText.length();

        if (fCurrent.fUndoModificationStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP)
            fCurrent.fUndoModificationStamp = beforeChangeModificationStamp;

        if (modelEnd < modelStart) {
            int tmp = modelEnd;
            modelEnd = modelStart;
            modelStart = tmp;
        }

        if (modelStart == modelEnd) {
            // text will be inserted
            if ((length == 1) || isWhitespaceText(insertedText)) {
                // by typing or whitespace
                if (!fInserting
                        || (modelStart != fCurrent.fStart
                                + fTextBuffer.length())) {
                    fCurrent.fRedoModificationStamp = beforeChangeModificationStamp;
                    if (fCurrent.attemptCommit())
                        fCurrent.fUndoModificationStamp = beforeChangeModificationStamp;

                    fInserting = true;
                }
                if (fCurrent.fStart < 0)
                    fCurrent.fStart = fCurrent.fEnd = modelStart;
                if (length > 0)
                    fTextBuffer.append(insertedText);
            } else if (length > 0) {
                // by pasting or model manipulation
                fCurrent.fRedoModificationStamp = beforeChangeModificationStamp;
                if (fCurrent.attemptCommit())
                    fCurrent.fUndoModificationStamp = beforeChangeModificationStamp;

                fCurrent.fStart = fCurrent.fEnd = modelStart;
                fTextBuffer.append(insertedText);
//                fCurrent.fRedoModificationStamp = afterChangeModificationStamp;
//                if ( fCurrent.attemptCommit() )
//                    fCurrent.fUndoModificationStamp = afterChangeModificationStamp;

            }
        } else {
            if (length == 0) {
                // text will be deleted by backspace or DEL key or empty
                // clipboard
                length = replacedText.length();
                String[] delimiters = fDocument.getLegalLineDelimiters();

                if ((length == 1)
                        || TextUtilities.equals(delimiters, replacedText) > -1) {

                    // whereby selection is empty

                    if (fPreviousDelete.fStart == modelStart
                            && fPreviousDelete.fEnd == modelEnd) {
                        // repeated DEL

                        // correct wrong settings of fCurrent
                        if (fCurrent.fStart == modelEnd
                                && fCurrent.fEnd == modelStart) {
                            fCurrent.fStart = modelStart;
                            fCurrent.fEnd = modelEnd;
                        }
                        // append to buffer && extend edit range
                        fPreservedTextBuffer.append(replacedText);
                        ++fCurrent.fEnd;

                    } else if (fPreviousDelete.fStart == modelEnd) {
                        // repeated backspace

                        // insert in buffer and extend edit range
                        fPreservedTextBuffer.insert(0, replacedText);
                        fCurrent.fStart = modelStart;

                    } else {
                        // either DEL or backspace for the first time

                        fCurrent.fRedoModificationStamp = beforeChangeModificationStamp;
                        if (fCurrent.attemptCommit())
                            fCurrent.fUndoModificationStamp = beforeChangeModificationStamp;

                        // as we can not decide whether it was DEL or backspace
                        // we initialize for backspace
                        fPreservedTextBuffer.append(replacedText);
                        fCurrent.fStart = modelStart;
                        fCurrent.fEnd = modelEnd;
                    }

                    fPreviousDelete.set(modelStart, modelEnd);

                } else if (length > 0) {
                    // whereby selection is not empty
                    fCurrent.fRedoModificationStamp = beforeChangeModificationStamp;
                    if (fCurrent.attemptCommit())
                        fCurrent.fUndoModificationStamp = beforeChangeModificationStamp;

                    fCurrent.fStart = modelStart;
                    fCurrent.fEnd = modelEnd;
                    fPreservedTextBuffer.append(replacedText);
                }
            } else {
                // text will be replaced

                if (length == 1) {
                    length = replacedText.length();
                    String[] delimiters = fDocument.getLegalLineDelimiters();

                    if ((length == 1)
                            || TextUtilities.equals(delimiters, replacedText) > -1) {
                        // because of overwrite mode or model manipulation
                        if (!fOverwriting
                                || (modelStart != fCurrent.fStart
                                        + fTextBuffer.length())) {
                            fCurrent.fRedoModificationStamp = beforeChangeModificationStamp;
                            if (fCurrent.attemptCommit())
                                fCurrent.fUndoModificationStamp = beforeChangeModificationStamp;

                            fOverwriting = true;
                        }

                        if (fCurrent.fStart < 0)
                            fCurrent.fStart = modelStart;

                        fCurrent.fEnd = modelEnd;
                        fTextBuffer.append(insertedText);
                        fPreservedTextBuffer.append(replacedText);
                        fCurrent.fRedoModificationStamp = afterChangeModificationStamp;
                        return;
                    }
                }
                // because of typing or pasting whereby selection is not empty
                fCurrent.fRedoModificationStamp = beforeChangeModificationStamp;
                if (fCurrent.attemptCommit())
                    fCurrent.fUndoModificationStamp = beforeChangeModificationStamp;

                fCurrent.fStart = modelStart;
                fCurrent.fEnd = modelEnd;
                fTextBuffer.append(insertedText);
                fPreservedTextBuffer.append(replacedText);
            }
        }
        // in all cases, the redo modification stamp is updated on the open
        // text edit
        fCurrent.fRedoModificationStamp = afterChangeModificationStamp;
    }

    /**
     * Initialize the receiver.
     */
    private void initialize() {
        initializeUndoHistory();

        // open up the current text edit
        fCurrent = new UndoableRichTextChange(this);
        fPreviousDelete = new UndoableRichTextChange(this);
        fTextBuffer = new StringBuffer();
        fPreservedTextBuffer = new StringBuffer();

        addListeners();
    }

    /**
     * Reset processChange state.
     * 
     * @since 3.2
     */
    private void resetProcessChangeState() {
        fInserting = false;
        fOverwriting = false;
        fPreviousDelete.reinitialize();
    }

    /**
     * Shutdown the receiver.
     */
    private void shutdown() {
        removeListeners();

        fCurrent = null;
        fPreviousDelete = null;
        fTextBuffer = null;
        fPreservedTextBuffer = null;

        disposeUndoHistory();
    }

    /**
     * Return whether or not any clients are connected to the receiver.
     * 
     * @return <code>true</code> if the receiver is connected to clients,
     *         <code>false</code> if it is not
     */
    boolean isConnected() {
        if (fConnected == null)
            return false;
        return !fConnected.isEmpty();
    }

    /*
     * @seeorg.eclipse.jface.text.IDocumentUndoManager#transferUndoHistory(
     * IDocumentUndoManager)
     */
    public void transferUndoHistory(IRichDocumentUndoManager manager) {
        IUndoContext oldUndoContext = manager.getUndoContext();
        // Get the history for the old undo context.
        IUndoableOperation[] operations = OperationHistoryFactory
                .getOperationHistory().getUndoHistory(oldUndoContext);
        for (int i = 0; i < operations.length; i++) {
            // First replace the undo context
            IUndoableOperation op = operations[i];
            if (op instanceof IContextReplacingOperation) {
                ((IContextReplacingOperation) op).replaceContext(
                        oldUndoContext, getUndoContext());
            } else {
                op.addContext(getUndoContext());
                op.removeContext(oldUndoContext);
            }
            // Now update the manager that owns the text edit.
            if (op instanceof UndoableRichTextChange) {
                ((UndoableRichTextChange) op).fDocumentUndoManager = this;
            }
        }

        // Record the transfer itself as an undoable change.
        // If the transfer results from some open operation, recording this change will
        // cause our undo context to be added to the outer operation.  If there is no
        // outer operation, there will be a local change to signify the transfer.
        // This also serves to synchronize the modification stamps with the documents.
        IUndoableOperation op = OperationHistoryFactory.getOperationHistory()
                .getUndoOperation(getUndoContext());
        UndoableRichTextChange cmd = new UndoableRichTextChange(this);
        cmd.fStart = cmd.fEnd = 0;
        cmd.fText = cmd.fPreservedText = ""; //$NON-NLS-1$
        if (fDocument instanceof IDocumentExtension4) {
            cmd.fRedoModificationStamp = ((IDocumentExtension4) fDocument)
                    .getModificationStamp();
            if (op instanceof UndoableRichTextChange) {
                cmd.fUndoModificationStamp = ((UndoableRichTextChange) op).fRedoModificationStamp;
            }
        }
        addToOperationHistory(cmd);
    }

}