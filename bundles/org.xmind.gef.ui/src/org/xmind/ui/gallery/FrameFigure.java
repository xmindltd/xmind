/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.gallery;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.AdvancedToolbarLayout;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.ui.resources.ColorUtils;

/**
 * @author Frank Shaka
 */
public class FrameFigure extends Figure {

    private static final int FLAG_SELECTED = MAX_FLAG << 1;
    private static final int FLAG_PRESELECTED = MAX_FLAG << 2;
    private static final int FLAG_HIDE_TITLE = MAX_FLAG << 3;
    private static final int FLAG_FLAT = MAX_FLAG << 4;

    static {
        MAX_FLAG = FLAG_FLAT;
    }

    protected static final Color ColorSelected = ColorUtils.getColor("#0070d8"); //$NON-NLS-1$
    protected static final Color ColorSelectedPreselected = ColorUtils
            .getColor("#2088e0"); //$NON-NLS-1$
//    protected static final Color ColorInactive = ColorUtils.gray(ColorSelected);

    private static final int PADDING = 6;

    private RotatableWrapLabel title;

    private IFigure titleContainer;

    private IFigure contentContainer;

    private ShadowedLayer contentLayer;

    private int titlePlacement = PositionConstants.TOP;
    private Layer contentCover;

    /**
     * 
     */
    public FrameFigure() {
        setOpaque(false);
        setBorder(new MarginBorder(PADDING));
        FrameBorderLayout layout = new FrameBorderLayout();
        layout.setVerticalSpacing(2);
        layout.setHorizontalSpacing(2);
        super.setLayoutManager(layout);

        titleContainer = new Layer();
        AdvancedToolbarLayout titleContainerLayout = new AdvancedToolbarLayout();
        titleContainerLayout.setStretchMinorAxis(true);
        titleContainer.setLayoutManager(titleContainerLayout);
        add(titleContainer, FrameBorderLayout.TOP);

        title = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
        title.setTextAlignment(PositionConstants.CENTER);
        title.setAbbreviated(true);
        title.setForegroundColor(ColorConstants.black);
        titleContainer.add(title, FrameBorderLayout.TOP);

        Layer contentPane = new Layer();
        contentPane.setLayoutManager(new StackLayout());
        add(contentPane, FrameBorderLayout.CENTER);

        contentContainer = new Layer();
        contentPane.add(contentContainer);
        AdvancedToolbarLayout contentContainerLayout = new AdvancedToolbarLayout(
                true);
        contentContainerLayout
                .setMajorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
        contentContainerLayout
                .setMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
        contentContainerLayout
                .setInnerMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
        contentContainer.setLayoutManager(contentContainerLayout);

        contentLayer = new ShadowedLayer();
        contentLayer.setBorderColor(ColorUtils.getColor(170, 170, 170));
        contentContainer.add(contentLayer);

        contentCover = new Layer();
        AdvancedToolbarLayout presentationLayout = new AdvancedToolbarLayout(
                true);
        presentationLayout
                .setMajorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
        presentationLayout
                .setMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
        presentationLayout
                .setInnerMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
        contentCover.setLayoutManager(presentationLayout);
        contentPane.add(contentCover, GEF.LAYER_PRESENTATION);
    }

    public void setContentSize(Dimension size) {
        if (size == null) {
            contentContainer.setPreferredSize(null);
        } else {
            Insets ins1 = contentContainer.getInsets();
            Insets ins2 = contentLayer.getInsets();
            contentContainer.setPreferredSize(
                    size.getExpanded(ins1.getWidth(), ins1.getHeight())
                            .expand(ins2.getWidth(), ins2.getHeight()));
        }
    }

    public void setLayoutManager(LayoutManager manager) {
        // Prevent external layout manager to be set.
    }

    @Override
    protected void paintFigure(Graphics graphics) {
        boolean preselected = isPreselected();
        boolean selected = isSelected();
        if (selected) {
            paintBackground(graphics, ColorSelected, 0xff);
        } else if (preselected) {
            paintBackground(graphics, ColorSelected, 0x20);
        }
        super.paintFigure(graphics);
    }

