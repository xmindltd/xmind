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
package org.xmind.ui.richtext;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author Frank Shaka
 */
public class RichTextEditViewer implements IRichTextEditViewer {

    private static class LessLatencyTextViewer extends TextViewer {
        private LessLatencyTextViewer(Composite parent, int styles) {
            super(parent, styles);
        }

        @Override
        protected int getEmptySelectionChangedEventDelay() {
            return 100;
        }
    }

    private IRichDocument document;

    private Composite control;

    private TextViewer textViewer;

    private boolean editable;

    private IRichTextRenderer renderer;

    private IRichTextActionBarContributor contributor;

    private ToolBarManager toolBarManager = null;

    private MenuManager contextMenu = null;

    public RichTextEditViewer(Composite parent) {
        this(parent, DEFAULT_CONTROL_STYLE, null);
    }

    public RichTextEditViewer(Composite parent,
            IRichTextActionBarContributor contributor) {
        this(parent, DEFAULT_CONTROL_STYLE, contributor);
    }

    public RichTextEditViewer(Composite parent, int textControlStyle) {
        this(parent, textControlStyle, null);
    }

    public RichTextEditViewer(Composite parent, int textControlStyle,
            IRichTextActionBarContributor contributor) {
        this.contributor = contributor;
        this.editable = (textControlStyle & SWT.READ_ONLY) == 0;
        this.control = createControl(parent, textControlStyle);
    }

