/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
package org.xmind.cathy.internal;

/**
 * @author Frank Shaka
 *
 */
public interface ILogger {

    ILogger DEFAULT = new ILogger() {

        public void logError(String message, Throwable error) {
            if (message != null) {
                System.err.println(message);
            }
            if (error != null) {
                error.printStackTrace(System.err);
            }
        }

        public void logWarning(String message, Throwable error) {
            if (message != null) {
                System.out.println(message);
            }
            if (error != null) {
                error.printStackTrace(System.out);
            }
        }

        public void logInfo(String message, Throwable error) {
            if (message != null) {
                System.out.println(message);
            }
            if (error != null) {
                error.printStackTrace(System.out);
            }
        }

    };

    void logError(String message, Throwable error);

    void logWarning(String message, Throwable error);

    void logInfo(String message, Throwable error);

}
