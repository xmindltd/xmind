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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.service.IPlaybackProvider;

public class LayoutPlaybackProvider implements IPlaybackProvider {

    public boolean doPlayback(IFigure figure, IGraphicalPart part,
            Object initState, Object finalState, float progress) {
        Map initial = (Map) initState;
        Map ending = (Map) finalState;
        List children = getAnimatableChildren(figure, part);

        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            Object childInit = initial.get(child);
            Object childEnd = ending.get(child);
            if (childInit != null && childEnd != null) {
                Object childState = calcCurrentState(child, childInit,
                        childEnd, progress, figure, part);
                if (childState != null)
                    doPlayback(child, childState, progress, figure, part);
            }
        }
        return true;
    }

    protected void doPlayback(Object child, Object childState, float progress,
            IFigure figure, IGraphicalPart part) {
        if (child instanceof IFigure && childState instanceof Rectangle) {
            ((IFigure) child).setBounds((Rectangle) childState);
        }
    }

    protected Object calcCurrentState(Object child, Object childInit,
            Object childEnd, float progress, IFigure figure, IGraphicalPart part) {
        if (childInit instanceof Rectangle && childEnd instanceof Rectangle) {
            float ssergorp = 1 - progress;
            Rectangle rect1 = (Rectangle) childInit;
            Rectangle rect2 = (Rectangle) childEnd;
            int x = Math.round(progress * rect2.x + ssergorp * rect1.x);
            int y = Math.round(progress * rect2.y + ssergorp * rect1.y);
            int width = Math.round(progress * rect2.width + ssergorp
                    * rect1.width);
            int height = Math.round(progress * rect2.height + ssergorp
                    * rect1.height);
            return new Rectangle(x, y, width, height);
        }
        return null;
    }

    protected List getAnimatableChildren(IFigure figure, IGraphicalPart part) {
        return figure.getChildren();
    }

    public Object getState(IFigure figure, IGraphicalPart part) {
        Map<Object, Object> childrenStates = new HashMap<Object, Object>();
        List children = getAnimatableChildren(figure, part);
        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            Object state = getChildState(child, figure, part);
            childrenStates.put(child, state);
        }
        return childrenStates;
    }

    protected Object getChildState(Object child, IFigure figure,
            IGraphicalPart part) {
        if (child instanceof IFigure) {
            IFigure childFigure = (IFigure) child;
            return childFigure.getBounds().getCopy();
        }
        return null;
    }

}