    protected Composite createControl(Composite parent, int textControlStyle) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        createContentArea(composite, textControlStyle);

        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleControlDispose(e);
            }
        });
        return composite;
    }

    protected void createContentArea(Composite parent, int textControlStyle) {
        Control toolBar = createToolBar(parent);
        if (toolBar != null) {
            createSeparator(parent);
        }
        createTextControl(parent, textControlStyle);
    }

    protected Control createToolBar(Composite parent) {
        if (contributor == null)
            return null;

        contributor.init(this);
        toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
        ToolBar toolBar = toolBarManager.createControl(parent);
        contributor.fillToolBar(toolBarManager);
        parent.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                toolBarManager.update(true);
            }
        });
        toolBar.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return toolBar;
    }

    protected void createSeparator(Composite parent) {
        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sep.setBackground(parent.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    protected Control createTextControl(Composite parent, int style) {
        textViewer = createTextViewer(parent, style);
        initTextViewer(textViewer);
        renderer = createRenderer(textViewer);

        Control textControl = textViewer.getControl();
        textControl.setBackground(parent.getBackground());
        textControl.setForeground(
                parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 160;
        gridData.horizontalIndent = 2;
        gridData.verticalIndent = 2;
        textControl.setLayoutData(gridData);
        return textControl;
    }

    protected TextViewer createTextViewer(Composite parent, int style) {
        return new LessLatencyTextViewer(parent, style);
    }

    protected RichTextRenderer createRenderer(TextViewer textViewer) {
        return new RichTextRenderer(textViewer);
    }

    private void initTextViewer(final TextViewer textViewer) {
        textViewer.addPostSelectionChangedListener(
                new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        updateToolBar(event.getSelection());
                    }
                });

        Control control = textViewer.getTextWidget();
        createContentPopupMenu(control);

        textViewer.setTextDoubleClickStrategy(
                new DefaultTextDoubleClickStrategy(),
                IDocument.DEFAULT_CONTENT_TYPE);

        //((StyledText) textViewer.getControl()).setLineSpacing(3);
        textViewer.setUndoManager(new RichTextViewerUndoManager(25));
        textViewer.activatePlugins();

        addHyperlinkListener(textViewer);
    }

    private void createContentPopupMenu(Control control) {
        contextMenu = new MenuManager();
        contextMenu.setRemoveAllWhenShown(true);
        contextMenu.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        control.setMenu(contextMenu.createContextMenu(control));
    }

    private void fillContextMenu(IMenuManager menu) {
        if (contributor != null)
            contributor.fillContextMenu(menu);
    }

    private void addHyperlinkListener(TextViewer viewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        RichTextDamagerRepairer dr = new RichTextDamagerRepairer(
                new RichTextScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setDocumentPartitioning(
                IDocumentExtension3.DEFAULT_PARTITIONING);
        reconciler.install(viewer);
    }

    protected void handleControlDispose(DisposeEvent e) {
        if (contributor != null) {
            contributor.dispose();
        }
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }
        if (toolBarManager != null) {
            toolBarManager.dispose();
            toolBarManager = null;
        }
        if (document != null) {
            unhookDocument(document);
            document = null;
        }
    }

    public Control getControl() {
        return control;
    }

    public Control getFocusControl() {
        return textViewer.getControl();
    }

    public IRichDocument getDocument() {
        return document;
    }

    public TextViewer getTextViewer() {
        return textViewer;
    }

    protected void hookDocument(IRichDocument document) {
    }

    protected void unhookDocument(IRichDocument document) {
    }

    public Object getInput() {
        return document;
    }

    public void refresh() {
        updateToolBar(getSelection());
        updateTextControl();
    }

    public IRichTextActionBarContributor getContributor() {
        return contributor;
    }

    private boolean isViewerEditable() {
        return document != null && isEditable();
    }

    private void updateToolBar(ISelection selection) {
        if (toolBarManager == null)
            return;

        ToolBar toolbar = toolBarManager.getControl();
        if (toolbar != null && !toolbar.isDisposed()) {
            toolbar.setEnabled(isViewerEditable());
        }
        if (contributor != null) {
            contributor.selectionChanged(selection, isViewerEditable());
        }
        toolBarManager.update(false);
    }

    protected void updateTextControl() {
        textViewer.setEditable(isViewerEditable());
        if (document != null) {
            textViewer.getTextWidget().setEnabled(true);
        } else {
            textViewer.getTextWidget().setEnabled(false);
        }
    }

    public void setInput(Object input) {
        if (input instanceof IRichDocument) {
            setDocument((IRichDocument) input);
        } else {
            setDocument(null);
        }
    }

    public void setDocument(IRichDocument document) {
        IRichDocument oldDocument = this.document;
        this.document = document;
        documentChanged(document, oldDocument);

        textViewer.setDocument(document == null ? new Document() : document);
        refresh();
        // move the caret to the end of document
        if (document != null) {
            textViewer.setSelectedRange(document.getLength(), 0);
        }
    }

    protected void documentChanged(IRichDocument newDocument,
            IRichDocument oldDocument) {
        if (newDocument != oldDocument) {
            if (oldDocument != null) {
                unhookDocument(oldDocument);
            }
            if (newDocument != null) {
                hookDocument(newDocument);
            }
        }
    }

    public IRichTextRenderer getRenderer() {
        return renderer;
    }

    public ISelection getSelection() {
        return textViewer.getSelection();
    }

    public Point getSelectedRange() {
        return textViewer.getSelectedRange();
    }

    public void setSelection(ISelection selection) {
        textViewer.setSelection(selection);
    }

    public void setSelection(ISelection selection, boolean reveal) {
        textViewer.setSelection(selection, reveal);
    }

    public void setSelectedRange(int selectionOffset, int selectionLength) {
        textViewer.setSelectedRange(selectionOffset, selectionLength);
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        textViewer.addSelectionChangedListener(listener);
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        textViewer.removeSelectionChangedListener(listener);
    }

    public void addPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        textViewer.addPostSelectionChangedListener(listener);
    }

    public void removePostSelectionChangedListener(
            ISelectionChangedListener listener) {
        textViewer.removePostSelectionChangedListener(listener);
    }

    public void addTextInputListener(ITextInputListener listener) {
        textViewer.addTextInputListener(listener);
    }

    public void addTextListener(ITextListener listener) {
        textViewer.addTextListener(listener);
    }

    public void removeTextInputListener(ITextInputListener listener) {
        textViewer.removeTextInputListener(listener);
    }

    public void removeTextListener(ITextListener listener) {
        textViewer.removeTextListener(listener);
    }

    public boolean isSelectedRangeEmpty() {
        Point p = getSelectedRange();
        return p.y <= 0;
    }

    public StyledText getTextWidget() {
        return textViewer.getTextWidget();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        if (editable == this.editable)
            return;
        this.editable = editable;
        refresh();
    }

}
