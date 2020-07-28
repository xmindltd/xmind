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

package org.xmind.ui.internal.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.PageBook;
import org.xmind.core.IRevision;
import org.xmind.core.ISheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.service.IRevealService;
import org.xmind.gef.tool.BrowsingTool;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.gallery.NavigationViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.mindmap.MindMapRevealService;
import org.xmind.ui.internal.mindmap.MindMapViewer;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.Cancelable;
import org.xmind.ui.util.ICancelable;
import org.xmind.ui.viewers.SWTUtils;

/**
 * @author Frank Shaka
 */
public class RevisionPreviewDialog extends Dialog {

    private class RevisionMindMapViewer extends MindMapViewer {
        protected Control internalCreateControl(Composite parent, int style) {
            FigureCanvas canvas = new FigureCanvas(parent, style,
                    getLightweightSystem());
            canvas.setViewport(getViewport());
            return canvas;
        }
    }

    private static final String USE_STORED_SIZE = "USE_STORED_SIZE"; //$NON-NLS-1$

    private static final IShellProvider NO_PARENT_SHELL = new IShellProvider() {
        public Shell getShell() {
            return null;
        }
    };

    private class DefaultPreviewTool extends BrowsingTool {

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.gef.tool.AbstractTool#handleKeyDown(org.xmind.gef.event
         * .KeyEvent)
         */
        @Override
        protected boolean handleKeyDown(KeyEvent ke) {
            if (SWTUtils.matchKey(ke.getState(), ke.keyCode, 0,
                    SWT.ARROW_LEFT)) {
                asyncExec(new Runnable() {
                    public void run() {
                        setIndex(index - 1);
                    }
                });
            } else if (SWTUtils.matchKey(ke.getState(), ke.keyCode, 0,
                    SWT.ARROW_RIGHT)) {
                asyncExec(new Runnable() {
                    public void run() {
                        setIndex(index + 1);
                    }
                });
            }
            return super.handleKeyDown(ke);
        }

    }

    private class ContainerLayout extends Layout {

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets
         * .Composite, int, int, boolean)
         */
        @Override
        protected org.eclipse.swt.graphics.Point computeSize(
                Composite composite, int wHint, int hHint, boolean flushCache) {
            if (wHint < 0 || hHint < 0) {
                Control[] children = composite.getChildren();
                int w = Math.max(0, wHint);
                int h = Math.max(0, hHint);
                for (int i = 0; i < children.length; i++) {
                    Control child = children[i];
                    Point childSize = child.getSize();
                    w = Math.max(w, childSize.x);
                    h = Math.max(h, childSize.y);
                }
                return new org.eclipse.swt.graphics.Point(w, h);
            }
            return new org.eclipse.swt.graphics.Point(wHint, hHint);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.
         * Composite , boolean)
         */
        @Override
        protected void layout(Composite composite, boolean flushCache) {
            Rectangle area = composite.getClientArea();
            int h = NavigationViewer.BIG_HEIGHT + 20;
            pageBook.setBounds(area.x, area.y, area.width, area.height - h);
            navBar.getControl().setBounds(area.x, area.y + area.height - h,
                    area.width, h);
        }

    }

    private static class NavigationLabelProvider extends LabelProvider
            implements IFontProvider {

        private Font font;

        private Image image = null;

        /**
         * 
         */
        public NavigationLabelProvider() {
            this.font = FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, 32);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            if (element instanceof IRevision) {
                IRevision revision = (IRevision) element;
                return String.valueOf(revision.getRevisionNumber());
            }
            return ""; //$NON-NLS-1$
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof ISheet) {
                return getSheetImage();
            }
            return super.getImage(element);
        }

        private Image getSheetImage() {
            if (image == null) {
                image = MindMapUI.getImages()
                        .get(IMindMapImages.DEFAULT_THUMBNAIL).createImage();
            }
            return image;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            return font;
        }

        @Override
        public void dispose() {
            if (image != null) {
                image.dispose();
                image = null;
            }
            super.dispose();
        }
    }

    private class NavigationSelectionChangedListener
            implements ISelectionChangedListener {

        private ICancelable updater = null;

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged
         * (org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event) {
            if (updater != null) {
                updater.cancel();
            }
            final ISelection selection = event.getSelection();
            if (selection.isEmpty())
                return;
            updater = new Cancelable() {
                @Override
                protected void doJob() {
                    updateSelection(selection);
                }
            };
            Display.getCurrent().timerExec(150, updater);
        }

        private void updateSelection(ISelection selection) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if (sel == sourceSheet) {
                setIndex(revisions.size());
            } else {
                setIndex(revisions.indexOf(sel));
            }
        }

    }

    private Shell parentShell;

    private ISheet sourceSheet;

    private List<IRevision> revisions;

    private int index;

    private PageBook pageBook;

    private NavigationViewer navBar = null;

    private Map<Object, MindMapViewer> viewers = new HashMap<Object, MindMapViewer>();

    private MindMapViewer viewer = null;

    private Control corruptionWarning = null;

    private Rectangle actualBounds = null;

    private Listener widgetListener = new Listener() {
        public void handleEvent(Event event) {
            if ((event.type == SWT.Traverse
                    && event.detail == SWT.TRAVERSE_ESCAPE)
                    || (event.type == SWT.KeyDown
                            && (SWTUtils.matchKey(event.stateMask,
                                    event.keyCode, 0, SWT.ESC)
                                    || SWTUtils.matchKey(event.stateMask,
                                            event.keyCode, 0, SWT.SPACE)))) {
                close();
            }
        }
    };

    /**
     * @param parentShell
     * @param revisions
     */
    public RevisionPreviewDialog(Shell parentShell, ISheet sourceSheet,
            List<IRevision> revisions, int index) {
        super(NO_PARENT_SHELL);
        this.parentShell = parentShell;
        this.sourceSheet = sourceSheet;
        this.revisions = revisions;
        this.index = index;
        setShellStyle(SWT.DIALOG_TRIM | SWT.MAX | SWT.MIN | SWT.RESIZE
                | getDefaultOrientation());
        setBlockOnOpen(false);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    public void update() {
        Object selection = getSelection();
        updateShellTitle(selection);
        showPreviewViewer(selection);
        updateNavBar(selection);
    }

    /**
     * 
     */
    private void updateNavBar(Object selection) {
        navBar.setSelection(new StructuredSelection(selection));
        navBar.getControl().setFocus();
        hookWidget(navBar.getControl());
    }

    private Object getSelection() {
        if (index < 0)
            return null;
        if (index >= revisions.size()) {
            return sourceSheet;
        }
        return revisions.get(index);
    }

    private void updateShellTitle(Object selection) {
        String sheetTitle = String.format("\"%s - %s\"", //$NON-NLS-1$
                sourceSheet.getTitleText(),
                sourceSheet.getRootTopic().getTitleText());
        String title;
        if (selection instanceof IRevision) {
            title = NLS.bind(
                    DialogMessages.RevisionPreviewDialog_Revision_titlePattern,
                    String.valueOf(((IRevision) selection).getRevisionNumber()),
                    sheetTitle);
        } else {
            title = NLS.bind(
                    DialogMessages.RevisionPreviewDialog_CurrentRevision_title,
                    sheetTitle);
        }
        getShell().setText(title);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Color background = parent.getDisplay()
                .getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        parent.setBackground(background);

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new ContainerLayout());
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setBackground(background);

        pageBook = new PageBook(container, SWT.NONE);
        pageBook.setBackground(background);
        hookWidget(pageBook);

        createNavigationBar(container);

        return pageBook;
    }

    /**
     * @param control
     */
    private void hookWidget(Control control) {
        control.addListener(SWT.Traverse, widgetListener);
        control.addListener(SWT.KeyDown, widgetListener);
    }

    /**
     * @param container
     * @return
     */
    private void createNavigationBar(Composite parent) {
        navBar = new NavigationViewer();
        navBar.setContentProvider(new ArrayContentProvider());
        navBar.setLabelProvider(new NavigationLabelProvider());
        navBar.createControl(parent);
        Object[] elements = new Object[revisions.size() + 1];
        revisions.toArray(elements);
        elements[elements.length - 1] = sourceSheet;
        navBar.setInput(elements);
        navBar.addSelectionChangedListener(
                new NavigationSelectionChangedListener());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 0;
        gridData.verticalAlignment = 0;
        gridData.verticalIndent = 0;
        gridData.verticalSpan = 0;
        navBar.getControl();
        hookWidget(navBar.getControl());
    }

    private void showPreviewViewer(Object selection) {
        if (selection instanceof IRevision) {
            IRevision revision = (IRevision) selection;
            ISheet sheet = (ISheet) revision.getContent();
            if (sheet == null) {
                pageBook.showPage(getCorruptionWarning());
            } else {
                viewer = getRevisionViewer(revision, sheet);
                pageBook.showPage(viewer.getControl());
            }
        } else {
            viewer = getRevisionViewer(sourceSheet, sourceSheet);
            pageBook.showPage(viewer.getControl());
        }
    }

    private Control getCorruptionWarning() {
        if (corruptionWarning == null) {
            corruptionWarning = createCorruptionWarning();
        }
        return corruptionWarning;
    }

    /**
     * @return
     */
    private Control createCorruptionWarning() {
        Composite composite = new Composite(pageBook, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        label.setText(
                DialogMessages.RevisionPreviewDialog_CorruptedRevision_message);

        return composite;
    }

    private MindMapViewer getRevisionViewer(Object selection, ISheet sheet) {
        MindMapViewer viewer = viewers.get(selection);
        if (viewer == null) {
            viewer = createViewer(pageBook, sheet);
            viewers.put(selection, viewer);
        }
        return viewer;
    }

    public MindMapViewer createViewer(Composite parent, ISheet sheet) {
        MindMapViewer viewer = new RevisionMindMapViewer();
        initViewer(viewer);
        viewer.createControl(parent);
        viewer.getCanvas().setScrollBarVisibility(FigureCanvas.AUTOMATIC);
        hookWidget(viewer.getControl());
        viewer.setInput(new MindMap(sheet));
        return viewer;
    }

    public void initViewer(MindMapViewer viewer) {
        viewer.getProperties().set(IMindMapViewer.VIEWER_CENTERED, true);
        viewer.getProperties().set(IMindMapViewer.VIEWER_MARGIN, 50);
        viewer.getProperties().set(IMindMapViewer.VIEWER_CORNERED, true);

        IRevealService revealService = new MindMapRevealService(viewer);
        viewer.installService(IRevealService.class, revealService);
        revealService.setActive(true);

        ITool tool = new DefaultPreviewTool();
        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_DEFAULT, tool);
        editDomain.setDefaultTool(GEF.TOOL_DEFAULT);
        viewer.setEditDomain(editDomain);
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return MindMapUIPlugin.getDefault()
                .getDialogSettings("org.xmind.ui.RevisionsDialog"); //$NON-NLS-1$
    }

    protected Point getInitialSize() {
        IDialogSettings settings = getDialogBoundsSettings();
        if (settings.getBoolean(USE_STORED_SIZE)) {
            return super.getInitialSize();
        }
        settings.put(USE_STORED_SIZE, true);
        return new Point(720, 620);
    }

    private void setIndex(int index) {
        index = Math.max(0, Math.min(revisions.size(), index));
        if (index == this.index)
            return;
        this.index = index;
        update();
    }

    private void asyncExec(final Runnable job) {
        parentShell.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (getShell() == null || getShell().isDisposed())
                    return;
                job.run();
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#open()
     */
    @Override
    public int open() {
        if (getShell() == null || getShell().isDisposed()) {
            create();
        }
        constrainShellSize();

        update();

        // open the window
        getShell().open();
        return OK;
    }

    public void open(Rectangle sourceBounds) {
        if (getShell() == null || getShell().isDisposed()) {
            create();
        }
        constrainShellSize();

        Shell shell = getShell();
        actualBounds = shell.getBounds();

        shell.setRedraw(false);
        shell.setBounds(sourceBounds);
        shell.setAlpha(0);
        shell.setVisible(true);
        shell.setActive();
        shell.setFocus();

        long start = System.currentTimeMillis();
        long end = start + 200;
        animateOpening(shell, sourceBounds, start, end);
    }

    /**
     * @param shell
     * @param r1
     * @param r2
     */
    private void animateOpening(final Shell shell, final Rectangle r1,
            final long start, final long end) {
        if (shell.isDisposed() || actualBounds == null)
            return;

        long time = System.currentTimeMillis();
        if (time > end) {
            finishOpening(shell);
        } else {
            double percent = ((double) (time - start))
                    / ((double) (end - start));
            int alpha = (int) (255 * percent);
            Rectangle r2 = actualBounds;
            int x = (int) ((r2.x - r1.x) * percent + r1.x);
            int y = (int) ((r2.y - r1.y) * percent + r1.y);
            int width = (int) ((r2.width - r1.width) * percent + r1.width);
            int height = (int) ((r2.height - r1.height) * percent + r1.height);
            shell.setAlpha(alpha);
            shell.setBounds(x, y, width, height);
            shell.getDisplay().timerExec(5, new Runnable() {
                public void run() {
                    animateOpening(shell, r1, start, end);
                }
            });
        }
    }

    /**
     * @param shell
     * @param targetBounds
     */
    private void finishOpening(Shell shell) {
        if (shell.isDisposed() || actualBounds == null)
            return;

        shell.setBounds(actualBounds);
        shell.setAlpha(255);
        shell.setVisible(true);
        shell.setActive();
        shell.setFocus();
        update();
        shell.setRedraw(true);
        actualBounds = null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        Shell shell = getShell();
        if (shell != null && !shell.isDisposed() && actualBounds != null) {
            shell.setBounds(actualBounds);
            actualBounds = null;
        }
        return super.close();
    }
}
