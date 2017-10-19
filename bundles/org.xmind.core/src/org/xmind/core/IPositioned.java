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
package org.xmind.core;

import org.xmind.core.util.Point;

public interface IPositioned {

    /**
     * @param x
     * @param y
     */
    void setPosition(int x, int y);

    /**
     * @return The position of this object; or <code>null</code> to indicate no
     *         position has been set.
     */
    Point getPosition();

    /**
     * @param position
     */
    void setPosition(Point position);

    /**
     * @return
     */
    boolean hasPosition();

}