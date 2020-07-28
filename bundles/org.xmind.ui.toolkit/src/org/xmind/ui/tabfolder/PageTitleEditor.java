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
package org.xmind.ui.tabfolder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.texteditor.IFloatingTextEditorListener;
import org.xmind.ui.texteditor.TextEvent;

public class PageTitleEditor extends IFloatingTextEditorListener.Stub implements
        Listener {

    private CTabFolder tabFolder;

    private FloatingTextEditor editor;

    private CTabItem sourceItem;

    private List<IPageTitleChangedListener> listeners = null;

    private String contextId;

    private IServiceLocator serviceLocator;

    private IContextService cs;

    private IContextActivation ca;

    public PageTitleEditor(CTabFolder tabFolder) {
        this.tabFolder = tabFolder;
        this.editor = new FloatingTextEditor(tabFolder, SWT.BORDER | SWT.SINGLE);
        hookControl(tabFolder);
        hookEditor(editor);
    }

    public String getContextId() {
        return contextId;
    }

    public IServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public void setContextId(IServiceLocator serviceLocator, String contextId) {
        this.serviceLocator = serviceLocator;
        this.contextId = contextId;
    }

    public FloatingTextEditor getEditor() {
        return editor;
    }

    protected void hookControl(CTabFolder tabFolder) {
        tabFolder.addListener(SWT.MouseDoubleClick, this);
    }

    protected void hookEditor(FloatingTextEditor editor) {
        editor.addFloatingTextEditorListener(this);
    }

    private void startEditing(Event e) {
        CTabItem item = tabFolder.getItem(new Point(e.x, e.y));
        if (item != null)
            startEditing(tabFolder.indexOf(item));
    }

    public void startEditing(int pageIndex) {
        startEditing(tabFolder.getItem(pageIndex));
    }

    private void startEditing(CTabItem item) {
        cancelEditing();
        this.sourceItem = item;
        if (item == null)
            return;

        Rectangle bounds = item.getBounds();
        String text = item.getText();
        editor.setInitialLocation(new Point(bounds.x, bounds.y));
        editor.setInitialSize(new Point(bounds.width, bounds.height));
        editor.setInput(new Document(text));
        editor.open();
        if (editor.canDoOperation(ITextOperationTarget.SELECT_ALL)) {
            editor.doOperation(ITextOperationTarget.SELECT_ALL);
        }
    }

    public void cancelEditing() {
        editor.close();
    }

    public void finishEditing() {
        editor.close(true);
    }

    public void handleEvent(Event event) {
        switch (event.type) {
        case SWT.MouseDoubleClick:
            startEditing(event);
            break;
        case SWT.FocusOut:
            if (editor != null && editor.getControl() != null
                    && !editor.getControl().isDisposed()) {
                editor.getTextViewer().getTextWidget().removeListener(
                        SWT.FocusOut, this);
                editor.close(true);
            }
        }
    }

    public void addPageTitleChangedListener(IPageTitleChangedListener listener) {
        if (listeners == null)
            listeners = new ArrayList<IPageTitleChangedListener>();
        listeners.add(listener);
    }

    public void removePageTitleChangedListener(
            IPageTitleChangedListener listener) {
        if (listeners == null)
            return;
        listeners.remove(listener);
    }

    protected void firePageTitleChanged(final int pageIndex,
            final String newValue) {
        if (listeners == null)
            return;
        for (final Object l : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IPageTitleChangedListener) l).pageTitleChanged(pageIndex,
                            newValue);
                }
            });
        }
    }

    public void editingCanceled(TextEvent e) {
        deactivateContext();
        sourceItem = null;
    }

    public void editingFinished(TextEvent e) {
        deactivateContext();
        if (sourceItem == null)
            return;

        int pageIndex = tabFolder.indexOf(sourceItem);
        if (pageIndex >= 0) {
            firePageTitleChanged(pageIndex, e.text);
        }
        sourceItem = null;
    }

    public void textChanged(TextEvent e) {
        updateEditorBounds();
    }

    public void editingStarted(TextEvent e) {
        super.editingStarted(e);
        activateContext();
        editor.getTextViewer().getTextWidget().addListener(SWT.FocusOut, this);
    }

    private void activateContext() {
        if (getContextId() != null && getServiceLocator() != null) {
            cs = (IContextService) getServiceLocator().getService(
                    IContextService.class);
            if (cs != null) {
                ca = cs.activateContext(getContextId());
            }
        }
    }

    private void deactivateContext() {
        if (cs != null && ca != null) {
            cs.deactivateContext(ca);
        }
        cs = null;
        ca = null;
    }

    private void updateEditorBounds() {
        //TODO update page title editor bounds
    }

}