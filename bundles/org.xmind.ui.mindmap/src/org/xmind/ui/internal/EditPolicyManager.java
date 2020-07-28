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
package org.xmind.ui.internal;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CLASS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.gef.policy.IEditPolicy;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.ui.mindmap.IEditPolicyManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class EditPolicyManager extends RegistryReader implements
        IEditPolicyManager {

    private static class EditPolicyDescriptor {

        private IConfigurationElement element;

        private String id;

        private IEditPolicy policy;

        public EditPolicyDescriptor(IConfigurationElement element)
                throws CoreException {
            this.element = element;
            this.id = element.getAttribute(RegistryConstants.ATT_ID);
            if (RegistryReader.getClassValue(element, ATT_CLASS) == null) {
                throw new CoreException(new Status(IStatus.ERROR, element
                        .getNamespaceIdentifier(), 0,
                        "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                        null));
            }
        }

        public String getId() {
            return id;
        }

        public IEditPolicy getPolicy() {
            if (policy == null) {
                policy = createPolicy();
            }
            return policy;
        }

        private IEditPolicy createPolicy() {
            try {
                return (IEditPolicy) element
                        .createExecutableExtension(ATT_CLASS);
            } catch (CoreException e) {
                Logger.log(e, "Failed to create edit policy: " + id); //$NON-NLS-1$
                return NullEditPolicy.getInstance();
            }
        }

    }

    private Map<String, EditPolicyDescriptor> descriptors = null;

    public IEditPolicy getEditPolicy(String editPolicyId) {
        EditPolicyDescriptor descriptor = getDescriptor(editPolicyId);
        if (descriptor != null) {
            return descriptor.getPolicy();
        }
        return NullEditPolicy.getInstance();
    }

    private EditPolicyDescriptor getDescriptor(String editPolicyId) {
        ensureLoaded();
        return descriptors.get(editPolicyId);
    }

    private void ensureLoaded() {
        if (descriptors != null)
            return;
        lazyLoad();
        if (descriptors == null)
            descriptors = Collections.emptyMap();
    }

    private void lazyLoad() {
        if (!Platform.isRunning())
            return;
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                RegistryConstants.EXT_EDIT_POLICIES);
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (RegistryConstants.TAG_EDIT_POLICY.equals(name)) {
            readEditPolicy(element);
            return true;
        }
        return false;
    }

    private void readEditPolicy(IConfigurationElement element) {
        EditPolicyDescriptor descriptor;
        try {
            descriptor = new EditPolicyDescriptor(element);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load edit policy: " + element); //$NON-NLS-1$
            return;
        }
        String id = descriptor.getId();
        if (id == null)
            return;

        if (descriptors == null)
            descriptors = new HashMap<String, EditPolicyDescriptor>();
        descriptors.put(id, descriptor);
    }

}