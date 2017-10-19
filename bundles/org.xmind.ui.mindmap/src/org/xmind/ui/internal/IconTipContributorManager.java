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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.ui.mindmap.IIconTipContributor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class IconTipContributorManager extends RegistryReader {

    private static final IconTipContributorManager instance = new IconTipContributorManager();

    private List<IIconTipContributor> contributors = null;

    private IconTipContributorManager() {
    }

    public List<IIconTipContributor> getContributors() {
        ensureLoaded();
        return contributors;
    }

    private void ensureLoaded() {
        if (contributors != null)
            return;
        lazyLoad();
        if (contributors == null)
            contributors = Collections.emptyList();
    }

    private void lazyLoad() {
        if (Platform.isRunning()) {
            readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                    RegistryConstants.EXT_ICONTIPS);
        }
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (RegistryConstants.TAG_ICONTIP.equals(name)) {
            readIconTip(element);
            return true;
        }
        return false;
    }

    private void readIconTip(IConfigurationElement element) {
        IIconTipContributor contributor;
        try {
            contributor = new IconTipContributorProxy(element);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load icon tip: " + element); //$NON-NLS-1$
            return;
        }
        if (contributors == null)
            contributors = new ArrayList<IIconTipContributor>();
        contributors.add(contributor);
    }

    public static IconTipContributorManager getInstance() {
        return instance;
    }

}