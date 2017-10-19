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

import org.xmind.core.Core;
import org.xmind.gef.Request;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * @author frankshaka
 * 
 */
public class InsertFloatingTopicAction extends PageAction {

    /**
     * @param page
     */
    public InsertFloatingTopicAction(String id, IGraphicalEditorPage page) {
        super(id, page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (isDisposed())
            return;

        if (MindMapActionFactory.INSERT_FLOATING_CENTRAL_TOPIC.getId().equals(
                getId())) {
            sendRequest(new Request(MindMapUI.REQ_CREATE_FLOAT).setParameter(
                    MindMapUI.PARAM_PROPERTY_PREFIX + Core.StructureClass,
                    "org.xmind.ui.map.floating")); //$NON-NLS-1$
        } else {
            sendRequest(MindMapUI.REQ_CREATE_FLOAT);
        }
    }

}
