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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.xmind.core.ITitled;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.internal.layers.TitleLayer;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.util.MindMapUtils;

public class BoundarySelectionFeedbackHelper extends SelectionFeedbackHelper {

    protected void updateOtherFeedback(IFeedbackService feedbackService,
            int newStatus) {
        super.updateOtherFeedback(feedbackService, newStatus);
        updateTitleFeedback(feedbackService, newStatus);
    }

    private void updateTitleFeedback(IFeedbackService feedbackService,
            int newStatus) {
        ITextFigure figure = getTitleFigure();
        if (figure != null) {
            figure.setVisible(getHost().getFigure().isVisible() && hasTitle());
            if (figure.getParent() instanceof TitleLayer) {
                TitleLayer layer = ((TitleLayer) figure.getParent());
                if ((newStatus & GEF.PART_SEL_MASK) != 0) {
                    layer.addOnTop(figure);
                } else {
                    layer.removeOnTop(figure);
                }
            }
        }
    }

    private ITextFigure getTitleFigure() {
        IFigure figure = getHost().getFigure();
        if (figure instanceof ITitledFigure) {
            ITextFigure title = ((ITitledFigure) figure).getTitle();
            if (title != null)
                return title;
        }
        ITitleTextPart titlePart = (ITitleTextPart) getHost().getAdapter(
                TitleTextPart.class);
        if (titlePart != null) {
            return titlePart.getTextFigure();
        }
        return null;
    }

    private boolean hasTitle() {
        Object m = MindMapUtils.getRealModel(getHost());
        if (m instanceof ITitled) {
            return ((ITitled) m).hasTitle();
        }
        return false;
    }

}