    private void paintBackground(Graphics graphics, Color color, int alpha) {
        Rectangle b = getBounds();
//        graphics.setAntialias(SWT.ON);
        graphics.setAlpha(alpha);
        graphics.setBackgroundColor(color);
        graphics.fillRectangle(b);
    }

    public Layer getContentCover() {
        return contentCover;
    }

    /**
     * @return the slide
     */
    public ShadowedLayer getContentPane() {
        return contentLayer;
    }

    protected IFigure getTitleContainer() {
        return titleContainer;
    }

    public ITextFigure getTitle() {
        return title;
    }

    public int getTitleRenderStyle() {
        return title.getRenderStyle();
    }

    public void setTitleRenderStyle(int renderStyle) {
        title.setRenderStyle(renderStyle);
    }

    /**
     * @return one of {@link PositionConstants#TOP},
     *         {@link PositionConstants#BOTTOM}, {@link PositionConstants#LEFT},
     *         {@link PositionConstants#RIGHT}
     */
    public int getTitlePlacement() {
        return titlePlacement;
    }

    /**
     * @param textPlacement
     *            one of {@link PositionConstants#TOP},
     *            {@link PositionConstants#BOTTOM},
     *            {@link PositionConstants#LEFT},
     *            {@link PositionConstants#RIGHT}
     */
    public void setTitlePlacement(int textPlacement) {
        if (textPlacement == getTitlePlacement())
            return;
        this.titlePlacement = textPlacement;
        updateTitlePlacement(textPlacement);
    }

    private void updateTitlePlacement(int textPlacement) {
        Object constraint = null;
        switch (textPlacement) {
        case PositionConstants.LEFT:
            constraint = FrameBorderLayout.LEFT;
            title.setTextAlignment(PositionConstants.RIGHT);
            break;
        case PositionConstants.RIGHT:
            constraint = FrameBorderLayout.RIGHT;
            title.setTextAlignment(PositionConstants.LEFT);
            break;
        case PositionConstants.TOP:
            constraint = FrameBorderLayout.TOP;
            title.setTextAlignment(PositionConstants.CENTER);
            break;
        case PositionConstants.BOTTOM:
            constraint = FrameBorderLayout.BOTTOM;
            title.setTextAlignment(PositionConstants.CENTER);
            break;
        }
        if (constraint != null && titleContainer.getParent() == this) {
            setConstraint(titleContainer, constraint);
        }
    }

    public boolean isSelected() {
        return getFlag(FLAG_SELECTED);
    }

    public void setSelected(boolean selected) {
        if (selected == isSelected())
            return;
        setFlag(FLAG_SELECTED, selected);
        repaint();
        title.setForegroundColor(
                selected ? ColorConstants.white : ColorConstants.black);
    }

    public void setPreselected(boolean preselected) {
        if (preselected == isPreselected())
            return;
        setFlag(FLAG_PRESELECTED, preselected);
        repaint();
    }

    public boolean isPreselected() {
        return getFlag(FLAG_PRESELECTED);
    }

    public boolean isPressed() {
        return contentLayer.isPressed();
    }

    public void setPressed(boolean pressed) {
        if (isFlat())
            return;
        contentLayer.setPressed(pressed);
    }

    public void press() {
        if (isFlat())
            return;
        contentLayer.press();
    }

    public void unpress() {
        if (isFlat())
            return;
        contentLayer.unpress();
    }

    public void togglePressed() {
        if (isFlat())
            return;
        contentLayer.togglePressed();
    }

    public boolean isHideTitle() {
        return getFlag(FLAG_HIDE_TITLE);
    }

    public boolean isFlat() {
        return getFlag(FLAG_FLAT);
    }

    public void setFlat(boolean flat) {
        if (flat == isFlat())
            return;
        setFlag(FLAG_FLAT, flat);
        if (flat) {
            contentLayer.setShadowDepths(0);
        } else {
            contentLayer.setShadowDepths(3);
        }
    }

    public void setHideTitle(boolean hideTitle) {
        boolean oldHideTitle = isHideTitle();
        if (hideTitle == oldHideTitle)
            return;
        setFlag(FLAG_HIDE_TITLE, hideTitle);
        if (hideTitle) {
            remove(titleContainer);
        } else {
            add(titleContainer);
            updateTitlePlacement(getTitlePlacement());
        }
    }

}
