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
package org.xmind.ui.internal.branch;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.util.Logger;

class AdditionalStructure {

    private BranchPolicyManager manager;

    private IConfigurationElement element;

    private String structureId;

    private Expression condition;

    private IStructureDescriptor structure;

    public AdditionalStructure(BranchPolicyManager manager,
            IConfigurationElement element) throws CoreException {
        this.manager = manager;
        this.element = element;
        load();
    }

    private void load() throws CoreException {
        structureId = element.getAttribute(RegistryConstants.ATT_STRUCTURE_ID);
        if (structureId == null)
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(), 0,
                    "Invalid extension (missing structure id)", //$NON-NLS-1$ 
                    null));
        initializeEnablement();
    }

    private void initializeEnablement() {
        IConfigurationElement[] elements = element
                .getChildren(IWorkbenchRegistryConstants.TAG_ENABLEMENT);
        if (elements.length == 0)
            return;

        try {
            condition = ExpressionConverter.getDefault().perform(elements[0]);
        } catch (CoreException e) {
            Logger.log(e, "Failed to convert expression: " //$NON-NLS-1$
                    + elements[0].getNamespaceIdentifier());
        }
    }

    public boolean isApplicableTo(IEvaluationContext context) {
        if (condition == null)
            return false;
        try {
            return condition.evaluate(context) == EvaluationResult.TRUE;
        } catch (CoreException e) {
            Logger.log(e, "Evaluation failed: " + condition); //$NON-NLS-1$
        }
        return false;
    }

    public IStructureDescriptor getStructure() {
        if (structure == null) {
            structure = loadStructure();
        }
        return structure;
    }

    private IStructureDescriptor loadStructure() {
        return manager.getStructureDescriptor(structureId);
    }

    public String getStructureId() {
        return structureId;
    }

}