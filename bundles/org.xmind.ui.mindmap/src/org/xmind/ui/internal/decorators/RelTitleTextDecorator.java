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
package org.xmind.ui.internal.decorators;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.xmind.core.ITitled;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.IUseTransparency;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class RelTitleTextDecorator extends TitleTextDecorator {

    private static final RelTitleTextDecorator instance = new RelTitleTextDecorator();

//    private static class RoundedRectBorder extends AbstractBackground {
//
//        public void paintBackground(IFigure figure, Graphics graphics,
//                Insets insets) {
//            Rectangle r = getPaintRectangle(figure, insets);
//            Path p = new Path(Display.getCurrent());
//            p.addRoundedRectangle(r, 5);
//            if (figure instanceof IUseTransparency) {
//                graphics.setAlpha(((IUseTransparency) figure).getSubAlpha());
//            }
//            graphics.setBackgroundColor(figure.getBackgroundColor());
//            graphics.fillPath(p);
//            p.dispose();
//        }
//
//    }

    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        //figure.setBorder(new RoundedRectBorder());
    }

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
//        Rectangle b = getTitleBounds(part, figure);
//        if (b != null) {
//            figure.setBounds(b);
//        }
    }

//    private Rectangle getTitleBounds(IGraphicalPart part, IFigure figure) {
//        if (part.getParent() instanceof IGraphicalPart) {
//            IGraphicalPart parent = (IGraphicalPart) part.getParent();
//            IFigure container = parent.getFigure();
//            if (container instanceof DecoratedConnectionFigure) {
//                IConnectionDecorationEx dec = ((DecoratedConnectionFigure) container)
//                        .getDecoration();
//                if (dec instanceof IRelationshipDecoration) {
//                    Point p = ((IRelationshipDecoration) dec).getTitlePosition(
//                            container).toDraw2DPoint();
//                    Rectangle r = new Rectangle();
//                    if (figure instanceof IReferencedFigure) {
//                        ((IReferencedFigure) figure).getPreferredBounds(r, p);
//                    } else {
//                        Dimension size = figure.getPreferredSize();
//                        r.setSize(size);
//                        r.setLocation(p.x - size.width / 2, p.y - size.height
//                                / 2);
//                    }
//                    return r;
//                }
//            }
//            Dimension titleSize = figure.getPreferredSize();
//            Rectangle relBounds = container.getBounds();
//            return new Rectangle(relBounds.x, relBounds.y, titleSize.width,
//                    titleSize.height);
//        }
//        return null;
//    }

    protected boolean hasTitle(ITitled titled) {
        return super.hasTitle(titled) && !"".equals(titled.getTitleText()); //$NON-NLS-1$
    }

    protected String getUntitledText(IGraphicalPart part, ITitled titled) {
        return MindMapMessages.TitleText_Relationship;
    }

    protected void decorateTextFigure(IGraphicalPart ownerPart,
            IStyleSelector ss, ITextFigure figure) {
        super.decorateTextFigure(ownerPart, ss, figure);
        figure.setBackgroundColor(getFillColor(ownerPart, ss));
        if (figure instanceof IUseTransparency) {
            ((IUseTransparency) figure).setSubAlpha(getAlpha(ownerPart, ss));
        }
    }

    protected Color getFillColor(IGraphicalPart part, IStyleSelector ss) {
        IViewer viewer = part.getSite().getViewer();
        if (viewer != null) {
            ISheetPart sheet = (ISheetPart) viewer.getAdapter(ISheetPart.class);
            if (sheet != null) {
                IStyleSelector sss = StyleUtils.getStyleSelector(sheet);
                return StyleUtils.getColor(sheet, sss, Styles.FillColor, null,
                        Styles.DEF_REL_TITLE_FILL_COLOR);
            }
        }
        return ColorConstants.white;
    }

    protected int getAlpha(IGraphicalPart part, IStyleSelector ss) {
        return 0xf0;
    }

    protected IGraphicalPart getOwnerPart(IGraphicalPart part) {
        if (part.getParent() instanceof IRelationshipPart)
            return (IRelationshipPart) part.getParent();
        return super.getOwnerPart(part);
    }

    public static RelTitleTextDecorator getInstance() {
        return instance;
    }
}