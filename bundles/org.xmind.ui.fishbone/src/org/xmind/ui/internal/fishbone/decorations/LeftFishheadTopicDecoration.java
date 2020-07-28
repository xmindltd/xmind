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
package org.xmind.ui.internal.fishbone.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.graphics.Path;

public class LeftFishheadTopicDecoration extends FishheadTopicDecoration {

    public LeftFishheadTopicDecoration() {
        super(false);
    }

    public Path createClippingPath(IFigure figure) {
        Path shape = new Path(Display.getCurrent());
        Rectangle box = getOutlineBox(figure);

        Insets ins = figure.getInsets();
        Rectangle clientArea = box.getShrinked(ins);

        float x = box.x
                + clientArea.width * FishheadTopicDecoration.headConScale;
        float y = box.y + box.height * 0.5f;

        shape.moveTo(box.right(), box.y);

        shape.quadTo(x, box.y, box.x, y);

        shape.quadTo(x, box.bottom(), box.right(), box.bottom());

        shape.close();

        return shape;
    }

}