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

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.osgi.framework.Bundle;
import org.xmind.cathy.internal.WorkbenchMessages;

import net.xmind.signin.XMindNet;

/**
 * @author Frank Shaka
 * 
 */
@Deprecated
public class HelpAction extends Action implements IWorkbenchAction {

    private static final String ONLINE_HELP_URL = "http://www.xmind.net/xmind/help/"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    /**
     * 
     */
    public HelpAction(String id, IWorkbenchWindow window) {
        super(WorkbenchMessages.Help_text);
        setId(id);
        setToolTipText(WorkbenchMessages.Help_toolTip);
        setActionDefinitionId(IWorkbenchCommandConstants.HELP_HELP_CONTENTS);
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (window == null)
            return;

        XMindNet.gotoURL(findHelpURL());
    }

    private String findHelpURL() {
        Bundle helpBundle = Platform.getBundle("org.xmind.ui.help"); //$NON-NLS-1$
        if (helpBundle != null) {
            URL url = FileLocator.find(helpBundle,
                    new Path("$nl$/contents/index.html"), null); //$NON-NLS-1$
            if (url != null) {
                try {
                    url = FileLocator.toFileURL(url);
                    return url.toExternalForm();
                } catch (IOException e) {
                }
            }
        }
        return ONLINE_HELP_URL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        window = null;
    }

}
