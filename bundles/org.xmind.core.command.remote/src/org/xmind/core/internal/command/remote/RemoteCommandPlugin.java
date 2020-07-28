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
package org.xmind.core.internal.command.remote;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.command.ICommandService;
import org.xmind.core.command.remote.ICommandServiceDomainManager;
import org.xmind.core.internal.command.XMindCommandPlugin;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class RemoteCommandPlugin implements BundleActivator {

    public static final String PLUGIN_ID = "org.xmind.core.command.remote"; //$NON-NLS-1$

    private static BundleContext context;

    private static RemoteCommandPlugin singleton;

    private ServiceRegistration<ICommandServiceDomainManager> frameworkRegistration = null;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        RemoteCommandPlugin.singleton = this;
        RemoteCommandPlugin.context = bundleContext;

        frameworkRegistration = bundleContext.registerService(
                ICommandServiceDomainManager.class,
                CommandServiceDomainManagerImpl.getDefault(), null);

    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (debugTracker != null) {
            debugTracker.close();
            debugTracker = null;
        }
        if (frameworkRegistration != null) {
            frameworkRegistration.unregister();
            frameworkRegistration = null;
        }
        RemoteCommandPlugin.context = null;
        RemoteCommandPlugin.singleton = null;
    }

    public static void log(String message, Throwable e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    public static void log(IStatus status) {
        Platform.getLog(context.getBundle()).log(status);
    }

    public DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    context, DebugOptions.class.getName(), null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public boolean isDebugging(String option) {
        DebugOptions debugOptions = getDebugOptions();
        return debugOptions!=null&&debugOptions.isDebugEnabled()
                && debugOptions
                        .getBooleanOption(PLUGIN_ID + option, false);
    }

    public ICommandService getCommandService() {
        return XMindCommandPlugin.getDefault().getCommandService();
    }

    public static RemoteCommandPlugin getDefault() {
        return RemoteCommandPlugin.singleton;
    }

}
