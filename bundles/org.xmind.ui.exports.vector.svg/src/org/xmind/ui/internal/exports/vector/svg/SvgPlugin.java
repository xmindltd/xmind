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
package org.xmind.ui.internal.exports.vector.svg;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xmind.core.usagedata.IUsageDataSampler;

/**
 * @author Jason Wong
 */
public class SvgPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.xmind.ui.exports.vector.svg"; //$NON-NLS-1$

	// The shared instance
	private static SvgPlugin plugin;

	private ServiceTracker<IUsageDataSampler, IUsageDataSampler> usageDataTracker;

	/**
	 * The constructor
	 */
	public SvgPlugin() {
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

		usageDataTracker = new ServiceTracker<IUsageDataSampler, IUsageDataSampler>(context, IUsageDataSampler.class,
				null);
		usageDataTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */
	public void stop(BundleContext context) throws Exception {
		usageDataTracker.close();
		usageDataTracker = null;

		plugin = null;
		super.stop(context);
	}

	public IUsageDataSampler getUsageDataCollector() {
		IUsageDataSampler service = usageDataTracker == null ? null : usageDataTracker.getService();
		return service == null ? IUsageDataSampler.NULL : service;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SvgPlugin getDefault() {
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
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

}
