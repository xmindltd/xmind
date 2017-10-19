package org.xmind.cathy.internal.dashboard;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FrameBorderLayout;
import org.xmind.ui.gallery.FrameDecorator;
import org.xmind.ui.gallery.FrameFigure;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryPartFactory;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class RecentFilesGalleryPartFactory extends GalleryPartFactory {
    public static class RecentFilesFramePart extends FramePart {
        RecentFilesFrameFigure figure;

        public RecentFilesFramePart(Object model) {
            super(model);
            setDecorator(RecentFilesFrameDecorator.DEFAULT);
        }

        @Override
        protected IFigure createFigure() {
            figure = new RecentFilesFrameFigure();
            Properties properties = getSite().getViewer().getProperties();
            boolean useAdvancedRenderer = properties.getBoolean(
                    IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
            figure.setTitleRenderStyle(useAdvancedRenderer
                    ? RotatableWrapLabel.ADVANCED : RotatableWrapLabel.NORMAL);

            return figure;
        }

        @Override
        protected void updateChildren() {
            super.updateChildren();
            boolean isSelected = figure.isSelected();
            if (isSelected) {
                figure.setForegroundColor(ColorConstants.white);
                figure.subTitle.setForegroundColor(ColorConstants.white);
            }
        }
    }

    private static class RecentFilesFrameDecorator extends FrameDecorator {

        public static final RecentFilesFrameDecorator DEFAULT = new RecentFilesFrameDecorator();

        @Override
        public void decorate(IGraphicalPart part, IFigure figure) {
            super.decorate(part, figure);

            IFigure f = part.getFigure();
            Object model = part.getModel();

            IViewer viewer = part.getSite().getViewer();
            IBaseLabelProvider labelProvider = viewer
                    .getAdapter(IBaseLabelProvider.class);

            if (f instanceof RecentFilesFrameFigure
                    && labelProvider instanceof RecentFilesLabelProvider) {
                decorateSubTitle(((RecentFilesFrameFigure) f).getSubTitle(),
                        model, (RecentFilesLabelProvider) labelProvider);
            }
        }

        private void decorateSubTitle(ITextFigure subTitle, Object model,
                RecentFilesLabelProvider labelProvider) {
            if (model == null)
                return;
            String text = labelProvider.getSubtitle(model);
            if (text == null)
                return;

            subTitle.setText(text);
            subTitle.setForegroundColor((Color) JFaceResources.getResources()
                    .get(ColorUtils.toDescriptor("#8f8f8f"))); //$NON-NLS-1$
            Font countFont = subTitle.getFont();
            if (countFont != null) {
                FontData[] fontData = countFont.getFontData();
                FontData[] newFontData = FontUtils.newHeight(fontData,
                        Util.isMac() ? 9 : 7);
                subTitle.setFont((Font) JFaceResources.getResources()
                        .get(FontDescriptor.createFrom(newFontData)));
            }
        }
    }

    private static class RecentFilesFrameFigure extends FrameFigure {

        private RotatableWrapLabel subTitle;

        Color subTitleColor = (Color) JFaceResources.getResources()
                .get(ColorUtils.toDescriptor("#8f8f8f")); //$NON-NLS-1$

        public RecentFilesFrameFigure() {
            super();

            subTitle = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
            subTitle.setTextAlignment(PositionConstants.CENTER);
            subTitle.setEnabled(false);
            subTitle.setAbbreviated(true);
            subTitle.setForegroundColor(subTitleColor);
            getTitleContainer().add(subTitle, FrameBorderLayout.BOTTOM);

        }

        public ITextFigure getSubTitle() {
            return subTitle;
        }

        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            subTitle.setForegroundColor(
                    selected ? ColorConstants.white : subTitleColor);
        }
    }

    @Override
    protected IPart createFramePart(IPart parent, Object model) {
        return new RecentFilesFramePart(model);
    }

}
