package org.xmind.cathy.internal.dashboard;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.util.Properties;
import org.xmind.ui.IEditorHistory;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class RecentFileViewer extends GalleryViewer {

    private static class RecentInputURILabelProvider extends LabelProvider {

        private Map<URI, String> labels;

        public void setLabels(Map<URI, String> labels) {
            this.labels = labels;
        }

        @Override
        public String getText(Object element) {
            if (!(element instanceof URI))
                return super.getText(element);
            URI uri = (URI) element;
            String label = labels == null ? null : labels.get(uri);
            if (label != null)
                return label;
            return uri.toString();
        }
    }

    private static class RecentFileFigure extends Figure {

        private URI recentFile;

        private static final Rectangle RECT = new Rectangle();

        private Image pinImage = null;

        public void setRecentFile(URI recentFile) {
            if (this.recentFile == recentFile)
                return;
            this.recentFile = recentFile;
            repaint();
        }

        public void setPinImage(Image pinImage) {
            if (pinImage == this.pinImage)
                return;
            this.pinImage = pinImage;
            repaint();
        }

        public void paint(Graphics graphics) {
            GraphicsUtils.fixGradientBugForCarbon(graphics, this);
            super.paint(graphics);
        }

        protected void paintFigure(Graphics graphics) {
            super.paintFigure(graphics);
            drawRecentFile(graphics);
        }

        protected void drawRecentFile(Graphics graphics) {
            if (recentFile == null)
                return;

            graphics.setAntialias(SWT.ON);
            graphics.setTextAntialias(SWT.ON);

            drawRecentFile(graphics, recentFile);
        }

        protected void drawRecentFile(Graphics graphics, URI recentFile) {
            Image image = getImageFromSource(recentFile);

            if (image != null) {
                Dimension imageSize = new Dimension(image);
                paintImage(graphics, image, imageSize,
                        getImageClientArea(imageSize));
            }

            if (pinImage != null) {
                Rectangle ca = getClientArea();
                graphics.drawImage(pinImage, ca.x + 1, ca.y + 1);
            }
        }

        protected Rectangle getImageClientArea(Dimension imageSize) {
            Rectangle area = getClientArea(RECT);
            if (imageSize.width > area.width
                    || imageSize.height > area.height) {
                adaptAreaToRatio(area, imageSize, false);
            } else {
                adaptAreaToSize(area, imageSize);
            }
            return area;
        }

        protected void adaptAreaToSize(Rectangle area, Dimension size) {
            area.x += (area.width - size.width) / 2;
            area.width = size.width;
            area.y += (area.height - size.height) / 2;
            area.height = size.height;
        }

        protected void adaptAreaToRatio(Rectangle area, Dimension ratio,
                boolean bigger) {
            int a = ratio.width * area.height;
            int b = ratio.height * area.width;
            if (bigger ? (a < b) : (a > b)) {
                int h = area.width == 0 ? 0 : b / ratio.width;
                area.y += (area.height - h) / 2;
                area.height = h;
            } else if (bigger ? (a > b) : (a < b)) {
                int w = area.height == 0 ? 0 : a / ratio.height;
                area.x += (area.width - w) / 2;
                area.width = w;
            }
        }

        protected void paintImage(Graphics graphics, Image image,
                Dimension imageSize, Rectangle clientArea) {
            if (clientArea.width == imageSize.width
                    && clientArea.height == imageSize.height) {
                graphics.drawImage(image, clientArea.x, clientArea.y);
            } else {
                graphics.drawImage(image, 0, 0, imageSize.width,
                        imageSize.height, clientArea.x, clientArea.y,
                        clientArea.width, clientArea.height);
            }
        }

        private Image getImageFromSource(URI recentFile) {
            String thumbnailPath = MindMapUI.getEditorHistory()
                    .getThumbnail(recentFile);
            if (thumbnailPath != null && new File(thumbnailPath).exists()) {
                ImageDescriptor imageDescriptor = ImageDescriptor
                        .createFromFile(null, thumbnailPath);
                return JFaceResources.getResources()
                        .createImage(imageDescriptor);
            } else {
                return MindMapUI.getImages()
                        .get(IMindMapImages.THUMBNAIL_LOST, true).createImage();
            }
        }

    }

    private static class RecentFilePart extends GraphicalEditPart {

        public RecentFilePart(URI uri) {
            setModel(uri);
        }

        public URI getURI() {
            return (URI) super.getModel();
        }

        protected IFigure createFigure() {
            return new RecentFileFigure();
        }

        protected void updateView() {
            super.updateView();
            ((RecentFileFigure) getFigure()).setRecentFile(getURI());
            ((RecentFileFigure) getFigure()).setPinImage(getPinImage(getURI()));

            Properties properties = ((GalleryViewer) getSite().getViewer())
                    .getProperties();
            Dimension size = (Dimension) properties
                    .get(GalleryViewer.FrameContentSize);
            if (size != null) {
                getFigure().setPreferredSize(size);
            }
        }

        protected void register() {
            registerModel(getURI());
            super.register();
        }

        @Override
        protected void unregister() {
            super.unregister();
            unregisterModel(getURI());
        }
    }

    private static class RecentFilePartFactory implements IPartFactory {

        private IPartFactory factory;

        public RecentFilePartFactory(IPartFactory factory) {
            this.factory = factory;
        }

        public IPart createPart(IPart context, Object model) {
            if (context instanceof FramePart && model instanceof URI)
                return new RecentFilePart((URI) model);
            return factory.createPart(context, model);
        }

    }

    private class PinRecentFileAction extends Action
            implements ISelectionAction {

        public PinRecentFileAction() {
            super(WorkbenchMessages.RecentFileViewer_PinThisMapAction_label,
                    AS_CHECK_BOX);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.PIN, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.PIN, true));
        }

        @Override
        public void run() {
            ISelection selection = getSelection();
            if (selection instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) selection)
                        .getFirstElement();
                if (element instanceof URI) {
                    boolean isChecked = MindMapUI.getEditorHistory()
                            .isPin((URI) element);
                    if (isChecked) {
                        unPinRecentFile((URI) element);
                    } else {
                        pinRecentFile((URI) element);
                    }
//                    setChecked(!isChecked);
                }
            }
        }

        boolean hasPinFor(ISelection selection) {
            boolean hasPin = false;
            if (selection instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) selection)
                        .getFirstElement();
                if (element instanceof URI) {
                    hasPin = MindMapUI.getEditorHistory().isPin((URI) element);
                }
            }
            return hasPin;
        }

        public void setSelection(ISelection selection) {
            boolean hasPin = hasPinFor(selection);
            setText(hasPin
                    ? WorkbenchMessages.RecentFileViewer_UnpinThisMap_label
                    : WorkbenchMessages.RecentFileViewer_PinThisMapAction_label);
            setChecked(hasPin);
        }
    }

    static Image pinImage;

    private List<ISelectionAction> selectionActions = new ArrayList<ISelectionAction>();

    public RecentFileViewer(Composite parent) {
        initViewer(parent);
        Control control = createControl(parent);
        MenuManager contextMenu = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        PinRecentFileAction pinRecentFileAction = new PinRecentFileAction();
        selectionActions.add(pinRecentFileAction);
        contextMenu.add(pinRecentFileAction);
        control.setMenu(contextMenu.createContextMenu(control));

    }

    private void initViewer(Composite parent) {
        setPartFactory(new RecentFilePartFactory(getPartFactory()));

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        setEditDomain(editDomain);

        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.ImageConstrained, true);

        properties.set(GalleryViewer.FrameContentSize, new Dimension(200, 100));
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_TOPLEFT, 10, 10, new Insets(10)));
        properties.set(GalleryViewer.EmptySelectionIgnored, true);

        final RecentFileListContentProvider contentProvider = new RecentFileListContentProvider();
        final RecentInputURILabelProvider labelProvider = new RecentInputURILabelProvider();
        contentProvider.addContentChangeListener(new Runnable() {
            public void run() {
                handleRecentFileListChanged(contentProvider, labelProvider,
                        true);
            }
        });

        setContentProvider(contentProvider);
        setLabelProvider(labelProvider);

        IEditorHistory editorHistory = MindMapUI.getEditorHistory();
        editorHistory.addEditorHistoryListener(contentProvider);
        setInput(editorHistory);
        handleRecentFileListChanged(contentProvider, labelProvider, true);

        addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof StructuredSelection) {
                    final Object recentFileURI = ((StructuredSelection) selection)
                            .getFirstElement();
                    if (recentFileURI instanceof URI) {
                        handleOpenRecentFile(recentFileURI.toString());
                    }
                }
            }

        });

        addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                handleSelectionChanged(event.getSelection());
            }
        });

    }

    private void handleSelectionChanged(ISelection selection) {
        updateSelectionActions(selection);
    }

    private void updateSelectionActions(ISelection selection) {
        for (ISelectionAction action : selectionActions) {
            action.setSelection(selection);
        }
    }

    public void pinRecentFile(URI fileURI) {
        MindMapUI.getEditorHistory().pin(fileURI);
        updateRecentFilePart(fileURI);
    }

    public void unPinRecentFile(URI fileURI) {
        MindMapUI.getEditorHistory().unPin(fileURI);
        updateRecentFilePart(fileURI);
    }

    private void handleOpenRecentFile(final Object recentFileURI) {
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IWorkbench workbench = PlatformUI.getWorkbench();
                if (workbench == null)
                    return;

                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                IWorkbenchPage page = window.getActivePage();
                if (page == null)
                    return;

                IEditorInput input = MME
                        .createEditorInputFromURI((String) recentFileURI);
                page.openEditor(input, MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }

    private void handleRecentFileListChanged(
            RecentFileListContentProvider contentProvider,
            RecentInputURILabelProvider labelProvider, boolean refresh) {
        Map<URI, String> labels = new HashMap<URI, String>();
        FilePathParser.calculateFileURILabels(
                contentProvider.getRecentInputURIs(), labels);
        labelProvider.setLabels(labels);
        if (refresh) {
//            refresh();
            setInput(getInput());
        }
    }

    private static Image getPinImage(URI uri) {
        boolean isPin = MindMapUI.getEditorHistory().isPin(uri);
        return isPin ? getPinImage() : null;
    }

    private static Image getPinImage() {
        if (pinImage == null) {
            ImageDescriptor desc = MindMapUI.getImages().get(IMindMapImages.PIN,
                    true);
            if (desc != null) {
                try {
                    pinImage = desc.createImage();
                } catch (Throwable e) {
                    //e.printStackTrace();
                }
            }
        }
        return pinImage;
    }

    private void updateRecentFilePart(URI pinURI) {
        RecentFilePart part = findRecentFilePart(pinURI);
        if (part != null)
            part.update();
    }

    private RecentFilePart findRecentFilePart(URI pinURI) {
        if (pinURI == null)
            return null;
        return (RecentFilePart) getPartRegistry().getPartByModel(pinURI);
    }

}
