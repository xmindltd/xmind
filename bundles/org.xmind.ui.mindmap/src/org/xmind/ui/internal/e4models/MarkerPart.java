package org.xmind.ui.internal.e4models;

import static org.xmind.ui.mindmap.MindMapUI.REQ_ADD_MARKER;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.forms.WidgetFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dnd.MindMapElementTransfer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.MarkerImageDescriptor;

public class MarkerPart extends ViewModelPart {

    private static final String VIEWMENU_ID_FOR_MARKERPART = "org.xmind.ui.modelpart.marker.viewmenu"; //$NON-NLS-1$

    private WidgetFactory factory;

    private ScrolledForm form;

    private MarkerGroupPart recentPart;

    private MarkerSheetPart systemPart;

    private MarkerSheetPart userPart;

    private Map<IMarkerGroup, MarkerGroupPart> groupToPart = new HashMap<IMarkerGroup, MarkerGroupPart>();

    private Map<IMarkerGroup, Section> groupToSection = new HashMap<IMarkerGroup, Section>();

    @Override
    protected Control doCreateContent(Composite parent) {
        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .trackView(UserDataConstants.VIEW_MARKER);

        factory = new WidgetFactory(parent.getDisplay());
        form = createForm(parent);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        form.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (factory != null) {
                    factory.dispose();
                    factory = null;
                }
            }
        });
        fillFormContent();
        return form;
    }

    @Override
    protected void init() {
        super.init();
        registerViewMenu(VIEWMENU_ID_FOR_MARKERPART);
    }

    private ScrolledForm createForm(Composite parent) {
        ScrolledForm form = factory.createScrolledForm(parent);
        form.setMinWidth(1);
        return form;
    }

    private void fillFormContent() {
        final Composite compositeformbady = form.getBody();
        final GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        layout.verticalSpacing = 7;
        compositeformbady.setLayout(layout);
        compositeformbady
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control control;

        control = createRecentSection(compositeformbady);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        control = createSystemSection(compositeformbady);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        control = createUserSection(compositeformbady);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        form.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                int width = form.getClientArea().width;
                width -= layout.marginLeft + layout.marginRight
                        + layout.marginWidth * 2;
                Control[] controls = compositeformbady.getChildren();
                for (int i = 0; i < controls.length; i++) {
                    Control c = controls[i];
                    ((GridData) c.getLayoutData()).widthHint = width;
                }
                form.reflow(true);
            }
        });

        form.reflow(true);
    }

    private Control createRecentSection(Composite composite) {
        recentPart = new MarkerGroupPart(
                MindMapUI.getResourceManager().getRecentMarkerGroup(), false);
        Control con = recentPart.createControl(composite);
        return con;
    }

    private Control createSystemSection(Composite composite) {
        systemPart = new MarkerSheetPart(
                MindMapUI.getResourceManager().getSystemMarkerSheet());
        Control con = systemPart.createControl(composite);
        return con;
    }

    private Control createUserSection(Composite composite) {
        userPart = new MarkerSheetPart(
                MindMapUI.getResourceManager().getUserMarkerSheet());
        Control con = userPart.createControl(composite);
        return con;
    }

    private Section createSection(Composite parent, String title,
            WidgetFactory factory) {
        Section section = factory.createSection(parent,
                Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED
                        | SWT.BORDER | Section.NO_TITLE_FOCUS_BOX);
        section.setText(title);
//      section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return section;
    }

    protected void setFocus() {
        super.setFocus();
        if (form != null && !form.isDisposed())
            form.setFocus();
    }

    public void dispose() {
        super.dispose();
        factory = null;
        form = null;
        recentPart = null;
        systemPart = null;
        userPart = null;
        groupToPart.clear();
        groupToSection.clear();
    }

    protected class MarkerGroupPart implements ICoreEventListener {

        private IMarkerGroup group;

        private Section section;

        private boolean hasTitle;

        private ToolBarManager toolbar = null;

        private Control control = null;

        private ICoreEventRegister eventRegister = null;

        public MarkerGroupPart(IMarkerGroup group) {
            this(group, true);
        }

        public MarkerGroupPart(IMarkerGroup group, boolean hasTitle) {
            this.group = group;
            this.hasTitle = hasTitle;
        }

        public IMarkerGroup getMarkerGroup() {
            return group;
        }

        public Control createControl(final Composite parent) {
            if (control == null) {
                section = createSection(parent, group.getName(), factory);
                if (toolbar == null) {
                    toolbar = new ToolBarManager(
                            SWT.RIGHT | SWT.FLAT | SWT.WRAP);
                }
                Composite c = factory.createComposite(section, SWT.WRAP);
                GridLayout layout = new GridLayout(1, true);
                layout.marginHeight = 2;
                layout.marginWidth = 2;
                layout.verticalSpacing = 2;
                layout.marginLeft = Util.isMac() ? 17 : 7;
                c.setLayout(layout);

                if (hasTitle) {
                    factory.createLabel(c, group.getName());
                }

                final ToolBar tb = toolbar.createControl(c);
                GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
                tb.setLayoutData(data);
                addDragSource(tb);
                addDropTarget(tb);

                control = section;
                section.setClient(c);

                refresh(false);
                installListeners();
                control.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        dispose();
                    }
                });
            }
            return control;
        }

        private void addDragSource(final ToolBar toolbar) {
            final DragSource dragSource = new DragSource(toolbar,
                    DND.DROP_COPY);
            dragSource.setTransfer(
                    new Transfer[] { MindMapElementTransfer.getInstance() });
            dragSource.addDragListener(new DragSourceListener() {

                ToolItem sourceItem;

                public void dragStart(DragSourceEvent event) {
                    sourceItem = toolbar.getItem(new Point(event.x, event.y));
                    if (sourceItem == null)
                        event.doit = false;
                    else {
                        event.image = sourceItem.getImage();
                    }
                }

                public void dragSetData(DragSourceEvent event) {
                    if (sourceItem == null)
                        return;

                    int index = toolbar.indexOf(sourceItem);
                    List<IMarker> visibleMarkers = new ArrayList<IMarker>();
                    for (IMarker marker : group.getMarkers())
                        if (!marker.isHidden())
                            visibleMarkers.add(marker);
                    IMarker marker = visibleMarkers.get(index);
                    event.data = new Object[] { marker };
                }

                public void dragFinished(DragSourceEvent event) {
                }
            });
            toolbar.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    dragSource.dispose();
                }
            });
        }

        private void addDropTarget(final ToolBar toolBar) {
            final DropTarget dropTarget = new DropTarget(toolBar,
                    DND.DROP_COPY | DND.DROP_MOVE);
            dropTarget
                    .setTransfer(new Transfer[] { FileTransfer.getInstance() });
            toolBar.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent e) {
                    dropTarget.dispose();
                }
            });
            dropTarget.addDropListener(new DropTargetListener() {

                private Map<String, List<String>> dirToMarkerPaths = new HashMap<String, List<String>>();

                public void dropAccept(DropTargetEvent event) {
                    if (!FileTransfer.getInstance()
                            .isSupportedType(event.currentDataType)) {
                        event.detail = DND.DROP_NONE;
                    } else if (event.detail == DND.DROP_DEFAULT) {
                        if ((event.operations & DND.DROP_COPY) != 0) {
                            event.detail = DND.DROP_COPY;
                        } else if ((event.operations & DND.DROP_MOVE) != 0) {
                            event.detail = DND.DROP_MOVE;
                        }
                    }
                }

                public void drop(DropTargetEvent event) {
                    if (event.data instanceof String[]) {
                        IMarkerSheet userMarkerSheet = MindMapUI
                                .getResourceManager().getUserMarkerSheet();
                        IMarkerSheet markerSheet = group.getParent();
                        for (String path : (String[]) event.data) {
                            File dir = new File(path);
                            if (dir.isDirectory()) {
                                collectDirToMarkers(path, dirToMarkerPaths);

                                Set<String> folders = dirToMarkerPaths.keySet();
                                for (String folder : folders) {
                                    File tempDir = new File(folder);
                                    if (tempDir.isDirectory()) {
                                        List<String> markerPaths = dirToMarkerPaths
                                                .get(folder);
                                        if (markerPaths != null
                                                && !markerPaths.isEmpty()) {
                                            IMarkerGroup markerGroup = userMarkerSheet
                                                    .createMarkerGroup(false);
                                            markerGroup
                                                    .setName(tempDir.getName());
                                            userMarkerSheet.addMarkerGroup(
                                                    markerGroup);
                                            for (String markerPath : markerPaths) {
                                                if (imageValid(markerPath))
                                                    createMarker(markerGroup,
                                                            markerPath);
                                            }
                                        }
                                    }
                                }
                            } else if (dir.isFile() && imageValid(path)) {
                                if (markerSheet == MindMapUI
                                        .getResourceManager()
                                        .getSystemMarkerSheet()) {
                                    IMarkerGroup markerGroup = userMarkerSheet
                                            .createMarkerGroup(false);
                                    markerGroup.setName(
                                            MindMapMessages.MarkerView_UntitledGroup_name);
                                    userMarkerSheet.addMarkerGroup(markerGroup);
                                    createMarker(markerGroup, path);
                                } else if (userMarkerSheet == markerSheet) {
                                    createMarker(group, path);
                                }
                            }
                        }
                        MindMapUI.getResourceManager().saveUserMarkerSheet();
                    }
                }

                private void collectDirToMarkers(String dirPath,
                        Map<String, List<String>> dirToMarkerPaths) {
                    File directory = new File(dirPath);
                    if (directory.isDirectory()) {
                        List<String> markerPaths = dirToMarkerPaths
                                .get(dirPath);
                        if (markerPaths == null)
                            markerPaths = new LinkedList<String>();
                        dirToMarkerPaths.put(dirPath, markerPaths);

                        File[] files = directory.listFiles();
                        for (File file : files) {
                            if (file.isDirectory()) {
                                collectDirToMarkers(file.getAbsolutePath(),
                                        dirToMarkerPaths);
                            } else if (file.isFile()
                                    && imageValid(file.getAbsolutePath())) {
                                markerPaths.add(file.getAbsolutePath());
                            }
                        }
                    }
                }

                private void createMarker(IMarkerGroup targetGroup,
                        String sourcePath) {
                    String path = Core.getIdFactory().createId()
                            + FileUtils.getExtension(sourcePath);
                    IMarker marker = targetGroup.getOwnedSheet()
                            .createMarker(path);
                    marker.setName(FileUtils.getFileName(sourcePath));
                    IMarkerResource resource = marker.getResource();
                    if (resource != null) {
                        OutputStream os = resource.getOutputStream();
                        if (os != null) {
                            try {
                                FileInputStream is = new FileInputStream(
                                        sourcePath);
                                FileUtils.transfer(is, os, true);
                            } catch (IOException e) {
                                Logger.log(e);
                            }
                        }
                    }
                    targetGroup.addMarker(marker);
                }

                private boolean imageValid(String sourcePath) {
                    try {
                        new Image(Display.getCurrent(), sourcePath).dispose();
                        return true;
                    } catch (Throwable e) {
                    }
                    return false;
                }

                public void dragOver(DropTargetEvent event) {
                    dropAccept(event);
                }

                public void dragOperationChanged(DropTargetEvent event) {
                    dropAccept(event);
                }

                public void dragLeave(DropTargetEvent event) {
                    dropAccept(event);
                }

                public void dragEnter(DropTargetEvent event) {
                    dropAccept(event);
                }
            });

        }

        private void installListeners() {
            eventRegister = new CoreEventRegister(group, this);
            eventRegister.register(Core.MarkerAdd);
            eventRegister.register(Core.MarkerRemove);
            eventRegister.register(Core.Name);
        }

        private void uninstallListeners() {
            if (eventRegister != null)
                eventRegister.unregisterAll();
        }

        public Control getControl() {
            return control;
        }

        public void refresh(boolean reflow) {
            if (toolbar == null || control == null || control.isDisposed())
                return;
            section.setText(group.getName());

            toolbar.removeAll();
            for (IMarker marker : group.getMarkers()) {
                if (!group.isHidden() && !marker.isHidden())
                    toolbar.add(new MarkerAction(marker));
            }
            toolbar.update(false);

            ToolBar tb = toolbar.getControl();
            GridData data = (GridData) tb.getLayoutData();
            data.exclude = toolbar.isEmpty();
            tb.setVisible(!toolbar.isEmpty());

            if (reflow)
                form.reflow(true);
        }

        public void dispose() {
            uninstallListeners();
            if (toolbar != null) {
                toolbar.dispose();
                toolbar = null;
            }
            if (control != null) {
                control.dispose();
                control = null;
            }
        }

        public void handleCoreEvent(final CoreEvent event) {
            if (control == null || control.isDisposed())
                return;

            control.getDisplay().syncExec(new Runnable() {
                public void run() {
                    String type = event.getType();
                    if (Core.MarkerAdd.equals(type)
                            || Core.MarkerRemove.equals(type)
                            || Core.Name.equals(type)) {
                        refresh(true);
                    }
                }
            });
        }

    }

    protected class MarkerAction extends Action {

        private IMarker marker;

        public MarkerAction(IMarker marker) {
            super();
            this.marker = marker;
            setImageDescriptor(MarkerImageDescriptor.createFromMarker(marker,
                    24, 24, false));
            setToolTipText(marker.getName());
        }

        public IMarker getMarker() {
            return marker;
        }

        public void run() {
            IWorkbenchPage page = getAdapter(IWorkbenchWindow.class)
                    .getActivePage();
            if (page != null) {
                IEditorPart editor = page.getActiveEditor();
                if (editor != null && editor instanceof IGraphicalEditor) {
                    IGraphicalEditorPage gp = ((IGraphicalEditor) editor)
                            .getActivePageInstance();
                    if (gp != null) {
                        EditDomain domain = gp.getEditDomain();
                        if (domain != null) {
                            Request req = new Request(REQ_ADD_MARKER)
                                    .setViewer(gp.getViewer()).setDomain(domain)
                                    .setParameter(MindMapUI.PARAM_MARKER_ID,
                                            marker.getId());
                            domain.handleRequest(req);
//                            MindMapUIPlugin.getDefault().getUsageDataCollector()
//                                    .increase(
//                                            UserDataConstants.USE_MARKERS_COUNT);
                        }
                        IViewer viewer = gp.getViewer();
                        if (viewer != null) {
                            Control control = viewer.getControl();
                            if (control != null && !control.isDisposed()) {
                                control.setFocus();
                            }
                        }
                    }
                }
            }
        }
    }

    protected class MarkerSheetPart implements ICoreEventListener {

        private IMarkerSheet sheet;

        private Composite composite;

        private List<MarkerGroupPart> groupParts = new ArrayList<MarkerGroupPart>();

        private List<Section> groupSections = new ArrayList<Section>();

        private ICoreEventRegister eventRegister = null;

        public MarkerSheetPart(IMarkerSheet sheet) {
            this.sheet = sheet;
        }

        public Control getControl() {
            return composite;
        }

        public Control createControl(Composite parent) {
            if (composite == null) {
                composite = createComposite(parent);
                refresh(false);
                installListeners();
                composite.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        dispose();
                    }
                });
            }
            return composite;
        }

        private Composite createComposite(Composite parent) {
            Composite composite = factory.createComposite(parent, SWT.WRAP);
            GridLayout layout = new GridLayout(1, true);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.marginBottom = 0;
            layout.marginLeft = 0;
            layout.marginRight = 0;
            layout.marginTop = 0;
            layout.verticalSpacing = 7;
            composite.setLayout(layout);

            return composite;
        }

        public void refresh(boolean reflow) {
            if (composite == null || composite.isDisposed())
                return;

            composite.setRedraw(false);
            List<IMarkerGroup> newGroups = sheet.getMarkerGroups();
            int i;
            for (i = 0; i < newGroups.size(); i++) {
                IMarkerGroup group = newGroups.get(i);
                if (i < groupParts.size()) {
                    MarkerGroupPart part = groupParts.get(i);
                    IMarkerGroup g = part.getMarkerGroup();
                    if (group.equals(g)) {
                        continue;
                    }
                }

                MarkerGroupPart part = groupToPart.get(group);
                if (part != null) {
                    if (!newGroups.get(i).isHidden()) {
                        reorderChild(part, i);
                    }
                } else {
                    if (!newGroups.get(i).isHidden()) {
                        part = createChild(group);
                        addChild(part, i);
                    }
                }
            }

            Object[] toTrim = groupParts.toArray();
            for (; i < toTrim.length; i++) {
                removeChild((MarkerGroupPart) toTrim[i]);
            }

            composite.setRedraw(true);
//            form.reflow(true);
            if (reflow)
                form.reflow(true);
        }

        public List<MarkerGroupPart> getGroupParts() {
            return groupParts;
        }

        private void reorderChild(MarkerGroupPart part, int index) {
            Control c = part.getControl();
            if (c == null) {
                c = part.createControl(composite);
//                c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            }
            groupParts.remove(part);
            groupParts.add(index, part);
            groupSections.remove(part.section);
            groupSections.add(index, part.section);
            if (index == 0) {
                c.moveAbove(null);
            } else {
                MarkerGroupPart g = groupParts.get(index - 1);
                c.moveAbove(g.getControl());
            }
        }

        private MarkerGroupPart createChild(IMarkerGroup group) {
            MarkerGroupPart part = new MarkerGroupPart(group, false);
            groupToPart.put(group, part);
            return part;
        }

        private void addChild(MarkerGroupPart part, int index) {
            groupParts.add(index, part);
            Control c = part.createControl(composite);
            groupSections.add(index, part.section);
            groupToSection.put(part.group, part.section);
            c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        }

        private void removeChild(MarkerGroupPart part) {
            groupParts.remove(part);
            groupToPart.remove(part.getMarkerGroup());
            groupSections.remove(part.section);
            groupToSection.remove(part.getMarkerGroup());
            part.section.dispose();
            part.dispose();
        }

        private void installListeners() {
            eventRegister = new CoreEventRegister(sheet, this);
            eventRegister.register(Core.MarkerGroupAdd);
            eventRegister.register(Core.MarkerGroupRemove);
        }

        private void uninstallListeners() {
            if (eventRegister != null)
                eventRegister.unregisterAll();
        }

        public void dispose() {
            uninstallListeners();
            if (composite != null) {
                composite.dispose();
                composite = null;
            }
            for (Object o : groupParts.toArray()) {
                MarkerGroupPart groupPart = (MarkerGroupPart) o;
                groupToPart.remove(groupPart.getMarkerGroup());
                groupToSection.remove(groupPart.getMarkerGroup());
                groupPart.dispose();
            }
            groupParts.clear();
            groupSections.clear();
        }

        public void handleCoreEvent(final CoreEvent event) {
            if (composite == null || composite.isDisposed())
                return;

            composite.getDisplay().syncExec(new Runnable() {
                public void run() {
                    String type = event.getType();
                    if (Core.MarkerGroupAdd.equals(type)
                            || Core.MarkerGroupRemove.equals(type)) {
                        refresh(true);
                    }
                }
            });
        }
    }

}
