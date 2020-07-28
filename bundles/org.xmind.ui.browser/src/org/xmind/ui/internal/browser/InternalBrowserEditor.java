/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.browser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.browser.IBrowserViewer;
import org.xmind.ui.browser.IBrowserViewerContainer;
import org.xmind.ui.internal.browser.actions.CopyAction;
import org.xmind.ui.internal.browser.actions.CutAction;
import org.xmind.ui.internal.browser.actions.DeleteAction;
import org.xmind.ui.internal.browser.actions.PasteAction;

public class InternalBrowserEditor extends EditorPart
        implements IBrowserViewerContainer {

    public static final String BROWSER_EDITOR_ID = "org.xmind.ui.browser.editor"; //$NON-NLS-1$

    private BrowserViewer viewer;

    private String initialURL;

    private Image image;

    private Map<String, IAction> actions;

    private boolean disposed;

    private boolean lockName;

    public String getClientId() {
        return getBrowserEditorInput().getClientId();
    }

    public IAction getAction(String id) {
        return actions == null ? null : actions.get(id);
    }

    public void doSave(IProgressMonitor monitor) {
    }

    public void doSaveAs() {
    }

    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        if (input instanceof BrowserEditorInput) {
            BrowserEditorInput bei = (BrowserEditorInput) input;
            initialURL = null;
            if (bei.getURL() != null)
                initialURL = bei.getURL();
            if (viewer != null && viewer.getControl() != null
                    && !viewer.getControl().isDisposed()) {
                viewer.setURL(initialURL);
//                int style = bei.getStyle();
//                if ((style & IBrowserSupport.NO_LOCATION_BAR) != 0 && (style
//                        & IBrowserSupport.NO_EXTRA_CONTRIBUTIONS) != 0) {
//                    style |= IBrowserSupport.NO_TOOLBAR;
//                }
//                viewer.changeStyle(style);
//                site.getWorkbenchWindow().getActivePage().activate(this);
//                if (initialURL != null
//                        && ((style & IBrowserSupport.NO_TOOLBAR) == 0 && (style
//                                & IBrowserSupport.NO_LOCATION_BAR) == 0)) {
//                    setContentDescription(initialURL);
//                } else {
//                    setContentDescription(""); //$NON-NLS-1$
//                }
            }

            setPartName(bei.getName());
            setTitleToolTip(bei.getToolTipText());
            lockName = bei.isNameLocked();

            Image oldImage = image;
            ImageDescriptor id = bei.getImageDescriptor();
            image = id.createImage();

            setTitleImage(image);
            if (oldImage != null && !oldImage.isDisposed())
                oldImage.dispose();
        } else
            throw new PartInitException(NLS.bind(
                    BrowserMessages.BrowserEditor_ErrorInvalidEditorInput_message,
                    input.getName()));

        setSite(site);
        setInput(input);
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void dispose() {
        if (image != null && !image.isDisposed())
            image.dispose();
        image = null;

        super.dispose();
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void createPartControl(final Composite parent) {
        int style = getBrowserEditorInput().getStyle();
        viewer = new BrowserViewer(parent, style, this);
        viewer.setURL(initialURL);

        addAction(new CopyAction(viewer));
        addAction(new CutAction(viewer));
        addAction(new PasteAction(viewer));
        addAction(new DeleteAction(viewer));

        PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (lockName)
                    return;
                if (IBrowserViewer.PROPERTY_TITLE
                        .equals(event.getPropertyName())) {
                    if (event.getNewValue() != null)
                        setPartName((String) event.getNewValue());
                } else if (IBrowserViewer.PROPERTY_LOCATION
                        .equals(event.getPropertyName())) {
                    String location = (String) event.getNewValue();
                    int currentStyle = getBrowserEditorInput().getStyle();
                    if (location == null || ((currentStyle
                            & IBrowserSupport.NO_TOOLBAR) == 0
                            && (currentStyle
                                    & IBrowserSupport.NO_LOCATION_BAR) == 0)) {
                        location = ""; //$NON-NLS-1$
                    }
                    Label descriptionLabel = viewer.getDescriptionLabel();
                    if (descriptionLabel != null
                            && !descriptionLabel.isDisposed()) {
                        descriptionLabel.setText(location);
                        descriptionLabel.setToolTipText(location);
                    }
                }
            }
        };
        viewer.addPropertyChangeListener(propertyChangeListener);

        viewer.getBusyIndicator()
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        if (!parent.isDisposed()) {
                            parent.getDisplay().asyncExec(new Runnable() {
                                public void run() {
                                    if (!parent.isDisposed() && image != null
                                            && !image.isDisposed()) {
                                        Image currentImage = viewer
                                                .getBusyIndicator()
                                                .getCurrentImage();
                                        if (currentImage == null
                                                || !viewer.getBusyIndicator()
                                                        .isAnimating()) {
                                            setTitleImage(image);
                                        } else {
                                            setTitleImage(currentImage);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void addAction(IAction action) {
        String actionId = action.getId();
        if (actionId == null)
            return;

        if (actions == null)
            actions = new HashMap<String, IAction>();
        actions.put(actionId, action);
    }

    /**
     * Returns the web editor input, if available. If the input was of another
     * type, <code>null</code> is returned.
     * 
     * @return org.eclipse.ui.internal.browser.IWebBrowserEditorInput
     */
    protected BrowserEditorInput getBrowserEditorInput() {
        return (BrowserEditorInput) getEditorInput();
    }

    /**
     * Open the input in the internal Web browser.
     */
    public static void open(BrowserEditorInput input) {
        IWorkbenchWindow workbenchWindow = BrowserPlugin.getDefault()
                .getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = workbenchWindow.getActivePage();

        try {
            IEditorReference[] editors = page.getEditorReferences();
            int size = editors.length;
            for (int i = 0; i < size; i++) {
                if (BROWSER_EDITOR_ID.equals(editors[i].getId())) {
                    IEditorPart editor = editors[i].getEditor(true);
                    if (editor != null
                            && editor instanceof InternalBrowserEditor) {
                        InternalBrowserEditor editor2 = (InternalBrowserEditor) editor;
                        BrowserEditorInput input2 = editor2
                                .getBrowserEditorInput();
                        if (input2 == null || input.canReplaceInput(input2)) {
                            editor.init(editor.getEditorSite(), input);
                            return;
                        }
                    }
                }
            }

            page.openEditor(input, InternalBrowserEditor.BROWSER_EDITOR_ID);
        } catch (Exception e) {
        }
    }

    public void setFocus() {
        if (viewer != null)
            viewer.setFocus();
    }

    public BrowserViewer getViewer() {
        return viewer;
    }

    public boolean close() {
        final boolean[] result = new boolean[1];
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                result[0] = getEditorSite().getPage()
                        .closeEditor(InternalBrowserEditor.this, false);
            }
        });
        return result[0];
    }

    public IActionBars getActionBars() {
        return getEditorSite().getActionBars();
    }

    public void openInExternalBrowser(String url) {
        BrowserUtil.gotoUrl(url);
    }

    public void setText(String html) {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.setText(html);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.browser.IBrowserViewerContainer#openNewBrowser()
     */
    public Browser openNewBrowser() {
        final Browser[] ret = new Browser[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final BrowserEditorInput input = new BrowserEditorInput("", //$NON-NLS-1$
                        getNewSecondaryId(),
                        getBrowserEditorInput().getStyle());
                IEditorPart editor = getSite().getPage().openEditor(input,
                        BROWSER_EDITOR_ID);
                if (editor instanceof InternalBrowserEditor) {
                    ret[0] = ((InternalBrowserEditor) editor).getViewer()
                            .getBrowser();
                }
            }
        });
        return ret[0];
    }

    private static Map<String, Integer> numbers = new HashMap<String, Integer>();

    private String getNewSecondaryId() {
        Integer num = numbers.get(getClientId());
        if (num == null) {
            num = Integer.valueOf(1);
        } else {
            num = Integer.valueOf(num.intValue() + 1);
        }
        numbers.put(getClientId(), num);
        return getClientId() + "-" + num.toString(); //$NON-NLS-1$
    }

}