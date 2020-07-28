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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A command handler executes a command.
 * 
 * @author Frank Shaka
 */
public interface ICommandHandler {

    /**
     * Executes the given command. This method will be called by
     * {@link ICommandService}.
     * 
     * <p>
     * Note that <code>null</code> should only be returned if the command is not
     * recognized. A non-<code>null</code> value is required if the execution
     * has been finished.
     * </p>
     * 
     * @param monitor
     *            the progress monitor
     * @param command
     *            the command to handle
     * @param matches
     *            the matching groups matched against the command name pattern
     * @return the return value, e.g. a normal
     *         {@link org.eclipse.core.runtime.Status}, a {@link ReturnValue}
     *         with additional information, or <code>null</code> indicating that
     *         the command is not recognized
     */
    IStatus execute(IProgressMonitor monitor, ICommand command, String[] matches);

}
