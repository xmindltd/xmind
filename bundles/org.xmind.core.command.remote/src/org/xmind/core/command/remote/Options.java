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
/**
 * 
 */
package org.xmind.core.command.remote;

/**
 * Options and configurations for controlling the remote command execution
 * process. For example, a timeout option specifies how long the client should
 * wait before the remote command service completes the command execution.
 * 
 * @author Frank Shaka
 * @see IRemoteCommandService
 */
public class Options {

    /**
     * The default timeout option value (value is <code>6000</code>, i.e. 6
     * seconds);
     */
    public static final int DEFAULT_TIMEOUT = 6000;

    /**
     * The timeout option.
     */
    private int timeout = DEFAULT_TIMEOUT;

    /**
     * Construct a new {@link Options} instance with default values.
     */
    public Options() {
    }

    /**
     * Sets the timeout option, in milliseconds. The service will wait for the
     * remote handler for the specified timeout before cutting down the
     * connection to the remote location and canceling the command execution.
     * 
     * <p>
     * Note that the timeout option should be a positive integer. Otherwise the
     * default value {@link #DEFAULT_TIMEOUT} will be used.
     * </p>
     * 
     * @param timeout
     *            the specified timeout, in milliseconds
     * @return this {@lin Options} instance
     */
    public Options timeout(int timeout) {
        if (timeout <= 0)
            timeout = DEFAULT_TIMEOUT;
        this.timeout = timeout;
        return this;
    }

    /**
     * Gets the timeout option.
     * 
     * @return the timeout option
     */
    public int timeout() {
        return this.timeout;
    }

    /**
     * A {@link Options} instance with default values.
     */
    public static final Options DEFAULT = new Options();

}