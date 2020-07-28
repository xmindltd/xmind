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
package org.xmind.gef.command;

import static org.xmind.gef.GEF.CS_POST_MASK;
import static org.xmind.gef.GEF.CS_PRE_MASK;

import java.util.EventObject;

/**
 * @author Brian Sun
 * @version 2005
 */
public class CommandStackEvent extends EventObject {

    private Command command;

    private int status;

    public CommandStackEvent(ICommandStack source, int status) {
        this(source, null, status);
    }

    public CommandStackEvent(ICommandStack source, Command command, int status) {
        super(source);
        this.command = command;
        this.status = status;
    }

    public Command getCommand() {
        return command;
    }

    public int getStatus() {
        return status;
    }

    public boolean isPreChangeEvent() {
        return (status & CS_PRE_MASK) != 0;
    }

    public boolean isPostChangeEvent() {
        return (status & CS_POST_MASK) != 0;
    }

    private static final long serialVersionUID = 154234080064326286L;

}