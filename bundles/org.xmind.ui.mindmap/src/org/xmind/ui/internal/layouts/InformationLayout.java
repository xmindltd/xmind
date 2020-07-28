package org.xmind.ui.internal.layouts;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IRotatableFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.ui.internal.figures.InformationFigure;
import org.xmind.ui.internal.mindmap.InfoItemContentPart;
import org.xmind.ui.mindmap.IInfoItemPart;
import org.xmind.ui.mindmap.IInfoPart;

public class InformationLayout extends MindMapLayoutBase {

    private int spacing = 5;

    public InformationLayout(IInfoPart part) {
        super(part);
    }

    protected void fillLayoutData(IFigure container,
            ReferencedLayoutData data) {
        IInfoPart information = (IInfoPart) getPart();

        List<IInfoItemPart> icons = information.getInfoItemIcons();
        List<InfoItemContentPart> contents = information.getInfoItemContents();

        if (!icons.isEmpty() && !contents.isEmpty()) {
            for (int i = 0; i < Math.max(icons.size(), contents.size()); i++) {
                Point ref = data.getReference();

                IFigure itemFig = null;
                Dimension itemSize = new Dimension();
                if (i < icons.size()) {
                    IInfoItemPart item = icons.get(i);
                    itemFig = item.getFigure();
                    itemSize = getChildPreferredSize(itemFig);
                }

                IFigure contentFig = null;
                Dimension contentSize = new Dimension();
                if (i < contents.size()) {
                    InfoItemContentPart content = contents.get(i);
                    contentFig = content.getFigure();

                    if (container instanceof InformationFigure) {
                        Object constraint = ((InformationFigure) container)
                                .getConstraint();
                        if (constraint instanceof Integer) {
                            int prefWidth = (Integer) constraint
                                    - itemSize.width
                                    - container.getInsets().right;
                            if (prefWidth > 0) {
                                ((RotatableWrapLabel) contentFig)
                                        .setPrefWidth(prefWidth);
                            }
                        }
                    }

                    contentSize = getChildPreferredSize(contentFig);
                }

                if (itemFig != null) {
                    Rectangle area = data.getClientArea();
                    Rectangle r;
                    if (area == null) {
                        r = createBounds(ref, itemSize);
                    } else {
                        int x = area.width < itemSize.width
                                ? (area.x - (itemSize.width + area.width) / 2)
                                : area.x;
                        r = new Rectangle(x,
                                area.bottom() + spacing
                                        + (contentSize.height - itemSize.height)
                                                / 4,
                                itemSize.width, itemSize.height);
                    }
                    data.put(itemFig, r);
                }

                if (contentFig != null) {
                    Rectangle area = data.getClientArea();
                    Rectangle r;
                    if (area == null) {
                        r = createBounds(ref, contentSize);
                    } else {
                        r = new Rectangle(area.x + itemSize.width + spacing,
                                area.bottom()
                                        - (contentSize.height + itemSize.height)
                                                / 2,
                                contentSize.width, contentSize.height);
                    }
                    data.put(contentFig, r);
                }
            }
        }
    }

    private Rectangle createBounds(Point ref, Dimension size) {
        return new Rectangle(ref.x - size.width / 2, ref.y, size.width,
                size.height);
    }

    private Dimension getChildPreferredSize(IFigure child) {
        if (child instanceof IRotatableFigure) {
            return ((IRotatableFigure) child).getNormalPreferredSize(-1, -1)
                    .toDraw2DDimension();
        }
        return child.getPreferredSize();
    }

}
