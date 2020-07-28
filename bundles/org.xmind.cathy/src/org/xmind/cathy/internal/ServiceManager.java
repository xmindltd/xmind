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

import org.xmind.core.licensing.ILicenseAgent;
import org.xmind.core.usagedata.IUsageDataSampler;
import org.xmind.ui.internal.statushandlers.IErrorReporter;

/**
 * @author Frank Shaka
 *
 */
public class ServiceManager {

    private boolean active;

    private IErrorReporter errorReporter;

    private IUsageDataSampler usageDataSampler;

    private ILicenseAgent licenseAgent;

    private CathyPlugin plugin;

    /**
     * 
     */
    public ServiceManager() {
        this.active = false;
        this.errorReporter = null;
        this.usageDataSampler = null;
        this.licenseAgent = null;
        this.plugin = CathyPlugin.getDefault();
    }

    public void activate() {
        if (active)
            return;

        plugin.setUsageDataCollector(usageDataSampler);
        plugin.setErrorReporter(errorReporter);
        plugin.setLicenseAgent(licenseAgent);

        active = true;
    }

    public void deactivate() {
        if (!active)
            return;

        plugin.setUsageDataCollector(null);
        plugin.setErrorReporter(null);
        plugin.setLicenseAgent(null);

        active = false;
    }

    public void setErrorReporter(IErrorReporter reporter) {
        this.errorReporter = reporter;

        if (active) {
            plugin.setErrorReporter(reporter);
        }
    }

    public void unsetErrorReporter(IErrorReporter reporter) {
        if (reporter != this.errorReporter)
            return;
        this.errorReporter = null;
        if (active) {
            plugin.setErrorReporter(null);
        }
    }

    /**
     * @param sampler
     *            the usageDataSampler to set
     */
    public void setUsageDataSampler(IUsageDataSampler sampler) {
        this.usageDataSampler = sampler;
        if (active) {
            plugin.setUsageDataCollector(sampler);
        }
    }

    public void unsetUsageDataSampler(IUsageDataSampler sampler) {
        if (sampler == this.usageDataSampler)
            return;
        this.usageDataSampler = null;
        if (active) {
            plugin.setUsageDataCollector(null);
        }
    }

    public void setLicenseAgent(ILicenseAgent agent) {
        this.licenseAgent = agent;
        if (active) {
            plugin.setLicenseAgent(agent);
        }
    }

    public void unsetLicenseAgent(ILicenseAgent agent) {
        if (agent == this.licenseAgent)
            return;
        this.licenseAgent = null;
        if (active) {
            plugin.setLicenseAgent(null);
        }
    }

}
