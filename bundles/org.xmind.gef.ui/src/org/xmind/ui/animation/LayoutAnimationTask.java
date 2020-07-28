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
package org.xmind.ui.animation;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

public class LayoutAnimationTask extends AbstractAnimationTask {

    private Rectangle currentBounds = new Rectangle();

    public LayoutAnimationTask(IFigure source, Rectangle start, Rectangle end) {
        super(source, start, end);
    }

    /**
     * @see cn.brainy.ui.animation.IAnimationTask#getCurrentValue(int, int)
     */
    public Rectangle getCurrentValue(int current, int total) {
        Rectangle startBounds = (Rectangle) getStartValue();
        Rectangle endBounds = (Rectangle) getEndValue();
        currentBounds.x = startBounds.x + (endBounds.x - startBounds.x)
                * current / total;
        currentBounds.y = startBounds.y + (endBounds.y - startBounds.y)
                * current / total;
        currentBounds.width = startBounds.width
                + (endBounds.width - startBounds.width) * current / total;
        currentBounds.height = startBounds.height
                + (endBounds.height - startBounds.height) * current / total;
        return currentBounds;
    }

    /**
     * @see cn.brainy.ui.animation.IAnimationTask#performSetAction(java.lang.Object)
     */
    public void setValue(Object value) {
        IFigure srcFig = (IFigure) getSource();
        Rectangle newBounds = (Rectangle) value;
        srcFig.setBounds(newBounds);
    }
}