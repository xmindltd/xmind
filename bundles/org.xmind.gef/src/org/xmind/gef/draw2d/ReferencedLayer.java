/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;

public class ReferencedLayer extends FreeformLayer implements IReferencedFigure {

    private IReferenceDescriptor referenceDescriptor = null;

    private Insets refDesc = null;

    private Point reference = null;

    private Point lastReference = null;

    public int getOrientation() {
        return Geometry.getOrientation(getReference(), getOrigin());
    }

    public Rectangle getPreferredBounds(Point reference) {
        return Geometry.getExpanded(reference, getReferenceDescription());
    }

    public Rectangle getPreferredBounds(Rectangle rect, Point reference) {
        Insets ins = getReferenceDescription();
        rect.setLocation(reference.x - ins.left, reference.y - ins.top);
        rect.setSize(ins.getWidth(), ins.getHeight());
        return rect;
    }

    public Rectangle getPreferredClientArea(Point reference) {
        if (getLayoutManager() instanceof IReferencedLayout) {
            Rectangle area = ((IReferencedLayout) getLayoutManager())
                    .getPreferredClientArea(this);
            return area.getTranslated(reference);
        }
        return new Rectangle(0, 0, 0, 0);
    }

    public Point getReference() {
        if (reference == null) {
            reference = calculateReference(getBounds());
            lastReference = reference;
        }
        return reference;
    }

    protected Point calculateReference(Rectangle bounds) {
        Insets ins = getReferenceDescription();
        int insWidth = ins.getWidth();
        int insHeight = ins.getHeight();
        int x = bounds.x;
        int y = bounds.y;
        if (bounds.width == insWidth) {
            x += ins.left;
        } else if (insWidth > 0) {
            x += bounds.width * ins.left / insWidth;
        }
        if (bounds.height == insHeight) {
            y += ins.top;
        } else if (insHeight > 0) {
            y += bounds.height * ins.top / insHeight;
        }
        return new Point(x, y);
    }

    public Insets getReferenceDescription() {
        if (refDesc != null)
            return refDesc;

        IReferenceDescriptor descriptor = getReferenceDescriptor();
        if (descriptor != null) {
            refDesc = descriptor.getReferenceDescription(this);
        }
        if (refDesc == null) {
            refDesc = calculateDefaultReferenceDescription(getPreferredSize());
        }
        return refDesc;
    }

    protected Insets calculateDefaultReferenceDescription(
            Dimension preferredSize) {
        int h = preferredSize.height / 2;
        int w = preferredSize.width / 2;
        return new Insets(h, w, preferredSize.height - h, preferredSize.width
                - w);
    }

    public IReferenceDescriptor getReferenceDescriptor() {
        return referenceDescriptor;
    }

    public void setReference(Point reference) {
        if (reference != null)
            setReference(reference.x, reference.y);
    }

    public void setReference(int referenceX, int referenceY) {
        setLocation(calculateLocation(referenceX, referenceY));
    }

    protected Point calculateLocation(int refX, int refY) {
        int width = bounds.width;
        int height = bounds.height;
        Insets ins = getReferenceDescription();
        int insWidth = ins.getWidth();
        int insHeight = ins.getHeight();
        if (width == insWidth) {
            refX -= ins.left;
        } else if (insWidth > 0) {
            refX -= width * ins.left / insWidth;
        }
        if (height == insHeight) {
            refY -= ins.top;
        } else if (insHeight > 0) {
            refY -= height * ins.top / insHeight;
        }
        return new Point(refX, refY);
    }

    public void setReferenceDescriptor(IReferenceDescriptor descriptor) {
        if (descriptor == this.referenceDescriptor)
            return;

        this.referenceDescriptor = descriptor;
        revalidate();
    }

    public Point getOrigin() {
        IOriginBased originBased = getOriginBasedAncestor(this);
        if (originBased != null)
            return originBased.getOrigin();
        return getReference();
    }

    private IOriginBased getOriginBasedAncestor(IFigure fig) {
        IFigure parent = fig.getParent();
        if (parent == null)
            return null;
        if (parent instanceof IOriginBased)
            return (IOriginBased) parent;
        return getOriginBasedAncestor(parent);
    }

    public void invalidate() {
        refDesc = null;
        reference = null;
        super.invalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.draw2d.IReferencedFigure#getLastReference()
     */
    public Point getLastReference() {
        if (lastReference == null) {
            lastReference = getReference();
        }
        return lastReference;
    }

}