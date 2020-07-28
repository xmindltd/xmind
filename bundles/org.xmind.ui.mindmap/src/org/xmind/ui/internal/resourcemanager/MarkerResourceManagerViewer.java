package org.xmind.ui.internal.resourcemanager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.utils.ResourceUtils;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.texteditor.IFloatingTextEditorListener;
import org.xmind.ui.texteditor.TextEvent;
import org.xmind.ui.util.MarkerImageDescriptor;
import org.xmind.ui.viewers.IToolTipProvider;

public class MarkerResourceManagerViewer extends ResourceManagerViewer {

    private static final int FRAME_HEIGHT = 32;

    private static final int FRAME_WIDTH = 32;

    private static final int RENAME_COMPOSITE_HEIGHT = 20;

    private static final int RENAME_COMPOSITE_WIDTH = 80;

    private FloatingTextEditor lastEditor;

    private static class MarkerCategorizedContentProvider
            implements ITreeContentProvider {

        public void dispose() {

        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {

        }

        @SuppressWarnings("unchecked")
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List<?>)
                return ((List<IMarkerGroup>) inputElement).toArray();
            else
                return null;
        }

        @SuppressWarnings("unchecked")
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof List<?>)
                return ((List<IMarkerGroup>) parentElement).toArray();
            else if (parentElement instanceof IMarkerGroup) {
                List<IMarker> markers = new ArrayList<IMarker>();
                for (IMarker marker : ((IMarkerGroup) parentElement)
                        .getMarkers())
                    if (!marker.isHidden())
                        markers.add(marker);
                return markers.toArray();
            } else
                return null;
        }

        public Object getParent(Object element) {
            if (element instanceof IMarker) {
                IMarker marker = (IMarker) element;
                return marker.getParent();
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            return element instanceof IMarkerGroup
                    || element instanceof List<?>;
        }

    }

    private class MarkerCategorizedLabelProvider
            extends CategorizedLabelProvider implements IToolTipProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof IMarker)
                return ((IMarker) element).getName();
            else if (element instanceof IMarkerGroup)
                return ((IMarkerGroup) element).getName();
            else
                return super.getText(element);
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IMarker) {

                IMarker marker = (IMarker) element;
                Image image = null;
                Dimension size = (Dimension) getProperties()
                        .get(GalleryViewer.FrameContentSize);
                if (size == null)
                    size = new Dimension(64, 64);

                ImageDescriptor imageDescriptor = MarkerImageDescriptor
                        .createFromMarker(marker, size.width, size.height,
                                false);

                image = getResourceManager().createImage(imageDescriptor);

                if (image != null)
                    return image;
            }
            return super.getImage(element);
        }

        public String getToolTip(Object element) {

            if (element instanceof IMarker) {
                return getText((IMarker) element);
            }

            return ""; //$NON-NLS-1$
        }

    }

    private Section activeSectionForSectionMenu;
    private IMarkerGroup activeMarkerGroupForSectionMenu;

    @Override
    public void createControl(Composite container) {
        super.createControl(container);
        setContentProvider(new MarkerCategorizedContentProvider());
        setLabelProvider(new MarkerCategorizedLabelProvider());
        EditDomain domain = new EditDomain();
        domain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        setEditDomain(domain);
        initProperties();
        createControl(container, SWT.WRAP);
        getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setInput(getMarkerGroups());
    }

    @Override
    protected void initNestedGalleryViewer(GalleryViewer galleryViewerer) {
        super.initNestedGalleryViewer(galleryViewerer);
        Properties properties = galleryViewerer.getProperties();
        properties.set(GalleryViewer.HideTitle, Boolean.TRUE);
    }

    @Override
    protected void initProperties() {
        super.initProperties();
        Properties properties = getProperties();
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    }

    @Override
    protected void configureSection(final Section section,
            final Object category) {
        super.configureSection(section, category);
        if (category instanceof IMarkerGroup) {
            final IMarkerGroup group = (IMarkerGroup) category;

            try {
                Field field = ExpandableComposite.class
                        .getDeclaredField("textLabel"); //$NON-NLS-1$
                field.setAccessible(true);
                Object textLabel = field.get(section);
                if (textLabel instanceof Control) {
                    ((Control) textLabel)
                            .addMenuDetectListener(new MenuDetectListener() {
                                @Override
                                public void menuDetected(MenuDetectEvent e) {
                                    activeMarkerGroupForSectionMenu = null;
                                    activeSectionForSectionMenu = null;
                                    IStructuredSelection ss = getStructuredSelection();
                                    if (ss != null && ss.isEmpty()) {
                                        activeSectionForSectionMenu = section;
                                        activeMarkerGroupForSectionMenu = group;
                                    }
                                }
                            });
                }
            } catch (NoSuchFieldException e1) {
                e1.printStackTrace();
            } catch (SecurityException e1) {
                e1.printStackTrace();
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }

            getNestedViewer(category).getControl()
                    .addMenuDetectListener(new MenuDetectListener() {
                        @Override
                        public void menuDetected(MenuDetectEvent e) {
                            activeMarkerGroupForSectionMenu = null;
                            activeSectionForSectionMenu = null;
                        }
                    });

            section.addMenuDetectListener(new MenuDetectListener() {

                @Override
                public void menuDetected(MenuDetectEvent e) {
                    activeMarkerGroupForSectionMenu = null;
                    activeSectionForSectionMenu = null;
                    IStructuredSelection ss = getStructuredSelection();
                    if (ss != null && ss.isEmpty()) {
                        activeSectionForSectionMenu = section;
                        activeMarkerGroupForSectionMenu = group;
                    }
                }
            });

            IMarkerSheet userSheet = MindMapUI.getResourceManager()
                    .getUserMarkerSheet();
            if (!userSheet.getMarkerGroups().contains(group))
                return;

            createSectionTextClient(section,
                    MindMapMessages.MarkerResourceManagerViewer_AddSection_title,
                    category);
        }
    }

    private List<IMarkerGroup> getMarkerGroups() {
        ArrayList<IMarkerGroup> mgs = new ArrayList<IMarkerGroup>();
        IResourceManager resourceManager = MindMapUI.getResourceManager();
        IMarkerSheet userSheet = resourceManager.getUserMarkerSheet();
        IMarkerSheet sysSheet = resourceManager.getSystemMarkerSheet();
        for (IMarkerGroup group : sysSheet.getMarkerGroups()) {
            if (!group.isHidden())
                mgs.add(group);
        }
        for (IMarkerGroup group : userSheet.getMarkerGroups()) {
            if (!group.isHidden())
                mgs.add(group);
        }

        return mgs;
    }

    public boolean canEditMarkerGroup() {
        if (activeMarkerGroupForSectionMenu == null
                || activeSectionForSectionMenu == null
                || activeSectionForSectionMenu.isDisposed()) {
            return false;
        }

        IMarkerSheet userSheet = MindMapUI.getResourceManager()
                .getUserMarkerSheet();
        if (!userSheet.getMarkerGroups()
                .contains(activeMarkerGroupForSectionMenu))
            return false;
        IStructuredSelection ss = getStructuredSelection();
        if (ss != null && !ss.isEmpty())
            return false;
        return true;
    }

    public void renameMarkerGroup() {
        if (activeMarkerGroupForSectionMenu == null
                || activeSectionForSectionMenu == null
                || activeSectionForSectionMenu.isDisposed()) {
            activeMarkerGroupForSectionMenu = null;
            activeSectionForSectionMenu = null;
            return;
        }

        final IMarkerGroup markerGroup = activeMarkerGroupForSectionMenu;
        final Section section = activeSectionForSectionMenu;

        activeMarkerGroupForSectionMenu = null;
        activeSectionForSectionMenu = null;

        Rectangle textLabelBounds = null;
        final String groupName = markerGroup.getName();
        for (Control control : section.getChildren())
            if (control instanceof Label
                    && groupName.equals(((Label) control).getText()))
                textLabelBounds = control.getBounds();

        if (lastEditor != null) {
            lastEditor.close();
            lastEditor = null;
        }
        final FloatingTextEditor editor = new FloatingTextEditor(section);
        if (textLabelBounds != null) {
            section.setText(""); //$NON-NLS-1$
            int x = textLabelBounds.x - 2;
            int y = textLabelBounds.y - 2;
            editor.setInitialLocation(new Point(x, y));
            editor.setInitialSize(new Point(100, 18));
        } else {
            editor.setInitialLocation(new Point(100, 0));
            editor.setInitialSize(new Point(100, 18));
        }

        editor.setInput(new Document(groupName));
        editor.open();
        editor.doOperation(FloatingTextEditor.SELECT_ALL);
        editor.addFloatingTextEditorListener(
                new IFloatingTextEditorListener.Stub() {
                    @Override
                    public void editingFinished(TextEvent e) {
                        String text = e.text;
                        if (null == text || "".equals(text) //$NON-NLS-1$
                                || text.equals(groupName)) {
                            section.setText(groupName);
                            editor.close();
                        } else
                            markerGroup.setName(text);
                        MindMapUI.getResourceManager().saveUserMarkerSheet();
                    }

                    @Override
                    public void editingCanceled(TextEvent e) {
                        refresh();
                    }

                });
        final Listener listener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (editor != null && !editor.isClosed()) {
                    if (!editor.getControl().getBounds().contains(event.x,
                            event.y))
                        editor.close(true);
                }
            }
        };
        Display.getCurrent().addFilter(SWT.MouseDown, listener);
        editor.getControl().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (listener != null)
                    Display.getCurrent().removeFilter(SWT.MouseDown, listener);

            }
        });
        lastEditor = editor;
        ITextViewer textViewer = editor.getTextViewer();
        textViewer.getTextWidget().setBackground(
                getResourceManager().createColor(new RGB(255, 255, 255)));
        textViewer.getTextWidget().setForeground(
                getResourceManager().createColor(new RGB(0, 0, 0)));
    }

    @Override
    protected void handleClickSectionTextClient(Object category) {
        if (category instanceof IMarkerGroup) {
            List<IMarker> newMarkers = ResourceUtils
                    .addMarkersFor((IMarkerGroup) category);
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_MARKER,
                    UserDataConstants.ADD_USER_MARKERS);
            refresh();
            reveal(category);
            setSelection(new StructuredSelection(newMarkers), true);
            MindMapUI.getResourceManager().saveUserMarkerSheet();
        }
    }

    public void startEditing(IMarker marker) {
        List<IMarkerGroup> markerGroups = getMarkerGroups();
        for (IMarkerGroup markerGroup : markerGroups) {
            List<IMarker> styles = markerGroup.getMarkers();
            if (styles.contains(marker)) {
                GalleryViewer galleryViewer = getNestedViewer(markerGroup);
                FramePart part = (FramePart) galleryViewer
                        .findGraphicalPart(marker);
                renameMarker((Composite) galleryViewer.getControl(), marker,
                        part.getFigure().getBounds());
                break;
            }
        }
    }

    private void renameMarker(Composite composite, final IMarker marker,
            org.eclipse.draw2d.geometry.Rectangle bounds) {

        int x = bounds.x + (bounds.width - RENAME_COMPOSITE_WIDTH) / 2;
        int y = bounds.y + (bounds.height - RENAME_COMPOSITE_HEIGHT) / 2;

        final FloatingTextEditor editor = new FloatingTextEditor(composite);
        editor.setInitialLocation(new Point(x, y));
        editor.setInitialSize(
                new Point(RENAME_COMPOSITE_WIDTH, RENAME_COMPOSITE_HEIGHT));
        editor.setInput(new Document(marker.getName()));
        editor.open();
        editor.doOperation(FloatingTextEditor.SELECT_ALL);
        editor.addFloatingTextEditorListener(
                new IFloatingTextEditorListener.Stub() {
                    public void editingFinished(TextEvent e) {
                        String text = e.text;
                        if (null != text && !("".equals(text))) //$NON-NLS-1$
                            marker.setName(text);
                    }
                });
    }

    public void updateInput() {
        setInput(getMarkerGroups());
    }

    public void deleteMarkerGroup() {
        IMarkerSheet markerSheet = MindMapUI.getResourceManager()
                .getUserMarkerSheet();
        markerSheet.removeMarkerGroup(activeMarkerGroupForSectionMenu);
        try {
            MindMapUI.getResourceManager().saveUserMarkerSheet();
        } catch (Exception e) {
            // TODO: handle exception
        }
        updateInput();
    }

    public void activateGroup(IMarkerGroup markerGroup) {
        activeMarkerGroupForSectionMenu = markerGroup;
        activeSectionForSectionMenu = (Section) getSection(markerGroup);
    }
}
