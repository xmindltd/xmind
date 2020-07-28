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
package org.xmind.ui.internal.actions;

import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapMessages;

public class OpenHyperlinkAction extends SelectionRetargetAction {

    public OpenHyperlinkAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.OPEN_HYPERLINK.getId(), page);
        setText(MindMapMessages.OpenHyperlink_text);
        setToolTipText(MindMapMessages.OpenHyperlink_toolTip);
    }

    protected String getHandlerId() {
        return MindMapActionFactory.OPEN_HYPERLINK.getId();
    }

    protected void runWithNoHandler() {
    }

}
