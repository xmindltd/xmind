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
import org.xmind.gef.GEF;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.internal.figures.PlusMinusFigure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;

public class PlusMinusSelectionHelper extends SelectionFeedbackHelper {

    protected void updateOtherFeedback(IFeedbackService feedbackService,
            int newStatus) {
        boolean preselected = (newStatus & GEF.PART_PRESELECTED) != 0;

        IFigure figure = getHost().getFigure();
        if (figure instanceof PlusMinusFigure) {
            ((PlusMinusFigure) figure).setPreselected(preselected);
        }

        IPart parent = getHost().getParent();
        if (parent instanceof IBranchPart) {
            ITopicPart topic = ((IBranchPart) parent).getTopicPart();
            if (topic != null) {
                ISelectionFeedbackHelper helper = (ISelectionFeedbackHelper) topic
                        .getAdapter(ISelectionFeedbackHelper.class);
                if (helper != null) {
                    if (preselected) {
                        helper.forceFeedback(GEF.PART_PRESELECTED, preselected);
                    } else {
                        helper.resetFeedback(GEF.PART_PRESELECTED);
                    }
                }
            }
        }
    }

    protected void removeOtherFeedback(IFeedbackService feedbackService) {
        IPart parent = getHost().getParent();
        if (parent instanceof IBranchPart) {
            ITopicPart topic = ((IBranchPart) parent).getTopicPart();
            if (topic != null) {
                ISelectionFeedbackHelper helper = (ISelectionFeedbackHelper) topic
                        .getAdapter(ISelectionFeedbackHelper.class);
                if (helper != null) {
                    helper.resetFeedback(GEF.PART_PRESELECTED);
                }
            }
        }
    }
}