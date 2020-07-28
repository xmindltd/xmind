package org.xmind.ui.internal.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.ui.blackbox.BlackBox;
import org.xmind.ui.blackbox.BlackBoxManager;
import org.xmind.ui.blackbox.IBlackBoxMap;
import org.xmind.ui.blackbox.IBlackBoxVersion;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.LazyPasswordBasedEncryptor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.SWTUtils;

public class BlackBoxView extends ViewPart implements ICoreEventListener {

    private static final String MAP_REMOVE = "mapRemove"; //$NON-NLS-1$

    private static final String VERSION_REMOVE = "versionRemove"; //$NON-NLS-1$

    private static final String VERSION_ADD = "versionAdd"; //$NON-NLS-1$

    private TreeViewer viewer;

    private MenuManager contextMenu;

    private CoreEventRegister coreEventRegister = new CoreEventRegister(this);

    private List<IAction> actions = new ArrayList<IAction>();

    public BlackBoxView() {
    }

    private static class BlackBoxContentProvider
            implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            return (IBlackBoxMap[]) inputElement;
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IBlackBoxMap) {
                return ((IBlackBoxMap) parentElement).getVersions().toArray();
            }
            return null;
        }

        public Object getParent(Object element) {
            if (element instanceof IBlackBoxVersion) {
                return ((IBlackBoxVersion) element).getMap();
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            if (element instanceof IBlackBoxMap) {
                return !((IBlackBoxMap) element).getVersions().isEmpty();
            }
            return false;
        }

    }

    private static class BlackBoxLabelProvide extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof IBlackBoxMap) {
                String filePath = ((IBlackBoxMap) element).getSource();
                int index = filePath.lastIndexOf(File.separatorChar);
                String fileName = index <= 0 ? filePath
                        : filePath.substring(index + 1);
                index = fileName.lastIndexOf('.');
                String fileNoExtension = index <= 0 ? fileName
                        : fileName.substring(0, index);
                return fileNoExtension;

            } else if (element instanceof IBlackBoxVersion) {
                return ((IBlackBoxVersion) element).getTimestamp();
            }
            return null;
        }
    }

    private class VersionsLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof IBlackBoxMap) {
                String path = ((IBlackBoxMap) element).getSource();
                int index = path.lastIndexOf(File.separatorChar);
                String mapName = index <= 0 ? path : path.substring(index + 1);
                return mapName;
            } else if (element instanceof IBlackBoxVersion) {
                Long timestamp = Long
                        .valueOf(((IBlackBoxVersion) element).getTimestamp());
                return String.format("%tF %tT", timestamp, timestamp); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IBlackBoxMap) {
                ImageDescriptor image = MindMapUI.getImages()
                        .get(IMindMapImages.XMIND_FILE_ICON);
                if (image != null)
                    return image.createImage();
            }
            return null;
        }
    }

    private class VersionsInfoLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof IBlackBoxMap) {
                return ((IBlackBoxMap) element).getSource();
            } else if (element instanceof IBlackBoxVersion) {

                float fileSize = ((float) ((IBlackBoxVersion) element).getFile()
                        .length()) / 1024;
                String fss = String.valueOf(fileSize);
                int index = fss.indexOf('.');
                if (index < 0)
                    return fss + "KB"; //$NON-NLS-1$
                else
                    return fss.substring(0, index + 2) + "KB"; //$NON-NLS-1$
            }
            return null;
        }

    }

    private class OpenReversionAction extends Action
            implements ISelectionChangedListener {
        private File reversionFile;

        private IBlackBoxMap map = null;

        public OpenReversionAction() {
            setEnabled(false);
        }

        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            this.reversionFile = null;
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) selection;
                if (ss.size() == 1) {
                    Object element = ss.getFirstElement();
                    if (element instanceof IBlackBoxVersion) {
                        this.reversionFile = ((IBlackBoxVersion) element)
                                .getFile();
                        this.map = ((IBlackBoxVersion) element).getMap();
                    }
                }
            }
            setEnabled(this.reversionFile != null);
        }

        @Override
        public void run() {
            if (reversionFile == null || !reversionFile.exists() || map == null)
                return;
            handleOpen(reversionFile, map);
        }

    }

    private class DeleteBackupsAction extends Action
            implements ISelectionChangedListener {
        private List<IBlackBoxMap> mapsToDelete = new ArrayList<IBlackBoxMap>();
        private List<IBlackBoxVersion> versionsToDelete = new ArrayList<IBlackBoxVersion>();

        public DeleteBackupsAction() {
            setEnabled(false);
        }

        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            mapsToDelete.clear();
            versionsToDelete.clear();
            if (!selection.isEmpty()
                    && selection instanceof IStructuredSelection) {
                Iterator it = ((IStructuredSelection) selection).iterator();
                while (it.hasNext()) {
                    Object element = it.next();
                    if (element instanceof IBlackBoxVersion) {
                        versionsToDelete.add((IBlackBoxVersion) element);
                    } else if (element instanceof IBlackBoxMap) {
                        mapsToDelete.add((IBlackBoxMap) element);
                    }
                }
            }
            setEnabled(!(mapsToDelete.isEmpty() && versionsToDelete.isEmpty()));
        }

        @Override
        public void run() {
            if (versionsToDelete.isEmpty() && mapsToDelete.isEmpty())
                return;
            if (!versionsToDelete.isEmpty()) {
                for (IBlackBoxVersion version : versionsToDelete) {
                    IBlackBoxMap map = version.getMap();
                    BlackBox.removeVersion(map, version.getTimestamp());
                }
            }
            if (!mapsToDelete.isEmpty()) {
                for (IBlackBoxMap blackBoxMap : mapsToDelete) {
                    BlackBox.removeMap(blackBoxMap);
                }
            }
        }
    }

    private class VersionOpenListener implements IDoubleClickListener {

        public void doubleClick(DoubleClickEvent event) {
            handleOpen(event.getSelection());
        }

    }

    private class BlackBoxSorter extends ViewerSorter {
        @Override
        public int category(Object element) {
            if (element instanceof IBlackBoxMap) {
                return 0;
            } else if (element instanceof IBlackBoxVersion) {
                return 1;
            }
            return 2;
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        Label top = new Label(composite, SWT.WRAP);
        top.setText(Messages.BlackBoxView_Description_text);
        top.setFont(FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, -1));
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Control viewerControl = createViewer(composite);
        viewerControl
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fillAndRegisterMenu();

        registerCoreEvent();
    }

    private void registerCoreEvent() {
        coreEventRegister
                .setNextSourceFrom(BlackBoxManager.getInstance().getLibrary());
        coreEventRegister.register(VERSION_ADD);
        coreEventRegister.register(VERSION_REMOVE);
        coreEventRegister.register(MAP_REMOVE);
    }

    private Control createViewer(Composite parent) {
        viewer = new TreeViewer(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(true);
        viewer.setContentProvider(new BlackBoxContentProvider());
        viewer.setLabelProvider(new BlackBoxLabelProvide());

        TreeViewerColumn col0 = new TreeViewerColumn(viewer, SWT.LEFT);
        col0.getColumn().setText(Messages.BlackBoxView_Versions);
        col0.getColumn().setWidth(200);
        col0.setLabelProvider(new VersionsLabelProvider());

        TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.LEFT);
        col1.getColumn().setText(Messages.BlackBoxView_Info);
        col1.getColumn().setWidth(300);
        col1.setLabelProvider(new VersionsInfoLabelProvider());

        viewer.setInput(BlackBox.getMaps());

        getSite().setSelectionProvider(viewer);

        viewer.setAutoExpandLevel(2);

        viewer.setSorter(new BlackBoxSorter());

        viewer.addDoubleClickListener(new VersionOpenListener());
        viewer.getTree().addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (SWTUtils.matchKey(e.stateMask, e.keyCode, 0, SWT.SPACE)) {
                    handleOpen(viewer.getSelection());
                }
            }
        });

        return viewer.getControl();
    }

    private void fillAndRegisterMenu() {
        OpenReversionAction openAction = new OpenReversionAction();
        openAction.setText(Messages.BlackBoxView_OpenVersion);
        openAction.setToolTipText(Messages.BlackBoxView_OpenVersion);
        openAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.OPEN, true));
        openAction.setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.OPEN, false));
        addAction(openAction);

        DeleteBackupsAction deleteAction = new DeleteBackupsAction();
        deleteAction.setText(Messages.BlackBoxView_DeleteBackups);
        deleteAction.setToolTipText(Messages.BlackBoxView_DeleteBackups);
        deleteAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, true));
        deleteAction.setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, false));
        addAction(deleteAction);

