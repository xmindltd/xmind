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
package org.xmind.core.command;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.xmind.core.internal.command.Messages;
import org.xmind.core.internal.command.XMindCommandPlugin;

public class CommandJob extends Job {

    private ICommand command;

    private IReturnValueConsumer returnValueConsumer;

    public CommandJob(ICommand command, IReturnValueConsumer returnValueConsumer) {
        this(Messages.CommandJob_Name, command, returnValueConsumer);
    }

    public CommandJob(String name, ICommand command,
            IReturnValueConsumer returnValueConsumer) {
        super(name);
        Assert.isNotNull(command);
        this.command = command;
        this.returnValueConsumer = returnValueConsumer;
    }

    protected IStatus run(IProgressMonitor monitor) {
        return XMindCommandPlugin.getDefault().getCommandService()
                .execute(monitor, command, returnValueConsumer);
    }

}
