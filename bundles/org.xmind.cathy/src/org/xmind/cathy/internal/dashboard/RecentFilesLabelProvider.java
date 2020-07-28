package org.xmind.cathy.internal.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Date;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.editor.IEditorHistoryItem;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.gallery.IDecorationContext;
import org.xmind.ui.gallery.ILabelDecorator;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class RecentFilesLabelProvider extends LabelProvider
        implements ILabelDecorator {

    Image pinImage;

    protected static class RecentFrameContentLayout
            extends RecentContainerLayout {

        private IDecorationContext context;

        public RecentFrameContentLayout(IDecorationContext context) {
            this.context = context;
        }

        @Override
        protected Dimension calculatePreferredSize(IFigure figure, int wHint,
                int hHint) {
            if (context != null) {
                Insets insets = figure.getInsets();
                Dimension contentSize = (Dimension) context
                        .getProperty(GalleryViewer.FrameContentSize, null);
                if (contentSize != null)
                    return new Dimension(contentSize.width + insets.getWidth(),
                            contentSize.height + insets.getHeight());
            }
            return super.calculatePreferredSize(figure, wHint, hHint);
        }
    }

    protected static final String COLOR_NONEXISTING_WORKBOOK_COVER = "#DDDDDD"; //$NON-NLS-1$
    protected static final String COLOR_NONEXISTING_WORKBOOK_TEXT = "#CCCCCC"; //$NON-NLS-1$

    private LocalResourceManager resources;
    private IEditorHistory editorHistory;
    private Map<Object, Image> images;

    public RecentFilesLabelProvider(Composite parent) {
        this.images = new HashMap<Object, Image>();
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);
        editorHistory = PlatformUI.getWorkbench()
                .getService(IEditorHistory.class);
    }

    public void clear() {
        Object[] imageArray = images.values().toArray();
        images.clear();
        for (Object image : imageArray) {
            ((Image) image).dispose();
        }
    }

    @Override
    public void dispose() {
        resources.dispose();
        clear();
        super.dispose();
    }

    @Override
    public String getText(Object element) {
        if (!(element instanceof URI))
            return super.getText(element);
        URI uri = (URI) element;
        IEditorHistoryItem item = editorHistory.getItem(uri);
        if (item != null) {
            String name = item.getName();
            Assert.isTrue(name != null);

            StringBuffer buf = new StringBuffer();
            if (name.length() > 20)
                name = name.substring(0, 20) + "..."; //$NON-NLS-1$
            buf.append(name);
            return buf.toString();
        }
        return uri.toString();
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof URI) {
            URI uri = (URI) element;
            Image image = images.get(uri);
            if (image != null && !image.isDisposed())
                return image;

            image = getImageByThumb(uri);
            if (image != null && !image.isDisposed()) {
                images.put(uri, image);
            }
            return image;
        }
        return resources.createImage(
                MindMapUI.getImages().get(IMindMapImages.THUMBNAIL_LOST, true));
    }

    private Image getImageByThumb(URI uri) {
        InputStream thumbnailData = null;
        try {
            thumbnailData = editorHistory.loadThumbnailData(uri);
            if (thumbnailData != null) {
                return new Image(resources.getDevice(), thumbnailData);
            }
        } catch (IOException e) {
            CathyPlugin.log(e, String.format(
                    "Failed to load preview image for recent page uri == %s", //$NON-NLS-1$
                    uri));
        } catch (SWTException e) {
            CathyPlugin.log(e, String.format(
                    "Failed to load preview image for recent page uri == %s", //$NON-NLS-1$
                    uri));
        } finally {
            try {
                if (thumbnailData != null)
                    thumbnailData.close();
            } catch (IOException e) {
            }
        }
        return resources.createImage(
                MindMapUI.getImages().get(IMindMapImages.THUMBNAIL_LOST, true));
    }

    public IFigure decorateFigure(IFigure figure, Object element,
            IDecorationContext context) {
        if (!(element instanceof URI))
            return figure;

        return decorateFrameFigure(figure, (URI) element, context);
    }

    protected IFigure decorateFrameFigure(IFigure contentPane, URI uri,
            IDecorationContext context) {
        SizeableImageFigure thumbnailFigure;
        SizeableImageFigure pinIcon;

        List figures = contentPane.getChildren();
        boolean needInitFigureContent = figures.isEmpty();
        if (needInitFigureContent) {
            contentPane.setLayoutManager(new RecentFrameContentLayout(context));

            thumbnailFigure = new SizeableImageFigure(getImage(uri));
            pinIcon = new SizeableImageFigure(getPinImage(uri));

            contentPane.add(thumbnailFigure);
            contentPane.add(pinIcon, Integer
                    .valueOf(PositionConstants.LEFT | PositionConstants.TOP));

            if (context != null) {
                boolean imageConstrained = Boolean.TRUE.equals(context
                        .getProperty(GalleryViewer.ImageConstrained, false));
                boolean imageStretched = Boolean.TRUE.equals(context
                        .getProperty(GalleryViewer.ImageStretched, false));
                thumbnailFigure.setConstrained(imageConstrained);
                thumbnailFigure.setStretched(imageStretched);
            }
        } else {
            thumbnailFigure = (SizeableImageFigure) figures.get(0);
            pinIcon = (SizeableImageFigure) figures.get(1);
        }

        thumbnailFigure.setImage(getImage(uri));
        pinIcon.setImage(getPinImage(uri));

        return contentPane;
    }

    private Image getPinImage(URI uri) {
        boolean isPin = editorHistory.isPinned(uri);
        return isPin ? getPinImage() : null;
    }

    private Image getPinImage() {
        if (pinImage == null) {
            ImageDescriptor desc = MindMapUI.getImages().get(IMindMapImages.PIN,
                    true);
            if (desc != null) {
                try {
                    pinImage = resources.createImage(desc);
                } catch (Throwable e) {
                    //e.printStackTrace();
                }
            }
        }
        return pinImage;
    }

    public String getSubtitle(Object element) {
        if (element instanceof URI) {
            IEditorHistoryItem item = editorHistory.getItem((URI) element);
            long t = item.getOpenedTime();
            Date date = new Date(t);
            return DateFormat
                    .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(date);
        }
        Date newDate = new Date(System.currentTimeMillis());
        return DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(newDate);
    }
}
