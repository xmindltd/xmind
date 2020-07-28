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

package org.xmind.ui.internal.views;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.Page;
import org.xmind.core.Core;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dialogs.RevisionPreviewDialog;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.TextFormatter;
import org.xmind.ui.viewers.SWTUtils;

/**
 * @author Frank Shaka
 * 
 */
public class RevisionsPage extends Page
        implements ICoreEventListener, IAdaptable {

    /**
     * @author Frank Shaka
     * 
     */
    public static class RevisionContentProvider
            implements IStructuredContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            return ((IRevisionManager) inputElement).getRevisions().toArray();
        }

    }

    private static class RevisionNumberLabelProvider
            extends ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.valueOf(revision.getRevisionNumber());
        }

    }

    private static class RevisionTimeLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.format("%tT", revision.getTimestamp()); //$NON-NLS-1$
        }

    }

    private static class RevisionDateLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.format("%tF", revision.getTimestamp()); //$NON-NLS-1$
        }

    }

    private class RevisionOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            handleOpen(event.getSelection());
        }

    }

    private ISheet sheet;

    private Control control;

    private TableViewer viewer;

    private RotatableWrapLabel titleLabel;

    private IRevisionManager revisionManager;

    private ICoreEventRegister coreEventRegister = new CoreEventRegister(this);

    private ICoreEventRegister topicEventRegister = new CoreEventRegister(this);

    private MenuManager popupMenuManager;

    /**
     * 
     */
    public RevisionsPage(IGraphicalEditorPage source) {
        this.sheet = (ISheet) source.getInput();
        this.revisionManager = this.sheet.getOwnedWorkbook()
                .getRevisionRepository()
                .getRevisionManager(sheet.getId(), IRevision.SHEET);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createTitleLabel(composite);

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Control viewerControl = createViewer(composite);
        viewerControl
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        setControl(composite);

        registerCoreEvents();

        getSite().setSelectionProvider(viewer);

        popupMenuManager = new MenuManager("#popup"); //$NON-NLS-1$
        popupMenuManager.add(new GroupMarker("start")); //$NON-NLS-1$
        popupMenuManager
                .add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        popupMenuManager.add(new GroupMarker("end")); //$NON-NLS-1$
        getSite().registerContextMenu(MindMapUI.VIEW_REVISIONS,
                popupMenuManager, viewer);
        final Menu popupMenu = popupMenuManager
                .createContextMenu(viewerControl);
        viewerControl.setMenu(popupMenu);

        composite.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event event) {
                titleLabel.setPrefWidth(composite.getSize().x);
            }
        });
    }

    private void setControl(Control control) {
        this.control = control;
    }

    private void createTitleLabel(Composite parent) {
        FigureCanvas canvas = new FigureCanvas(parent);
        canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        canvas.setVisible(true);
        canvas.setScrollBarVisibility(FigureCanvas.NEVER);
        titleLabel = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
        titleLabel.setSingleLine(true);
        titleLabel.setAbbreviated(true);
        canvas.setContents(titleLabel);
        titleLabel.setText(getTitleText());
    }

    private Control createViewer(Composite parent) {
        viewer = new TableViewer(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setContentProvider(new RevisionContentProvider());

        TableViewerColumn col0 = new TableViewerColumn(viewer, SWT.RIGHT);
        col0.getColumn().setText("#"); //$NON-NLS-1$
        col0.getColumn().setWidth(36);
        col0.setLabelProvider(new RevisionNumberLabelProvider());

        TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.LEFT);
        col1.getColumn().setText(MindMapMessages.RevisionsView_DateColumn_text);
        col1.getColumn().setWidth(120);
        col1.setLabelProvider(new RevisionDateLabelProvider());

        TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.LEFT);
        col2.getColumn().setText(MindMapMessages.RevisionsView_TimeColumn_text);
        col2.getColumn().setWidth(120);
        col2.setLabelProvider(new RevisionTimeLabelProvider());

        viewer.setInput(revisionManager);

        viewer.addOpenListener(new RevisionOpenListener());
        viewer.getTable().addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (SWTUtils.matchKey(e.stateMask, e.keyCode, 0, SWT.SPACE)) {
                    handleOpen(viewer.getSelection());
                }
            }
        });

        viewer.getControl().setToolTipText(
                MindMapMessages.RevisionPage_ShowDetails_message);

        return viewer.getControl();
    }

    /**
     * 
     */
    private void registerCoreEvents() {
        coreEventRegister.setNextSourceFrom(revisionManager);
        coreEventRegister.register(Core.RevisionAdd);
        coreEventRegister.register(Core.RevisionRemove);
        coreEventRegister.setNextSourceFrom(sheet);
        coreEventRegister.register(Core.TitleText);
        coreEventRegister.register(Core.RootTopic);
        ITopic rootTopic = sheet.getRootTopic();
        topicEventRegister.setNextSourceFrom(rootTopic);
        topicEventRegister.register(Core.TitleText);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        if (viewer != null) {
            if (viewer.getControl() != null
                    && !viewer.getControl().isDisposed()) {
                viewer.getControl().setMenu(null);
            }
            viewer = null;
        }
        if (popupMenuManager != null) {
            popupMenuManager.dispose();
            popupMenuManager = null;
        }
        topicEventRegister.unregisterAll();
        coreEventRegister.unregisterAll();
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return control;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private void viewRevision(IRevision revision) {
        List<IRevision> revisions = revisionManager.getRevisions();
        int index = revisions.indexOf(revision);
        RevisionPreviewDialog dialog = new RevisionPreviewDialog(
                getSite().getShell(), sheet, revisions, index);
        dialog.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventListener#handleCoreEvent(org.xmind.core
     * .event.CoreEvent)
     */
    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.RevisionAdd.equals(type) || Core.RevisionRemove.equals(type)) {
            asyncExec(new Runnable() {
                public void run() {
                    if (viewer != null) {
                        viewer.refresh();
                    }
                }
            });
        } else if (Core.TitleText.equals(type)) {
            asyncExec(new Runnable() {
                public void run() {
//                    if (titleLabel != null && !titleLabel.isDisposed()) {
                    if (titleLabel != null) {
                        titleLabel.setText(getTitleText());
                    }
                }
            });
        } else if (Core.RootTopic.equals(type)) {
            topicEventRegister.unregisterAll();
            ITopic rootTopic = sheet.getRootTopic();
            topicEventRegister.setNextSourceFrom(rootTopic);
            topicEventRegister.register(Core.TitleText);
        }
    }

    private void asyncExec(Runnable runnable) {
        getSite().getWorkbenchWindow().getWorkbench().getDisplay()
                .asyncExec(runnable);
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == ISelectionProvider.class)
            return viewer;
        return null;
    }

    /**
     * @param selection
     */
    private void handleOpen(ISelection selection) {
        if (selection.isEmpty())
            return;
        IRevision revision = (IRevision) ((IStructuredSelection) selection)
                .getFirstElement();
        viewRevision(revision);
    }

    private String getTitleText() {
        String text = String.format("%s (%s)", sheet.getTitleText(), //$NON-NLS-1$
                sheet.getRootTopic().getTitleText());
        return TextFormatter.removeNewLineCharacter(text);
    }

}
