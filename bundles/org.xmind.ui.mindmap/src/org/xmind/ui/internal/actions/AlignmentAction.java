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
package org.xmind.ui.internal.actions;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.ui.actions.RetargetAction;
import org.xmind.ui.internal.MindMapMessages;

public class AlignmentAction extends RetargetAction {

    public AlignmentAction(int alignment) {
        super(null, null);
        switch (alignment) {
        case PositionConstants.LEFT:
            setId(ActionConstants.ALIGNMENT_LEFT_ID);
            setText(MindMapMessages.AlignLeft_text);
            setToolTipText(MindMapMessages.AlignLeft_toolTip);
            break;
        case PositionConstants.CENTER:
            setId(ActionConstants.ALIGNMENT_CENTER_ID);
            setText(MindMapMessages.AlignCenter_text);
            setToolTipText(MindMapMessages.AlignCenter_toolTip);
            break;
        case PositionConstants.RIGHT:
            setId(ActionConstants.ALIGNMENT_RIGHT_ID);
            setText(MindMapMessages.AlignRight_text);
            setToolTipText(MindMapMessages.AlignRight_toolTip);
            break;
        case PositionConstants.TOP:
            setId(ActionConstants.ALIGNMENT_TOP_ID);
            setText(MindMapMessages.AlignTop_text);
            setToolTipText(MindMapMessages.AlignTop_toolTip);
            break;
        case PositionConstants.MIDDLE:
            setId(ActionConstants.ALIGNMENT_MIDDLE_ID);
            setText(MindMapMessages.AlignMiddle_text);
            setToolTipText(MindMapMessages.AlignMiddle_toolTip);
            break;
        case PositionConstants.BOTTOM:
            setId(ActionConstants.ALIGNMENT_BOTTOM_ID);
            setText(MindMapMessages.AlignBottom_text);
            setToolTipText(MindMapMessages.AlignBottom_toolTip);
            break;
        }
    }

}