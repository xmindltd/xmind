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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.AbstractAnchor;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.IDecoratedFigureListener;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecorationEx;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

public class DecoratedAnchor extends AbstractAnchor implements
        IDecoratedFigureListener {

    public DecoratedAnchor(IFigure owner) {
        super(owner);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.AbstractAnchor#hookOwner(org.eclipse.draw2d.IFigure)
     */
    @Override
    protected void hookOwner(IFigure owner) {
        super.hookOwner(owner);
        if (getOwner() instanceof IDecoratedFigure) {
            ((IDecoratedFigure) getOwner()).addDecoratedFigureListener(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.AbstractAnchor#unhookOwner(org.eclipse.draw2d.IFigure
     * )
     */
    @Override
    protected void unhookOwner(IFigure owner) {
        if (getOwner() instanceof IDecoratedFigure) {
            ((IDecoratedFigure) getOwner()).removeDecoratedFigureListener(this);
        }
        super.unhookOwner(owner);
    }

    public PrecisionPoint getLocation(int orientation, double expansion) {
        if (getOwner() instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) getOwner())
                    .getDecoration();
            if (decoration instanceof IShapeDecorationEx) {
                PrecisionPoint loc = ((IShapeDecorationEx) decoration)
                        .getAnchorLocation(getOwner(), orientation, expansion);
                if (loc != null)
                    return loc;
            }
        }
        return super.getLocation(orientation, expansion);
    }

    public PrecisionPoint getLocation(double x, double y, double expansion) {
        if (getOwner() instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) getOwner())
                    .getDecoration();
            if (decoration instanceof IShapeDecorationEx) {
                PrecisionPoint loc = ((IShapeDecorationEx) decoration)
                        .getAnchorLocation(getOwner(), x, y, expansion);
                if (loc != null)
                    return loc;
            }
        }
        if (getOwner() != null)
            return Geometry.getChopBoxLocation(x, y, getOwner().getBounds(),
                    expansion);
        return new PrecisionPoint(x, y);
    }

    public void decorationChanged(IDecoratedFigure figure,
            IDecoration oldDecoration, IDecoration newDecoration) {
        fireAnchorMoved();
    }

}