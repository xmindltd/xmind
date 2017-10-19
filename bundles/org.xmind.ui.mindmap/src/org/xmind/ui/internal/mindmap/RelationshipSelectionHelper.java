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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.xmind.core.ITitled;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.internal.layers.TitleLayer;
import org.xmind.ui.util.MindMapUtils;

public class RelationshipSelectionHelper extends SelectionFeedbackHelper {

    protected void updateFeedback(IFeedback feedback, int newStatus) {
        int alpha;
        if ((newStatus & GEF.PART_SELECTED) != 0) {
            alpha = 0xff;
        } else if ((newStatus & GEF.PART_PRESELECTED) != 0) {
            alpha = 0xb0;
        } else {
            alpha = 0;
        }
        ((RelationshipFeedback) feedback).setAlpha(alpha);
        super.updateFeedback(feedback, newStatus);
    }

    protected void updateOtherFeedback(IFeedbackService feedbackService,
            int newStatus) {
        super.updateOtherFeedback(feedbackService, newStatus);
        updateTitleFeedback(feedbackService, newStatus);
    }

    protected void removeOtherFeedback(IFeedbackService feedbackService) {
        super.removeOtherFeedback(feedbackService);
        ITextFigure figure = getTitleFigure();
        if (figure != null) {
            feedbackService.removeSelection(figure);
        }
    }

    private void updateTitleFeedback(IFeedbackService feedbackService,
            int newStatus) {
        ITextFigure figure = getTitleFigure();
        if (figure != null) {
            if ((newStatus & GEF.PART_SELECTED) != 0) {
                feedbackService.setSelected(figure);
            } else if ((newStatus & GEF.PART_PRESELECTED) != 0) {
                feedbackService.setPreselected(figure);
            } else if (newStatus == 0) {
                feedbackService.removeSelection(figure);
            }
            figure.setVisible(newStatus != 0
                    || (getHost().getFigure().isVisible() && hasTitle()));
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
            return ((ITitledFigure) figure).getTitle();
        }
        return null;
    }

    private boolean hasTitle() {
        Object m = MindMapUtils.getRealModel(getHost());
        if (m instanceof ITitled) {
            ITitled titled = (ITitled) m;
            return titled.hasTitle() && !"".equals(titled.getTitleText()); //$NON-NLS-1$
        }
        return false;
    }

}