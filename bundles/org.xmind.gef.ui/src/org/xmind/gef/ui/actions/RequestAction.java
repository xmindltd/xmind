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
package org.xmind.gef.ui.actions;

import org.xmind.gef.ui.editor.IGraphicalEditorPage;

/**
 * @author Brian Sun
 */
public class RequestAction extends PageAction {

    private String requestType;

    public RequestAction(IGraphicalEditorPage page, String requestType) {
        super(page);
        this.requestType = requestType;
    }

    public RequestAction(String id, IGraphicalEditorPage page,
            String requestType) {
        this(page, requestType);
        setId(id);
    }

    public String getRequestType() {
        return requestType;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (isDisposed())
            return;

        sendRequest(requestType);
    }

}