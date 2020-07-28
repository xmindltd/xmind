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
package org.xmind.core.util;

/**
 * A simple data structure representing an X-Y coordinate.
 * 
 * @author briansun
 * 
 */
public class Point {

    public int x;

    public int y;

    public Point() {
        this(0, 0);
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Point))
            return false;
        Point that = (Point) obj;
        return this.x == that.x && this.y == that.y;
    }

    public int hashCode() {
        return x ^ y;
    }

    public String toString() {
        return "(" + x + "," + y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}