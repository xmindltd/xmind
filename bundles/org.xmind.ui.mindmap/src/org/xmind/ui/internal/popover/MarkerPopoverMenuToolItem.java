package org.xmind.ui.internal.popover;

import static org.xmind.ui.mindmap.MindMapUI.REQ_ADD_MARKER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.internal.MarkerGroup;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.forms.WidgetFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dnd.MindMapElementTransfer;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.internal.views.Messages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.MarkerImageDescriptor;

public class MarkerPopoverMenuToolItem extends PopoverMenuToolItem {

    private static class MarkerSheetPart {

        private List<IMarkerSheet> sheets;

        private Composite composite;

        private ArrayList<MarkerGroupPart> groupParts = new ArrayList<MarkerGroupPart>();

        private List<Section> groupSections = new ArrayList<Section>();

        private Map<IMarkerGroup, MarkerGroupPart> groupToPart = new HashMap<IMarkerGroup, MarkerGroupPart>();

        private Map<IMarkerGroup, Section> groupToSection = new HashMap<IMarkerGroup, Section>();

        public MarkerSheetPart(List<IMarkerSheet> sheets) {
            this.sheets = sheets;
        }

        public Control createControl(Composite parent) {
            if (composite == null) {
                composite = createComposite(parent);

                refresh(false);

                composite.addDisposeListener(new DisposeListener() {

                    public void widgetDisposed(DisposeEvent e) {
                        dispose();
                    }
                });
            }

            return composite;
        }

        private Composite createComposite(Composite parent) {
            Composite composite = new Composite(parent, SWT.WRAP);
            composite.setBackground(composite.getParent().getBackground());

            GridLayout layout = new GridLayout(1, true);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.verticalSpacing = 3;
            composite.setLayout(layout);

            return composite;
        }

