/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.core.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.xmind.core.internal.runtime.RuntimeLogger;
import org.xmind.core.internal.security.BouncyCastleSecurityProvider;
import org.xmind.core.internal.security.Crypto;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("deprecation")
public class XmindCore extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.xmind.core.runtime"; //$NON-NLS-1$

    // The shared instance
    private static XmindCore plugin;

    /**
     * The constructor
     */
    public XmindCore() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        InternalCore.getInstance().setLogger(new RuntimeLogger());

        Crypto.setProvider(new BouncyCastleSecurityProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
    public static XmindCore getDefault() {
        return plugin;
    }

}