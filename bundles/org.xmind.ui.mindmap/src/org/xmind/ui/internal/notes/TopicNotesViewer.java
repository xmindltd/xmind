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
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.ui.richtext.Hyperlink;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.IRichDocumentListener;
import org.xmind.ui.richtext.IRichTextActionBarContributor;
import org.xmind.ui.richtext.IRichTextEditViewer;
import org.xmind.ui.richtext.ImagePlaceHolder;
import org.xmind.ui.richtext.LineStyle;
import org.xmind.ui.richtext.RichTextEditViewer;

public class TopicNotesViewer implements INotesContentViewer,
        IInputSelectionProvider {

    private class SelectionSynchronizer implements ISelectionProvider,
            ISelectionChangedListener {

        private List<ISelectionChangedListener> selectionChangedListeners = null;

        private ISelection selection;

        private boolean synchronizingSelection = false;

        public void addSelectionChangedListener(
                ISelectionChangedListener listener) {
            if (selectionChangedListeners == null)
                selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
            selectionChangedListeners.add(listener);
        }

        public ISelection getSelection() {
            return selection == null ? StructuredSelection.EMPTY : selection;
        }

        public void removeSelectionChangedListener(
                ISelectionChangedListener listener) {
            if (selectionChangedListeners == null)
                return;
            selectionChangedListeners.remove(listener);
        }

        public void setSelection(ISelection selection) {
            this.selection = selection;
            if (implementation != null) {
                synchronizingSelection = true;
                implementation.setSelection(selection);
                synchronizingSelection = false;
            }
        }

        public void selectionChanged(SelectionChangedEvent event) {
            if (synchronizingSelection)
                return;
            fireSelectionChanged(event);
        }

        private void fireSelectionChanged(SelectionChangedEvent event) {
            if (selectionChangedListeners == null)
                return;
            for (Object o : selectionChangedListeners.toArray()) {
                ((ISelectionChangedListener) o).selectionChanged(event);
            }
        }

    }

    private class ModificationListener implements IDocumentListener,
            IRichDocumentListener, ITextInputListener {

        private boolean modified = false;

        public void textStyleChanged(IRichDocument document,
                StyleRange[] oldTextStyles, StyleRange[] newTextStyles) {
            modified = true;
        }

        public void lineStyleChanged(IRichDocument document,
                LineStyle[] oldLineStyles, LineStyle[] newLineStyles) {
            modified = true;
        }

        public void imageChanged(IRichDocument document,
                ImagePlaceHolder[] oldImages, ImagePlaceHolder[] newImages) {
            modified = true;
        }

        public void hyperlinkChanged(IRichDocument document,
                Hyperlink[] oldHyperlinks, Hyperlink[] newHyperlinks) {
            modified = true;
        }

        public void documentAboutToBeChanged(DocumentEvent event) {
        }

        public void documentChanged(DocumentEvent event) {
            modified = true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged
         * (org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
         */
        public void inputDocumentAboutToBeChanged(IDocument oldInput,
                IDocument newInput) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org
         * .eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
         */
        public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
            if (oldInput != null) {
                oldInput.removeDocumentListener(this);
                if (oldInput instanceof IRichDocument) {
                    ((IRichDocument) oldInput).removeRichDocumentListener(this);
                }
            }
            if (newInput != null) {
                newInput.addDocumentListener(this);
                if (newInput instanceof IRichDocument) {
                    ((IRichDocument) newInput).addRichDocumentListener(this);
                }
            }
        }

        /**
         * @return the modified
         */
        public boolean isModified() {
            return modified;
        }

        public void reset() {
            modified = false;
        }

    }

    private IRichTextEditViewer implementation;

    private Object input;

    private SelectionSynchronizer selectionProvider;

    private IRichTextActionBarContributor contributor;

    private ModificationListener modificationListener;

    private Composite composite;

    public TopicNotesViewer() {
    }

    public TopicNotesViewer(IRichTextActionBarContributor contributor) {
        this.contributor = contributor;
    }

    public void setContributor(IRichTextActionBarContributor contributor) {
        this.contributor = contributor;
    }

    public Control createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createControl(composite, IRichTextEditViewer.DEFAULT_CONTROL_STYLE);

        return composite;
    }

    public void createControl(Composite parent, int textControlStyle) {
        implementation = new RichTextEditViewer(parent, textControlStyle,
                contributor);
        TextViewer viewer = implementation.getTextViewer();
        RGB red = new RGB(183, 0, 91);
        viewer.setHyperlinkPresenter(new DefaultHyperlinkPresenter(red));
        viewer.setHyperlinkDetectors(
                new IHyperlinkDetector[] { new NotesHyperlinkDetector() },
                SWT.MOD1);

        modificationListener = new ModificationListener();
        viewer.addTextInputListener(modificationListener);

        IRichDocument document = getDocument();
        implementation.setInput(document);

        Control control = implementation.getControl();
        control.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private IRichDocument getDocument() {
        if (input instanceof IRichDocument)
            return (IRichDocument) input;
        if (input instanceof IAdaptable) {
            return (IRichDocument) ((IAdaptable) input)
                    .getAdapter(IRichDocument.class);
        }
        return null;
    }

    public Control getControl() {
        return implementation == null ? null : implementation.getControl();
    }

    public IRichTextEditViewer getImplementation() {
        return implementation;
    }

    public void setInput(Object input) {
        this.input = input;
        if (implementation != null) {
            implementation.setInput(getDocument());
        }
    }

    public Object getInput() {
        return input;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        getSelectionProvider().addSelectionChangedListener(listener);
    }

    private SelectionSynchronizer getSelectionProvider() {
        if (selectionProvider == null) {
            selectionProvider = new SelectionSynchronizer();
        }
        return selectionProvider;
    }

    public ISelection getSelection() {
        return getSelectionProvider().getSelection();
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (selectionProvider != null) {
            selectionProvider.removeSelectionChangedListener(listener);
        }
    }

    public void setSelection(ISelection selection) {
        getSelectionProvider().setSelection(selection);
    }

    public boolean hasModified() {
        return modificationListener == null
                || modificationListener.isModified();
    }

    public void resetModified() {
        if (modificationListener != null) {
            modificationListener.reset();
        }
    }

    public void dispose() {
        composite.dispose();
    }

}