        public void refresh(boolean reflow) {
            if (composite == null || composite.isDisposed()) {
                return;
            }
            composite.setRedraw(false);

            List<IMarkerGroup> newGroups = new ArrayList<IMarkerGroup>();
            if (!RecentMarkerGroup.instance.isEmpty())
                newGroups.add(RecentMarkerGroup.instance);
            for (IMarkerSheet sheet : sheets) {
                List<IMarkerGroup> markerGroups = sheet.getMarkerGroups();
                for (IMarkerGroup markerGroup : markerGroups)
                    if (!markerGroup.isEmpty())
                        newGroups.add(markerGroup);
            }

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
                if (part == null) {
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
        }

        private MarkerGroupPart createChild(IMarkerGroup group) {
            MarkerGroupPart part = new MarkerGroupPart(group, false);
            groupToPart.put(group, part);

            return part;
        }

        private void addChild(MarkerGroupPart part, int index) {
            index = index < groupParts.size() ? index : groupParts.size();
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

        public void dispose() {
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
    }

    private static class MarkerGroupPart {

        private IMarkerGroup group;

        private boolean hasTitle;

        private Control control;

        private Section section;

        private ToolBarManager toolbar;

        public MarkerGroupPart(IMarkerGroup group, boolean hasTitle) {
            this.group = group;
            this.hasTitle = hasTitle;
        }

        public IMarkerGroup getMarkerGroup() {
            return group;
        }

        public Control createControl(final Composite parent) {
            if (control == null) {
                WidgetFactory factory = new WidgetFactory(parent.getDisplay());

                section = createSection(parent, group.getName(), factory);
                if (toolbar == null) {
                    toolbar = new ToolBarManager(
                            SWT.RIGHT | SWT.FLAT | SWT.WRAP);
                }

                Composite c = factory.createComposite(section, SWT.WRAP);
                GridLayout layout = new GridLayout(1, true);
                layout.marginHeight = 0;
                layout.marginWidth = 0;
                layout.verticalSpacing = 0;
                c.setLayout(layout);

                if (hasTitle) {
                    factory.createLabel(c, group.getName());
                }

                final ToolBar tb = toolbar.createControl(c);
                tb.setBackground(
                        tb.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
                tb.setLayoutData(data);

                addDragSource(tb);

                control = section;
                section.setClient(c);

                refresh(false);
                control.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        dispose();
                    }
                });
            }

            return control;
        }

        private Section createSection(Composite parent, String title,
                WidgetFactory factory) {
            Section section = factory.createSection(parent, SWT.None);
            section.setText(title);
            return section;
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
                    IMarker marker = group.getMarkers().get(index);
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

        public void refresh(boolean reflow) {
            if (toolbar == null || control == null || control.isDisposed())
                return;
            section.setText(group.getName());

            toolbar.removeAll();
            for (int index = 0; index < group.getMarkers().size(); index++) {
                IMarker marker = group.getMarkers().get(index);
                if (!group.isHidden() && !marker.isHidden()) {
                    toolbar.add(new MarkerAction(marker));
                }
            }
            toolbar.update(false);
        }

        public void dispose() {
            if (toolbar != null) {
                toolbar.dispose();
                toolbar = null;
            }

            if (control != null) {
                control.dispose();
                control = null;
            }
        }
    }

    private static class RecentMarkerGroup extends MarkerGroup {

        public static final RecentMarkerGroup instance = new RecentMarkerGroup();

        private static final int CAPACITY = 7;

        private List<IMarker> markers = new ArrayList<IMarker>(CAPACITY);

        private RecentMarkerGroup() {
        }

        public void addMarker(IMarker marker) {
            if (markers.contains(marker))
                return;

            while (markers.size() >= CAPACITY) {
                markers.remove(markers.size() - 1);
            }
            markers.add(0, marker);
        }

        public <T> T getAdapter(Class<T> adapter) {
            if (adapter == ICoreEventSource.class)
                return adapter.cast(this);
            return super.getAdapter(adapter);
        }

        public List<IMarker> getMarkers() {
            return markers;
        }

        /*
         * (non-Javadoc)
         * @see org.xmind.core.marker.IMarkerGroup#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return markers.isEmpty();
        }

        public String getName() {
            return MindMapMessages.RecentUsed;
        }

        public void setSingleton(boolean singleton) {
        }

        public IMarkerSheet getOwnedSheet() {
            return null;
        }

        public IMarkerSheet getParent() {
            return null;
        }

        public boolean isSingleton() {
            return false;
        }

        public boolean isHidden() {
            return false;
        }

        public void setHidden(boolean hidden) {

        }

        public void removeMarker(IMarker marker) {
            if (!markers.contains(marker))
                return;
            markers.remove(marker);
        }

        public void setName(String name) {
        }

        public String getId() {
            return "org.xmind.ui.RecentMarkerGroup"; //$NON-NLS-1$
        }

        public int hashCode() {
            return super.hashCode();
        }

    }

    private static class MarkerAction extends Action {

        private static final int ICON_WIDTH = 24;

        private static final int ICON_HEIGHT = 24;

        private IMarker marker;

        public MarkerAction(IMarker marker) {
            super();
            this.marker = marker;
            setImageDescriptor(MarkerImageDescriptor.createFromMarker(marker,
                    ICON_WIDTH, ICON_HEIGHT, false));
            setToolTipText(marker.getName());
        }

        public void run() {
            RecentMarkerGroup.instance.addMarker(marker);
            IWorkbenchPage page = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage();
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
                        }
//                        IViewer viewer = gp.getViewer();
//                        if (viewer != null) {
//                            Control control = viewer.getControl();
//                            if (control != null && !control.isDisposed()) {
//                                control.setFocus();
//                            }
//                        }
                    }
                }
            }
        }
    }

    private static final int POPOVER_WIDTH = 255;
    private static final int POPOVER_HEIGHT = 380;

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = (Composite) super.createContents(parent);

        Composite composite2 = new Composite(composite, SWT.WRAP);
        composite2.setBackground(composite2.getParent().getBackground());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = POPOVER_WIDTH;
        gridData.heightHint = POPOVER_HEIGHT;
        composite2.setLayoutData(gridData);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 2;
        composite2.setLayout(layout);

        createMarkersContainer(composite2);
        createSeperator(composite2);
        createHyperlinks(composite2);

        return composite;
    }

    private ScrolledForm createForm(Composite parent) {
        final WidgetFactory factory = new WidgetFactory(parent.getDisplay());
        final ScrolledForm form = new ScrolledForm(parent,
                SWT.V_SCROLL | factory.getOrientation());
        form.setExpandHorizontal(true);
        form.setExpandVertical(true);
        form.setBackground(factory.getColors().getBackground());
        form.setForeground(factory.getColors().getColor(IFormColors.TITLE));
        form.setFont(JFaceResources.getHeaderFont());
        form.setMinWidth(1);
        form.setAlwaysShowScrollBars(true);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        form.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (factory != null) {
                    factory.dispose();
                }
            }
        });
        return form;
    }

    private void createMarkersContainer(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(1, true);
        layout.marginTop = 1;
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        layout.verticalSpacing = 7;
        composite.setLayout(layout);

        final ScrolledForm form = createForm(composite);
        final Composite formBody = form.getBody();
        final GridLayout layout2 = new GridLayout(1, true);
        layout2.marginLeft = 12;
        layout2.marginWidth = 0;
        formBody.setLayout(layout2);
        formBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createMarkersControl(formBody);
        form.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                int width = form.getClientArea().width;
                width -= layout2.marginLeft + layout2.marginRight
                        + layout2.marginWidth * 2;
                Control[] controls = formBody.getChildren();
                for (int i = 0; i < controls.length; i++) {
                    Control c = controls[i];
                    ((GridData) c.getLayoutData()).widthHint = width;
                }
                form.reflow(true);
            }
        });

        form.reflow(true);
    }

    private void createMarkersControl(Composite parent) {
        List<IMarkerSheet> sheets = new ArrayList<IMarkerSheet>();
        if (MindMapUI.getResourceManager().getSystemMarkerSheet() != null)
            sheets.add(MindMapUI.getResourceManager().getSystemMarkerSheet());
        if (MindMapUI.getResourceManager().getUserMarkerSheet() != null)
            sheets.add(MindMapUI.getResourceManager().getUserMarkerSheet());
        MarkerSheetPart markerPart = new MarkerSheetPart(sheets);
        Control control = markerPart.createControl(parent);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        control.setLayoutData(gridData);
    }

    private void createSeperator(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        composite.setBackground(composite.getParent().getBackground());
        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true,
                false);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite seperator = new Composite(composite, SWT.NONE);
        seperator.setBackground(
                new LocalResourceManager(JFaceResources.getResources(),
                        composite).createColor(ColorUtils.toRGB("#cbcbcb"))); //$NON-NLS-1$
        GridData gridData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData2.heightHint = 1;
        seperator.setLayoutData(gridData2);
        seperator.setLayout(layout);
    }

    private void createHyperlinks(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 0;
        layout.marginBottom = 2;
        layout.verticalSpacing = 2;
        composite.setLayout(layout);

        createManageMarkersHyperlink(composite);
        createImportMarkersHyperlink(composite);
        createExportMarkersHyperlink(composite);
    }

    private void createManageMarkersHyperlink(Composite parent) {
        Hyperlink manageMarkersHyperlink = createHyperlink(parent,
                Messages.MarkersPopover_ManageMarkers_label);

        manageMarkersHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                E4Utils.showPart(IModelConstants.COMMAND_SHOW_DIALOG_PART,
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                        IModelConstants.PART_ID_RESOURCE_MANAGER,
                        IModelConstants.PAGE_ID_RESOURCE_MANAGER_MARKER, null);

            }
        });
    }

    private void createImportMarkersHyperlink(Composite parent) {
        Hyperlink importManagerHyperlink = createHyperlink(parent,
                Messages.MarkersPopover_ImportMarkers_label);
        importManagerHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {

                handleShellDeactived();

                IWorkbenchWindow window = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();
                final IHandlerService handlerService = window
                        .getService(IHandlerService.class);
                SafeRunner.run(new SafeRunnable() {
                    @Override
                    public void run() throws Exception {
                        handlerService.executeCommand(
                                "org.xmind.ui.command.marker.import", null); //$NON-NLS-1$
                    }
                });
            }
        });
    }

    private void createExportMarkersHyperlink(Composite parent) {
        Hyperlink link = createHyperlink(parent,
                Messages.MarkersPopover_ExportMarkers_label);
        link.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {

                handleShellDeactived();

                IWorkbenchWindow window = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();

                final IHandlerService handlerService = window
                        .getService(IHandlerService.class);
                SafeRunner.run(new SafeRunnable() {
                    @Override
                    public void run() throws Exception {
                        handlerService.executeCommand(
                                "org.xmind.ui.command.marker.export", null); //$NON-NLS-1$
                    }
                });
            }
        });
    }

    private Hyperlink createHyperlink(final Composite parent, String message) {
        final Composite padding = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 15;
        layout.marginHeight = 0;
        padding.setBackground(parent.getBackground());
        padding.setLayout(layout);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        padding.setLayoutData(gridData);

        final Hyperlink hyperlink = new Hyperlink(padding, SWT.SINGLE);
        hyperlink.setBackground(hyperlink.getParent().getBackground());

        hyperlink.setLayoutData(gridData);
        hyperlink.setUnderlined(false);
        hyperlink.setText(message);

        hyperlink.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                padding.setBackground(new LocalResourceManager(
                        JFaceResources.getResources(), parent)
                                .createColor(ColorUtils.toRGB("#FFFFFF"))); //$NON-NLS-1$
                hyperlink.setBackground(new LocalResourceManager(
                        JFaceResources.getResources(), parent)
                                .createColor(ColorUtils.toRGB("#FFFFFF"))); //$NON-NLS-1$
                hyperlink.setForeground(new LocalResourceManager(
                        JFaceResources.getResources(), parent)
                                .createColor(ColorUtils.toRGB("#000000"))); //$NON-NLS-1$
            }

            @Override
            public void mouseEnter(MouseEvent e) {
                padding.setBackground(new LocalResourceManager(
                        JFaceResources.getResources(), parent)
                                .createColor(ColorUtils.toRGB("#0070D8"))); //$NON-NLS-1$
                hyperlink.setBackground(new LocalResourceManager(
                        JFaceResources.getResources(), parent)
                                .createColor(ColorUtils.toRGB("#0070D8"))); //$NON-NLS-1$
                hyperlink.setForeground(new LocalResourceManager(
                        JFaceResources.getResources(), parent)
                                .createColor(ColorUtils.toRGB("#FFFFFF"))); //$NON-NLS-1$
            }

        });

        return hyperlink;
    }

}
