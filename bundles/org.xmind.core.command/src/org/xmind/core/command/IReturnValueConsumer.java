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
 * A return value consumer.
 * 
 * @author Frank Shaka
 */
public interface IReturnValueConsumer {

    /**
     * Consumes the value returned from execution of a command. The return value
     * may be a normal {@link org.eclipse.core.runtime.Status} or a
     * {@link ReturnValue} instance containing additional information and/or
     * cached resources.
     * 
     * @param monitor
     *            the progress monitor, never <code>null</code>
     * @param returnValue
     *            the return value, never <code>null</code>
     * @return a status indicating the consumption of the return value
     */
    IStatus consumeReturnValue(IProgressMonitor monitor, IStatus returnValue);

}
