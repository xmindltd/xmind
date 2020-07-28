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
package org.xmind.core;

public interface IControlPoint extends IAdaptable, ISheetComponent, IPositioned {

    /**
     * 
     * @return
     */
    IRelationship getParent();

    /**
     * 
     * @return
     */
    int getIndex();

    /**
     * Checks whether or not the polar position is ready to use.
     * <p>
     * A <i>Polar Position</i> is defined as a pair of coordinates containing an
     * <i>angle</i> and an <i>amount</i>.
     * </p>
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     * @return True if the polar position is ready to use
     */
    boolean usesPolarPosition();

    /**
     * 
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     * @return
     */
    boolean hasPolarAngle();

    /**
     * 
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     * @return
     */
    boolean hasPolarAmount();

    /**
     * 
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     */
    double getPolarAngle();

    /**
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     * @return
     */
    double getPolarAmount();

    /**
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     * @param angle
     */
    void setPolarAngle(double angle);

    /**
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     * @param amount
     */
    void setPolarAmount(double amount);

    /**
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     */
    void resetPolarAngle();

    /**
     * <p>
     * <b>WARNING</b>: Experimental method.
     * </p>
     * 
     */
    void resetPolarAmount();

}