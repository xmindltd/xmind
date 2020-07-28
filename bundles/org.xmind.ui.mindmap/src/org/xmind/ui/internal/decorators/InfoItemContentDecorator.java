package org.xmind.ui.internal.decorators;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.mindmap.InfoItemContentPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.style.Styles;

public class InfoItemContentDecorator extends Decorator {

    private static final InfoItemContentDecorator instance = new InfoItemContentDecorator();

    @Override
    public void activate(IGraphicalPart part, IFigure figure) {
        super.activate(part, figure);
        figure.setForegroundColor(
                ColorUtils.getColor(Styles.YELLOWBOX_TEXT_COLOR));
    }

    @Override
    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        ITopicPart topicPart = null;
        if (part instanceof InfoItemContentPart) {
            topicPart = ((InfoItemContentPart) part).getTopicPart();
            if (topicPart != null) {
                figure.setFont(
                        FontUtils.getNewHeight(JFaceResources.getDefaultFont(),
                                Util.isMac() ? 10 : 8));
            } else {
                figure.setFont(JFaceResources.getDefaultFont());
            }
        }
        if (figure instanceof RotatableWrapLabel) {
            RotatableWrapLabel itemFigure = (RotatableWrapLabel) figure;
            if (part instanceof InfoItemContentPart) {
                itemFigure.setAbbreviated(true);
                itemFigure.setSingleLine(true);
                itemFigure.setShowTabToSpace(true);
                InfoItemContentPart item = (InfoItemContentPart) part;
                itemFigure.setText(item.getContent());
            }
        }
    }

    public static InfoItemContentDecorator getInstance() {
        return instance;
    }

}
