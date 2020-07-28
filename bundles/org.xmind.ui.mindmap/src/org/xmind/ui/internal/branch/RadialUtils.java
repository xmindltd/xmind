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
package org.xmind.ui.internal.branch;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.ui.mindmap.IBranchPart;

public class RadialUtils {

    static Insets getRefInsets(IFigure f, boolean rightOrLeft) {
        return getRefInsets(f.getPreferredSize(), rightOrLeft);
    }

    static Insets getRefInsets(Dimension size, boolean rightOrLeft) {
        Insets ins = new Insets(size.height / 2, rightOrLeft ? 0 : size.width,
                size.height - size.height / 2, rightOrLeft ? size.width : 0);
        return ins;
    }

    static boolean isLeft(int parentX, int childX) {
        return childX < parentX;
    }

    public static Rectangle getPrefBounds(IBranchPart branch, Point reference,
            boolean firstOrSecond) {
        Insets ins = getRefInsets(branch.getFigure(), firstOrSecond);
        return Geometry.getExpanded(reference, ins);
    }

    public static Rectangle getPrefBounds(Dimension size, Point reference,
            boolean firstOrSecond) {
        Insets ins = getRefInsets(size, firstOrSecond);
        return Geometry.getExpanded(reference, ins);
    }

}