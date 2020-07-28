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
package org.xmind.gef.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GEFPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.gef.ui"; //$NON-NLS-1$

    // The shared instance
    private static GEFPlugin plugin;

    /**
     * The constructor
     */
    public GEFPlugin() {
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
     * BundleContext )
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
    public static GEFPlugin getDefault() {
        return plugin;
    }

    public static <T> T getAdapter(Object sourceObject, Class<T> adapterType) {
        Assert.isNotNull(adapterType);
        if (sourceObject == null) {
            return null;
        }
        if (adapterType.isInstance(sourceObject)) {
            return adapterType.cast(sourceObject);
        }

        if (sourceObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) sourceObject;

            T result = adaptable.getAdapter(adapterType);
            if (result != null) {
                // Sanity-check
                Assert.isTrue(adapterType.isInstance(result));
                return result;
            }
        }

        if (!(sourceObject instanceof PlatformObject)) {
            T result = Platform.getAdapterManager().getAdapter(sourceObject,
                    adapterType);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}