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
package org.xmind.ui.internal.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.xmind.core.IWorkbook;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tree.ITreeViewer;
import org.xmind.gef.tree.TreeRootPart;
import org.xmind.gef.tree.TreeSelectTool;
import org.xmind.gef.tree.TreeViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.outline.GraphicalOutlinePage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dnd.MindMapElementTransfer;
import org.xmind.ui.internal.editpolicies.ModifiablePolicy;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapOutlinePage extends GraphicalOutlinePage {

    private class ShowWorkbookAction extends Action {

        public ShowWorkbookAction() {
            super(MindMapMessages.ShowWorkbook_text, AS_RADIO_BUTTON);
            setId("org.xmind.ui.showWorkbook"); //$NON-NLS-1$
            setToolTipText(MindMapMessages.ShowWorkbook_toolTip);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.WORKBOOK, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.WORKBOOK, false));
        }

        public void run() {
            setShowCurrentPageViewer(false);
        }
    }

    private class ShowCurrentSheetAction extends Action {

        public ShowCurrentSheetAction() {
            super(MindMapMessages.ShowSheet_text, AS_RADIO_BUTTON);
            setId("org.xmind.ui.showCurrentSheet"); //$NON-NLS-1$
            setToolTipText(MindMapMessages.ShowSheet_toolTip);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SHEET, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SHEET, false));
        }

        public void run() {
            setShowCurrentPageViewer(true);
        }
    }

    private int controlStyle;

    private EditDomain domain;

    IAction showWorkbookAction;

    IAction showCurrentSheetAction;

    public MindMapOutlinePage(IGraphicalEditor parentEditor, int controlStyle) {
        super(parentEditor);
        this.controlStyle = controlStyle;
        this.domain = new EditDomain();
        domain.installTool(GEF.TOOL_SELECT, new TreeSelectTool());
        domain.setCommandStack(parentEditor.getCommandStack());
        domain.installEditPolicy(MindMapUI.POLICY_MODIFIABLE,
                new ModifiablePolicy());
    }

    public void createControl(Composite parent) {
        showWorkbookAction = new ShowWorkbookAction();
        showCurrentSheetAction = new ShowCurrentSheetAction();

        IMenuManager menu = getSite().getActionBars().getMenuManager();
        menu.add(showWorkbookAction);
        menu.add(showCurrentSheetAction);
        menu.add(new Separator());
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

//        IToolBarManager toolBar = getSite().getActionBars().getToolBarManager();
//        toolBar.add(showWorkbookAction);
//        toolBar.add(showCurrentSheetAction);
//        toolBar.add(new Separator());
//        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        showWorkbookAction.setChecked(!isShowCurrentPageViewer());
        showCurrentSheetAction.setChecked(isShowCurrentPageViewer());

        super.createControl(parent);
    }

    protected TreeViewer createEditorTreeViewer() {
        MindMapTreeViewer viewer = new MindMapTreeViewer();
        viewer.getProperties().set(ITreeViewer.PROP_HEADER_VISIBLE, false);
        return viewer;
    }

    protected Control createEditorTreeViewerControl(ITreeViewer viewer,
            Composite parent) {
        Control control = ((TreeViewer) viewer).createControl(parent,
                controlStyle);
        hookViewerControl(viewer, control);
        return control;
    }

    protected Object createEditorTreeViewerInput(
            IGraphicalEditor parentEditor) {
        return parentEditor.getAdapter(IWorkbook.class);
    }

    protected ITreeViewer createPageTreeViewer() {
        MindMapTreeViewer viewer = new MindMapTreeViewer();
        viewer.getProperties().set(ITreeViewer.PROP_HEADER_VISIBLE, false);
        return viewer;
    }

    protected Control createPageTreeViewerControl(ITreeViewer viewer,
            Composite parent) {
        Control control = ((TreeViewer) viewer).createControl(parent,
                controlStyle);
        hookViewerControl(viewer, control);
        return control;
    }

    protected Object createPageTreeViewerInput(Object pageInput) {
        return pageInput;
    }

    protected void configureTreeViewer(ITreeViewer viewer) {
        super.configureTreeViewer(viewer);
        viewer.setEditDomain(domain);
        viewer.setPartFactory(MindMapUI.getMindMapTreePartFactory());
        viewer.setRootPart(new TreeRootPart());
    }

    protected void hookViewerControl(final IViewer viewer,
            final Control control) {
        final DragSource dragSource = new DragSource(control,
                DND.DROP_COPY | DND.DROP_MOVE);
        dragSource.setTransfer(
                new Transfer[] { MindMapElementTransfer.getInstance(),
                        TextTransfer.getInstance() });
        dragSource.addDragListener(new DragSourceListener() {

            Object[] elements;

            String text;

            public void dragStart(DragSourceEvent event) {
                elements = getElements();
                if (elements == null || elements.length == 0)
                    event.doit = false;
                else
                    text = createText(elements);
            }

            private Object[] getElements() {
                if (control instanceof Tree) {
                    TreeItem[] selection = ((Tree) control).getSelection();
                    if (selection.length > 0) {
                        Object[] elements = new Object[selection.length];
                        for (int i = 0; i < selection.length; i++) {
                            TreeItem item = selection[i];
                            Object data = item.getData();
                            if (data instanceof IPart) {
                                data = ((IPart) data).getModel();
                            }
                            elements[i] = data;
                        }
                        return elements;
                    }
                }
                return null;
            }

            public void dragSetData(DragSourceEvent event) {
                if (MindMapElementTransfer.getInstance()
                        .isSupportedType(event.dataType)) {
                    event.data = elements;
                } else if (TextTransfer.getInstance()
                        .isSupportedType(event.dataType)) {
                    event.data = text;
                }
            }

            private String createText(Object[] elements) {
                IDndClient textClient = MindMapUI.getMindMapDndSupport()
                        .getDndClient(MindMapUI.DND_TEXT);
                if (textClient == null)
                    return null;

                Object data = textClient.toTransferData(elements, viewer);
                return data instanceof String ? (String) data : null;
            }

            public void dragFinished(DragSourceEvent event) {
            }

        });
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                dragSource.dispose();
            }
        });

        control.addListener(SWT.MouseDoubleClick, new Listener() {
            public void handleEvent(Event event) {
                handleDoubleClick(event, viewer, control);
                event.doit = false;
            }
        });
    }

    protected void handleDoubleClick(Event event, IViewer viewer,
            Control control) {
        startEditing(viewer, control);
    }

    private void startEditing(IViewer viewer, Control control) {
        Tree tree = (Tree) control;
        TreeItem[] selection = tree.getSelection();
        if (selection.length == 0)
            return;
        final TreeItem item = selection[0];
        if (!(item.getData() instanceof TopicTreePart)) {
            return;
        }

        TreeEditor editor = new TreeEditor(tree) {
            @Override
            public void layout() {
                super.layout();
                Control editor = getEditor();
                if (editor == null || editor.isDisposed())
                    return;
                Rectangle bounds = editor.getBounds();
                Point prefSize = editor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                if (prefSize.y > bounds.height) {
                    bounds.y += (bounds.height - prefSize.y - 1) / 2;
                    bounds.height = prefSize.y;
                }
                editor.setBounds(bounds);
            }
        };
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;

        final Text text = new Text(tree, SWT.SINGLE | SWT.BORDER);
        text.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_IBEAM));
        editor.setEditor(text, item);

        final String oldValue = item.getText();
        text.setText(oldValue);
        text.setFocus();
        text.selectAll();
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                item.setText(text.getText());
                modifyTreeItem(item);

                // This async process fixes a bug on Leopard:
                // Whole workbench crashes
                e.display.asyncExec(new Runnable() {
                    public void run() {
                        text.dispose();
                    }
                });

            }
        });

        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.character) {
                case SWT.ESC:
                    item.setText(oldValue);

                    // This async process fixes a bug on Leopard:
                    // Whole workbench crashes
                    e.display.asyncExec(new Runnable() {
                        public void run() {
                            text.dispose();
                        }
                    });

                    break;
                case SWT.CR:
                    item.setText(text.getText());
                    modifyTreeItem(item);

                    // This async process fixes a bug on Leopard:
                    // Whole workbench crashes
                    e.display.asyncExec(new Runnable() {
                        public void run() {
                            text.dispose();
                        }
                    });

                    break;
                }
            }
        });
    }

    protected void modifyTreeItem(TreeItem item) {
        Object o = item.getData();
        if (o instanceof IPart) {
            IPart part = (IPart) o;
            part.handleRequest(new Request(GEF.REQ_MODIFY)
                    .setViewer(part.getSite().getViewer())
                    .setParameter(GEF.PARAM_TEXT, item.getText())
                    .setPrimaryTarget(part), GEF.ROLE_MODIFIABLE);
        }
    }

}