//        IToolBarManager toolbar = getViewSite().getActionBars()
//                .getToolBarManager();
//        toolbar.add(openAction);
//        toolbar.add(deleteAction);
//        toolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        menu.add(openAction);
        menu.add(deleteAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        getViewSite().getActionBars().updateActionBars();

        contextMenu = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        contextMenu.add(openAction);
        contextMenu.add(deleteAction);
        viewer.getControl()
                .setMenu(contextMenu.createContextMenu(viewer.getControl()));
        getSite().registerContextMenu(contextMenu, viewer);
    }

    private void addAction(IAction action) {
        actions.add(action);
        if (action.getId() != null) {
            getViewSite().getActionBars().setGlobalActionHandler(action.getId(),
                    action);
        }
        if (action instanceof ISelectionChangedListener) {
            getViewSite().getSelectionProvider().addSelectionChangedListener(
                    (ISelectionChangedListener) action);
        }
    }

    public void setDamagedFile(File damagedFile) {
        if (damagedFile == null)
            return;
        String source = damagedFile.getAbsolutePath();
        IBlackBoxMap map = BlackBox.findMapBySource(source);
        if (map != null)
            viewer.setSelection(new StructuredSelection(map), true);
    }

    @Override
    public void setFocus() {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    public void dispose() {
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }
        for (IAction action : actions) {
            if (action instanceof ISelectionChangedListener) {
                getSite().getSelectionProvider().removeSelectionChangedListener(
                        (ISelectionChangedListener) action);
            }
        }
        actions.clear();
        coreEventRegister.unregisterAll();
        super.dispose();
    }

    private void handleOpen(ISelection selection) {
        File reversionFile = null;
        IBlackBoxMap map = null;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if (ss.size() == 1) {
                Object element = ss.getFirstElement();
                if (element instanceof IBlackBoxVersion) {
                    reversionFile = ((IBlackBoxVersion) element).getFile();
                    map = ((IBlackBoxVersion) element).getMap();
                } else if (element instanceof IBlackBoxMap) {
                    if (viewer.getExpandedState(element))
                        viewer.collapseToLevel(element, 2);
                    else
                        viewer.expandToLevel(element, 2);
                }
            }
        }
        if (reversionFile == null || !reversionFile.exists() || map == null)
            return;
        handleOpen(reversionFile, map);
    }

    private void handleOpen(File reversionFile, IBlackBoxMap map) {
        try {
            Core.getWorkbookBuilder()
                    .setEntryStreamNormalizer(new LazyPasswordBasedEncryptor());
            IWorkbook workbook = Core.getWorkbookBuilder()
                    .loadFromFile(reversionFile);
            IEditorInput input = MindMapUI.getEditorInputFactory()
                    .createEditorInputForPreLoadedWorkbook(workbook,
                            new File(map.getSource()).getName());
            getSite().getPage().openEditor(input, MindMapUI.MINDMAP_EDITOR_ID);
            if (workbook instanceof ICoreEventSource2) {
                ((ICoreEventSource2) workbook).registerOnceCoreEventListener(
                        Core.WorkbookPreSaveOnce, ICoreEventListener.NULL);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (CoreException e1) {
            e1.printStackTrace();
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    public void handleCoreEvent(CoreEvent event) {
        final String type = event.getType();
        getSite().getWorkbenchWindow().getWorkbench().getDisplay()
                .asyncExec(new Runnable() {
                    public void run() {
                        if (VERSION_REMOVE.equals(type)) {
                            viewer.refresh(true);
                        } else if (VERSION_ADD.equals(type)
                                || MAP_REMOVE.equals(type)) {
                            viewer.setInput(BlackBox.getMaps());
                        }
                    }
                });
    }
}
