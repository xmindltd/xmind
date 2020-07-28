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
import org.xmind.gef.service.IFeedbackService;

public class DefaultSelectionFeedbackHelper extends SelectionFeedbackHelper {

    protected void updateOtherFeedback(IFeedbackService feedbackService,
            int newStatus) {
        if (getHost() != null) {
            IFigure figure = getHost().getFigure();
            if (figure != null) {
                if ((newStatus & GEF.PART_SELECTED) != 0) {
                    feedbackService.setSelected(figure);
                } else if ((newStatus & GEF.PART_PRESELECTED) != 0) {
                    feedbackService.setPreselected(figure);
                } else if (newStatus == 0) {
                    feedbackService.removeSelection(figure);
                }
            }
        }
    }

    protected void removeOtherFeedback(IFeedbackService feedbackService) {
        if (getHost() != null) {
            feedbackService.removeSelection(getHost().getFigure());
        }
    }

}