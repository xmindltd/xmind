package org.xmind.ui.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.SimpleRectangleFigure;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FrameFigure;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.gallery.IDecorationContext;
import org.xmind.ui.gallery.ILabelDecorator;
import org.xmind.ui.gallery.ShadowedLayer;
import org.xmind.ui.resources.ColorUtils;

public class GalleryMoveTool extends DummyMoveTool {

    public static final String PARAM_INSERT_TARGET = "insertTarget"; //$NON-NLS-1$
    public static final String PARAM_INSERT_POSITION = "insertPosition"; //$NON-NLS-1$

    private static final int DELTA = 5;

    private Layer layer;
    private SimpleRectangleFigure cover;

    private IFigure placeholder;
    private boolean horizontalLayout;

    private IPart relativePart;

    /**
     * true is for before, false for after.
     */
    private boolean beforeOrAfter;

    private ResourceManager resources;

    @Override
    protected void start() {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                getTargetViewer().getControl());

        super.start();
        horizontalLayout = getTargetViewer().getProperties()
                .getBoolean(GalleryViewer.HorizontalLayout, false);
        Layer contentCover = getContentCover();
        cover = new SimpleRectangleFigure(
                contentCover.getBounds().getShrinked(2, 2));
        cover.setBackgroundColor(ColorConstants.gray);
        cover.setSubAlpha(0x33);
        contentCover.add(cover);
    }

    private Layer getContentCover() {
        FramePart framePart = getFramePart();
        FrameFigure frameFigure = framePart.getFigure();
        return frameFigure.getContentCover();
    }

    protected FramePart getFramePart() {
        return (FramePart) getSource();
    }

    @Override
    protected void end() {
        horizontalLayout = false;
        getContentCover().remove(cover);
        cover = null;
        layer.remove(placeholder);
        placeholder = null;
        super.end();
    }

    private RectangleFigure createPlaceholder() {
        RectangleFigure placeholder = new RectangleFigure();
        placeholder.setFill(true);
//        placeholder.setOutline(true);
        placeholder.setForegroundColor(
                (Color) resources.get(ColorUtils.toDescriptor("#a3a3a3"))); //$NON-NLS-1$
        placeholder.setBackgroundColor(
                (Color) resources.get(ColorUtils.toDescriptor("#a3a3a3"))); //$NON-NLS-1$
        placeholder.setLineWidth(4);
        return placeholder;
    }

    @Override
    protected IFigure createDummy() {
        IGraphicalViewer viewer = getTargetViewer();
        layer = viewer.getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            FrameFigure dummyFrameFigure = new FrameFigure() {

                @Override
                public void paint(Graphics graphics) {
                    graphics.setAlpha(0x4c);
                    super.paint(graphics);
                }
            };
            decorate(dummyFrameFigure);
            dummyFrameFigure.setBounds(getFramePart().getFigure().getBounds());
            layer.add(dummyFrameFigure);

            placeholder = createPlaceholder();
            layer.add(placeholder);
            return dummyFrameFigure;
        }
        return null;
    }

    private void decorate(FrameFigure dummyFrameFigure) {
        Object model = getFramePart().getModel();
        IViewer viewer = getTargetViewer();
        Properties properties = viewer.getProperties();
        IBaseLabelProvider labelProvider = viewer
                .getAdapter(IBaseLabelProvider.class);

        boolean hideTitle = properties.getBoolean(GalleryViewer.HideTitle,
                false);
        dummyFrameFigure.setHideTitle(hideTitle);

        boolean flat = properties.getBoolean(GalleryViewer.FlatFrames, false);
        dummyFrameFigure.setFlat(flat);
        dummyFrameFigure.setContentSize(
                (Dimension) properties.get(GalleryViewer.FrameContentSize));

        int titlePlacement = properties.getInteger(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_TOP.intValue());
        dummyFrameFigure.setTitlePlacement(titlePlacement);
        if (!hideTitle) {
            decorateTitle(dummyFrameFigure.getTitle(), model, labelProvider);
        }

        boolean useCustomDecorator = properties
                .getBoolean(GalleryViewer.CustomContentPaneDecorator, false);
        if (useCustomDecorator && labelProvider instanceof ILabelDecorator) {
            IDecorationContext context = viewer instanceof IDecorationContext
                    ? (IDecorationContext) viewer : null;
            ((ILabelDecorator) labelProvider).decorateFigure(
                    dummyFrameFigure.getContentPane(), model, context);
        }

        ShadowedLayer layer = dummyFrameFigure.getContentPane();
        layer.setBorderWidth(
                properties.getInteger(GalleryViewer.ContentPaneBorderWidth, 1));
        Object color = properties.get(GalleryViewer.ContentPaneBorderColor);
        if (color != null && color instanceof Color) {
            layer.setBorderAlpha(0xff);
            layer.setBorderColor((Color) color);
        }
        layer.setLayoutManager(
                getFramePart().getFigure().getContentPane().getLayoutManager());

        Image image = getImage(model, labelProvider);
        SizeableImageFigure imageFigure = new SizeableImageFigure();
        decorateImage(imageFigure, image, properties);
        dummyFrameFigure.getContentPane().add(imageFigure);

    }

    private Image getImage(Object element, IBaseLabelProvider labelProvider) {
        if (labelProvider instanceof ILabelProvider)
            return ((ILabelProvider) labelProvider).getImage(element);
        return null;
    }

    protected void decorateImage(SizeableImageFigure imageFigure, Image image,
            Properties properties) {
        imageFigure.setImage(image);
        boolean stretched = properties.getBoolean(GalleryViewer.ImageStretched,
                false);
        boolean constained = properties
                .getBoolean(GalleryViewer.ImageConstrained, false);
        imageFigure.setConstrained(constained);
        imageFigure.setStretched(stretched);
        Insets margins = (Insets) properties
                .get(GalleryViewer.ContentPaneMargins);
        imageFigure.setMargins(margins);
    }

    private String getText(Object element, IBaseLabelProvider labelProvider) {
        if (labelProvider instanceof ILabelProvider)
            return ((ILabelProvider) labelProvider).getText(element);
        return null;
    }

    protected void decorateTitle(ITextFigure titleFigure, Object model,
            IBaseLabelProvider labelProvider) {
        String text = getText(model, labelProvider);
        if (text == null)
            text = ""; //$NON-NLS-1$
        titleFigure.setText(text);
        if (labelProvider instanceof IFontProvider) {
            IFontProvider fontProvider = (IFontProvider) labelProvider;
            titleFigure.setFont(fontProvider.getFont(model));
        }
        if (labelProvider instanceof IColorProvider) {
            IColorProvider colorProvider = (IColorProvider) labelProvider;
            titleFigure.setForegroundColor(colorProvider.getForeground(model));
            titleFigure.setBackgroundColor(colorProvider.getBackground(model));
        }
    }

    @Override
    protected void onMoving(Point currentPos, MouseDragEvent me) {
        super.onMoving(currentPos, me);
        IGraphicalViewer viewer = getTargetViewer();
        MouseEvent swtEvent = me.getCurrentSWTEvent();
        IPart framePart = viewer.findPart(swtEvent.x, swtEvent.y);
        if (framePart instanceof FramePart) {
            this.relativePart = framePart;
            FrameFigure frameFigure = ((FramePart) framePart).getFigure();
            Rectangle bounds = frameFigure.getBounds();
            int lineWidth = 2;
            if (horizontalLayout) {
                Rectangle leftRect = bounds.getResized(-bounds.width / 2, 0);
                if (leftRect.contains(currentPos)) {
                    placeholder.setBounds(
                            new Rectangle(bounds.x - 1, bounds.y - DELTA,
                                    lineWidth, bounds.height + DELTA * 2));
                    beforeOrAfter = true;
                } else {
                    placeholder.setBounds(new Rectangle(
                            bounds.x + bounds.width + 2, bounds.y - DELTA,
                            lineWidth, bounds.height + DELTA * 2));
                    beforeOrAfter = false;
                }

            } else {
                Rectangle upRect = bounds.getResized(0, -bounds.height / 2);
                if (upRect.contains(currentPos)) {
                    placeholder.setBounds(new Rectangle(bounds.x - DELTA,
                            bounds.y - 1, bounds.width + DELTA * 2, lineWidth));
                    beforeOrAfter = true;
                } else {
                    placeholder.setBounds(new Rectangle(bounds.x - DELTA,
                            bounds.y + bounds.height + 2,
                            bounds.width + DELTA * 2, lineWidth));
                    beforeOrAfter = false;
                }
            }
        }

    }

    @Override
    protected Request createRequest() {
        Request request = new Request(GEF.REQ_MOVETO);
        request.setDomain(getDomain());
        request.setViewer(getTargetViewer());
        //both target and source are moved parts
        List<IPart> parts = new ArrayList<IPart>();
        for (IPart p : getSelectedParts(getTargetViewer())) {
            if (p.hasRole(GEF.ROLE_MOVABLE)) {
                parts.add(p);
            }
        }
        request.setTargets(parts);
        request.setPrimaryTarget(getSource());
        request.setParameter(GEF.PARAM_POSITION, getCursorPosition());
        request.setParameter(GEF.PARAM_PARENT, getFramePart().getParent());
        request.setParameter(GalleryMoveTool.PARAM_INSERT_TARGET, relativePart);
        request.setParameter(GalleryMoveTool.PARAM_INSERT_POSITION,
                beforeOrAfter);

        return request;
    }

}
