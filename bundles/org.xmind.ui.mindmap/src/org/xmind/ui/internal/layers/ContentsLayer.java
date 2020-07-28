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
package org.xmind.ui.internal.layers;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.FreeformListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IOriginBased;
import org.xmind.gef.draw2d.IReferencedFigure;

public class ContentsLayer extends BaseLayer implements IOriginBased {

    private class ContentsHook implements FreeformListener {
        public void notifyFreeformExtentChanged() {
            repaint();
        }
    }

    private IFigure contents = null;

    private ContentsHook contentsHook = null;

    private Point origin = null;

    private IFigure topLeft = null;

    private IFigure bottomRight = null;

    private int margin = 0;

    private boolean centered;

    private boolean constrained;

    private boolean export;

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(boolean centered) {
        if (centered == this.centered)
            return;
        this.centered = centered;
        revalidate();
    }

    public boolean isConstrained() {
        return constrained;
    }

    public void setConstrained(boolean constrained) {
        if (constrained == this.constrained)
            return;

        this.constrained = constrained;
        revalidate();
    }

    /**
     * Have no effect when corners are already added.
     */
    public void addCorners() {
        if (topLeft == null || topLeft.getParent() != this) {
            topLeft = new Figure();
            topLeft.setSize(1, 1);
            add(topLeft);
        }
        if (bottomRight == null || bottomRight.getParent() != this) {
            bottomRight = new Figure();
            bottomRight.setSize(1, 1);
            add(bottomRight);
        }
    }

    /**
     * Have no effect when corners are already removed.
     */
    public void removeCorners() {
        if (topLeft != null) {
            remove(topLeft);
            topLeft = null;
        }
        if (bottomRight != null) {
            remove(bottomRight);
            bottomRight = null;
        }
    }

    public IFigure getContents() {
        return contents;
    }

    public void setContents(IFigure fig) {
        if (fig == contents)
            return;

        if (contents != null) {
            unhookContents(contents);
            remove(contents);
        }

        contents = fig;

        if (contents != null) {
            add(contents, 0);
            hookContents(contents);
        }
    }

    protected void hookContents(IFigure contents) {
        if (contents instanceof FreeformFigure) {
            if (contentsHook == null) {
                contentsHook = new ContentsHook();
            }
            ((FreeformFigure) contents).addFreeformListener(contentsHook);
        }
    }

    protected void unhookContents(IFigure contents) {
        if (contents instanceof FreeformFigure && contentsHook != null) {
            ((FreeformFigure) contents).removeFreeformListener(contentsHook);
        }
    }

    public Point getOrigin() {
        if (origin == null) {
            origin = calculateOrigin();
        }
        return origin;
    }

    protected Point calculateOrigin() {
        if (centered) {
            if (topLeft == null || bottomRight == null) {
                if (contents != null) {
                    Rectangle area = getViewportClientArea(this);
                    if (area != null) {
                        area = area.getCopy().scale(1 / getScale(this, 1));
                        Point p = new Point(area.x + (area.width) / 2,
                                area.y + (area.height) / 2);
                        return p;
                    }
                }
            }
        } else if (constrained) {
            if (contents instanceof IReferencedFigure) {
                Insets ins = ((IReferencedFigure) contents)
                        .getReferenceDescription();
                Rectangle area = getViewportClientArea(this);
                if (area == null) {
                    area = getBounds();
                } else {
                    area = area.getCopy().scale(1 / getScale(this, 1));
                }
                return new Point(
                        area.x + (area.width - ins.left - ins.right) / 2
                                + ins.left,
                        area.y + (area.height - ins.top - ins.bottom) / 2
                                + ins.top);
            }
            return contents.getBounds().getLocation();
        }
        return new Point();
    }

    private double getScale(IFigure figure, double scale) {
        IFigure parent = figure.getParent();
        if (parent instanceof ScalableFigure) {
            scale *= ((ScalableFigure) parent).getScale();
        }
        if (parent != null)
            scale = getScale(parent, scale);
        return scale;
    }

    private Rectangle getViewportClientArea(IFigure figure) {
        IFigure parent = figure.getParent();
        if (parent instanceof Viewport)
            return parent.getClientArea();
        if (parent != null)
            return getViewportClientArea(parent);
        return null;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        if (margin == this.margin)
            return;

        this.margin = margin;
        revalidate();
    }

    protected void layout() {
        boolean hasCorners = topLeft != null && bottomRight != null;
        if (contents != null) {
            layoutContents(hasCorners);
        }
        if (hasCorners) {
            layoutCorner();
        }
    }

    protected void layoutContents(boolean hasCorners) {
        if (constrained) {
            Dimension size = contents.getPreferredSize();
            Rectangle area = getViewportClientArea(this);
            if (area == null) {
                area = getBounds();
            } else {
                area = area.getCopy().scale(1 / getScale(this, 1));
            }
            Rectangle contentBounds = new Rectangle(
                    area.x + (area.width - size.width) / 2,
                    area.y + (area.height - size.height) / 2, size.width,
                    size.height);
            if (contents instanceof FreeformFigure) {
                ((FreeformFigure) contents).setFreeformBounds(contentBounds);
            } else {
                contents.setBounds(contentBounds);
            }
        } else {
            Point o = getOrigin();
            Insets ins;
            if (contents instanceof IReferencedFigure) {
                ins = ((IReferencedFigure) contents).getReferenceDescription();
            } else {
                Dimension size = contents.getPreferredSize();
                ins = new Insets(size.height - size.height / 2,
                        size.width - size.width / 2, size.height / 2,
                        size.width / 2);
            }
            Rectangle r = new Rectangle(o.x - ins.left, o.y - ins.top,
                    ins.getWidth(), ins.getHeight());
            contents.setBounds(r);
        }
    }

    protected void layoutCorner() {
        Point origin = getOrigin();
        if (contents != null) {
            Rectangle area = getViewportClientArea(this);
            if (area != null) {
                area = area.getCopy().scale(1 / getScale(this, 1));
                Insets ins = getContentsReferenceDescription(true);
                int left = origin.x - Math.max(ins.left, area.width);
                int top = origin.y - Math.max(ins.top, area.height);
                int right = origin.x + Math.max(ins.right, area.width);
                int bottom = origin.y + Math.max(ins.bottom, area.height);
                topLeft.setLocation(new Point(left, top));
                bottomRight.setLocation(new Point(right - 1, bottom - 1));
                return;
            }
        }

        topLeft.setLocation(origin);
        bottomRight.setLocation(origin);
    }

    protected Insets getContentsReferenceDescription(boolean hasCorners) {
        Insets ins = new Insets(getMargin());
        if (contents instanceof IReferencedFigure) {
            ins.add(((IReferencedFigure) contents).getReferenceDescription());
        } else {
            Dimension cSize = contents.getPreferredSize();
            ins.right += cSize.width / 2;
            ins.bottom += cSize.height / 2;
            ins.left += cSize.width - cSize.width / 2;
            ins.top += cSize.height - cSize.height / 2;
        }
        return ins;
    }

    public void invalidate() {
        super.invalidate();
        origin = null;
    }

    public void setExport(boolean export) {
        this.export = export;
    }

    @Override
    public Insets getInsets() {
        if (export && contents != null) {
            return getContentsReferenceDescription(false);
        }
        return super.getInsets();
    }

}
