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
package org.xmind.ui.internal.layouts;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.core.IImage;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.draw2d.IRotatableFigure;
import org.xmind.gef.draw2d.IRotatableReferenceDescriptor;
import org.xmind.gef.draw2d.IRotatableReferencedLayout;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.ui.mindmap.IIconTipPart;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.INumberingPart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.ITopicPart;

public class TopicLayout extends MindMapLayoutBase
        implements IRotatableReferenceDescriptor, IRotatableReferencedLayout {

    private int spacing = 2;

    private PrecisionInsets normalRefDesc = null;

    private PrecisionDimension normalSize = null;

    private PrecisionRectangle normalClientArea = null;

    private Insets rotatedRefDesc = null;

    private Dimension rotatedSize = null;

    private Rectangle rotatedClientArea = null;

    private PrecisionRotator rotator = null;

    public TopicLayout(ITopicPart topic) {
        super(topic);
    }

    protected ITopicPart getTopic() {
        return (ITopicPart) super.getPart();
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    protected void fillLayoutData(IFigure container,
            ReferencedLayoutData data) {
        boolean minimized = isMinimized(container);
        ITopicPart topic = getTopic();

        ITitleTextPart title = topic.getTitle();
        IImagePart image = topic.getImagePart();
        if ((topic.getTopic().hasTitle() && !"".equals(topic.getTopic() //$NON-NLS-1$
                .getTitleText())) || image == null) {
            if (title != null) {
                if (minimized) {
                    data.put(title.getFigure(), createMinArea(data));
                } else {
                    fillText(title, data);
                }
            }
        }

        List<IMarkerPart> markers = topic.getMarkers();
        if (!markers.isEmpty()) {
            for (int i = markers.size() - 1; i >= 0; i--) {
                IMarkerPart marker = markers.get(i);
                fillMarker(marker, data);
            }
        }

        INumberingPart numbering = topic.getNumberingPart();
        if (numbering != null) {
            if (minimized) {
                data.put(numbering.getFigure(), createMinArea(data));
            } else {
                fillNumbering(numbering, data);
            }
        }

        List<IIconTipPart> iconTips = topic.getIconTips();
        for (IIconTipPart iconTip : iconTips) {
            if (minimized) {
                data.put(iconTip.getFigure(), createMinArea(data));
            } else {
                fillIconTip(iconTip, data);
            }
        }

        if (image != null) {
            if (minimized) {
                data.put(image.getFigure(), createMinArea(data));
            } else {
                fillImage(image, data);
            }
        }

        if (title != null && data.get(title.getFigure()) == null) {
            data.put(title.getFigure(), createMinArea(data));
        }
    }

    private void fillImage(IImagePart image, ReferencedLayoutData data) {
        IImage imageModel = image.getImageModel();
        String alignment = imageModel.getAlignment();
        IFigure imageFigure = image.getFigure();
        PrecisionDimension size = r()
                .rd(new PrecisionDimension(imageFigure.getPreferredSize()));
        PrecisionDimension size2 = r()
                .td(new PrecisionDimension(imageFigure.getPreferredSize()));
        Point ref = data.getReference();
        Rectangle r;
        Rectangle area = data.getClientArea();
        if (area == null) {
            r = createBounds(ref, size.toDraw2DDimension());
        } else {
            if (IImage.LEFT.equals(alignment)) {
                data.translate((int) ((size2.width + spacing) / 2), 0);
                area = data.getClientArea();
                r = new PrecisionRectangle(
                        area.x - size2.width / 2 - size.width / 2 - spacing,
                        ref.y - size.height / 2, size.width, size.height)
                                .toDraw2DRectangle();
                data.add(new PrecisionRectangle(area.x - size2.width - spacing,
                        ref.y - size2.height / 2, size2.width, size2.height)
                                .toDraw2DRectangle());
            } else if (IImage.RIGHT.equals(alignment)) {
                data.translate((int) (-(size2.width + spacing) / 2), 0);
                area = data.getClientArea();
                r = new PrecisionRectangle(
                        area.x + area.width + spacing + size2.width / 2
                                - size.width / 2,
                        ref.y - size.height / 2, size.width, size.height)
                                .toDraw2DRectangle();
                data.add(new PrecisionRectangle(area.x + area.width + spacing,
                        ref.y - size2.height / 2, size2.width, size2.height)
                                .toDraw2DRectangle());
            } else if (IImage.BOTTOM.equals(alignment)) {
                data.translate(0, -(int) ((size2.height + spacing) / 2));
                area = data.getClientArea();
                r = new PrecisionRectangle(ref.x - size.width / 2,
                        area.y + area.height + size2.height / 2
                                - size.height / 2 + spacing,
                        size.width, size.height).toDraw2DRectangle();
                data.add(new PrecisionRectangle(ref.x - size2.width / 2,
                        area.y + area.height + spacing, size2.width,
                        size2.height).toDraw2DRectangle());
            } else /* if (IImage.TOP.equals(alignment) */ {
                data.translate(0, (int) ((size2.height + spacing) / 2));
                area = data.getClientArea();
                r = new PrecisionRectangle(ref.x - size.width / 2,
                        area.y - size2.height / 2 - size.height / 2 - spacing,
                        size.width, size.height).toDraw2DRectangle();
                data.add(new PrecisionRectangle(ref.x - size2.width / 2,
                        area.y - size2.height - spacing, size2.width,
                        size2.height).toDraw2DRectangle());
            }
        }
        data.put(imageFigure, r);
    }

    private void fillMarker(IMarkerPart marker, ReferencedLayoutData data) {
        Point ref = data.getReference();
        IFigure fig = marker.getFigure();
        Dimension size = getChildPreferredSize(fig);
        int rotatedOffset = 0;
        if (getRotationDegrees() != 0) {
            double cos = Math
                    .abs(Math.cos(Math.toRadians(getRotationDegrees())));
            rotatedOffset = (int) (size.width * cos);
            data.translate(rotatedOffset / 2, 0);
        }
        Rectangle area = data.getClientArea();
        Rectangle r;
        if (area == null) {
            r = createBounds(ref, size);
        } else {
            int dx = (size.width + spacing) / 2;
            data.translate(dx, 0);
            area = data.getClientArea();
            r = new Rectangle(area.x - size.width - rotatedOffset - spacing,
                    ref.y - size.height / 2, size.width, size.height);
        }
        data.put(fig, r);
    }

    private void fillIconTip(IIconTipPart iconTip, ReferencedLayoutData data) {
        Point ref = data.getReference();
        IFigure fig = iconTip.getFigure();
        Dimension size = getChildPreferredSize(fig);
        Rectangle area = data.getClientArea();
        Rectangle r;
        if (area == null) {
            r = createBounds(ref, size);
        } else {
            int dx = (size.width + spacing) / 2;
            data.translate(-dx, 0);
            area = data.getClientArea();
            r = new Rectangle(area.right() + spacing, ref.y - size.height / 2,
                    size.width, size.height);
        }
        data.put(fig, r);
    }

    private void fillNumbering(INumberingPart numbering,
            ReferencedLayoutData data) {
        Point ref = data.getReference();
        IFigure fig = numbering.getFigure();
        Dimension size = getChildPreferredSize(fig);
        Rectangle area = data.getClientArea();
        Rectangle r;
        if (area == null) {
            r = createBounds(ref, size);
        } else {
            int dx = (size.width + spacing) / 2;
            data.translate(dx, 0);
            area = data.getClientArea();
            r = new Rectangle(area.x - size.width - spacing,
                    ref.y - size.height / 2, size.width, size.height);
        }
        data.put(fig, r);
    }

    protected void fillText(ITitleTextPart text, ReferencedLayoutData data) {
        Point ref = data.getReference();
        IFigure textFigure = text.getFigure();
        Dimension size = getChildPreferredSize(textFigure);
        Rectangle r = createBounds(ref, size);
        data.put(textFigure, r);
    }

    private Rectangle createMinArea(ReferencedLayoutData data) {
        return data.createInitBounds();
//        Point ref = data.getReference();
//        return new Rectangle(ref.x, ref.y, 0, 0);
    }

    private boolean isMinimized(IFigure container) {
        if (container instanceof IMinimizable)
            return ((IMinimizable) container).isMinimized();
        return false;
    }

    private static Rectangle createBounds(Point ref, Dimension size) {
        return new Rectangle(ref.x - size.width / 2, ref.y - size.height / 2,
                size.width, size.height);
    }

    protected Dimension getChildPreferredSize(IFigure child) {
        if (child instanceof IRotatableFigure) {
            return ((IRotatableFigure) child).getNormalPreferredSize(-1, -1)
                    .toDraw2DDimension();
        }
        return child.getPreferredSize();
    }

    protected void layoutChild(IFigure child, Rectangle bounds, Point offset) {
        if (getRotationDegrees() != 0) {
            r().setOrigin(offset.x, offset.y);
            bounds = r().t(new PrecisionRectangle(bounds)).toDraw2DRectangle();
        }
        super.layoutChild(child, bounds, offset);
    }

    public void invalidate() {
        super.invalidate();
        normalClientArea = null;
        normalRefDesc = null;
        normalSize = null;
        rotatedClientArea = null;
        rotatedRefDesc = null;
        rotatedSize = null;
    }

    public Insets getReferenceDescription(IFigure figure) {
        if (rotatedRefDesc == null) {
            PrecisionInsets ins = getNormalReferenceDescription(figure);
            if (getRotationDegrees() != 0) {
                ins = r().ti(ins);
            }
            rotatedRefDesc = ins.toDraw2DInsets();
        }
        return rotatedRefDesc;
    }

    public Rectangle getPreferredClientArea(IFigure container) {
        if (rotatedClientArea == null) {
            PrecisionRectangle rect = getNormalPreferredClientArea(container);
            if (getRotationDegrees() != 0) {
                Point ref = getLayoutData(container).getReference();
                r().setOrigin(ref.x, ref.y);
                rect = r().tr(rect);
            }
            rotatedClientArea = rect.toDraw2DRectangle();
        }
        return rotatedClientArea;
    }

    public Dimension getPreferredSize(IFigure container, int wHint, int hHint) {
        if (rotatedSize == null) {
            PrecisionDimension d = getNormalPreferredSize(container, wHint,
                    hHint);
            if (getRotationDegrees() != 0) {
                d = r().td(d);
            }
            rotatedSize = d.toDraw2DDimension();
        }
        return rotatedSize;
    }

    public PrecisionInsets getNormalReferenceDescription(IFigure figure) {
        if (normalRefDesc == null) {
            Point reference = getLayoutData(figure).getReference();
            PrecisionInsets description = getNormalPreferredClientArea(figure)
                    .getInsets(reference.x, reference.y);
            normalRefDesc = description.add(figure.getInsets());
        }
        return normalRefDesc;
    }

    public PrecisionRectangle getNormalPreferredClientArea(IFigure container) {
        if (normalClientArea == null) {
            normalClientArea = new PrecisionRectangle(
                    super.getPreferredClientArea(container));
        }
        return normalClientArea;
    }

    public PrecisionDimension getNormalPreferredSize(IFigure container,
            int wHint, int hHint) {
        if (normalSize == null) {
            Insets ins = container.getInsets();
            normalSize = getNormalPreferredClientArea(container).getSize()
                    .expand(ins.getWidth(), ins.getHeight());
        }
        return normalSize;
    }

    public double getRotationDegrees() {
        return r().getAngle();
    }

    public void setRotationDegrees(double angle) {
        r().setAngle(angle);
        invalidate();
    }

    public PrecisionRotator r() {
        if (rotator == null)
            rotator = new PrecisionRotator();
        return rotator;
    }

}
