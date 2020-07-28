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
package org.xmind.ui.internal.decorations;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_DESCRIPTION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_NAME;

import org.eclipse.core.runtime.IConfigurationElement;

final class DecorationCategory {

    private IConfigurationElement element;

    private String id;

    DecorationCategory(IConfigurationElement element) {
        this.element = element;
        load();
    }

    private void load() {
        this.id = element.getAttribute(ATT_ID);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return element.getAttribute(ATT_NAME);
    }

    public String getDescription() {
        return element.getAttribute(ATT_DESCRIPTION);
    }

}