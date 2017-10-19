package org.xmind.ui.internal.resourcemanager;

import java.util.List;

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Section;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.CategorizedGalleryViewer;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.gallery.IDecorationContext;
import org.xmind.ui.gallery.ILabelDecorator;
import org.xmind.ui.resources.ColorUtils;

public abstract class ResourceManagerViewer extends CategorizedGalleryViewer {

    private static final int DEFAULT_FRAME_HEIGHT = 100;
    private static final int DEFAULT_FRAME_WIDTH = 200;
    public static final int DEFAULT_FLOATING_TEXT_EDITOR_WIDTH_EXPAND = 10;
//    private static final RGB BACKGROUND_SECTION = new RGB(248, 248, 248);
//    private static final RGB FOREGROUND_SECTION_TITLE_TEXT_CLIENT = new RGB(95,
//            64, 213);

    protected class ResourceCategorizedSelectTool extends GallerySelectTool {

        protected boolean handleMouseDown(org.xmind.gef.event.MouseEvent me) {
            FramePart targetFrame = findFrame(me.target);
            if (targetFrame != null && targetFrame.getFigure().isSelected()) {
                return super.handleMouseDown(me);
            } else {
                return handleSelectionOnMouseDown(me);
            }
        }

        private FramePart findFrame(IPart part) {
            while (part != null) {
                if (part instanceof FramePart)
                    return (FramePart) part;
                part = part.getParent();
            }
            return null;
        }
    }

    protected static class Layout extends AbstractHintLayout {

        private IDecorationContext properties;

        public Layout(IDecorationContext properties) {
            this.properties = properties;
        }

        public void layout(IFigure container) {
            Rectangle area = container.getClientArea();
            for (Object child : container.getChildren()) {
                IFigure figure = (IFigure) child;
                Dimension childSize = figure.getPreferredSize(-1, -1);
                int childWidth = Math.min(area.width, childSize.width);
                int childHeight = Math.min(area.height, childSize.height);
                figure.setBounds(
                        new Rectangle(area.x, area.y, childWidth, childHeight));
            }
        }

        @Override
        protected Dimension calculatePreferredSize(IFigure figure, int wHint,
                int hHint) {
            if (wHint > -1)
                wHint = Math.max(0, wHint - figure.getInsets().getWidth());
            if (hHint > -1)
                hHint = Math.max(0, hHint - figure.getInsets().getHeight());

            Insets insets = figure.getInsets();
            Dimension contentSize = (Dimension) properties
                    .getProperty(GalleryViewer.FrameContentSize, null);
            if (contentSize != null)
                return new Dimension(contentSize.width + insets.getWidth(),
                        contentSize.height + insets.getHeight());
            Dimension d = new Dimension();
            @SuppressWarnings("rawtypes")
            List children = figure.getChildren();
            IFigure child;
            for (int i = 0; i < children.size(); i++) {
                child = (IFigure) children.get(i);
                if (!isObservingVisibility() || child.isVisible())
                    d.union(child.getPreferredSize(wHint, hHint));
            }

            d.expand(figure.getInsets().getWidth(),
                    figure.getInsets().getHeight());
            d.union(getBorderPreferredSize(figure));
            return d;
        }

    }

    protected class CategorizedLabelProvider extends LabelProvider
            implements ILabelDecorator {

        public IFigure decorateFigure(IFigure figure, Object element,
                IDecorationContext context) {
            @SuppressWarnings("rawtypes")
            List children = figure.getChildren();
            boolean needInitFigureContent = children.isEmpty();
            if (needInitFigureContent) {
                SizeableImageFigure themeContentFigure = new SizeableImageFigure(
                        getImage(element));
                figure.add(themeContentFigure);

                if (context != null) {
                    figure.setLayoutManager(new Layout(context));
                    boolean imageConstrained = Boolean.TRUE.equals(
                            context.getProperty(GalleryViewer.ImageConstrained,
                                    false));
                    boolean imageStretched = Boolean.TRUE.equals(context
                            .getProperty(GalleryViewer.ImageStretched, false));
                    themeContentFigure.setConstrained(imageConstrained);
                    themeContentFigure.setStretched(imageStretched);
                }
            }

            children = figure.getChildren();
            if (children.size() == 1) {
                Object themeContentFigure = children.get(0);
                if (themeContentFigure instanceof SizeableImageFigure) {
                    ((SizeableImageFigure) themeContentFigure)
                            .setImage(getImage(element));
                }
            }

            return figure;
        }

    }

    private ResourceManager resourceManager;

    public void createControl(Composite container) {
        resourceManager = new LocalResourceManager(
                JFaceResources.getResources(), container);
    }

    @Override
    protected void configureNestedViewer(GalleryViewer viewer,
            Object category) {
        super.configureNestedViewer(viewer, category);
        initNestedGalleryViewer(viewer);
    }

    protected void initNestedGalleryViewer(GalleryViewer galleryViewerer) {
        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT,
                new ResourceCategorizedSelectTool());
        galleryViewerer.setEditDomain(editDomain);

        Properties properties = galleryViewerer.getProperties();
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.HideTitle, false);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.ImageConstrained, true);
        properties.set(GalleryViewer.CustomContentPaneDecorator, true);
    }

    protected void initProperties() {
        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT));
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, Boolean.TRUE);
        properties.set(GalleryViewer.FlatFrames, Boolean.TRUE);
        properties.set(GalleryViewer.HideTitle, Boolean.FALSE);
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout().align(
                GalleryLayout.ALIGN_TOPLEFT, GalleryLayout.ALIGN_TOPLEFT));
    }

    @Override
    protected void configureSection(final Section section,
            final Object category) {
        super.configureSection(section, category);
//        Color bg = resourceManager.createColor(BACKGROUND_SECTION);
//        section.setTitleBarBackground(bg);
//        section.setTitleBarBorderColor(bg);
//        section.setTitleBarGradientBackground(bg);
    }

    protected void createSectionTextClient(Section section, String text,
            final Object category) {
        Label label = new Label(section, SWT.NONE);
        label.setText(text);

        label.setForeground((Color) resourceManager
                .get(ColorUtils.toDescriptor("#0082F9"))); //$NON-NLS-1$
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                handleClickSectionTextClient(category);
            }
        });
        label.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_HAND));
        section.setTextClient(label);
    }

    protected void handleClickSectionTextClient(Object category) {
    }

    protected ResourceManager getResourceManager() {
        return resourceManager;
    }

    protected org.eclipse.swt.widgets.Layout createFormLayout() {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = -3;
        layout.marginHeight = 0;
        return layout;
    }
}
