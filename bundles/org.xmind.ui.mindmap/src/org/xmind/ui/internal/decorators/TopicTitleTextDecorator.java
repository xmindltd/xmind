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
package org.xmind.ui.internal.decorators;

import org.eclipse.draw2d.IFigure;
import org.xmind.core.ITopic;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.IWrapFigure;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public class TopicTitleTextDecorator extends TitleTextDecorator {

    private static final TopicTitleTextDecorator instance = new TopicTitleTextDecorator();

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setOpaque(false);
    }

    protected IGraphicalPart getOwnerPart(IGraphicalPart part) {
        if (part.getParent() instanceof ITopicPart)
            return ((ITopicPart) part.getParent()).getOwnerBranch();
        return super.getOwnerPart(part);
    }

    protected void decorateTextFigure(IGraphicalPart part, ITextFigure figure) {
        super.decorateTextFigure(part, figure);
        if (figure instanceof IWrapFigure) {
            Object model = MindMapUtils.getRealModel(part);
            if (model instanceof ITopic) {
                ITopic t = (ITopic) model;
                ((IWrapFigure) figure).setPrefWidth(t.getTitleWidth());
            }
        }
    }

    public static TopicTitleTextDecorator getInstance() {
        return instance;
    }
}