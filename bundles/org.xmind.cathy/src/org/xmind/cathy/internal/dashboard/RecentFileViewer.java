package org.xmind.cathy.internal.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.util.Properties;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

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

    private class RecentFileFigure extends Figure {

        private URI recentFile;

        private final Rectangle RECT = new Rectangle();

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
            Boolean isStretched = (Boolean) RecentFileViewer.this
                    .getProperty(GalleryViewer.ImageStretched, false);
            Boolean isConstrained = (Boolean) RecentFileViewer.this
                    .getProperty(GalleryViewer.ImageConstrained, false);
            if (isConstrained && (isStretched || imageSize.width > area.width
                    || imageSize.height > area.height)) {
                adaptAreaToRatio(area, imageSize, isStretched);
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
            InputStream thumbnailData = null;
            try {
                IEditorHistory editorHistory = PlatformUI.getWorkbench()
                        .getService(IEditorHistory.class);
                thumbnailData = editorHistory.loadThumbnailData(recentFile);
                if (thumbnailData != null) {
                    ImageDescriptor imageDescriptor = ImageDescriptor
                            .createFromImageData(new ImageData(thumbnailData));
                    return JFaceResources.getResources()
                            .createImage(imageDescriptor);
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (thumbnailData != null)
                        thumbnailData.close();
                } catch (IOException e) {
                }
            }
            return MindMapUI.getImages()
                    .get(IMindMapImages.THUMBNAIL_LOST, true).createImage();
        }

    }

    private class RecentFilePart extends GraphicalEditPart {

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

    private class RecentFilePartFactory implements IPartFactory {

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

    static Image pinImage;

    private IEditorHistory editorHistory;

    public RecentFileViewer(Composite parent) {
        editorHistory = PlatformUI.getWorkbench()
                .getService(IEditorHistory.class);
        initViewer(parent);
        createControl(parent);
        registerHelper(parent.getShell());
    }

    private void registerHelper(Shell shell) {
        shell.setData(ICathyConstants.HELPER_RECENTFILE_PIN, new Runnable() {
            public void run() {
                final ISelection selection = getSelection();
                if (selection instanceof IStructuredSelection) {
                    final List list = ((IStructuredSelection) selection)
                            .toList();
                    for (final Object element : list) {
                        if (element instanceof URI) {
                            final boolean isChecked = editorHistory
                                    .isPinned((URI) element);
                            if (isChecked) {
                                unPinRecentFile((URI) element);
                            } else {
                                pinRecentFile((URI) element);
                            }
                        }
                    }
                }

            }
        });
        shell.setData(ICathyConstants.HELPER_RECENTFILE_DELETE, new Runnable() {
            public void run() {
                final ISelection selection = getSelection();
                if (selection instanceof IStructuredSelection) {
                    final List list = ((IStructuredSelection) selection)
                            .toList();
                    for (final Object element : list) {
                        if (element instanceof URI) {
                            deleteRecentFile((URI) element);
                        }
                    }
                }
            }
        });
        shell.setData(ICathyConstants.HELPER_RECENTFILE_CLEAR, new Runnable() {
            public void run() {
                clearRecentFile();
            }
        });
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
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
        properties.set(GalleryViewer.ImageStretched, Boolean.TRUE);

        properties.set(GalleryViewer.FrameContentSize, new Dimension(215, 130));
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_TOPLEFT, 10, 10, new Insets(10)));

        properties.set(GalleryViewer.ContentPaneBorderWidth, 1);
        properties.set(GalleryViewer.ContentPaneBorderColor,
                ColorUtils.getColor("#cccccc"));

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

        IEditorHistory editorHistory = PlatformUI.getWorkbench()
                .getService(IEditorHistory.class);
        editorHistory.addEditorHistoryListener(contentProvider);
        setInput(editorHistory);
        handleRecentFileListChanged(contentProvider, labelProvider, true);

    }

    private void clearRecentFile() {
        editorHistory.clear();
    }

    private void deleteRecentFile(URI fileURI) {
        editorHistory.remove(fileURI);
    }

    private void pinRecentFile(URI fileURI) {
        editorHistory.pin(fileURI);
        updateRecentFilePart(fileURI);
    }

    private void unPinRecentFile(URI fileURI) {
        editorHistory.unPin(fileURI);
        updateRecentFilePart(fileURI);
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

    private Image getPinImage(URI uri) {
        boolean isPin = editorHistory.isPinned(uri);
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
