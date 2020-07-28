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
package org.xmind.ui.internal.tools;

import org.xmind.gef.service.IRectangleProvider;
import org.xmind.gef.service.RectangleFeedback;
import org.xmind.ui.resources.ColorUtils;

public class MindMapFeedbackFactory {

    public static final String LINE_COLOR_AREA_SELECT = "#00007f"; //$NON-NLS-1$
    public static final String FILL_COLOR_AREA_SELECT = "#aaaac8"; //$NON-NLS-1$

    public static final String LINE_COLOR_SIMPLE_SELECTION = "#0000f0"; //$NON-NLS-1$

    public static final String LINE_COLOR_SIMPLE_PRESELECTION = "#0000ff"; //$NON-NLS-1$
    public static final String FILL_COLOR_SIMPLE_PRESELECTION = "#0000ff"; //$NON-NLS-1$

    private MindMapFeedbackFactory() {
    }

    public static RectangleFeedback createAreaSelectFeedback(
            IRectangleProvider boundsProvider) {
        RectangleFeedback feedback = new RectangleFeedback();
        feedback.setBorderColor(ColorUtils.getColor(LINE_COLOR_AREA_SELECT));
        feedback.setFillColor(ColorUtils.getColor(FILL_COLOR_AREA_SELECT));
        feedback.setBorderAlpha(0xa0);
        feedback.setFillAlpha(0x60);
        feedback.setBoundsProvider(boundsProvider);
        return feedback;
    }

    public static RectangleFeedback createSimpleSelectionFeedback(
            IRectangleProvider boundsProvider) {
        RectangleFeedback feedback = new RectangleFeedback();
        feedback.setBorderColor(ColorUtils
                .getColor(LINE_COLOR_SIMPLE_SELECTION));
        feedback.setBorderAlpha(0xff);
        feedback.setBoundsProvider(boundsProvider);
        return feedback;
    }

    public static RectangleFeedback createSimplePreselectionFeedback(
            IRectangleProvider boundsProvider) {
        RectangleFeedback feedback = new RectangleFeedback();
        feedback.setBorderColor(ColorUtils
                .getColor(LINE_COLOR_SIMPLE_PRESELECTION));
        feedback.setFillColor(ColorUtils
                .getColor(FILL_COLOR_SIMPLE_PRESELECTION));
        feedback.setBorderAlpha(0xff);
        feedback.setFillAlpha(0x20);
        feedback.setBoundsProvider(boundsProvider);
        return feedback;
    }

}