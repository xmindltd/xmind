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
package org.xmind.gef.policy;

import org.xmind.gef.EditDomain;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;

public abstract class AbstractEditPolicy implements IEditPolicy {

    protected ICommandStack getCommandStack(EditDomain domain) {
        return domain == null ? null : domain.getCommandStack();
    }

    protected void saveAndRun(Command command, EditDomain domain) {
        if (command == null || domain == null)
            return;
        ICommandStack cs = getCommandStack(domain);
        if (cs == null)
            return;
        executeCommand(cs, command, domain);
    }

    protected void executeCommand(ICommandStack cs, Command command,
            EditDomain domain) {
        cs.execute(command);
    }

    public boolean understands(String requestType) {
        return false;
    }

}