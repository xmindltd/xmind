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
package org.xmind.ui.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.Core;
import org.xmind.core.command.ICommandService;
import org.xmind.ui.internal.editor.BackgroundWorkbookSaver;

public class MindMapUIPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.ui.mindmap"; //$NON-NLS-1$

    // The shared instance.
    private static MindMapUIPlugin plugin;

    private ServiceTracker<ICommandService, ICommandService> commandServiceTracker = null;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    private Set<Job> jobs = new HashSet<Job>();

    /**
     * The constructor
     */
    public MindMapUIPlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
     * BundleContext )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        //Shell shell = plugin.getWorkbench().getDisplay().getActiveShell();
        Core.getWorkbookBuilder()
                .setDefaultEncryptionHandler(new PasswordProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext )
     */
    public void stop(BundleContext context) throws Exception {
        cancelAllJobs();

        BackgroundWorkbookSaver.getInstance().stopAll();

        if (commandServiceTracker != null) {
            commandServiceTracker.close();
            commandServiceTracker = null;
        }
        plugin = null;
        super.stop(context);
    }

    public ICommandService getCommandService() {
        if (commandServiceTracker == null) {
            commandServiceTracker = new ServiceTracker<ICommandService, ICommandService>(
                    getBundle().getBundleContext(),
                    ICommandService.class.getName(), null);
            commandServiceTracker.open();
        }
        return commandServiceTracker.getService();
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static MindMapUIPlugin getDefault() {
        return plugin;
    }

    public IDialogSettings getDialogSettings(String sectionName) {
        IDialogSettings ds = getDialogSettings();
        IDialogSettings section = ds.getSection(sectionName);
        if (section == null) {
            section = ds.addNewSection(sectionName);
        }
        return section;
    }

    private DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    getBundle().getBundleContext(), DebugOptions.class, null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    /**
     * Returns the debug switch for the specified option.
     * 
     * @param option
     *            value like <code>"/debug/some/feature"</code>
     * @return <code>true</code> if debugging is turned on for this option, or
     *         <code>false</code> otherwise
     */
    public static boolean isDebugging(String option) {
        return getDefault().getDebugOptions()
                .getBooleanOption(PLUGIN_ID + option, false);
    }

    public static <T> T getAdapter(Object obj, Class<T> adapter) {
        Assert.isNotNull(adapter);
        if (adapter.isInstance(obj))
            return adapter.cast(obj);

        if (obj instanceof IAdaptable) {
            T result = ((IAdaptable) obj).getAdapter(adapter);
            if (result != null)
                return result;
        }

        if (!(obj instanceof PlatformObject)) {
            T result = Platform.getAdapterManager().getAdapter(obj, adapter);
            if (result != null)
                return result;
        }

        return null;
    }

    public void registerJob(Job job) {
        jobs.add(job);
        job.addJobChangeListener(new JobChangeAdapter() {
            public void done(IJobChangeEvent event) {
                super.done(event);
                jobs.remove(event.getJob());
            }
        });
    }

    private void cancelAllJobs() {
        Object[] runningJobs = jobs.toArray();
        for (int i = 0; i < runningJobs.length; i++) {
            ((Job) runningJobs[i]).cancel();
        }
    }

}