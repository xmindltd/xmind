/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.xmind.ui.exports.vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Jason Wong
 */
public class VectorPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.ui.exports.vector"; //$NON-NLS-1$

    // The shared instance
    private static VectorPlugin plugin;

    /**
     * The constructor
     */
    public VectorPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static VectorPlugin getDefault() {
        return plugin;
    }

    public static IDialogSettings getDialogSettings(String sectionName) {
        IDialogSettings ds = getDefault().getDialogSettings();
        if (sectionName == null)
            return ds;
        IDialogSettings section = ds.getSection(sectionName);
        if (section == null) {
            section = ds.addNewSection(sectionName);
        }
        return section;
    }

    public static void log(Throwable e, String message) {
        getDefault().getLog().log(
                new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

}
