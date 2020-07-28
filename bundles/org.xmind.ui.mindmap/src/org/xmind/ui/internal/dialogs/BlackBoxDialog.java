package org.xmind.ui.internal.dialogs;

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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.ViewComparator;
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
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.views.Messages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.LazyPasswordBasedEncryptor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;

public class BlackBoxDialog extends Dialog
        implements ICoreEventListener, ISelectionChangedListener {

    private static final String MAP_REMOVE = "mapRemove"; //$NON-NLS-1$

    private static final String VERSION_REMOVE = "versionRemove"; //$NON-NLS-1$

    private static final String VERSION_ADD = "versionAdd"; //$NON-NLS-1$

    private static final int DELETE_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;

    private TreeViewer viewer;

    private MenuManager contextMenu;

    private CoreEventRegister coreEventRegister = new CoreEventRegister(this);

    private IAction openAction, deleteAction;
    private Button openButton, deleteButton;
    private IStructuredSelection currentSelection;

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
                if (mapName.contains(".")) //$NON-NLS-1$
                    mapName = mapName.substring(0, mapName.lastIndexOf('.'));
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

    private class OpenReversionAction extends Action {

        public OpenReversionAction() {
            setEnabled(false);
        }

        @Override
        public void run() {
            handleOpen(currentSelection);
        }

    }

    private class DeleteBackupsAction extends Action {
        public DeleteBackupsAction() {
            setEnabled(false);
        }

        @Override
        public void run() {
            handleDelete();
        }
    }

    private class VersionOpenListener implements IDoubleClickListener {

        public void doubleClick(DoubleClickEvent event) {
            handleOpen(event.getSelection());
        }

    }

    private class BlackBoxComparator extends ViewComparator {
        @Override
        public int category(Object element) {
            if (element instanceof IBlackBoxMap) {
                return 0;
            } else if (element instanceof IBlackBoxVersion) {
                return 1;
            }
            return 2;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            if (e1 != null && e2 != null && e1 instanceof IBlackBoxVersion
                    && e2 instanceof IBlackBoxVersion) {

                long time1 = Long
                        .parseLong(((IBlackBoxVersion) e1).getTimestamp());
                long time2 = Long
                        .parseLong(((IBlackBoxVersion) e2).getTimestamp());

                return time1 - time2 > 0 ? -1 : 1;
            }

            return super.compare(viewer, e1, e2);
        }
    }

    public BlackBoxDialog(Shell parentShell) {
        super(parentShell);

        setShellStyle(SWT.MODELESS | SWT.RESIZE | SWT.DIALOG_TRIM | SWT.MIN
                | SWT.MAX);
        setBlockOnOpen(false);

    }

    @Override
    public void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(MindMapMessages.BlackBoxDialog_title);
    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 14;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createDescriptionArea(composite);
        createContentArea(composite);

        fillAndRegisterMenu();
        registerCoreEvent();

        return composite;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        layout.marginWidth = 13;
        layout.marginHeight = 23;
        layout.horizontalSpacing = 18;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        GridData data = new GridData(
                GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite);
        return composite;
    }

    private void createContentArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        composite.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);

        Control viewerControl = createViewer(composite);
        viewerControl
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private void createDescriptionArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        composite.setBackground(parent.getBackground());

        GridLayout middleLayerLayout = new GridLayout(1, false);
        middleLayerLayout.marginWidth = 0;
        middleLayerLayout.marginHeight = 18;
        composite.setLayout(middleLayerLayout);

        Label descriptionLabel = new Label(composite, SWT.WRAP);
        descriptionLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        descriptionLabel.setBackground(composite.getBackground());
        descriptionLabel.setText(Messages.BlackBoxView_Description_text);
        descriptionLabel.setFont(
                FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, -1));
    }

    private Control createViewer(Composite parent) {
        viewer = new TreeViewer(parent,
                SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(true);
        viewer.setContentProvider(new BlackBoxContentProvider());
        viewer.setLabelProvider(new BlackBoxLabelProvide());

        viewer.addSelectionChangedListener(this);

        TreeViewerColumn col0 = new TreeViewerColumn(viewer, SWT.LEFT);
        col0.getColumn().setText(Messages.BlackBoxView_Versions);
        col0.getColumn().setWidth(200);
        col0.setLabelProvider(new VersionsLabelProvider());

        TreeViewerColumn col1 = new TreeViewerColumn(viewer, SWT.LEFT);
        col1.getColumn().setText(Messages.BlackBoxView_Info);
        col1.getColumn().setWidth(268);
        col1.setLabelProvider(new VersionsInfoLabelProvider());

        viewer.setInput(BlackBox.getMaps());

        viewer.setAutoExpandLevel(2);

        viewer.setComparator(new BlackBoxComparator());

        viewer.addDoubleClickListener(new VersionOpenListener());

        return viewer.getControl();
    }

    private void fillAndRegisterMenu() {
        //TODO String is in View, extract to Dialog
        openAction = new OpenReversionAction();
        openAction.setText(Messages.BlackBoxView_OpenVersion);
        openAction.setToolTipText(Messages.BlackBoxView_OpenVersion);
        openAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.OPEN, true));
        openAction.setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.OPEN, false));

        deleteAction = new DeleteBackupsAction();
        deleteAction.setText(Messages.BlackBoxView_DeleteBackups);
        deleteAction.setToolTipText(Messages.BlackBoxView_DeleteBackups);
        deleteAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, true));
        deleteAction.setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, false));

        IMenuManager menu = new MenuManager();
        menu.add(openAction);
        menu.add(deleteAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        contextMenu = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        contextMenu.add(openAction);
        contextMenu.add(deleteAction);
        viewer.getControl()
                .setMenu(contextMenu.createContextMenu(viewer.getControl()));
    }

    private void registerCoreEvent() {
        coreEventRegister
                .setNextSourceFrom(BlackBoxManager.getInstance().getLibrary());
        coreEventRegister.register(VERSION_ADD);
        coreEventRegister.register(VERSION_REMOVE);
        coreEventRegister.register(MAP_REMOVE);
    }

    @Override
    protected void initializeBounds() {
        getShell().setBounds(300, 150, 516, 500);
        super.initializeBounds();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        openButton = createButton(parent, IDialogConstants.OPEN_ID,
                Messages.BlackBoxView_OpenVersion, false);
        openButton.setEnabled(false);
        deleteButton = createButton(parent, DELETE_BUTTON_ID,
                Messages.BlackBoxView_DeleteBackups, false);
        deleteButton.setEnabled(false);

        createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, true);
    }

    @Override
    protected void buttonPressed(int buttonId) {

        super.buttonPressed(buttonId);

        if (IDialogConstants.OPEN_ID == buttonId)
            handleOpen(currentSelection);
        else if (IDialogConstants.CLOSE_ID == buttonId)
            handleClose();
        else if (DELETE_BUTTON_ID == buttonId)
            handleDelete();

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
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, MindMapUI.MINDMAP_EDITOR_ID);
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

    private void handleDelete() {

        List<IBlackBoxMap> mapsToDelete = new ArrayList<IBlackBoxMap>();
        List<IBlackBoxVersion> versionsToDelete = new ArrayList<IBlackBoxVersion>();
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        Iterator it = selection.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element instanceof IBlackBoxVersion) {
                versionsToDelete.add((IBlackBoxVersion) element);
            } else if (element instanceof IBlackBoxMap) {
                mapsToDelete.add((IBlackBoxMap) element);
            }
        }

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

    private void handleClose() {
        close();
    }

    @Override
    public boolean close() {
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }

        coreEventRegister.unregisterAll();

        return super.close();

    }

    public void handleCoreEvent(CoreEvent event) {
        final String type = event.getType();
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
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

    public void selectionChanged(SelectionChangedEvent event) {

        openAction.setEnabled(false);
        openButton.setEnabled(false);
        deleteAction.setEnabled(false);
        deleteButton.setEnabled(false);

        if (!(event.getSelection() instanceof IStructuredSelection))
            return;

        currentSelection = (IStructuredSelection) event.getSelection();

        List<IBlackBoxVersion> selectVersions = new ArrayList<IBlackBoxVersion>();
        List<IBlackBoxMap> selectMaps = new ArrayList<IBlackBoxMap>();

        Iterator it = currentSelection.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element instanceof IBlackBoxVersion) {
                selectVersions.add((IBlackBoxVersion) element);
            } else if (element instanceof IBlackBoxMap) {
                selectMaps.add((IBlackBoxMap) element);
            }
        }

        if (1 == selectVersions.size() && selectMaps.isEmpty()) {
            openAction.setEnabled(true);
            openButton.setEnabled(true);
        }

        if (!selectMaps.isEmpty() || !selectVersions.isEmpty()) {
            deleteAction.setEnabled(true);
            deleteButton.setEnabled(true);
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

}
