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
package org.xmind.gef.ui.outline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.xmind.gef.tree.ITreeViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

/**
 * @author Brian Sun
 */
public abstract class GraphicalOutlinePage extends Page
        implements ISelectionProvider, ISelectionChangedListener,
        IPageChangedListener, IPropertyListener {

    private List<ISelectionChangedListener> selectionChangedListeners = null;

    private IGraphicalEditor editor;

    private PageBook pageBook;

    private ITreeViewer editorTreeViewer;

    private Map<Object, ITreeViewer> pageViewers = new HashMap<Object, ITreeViewer>();

    private boolean showCurrentPageViewer = false;

    public GraphicalOutlinePage(IGraphicalEditor editor) {
        this.editor = editor;
        hookEditor(editor);
    }

    protected void hookEditor(IGraphicalEditor editor) {
        editor.addPageChangedListener(this);
        editor.addPropertyListener(this);
    }

    protected void unhookEditor(IGraphicalEditor editor) {
        editor.removePropertyListener(this);
        editor.removePageChangedListener(this);
    }

    public void pageChanged(PageChangedEvent event) {
        if (pageBook == null || pageBook.isDisposed() || !showCurrentPageViewer)
            return;
        updatePageBook();
    }

    protected IGraphicalEditor getParentEditor() {
        return editor;
    }

    public void init(IPageSite pageSite) {
        super.init(pageSite);
        pageSite.setSelectionProvider(this);
    }

    public void createControl(Composite parent) {
        pageBook = new PageBook(parent, SWT.NONE);
        updatePageBook();
    }

    public boolean isShowCurrentPageViewer() {
        return showCurrentPageViewer;
    }

    public void setShowCurrentPageViewer(boolean showCurrentPageViewer) {
        if (showCurrentPageViewer == this.showCurrentPageViewer)
            return;

        this.showCurrentPageViewer = showCurrentPageViewer;
        updatePageBook();
    }

    protected void updatePageBook() {
        if (isShowCurrentPageViewer()) {
            showCurrentPageViewer(false);
        } else {
            showEditorTreeViewer(false);
        }
    }

    protected void refresh() {
        if (isShowCurrentPageViewer()) {
            showCurrentPageViewer(true);
        } else {
            showEditorTreeViewer(true);
        }
    }

    protected void showEditorTreeViewer(boolean refresh) {
        if (editorTreeViewer == null || editorTreeViewer.getControl() == null
                || editorTreeViewer.getControl().isDisposed()) {
            editorTreeViewer = createEditorTreeViewer();
            if (editorTreeViewer != null) {
                configureEditorTreeViewer(editorTreeViewer);
                createEditorTreeViewerControl(editorTreeViewer, pageBook);
                if (!refresh) {
                    editorTreeViewer
                            .setInput(createEditorTreeViewerInput(editor));
                }
            }
        }
        if (editorTreeViewer != null) {
            if (refresh) {
                Object newInput = createEditorTreeViewerInput(editor);
                if (newInput != editorTreeViewer.getInput()) {
                    editorTreeViewer.setInput(newInput);
                }
            }
            pageBook.showPage(editorTreeViewer.getControl());
        }
    }

    protected void showCurrentPageViewer(boolean refresh) {
        IGraphicalEditorPage page = editor.getActivePageInstance();
        if (page == null)
            return;
        Object pageInput = page.getInput();
        ITreeViewer pageTreeViewer = pageViewers.get(pageInput);
        if (pageTreeViewer == null || pageTreeViewer.getControl() == null
                || pageTreeViewer.getControl().isDisposed()) {
            pageTreeViewer = createPageTreeViewer();
            if (pageTreeViewer != null) {
                configurePageTreeViewer(pageTreeViewer);
                createPageTreeViewerControl(pageTreeViewer, pageBook);
                if (!refresh) {
                    pageTreeViewer
                            .setInput(createPageTreeViewerInput(pageInput));
                }
            }
        }
        if (pageTreeViewer != null) {
            if (refresh) {
                Object newInput = createPageTreeViewerInput(pageInput);
                if (newInput != pageTreeViewer.getInput()) {
                    pageTreeViewer.setInput(newInput);
                }
            }
            pageBook.showPage(pageTreeViewer.getControl());
        }
    }

    protected abstract ITreeViewer createEditorTreeViewer();

    protected abstract ITreeViewer createPageTreeViewer();

    protected abstract Control createEditorTreeViewerControl(ITreeViewer viewer,
            Composite parent);

    protected abstract Control createPageTreeViewerControl(ITreeViewer viewer,
            Composite parent);

    protected abstract Object createEditorTreeViewerInput(
            IGraphicalEditor parentEditor);

    protected abstract Object createPageTreeViewerInput(Object pageInput);

    protected void configureEditorTreeViewer(ITreeViewer viewer) {
        configureTreeViewer(viewer);
    }

    protected void configurePageTreeViewer(ITreeViewer viewer) {
        configureTreeViewer(viewer);
    }

    protected void configureTreeViewer(ITreeViewer viewer) {
        viewer.addSelectionChangedListener(this);
    }

    public Control getControl() {
        return pageBook;
    }

    public void setFocus() {
        pageBook.setFocus();
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (selectionChangedListeners == null)
            selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
        selectionChangedListeners.add(listener);
    }

    public ISelection getSelection() {
        if (editorTreeViewer == null)
            return StructuredSelection.EMPTY;
        return editorTreeViewer.getSelection();
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (selectionChangedListeners == null)
            return;
        selectionChangedListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        if (editorTreeViewer != null) {
            editorTreeViewer.setSelection(selection);
        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        fireSelectionChanged(event.getSelection());
    }

    /**
     * Fires a selection changed event.
     * 
     * @param selection
     *            the new selection
     */
    protected void fireSelectionChanged(ISelection selection) {
        if (selectionChangedListeners == null)
            return;

        // create an event
        final SelectionChangedEvent event = new SelectionChangedEvent(this,
                selection);

        // fire the event
        Object[] listeners = selectionChangedListeners.toArray();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
     * int)
     */
    public void propertyChanged(Object source, int propId) {
        if (propId == IEditorPart.PROP_INPUT) {
            editorInputChanged();
        }
    }

    /**
     * 
     */
    protected void editorInputChanged() {
        refresh();
    }

    public void dispose() {
        unhookEditor(editor);
        super.dispose();
    }

}