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
package org.xmind.cathy.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.handlers.ShowKeyAssistHandler;
import org.xmind.cathy.internal.WorkbenchMessages;

@Deprecated
public class ShowKeyAssistAction extends Action {

    private ShowKeyAssistHandler handler;

    /**
     * @param text
     * @param window
     */
    public ShowKeyAssistAction() {
        super(WorkbenchMessages.KeyAssist_text);
        setId("showKeyAssist"); //$NON-NLS-1$
        setActionDefinitionId("org.eclipse.ui.window.showKeyAssist"); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (handler == null)
            handler = new ShowKeyAssistHandler();
        handler.execute(null);
    }

}