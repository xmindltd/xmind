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
package org.xmind.ui.internal.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.ui.browser.IBrowserViewerContribution;

public class BrowserContributionManager extends RegistryReader {

    private static final String EXT_POINT_ID = "browserViewerContributions"; //$NON-NLS-1$

    private static final BrowserContributionManager instance = new BrowserContributionManager();

    private List<IBrowserViewerContribution> contributions;

    private BrowserContributionManager() {
    }

    public List<IBrowserViewerContribution> getContributions() {
        ensureLoaded();
        return contributions;
    }

    private void ensureLoaded() {
        if (contributions != null)
            return;
        lazyLoad();
        if (contributions == null)
            contributions = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), BrowserPlugin.PLUGIN_ID,
                EXT_POINT_ID);
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if ("contribution".equals(name)) { //$NON-NLS-1$
            readContribution(element);
            readElementChildren(element);
            return true;
        }
        return false;
    }

    private void readContribution(IConfigurationElement element) {
        if (getClassValue(element, IWorkbenchRegistryConstants.ATT_CLASS) == null) {
            BrowserPlugin.getDefault().getLog().log(
                    new Status(IStatus.ERROR, element.getNamespaceIdentifier(),
                            "Invalid extension: (class missing)")); //$NON-NLS-1$
            return;
        }

        try {
            Object obj = element
                    .createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
            if (obj instanceof IBrowserViewerContribution) {
                registerContribution((IBrowserViewerContribution) obj);
            }
        } catch (CoreException e) {
            BrowserPlugin.log(e);
        }
    }

    private void registerContribution(IBrowserViewerContribution contribution) {
        if (contributions == null)
            contributions = new ArrayList<IBrowserViewerContribution>();
        contributions.add(contribution);
    }

    public static BrowserContributionManager getInstance() {
        return instance;
    }
}