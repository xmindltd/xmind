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

import org.xmind.gef.GEF;
import org.xmind.gef.service.IFeedback;

public class ImageSelectionHelper extends SelectionFeedbackHelper {

    protected void updateFeedback(IFeedback feedback, int newStatus) {
        int fillAlpha;
        int lineAlpha;
        if (newStatus == GEF.PART_PRESELECTED) {
            fillAlpha = 0x30;
            lineAlpha = 0x80;
        } else {
            fillAlpha = 0;
            lineAlpha = 0xff;
        }
        ImageFeedback f = (ImageFeedback) feedback;
        f.getBorder().setBorderAlpha(lineAlpha);
        f.getBorder().setFillAlpha(fillAlpha);
        f.getPoints().setAlpha(lineAlpha);
        super.updateFeedback(feedback, newStatus);
    }

}