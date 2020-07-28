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
package org.xmind.ui.internal.statushandlers;

import org.eclipse.swt.program.Program;

public class DefaultErrorReporter implements org.xmind.ui.internal.statushandlers.IErrorReporter {

    private static DefaultErrorReporter delegate = null;

    private static DefaultErrorReporter instance = new DefaultErrorReporter();

    private DefaultErrorReporter() {
    }

    public boolean report(StatusDetails error) throws InterruptedException {
        if (delegate != null && delegate.report(error))
            return true;
        return Program.launch(error.buildMailingURL());
    }

    public static DefaultErrorReporter getInstance() {
        return instance;
    }

    public static void setDelegate(DefaultErrorReporter errorReporter) {
        delegate = errorReporter;
    }

}
