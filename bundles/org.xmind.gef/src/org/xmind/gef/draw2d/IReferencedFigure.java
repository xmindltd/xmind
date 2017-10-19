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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Figure based on a reference point.
 * <p>
 * The location of a reference point is determined by the distances from that
 * reference point to the four sides of the figure's bounds, which is called the
 * "reference description", provided by an <code>IReferenceDescriptor</code>. It
 * means that, given a specified <code>IReferenceDescriptor</code> and a
 * reference point, the figure's preferred bounds can be calculated and those
 * bounds can be used by layout managers to determine the figure's size and
 * location.
 * </p>
 * 
 * @author Frank Shaka
 */
public interface IReferencedFigure extends IFigure, IOriginBased {

    /**
     * Returns the reference point of this figure. Any resizing or translating
     * of this figure will cause the reference be re-calculated.
     * 
     * @return the reference point of this figure.
     */
    Point getReference();

    /**
     * Sets the location of this figure to fit the given reference point.
     * 
     * @param reference
     */
    void setReference(Point reference);

    /**
     * Sets the location of this figure to fit the given reference point.
     * 
     * @param referenceX
     * @param referenceY
     */
    void setReference(int referenceX, int referenceY);

    /**
     * Returns the reference description of this figure.
     * 
     * @return the reference description of this figure.
     */
    Insets getReferenceDescription();

    /**
     * Returns the preferred client area of this figure based on the given
     * reference point.
     * 
     * @param reference
     * @return
     */
    Rectangle getPreferredClientArea(Point reference);

    /**
     * Returns the preferred bounds of this figure based on the given reference
     * point.
     * 
     * @param reference
     * @return
     */
    Rectangle getPreferredBounds(Point reference);

    /**
     * Calculates the preferred bounds of this figure based on the given
     * reference point, copies the values into the given rectangle and returns
     * that rectangle.
     * 
     * @param rect
     * @param reference
     * @return
     */
    Rectangle getPreferredBounds(Rectangle rect, Point reference);

    /**
     * Returns the reference descriptor used to describe the reference point of
     * this figure.
     * 
     * @return
     */
    IReferenceDescriptor getReferenceDescriptor();

    /**
     * Sets the reference descriptor to describe the reference point of this
     * figure.
     * 
     * @param descriptor
     */
    void setReferenceDescriptor(IReferenceDescriptor descriptor);

    /**
     * Returns the orientation of this figure based on the origin location
     * obtained from <code>getOrigin()</code>.
     * 
     * @return the orientation of this figure
     * @see org.xmind.util.geometry.Geometry#getOrientation(Point, Point)
     */
    int getOrientation();

    /**
     * Returns the last calculated reference point. This value maintains the
     * calculated reference point through the time when the cached reference
     * point is cleared due to some resizing or translating and a new reference
     * point is not calculated yet. Reference descriptors may take advantage of
     * this value to calculate new reference description, for calling
     * {@link #getReference()} when calculating new reference point may cause
     * endless recursion or get a newly calculated reference point that's not
     * corresponding to the actual state of this figure.
     * 
     * @return the last calculated reference point
     */
    Point getLastReference();

}