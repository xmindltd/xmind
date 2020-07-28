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

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CLASS;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_ENABLEMENT;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.Logger;

public class BranchHookFactory {

    private static class NullBranchHook implements IBranchHook {

        public void hook(IBranchPart branch) {
        }

        public void unhook(IBranchPart branch) {
        }

    }

    private static NullBranchHook NullInstance = null;

    private IConfigurationElement element;

    private boolean singleton;

    private IBranchHook singletonInstance;

    private Expression condition;

    public BranchHookFactory(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.singleton = Boolean.parseBoolean(element
                .getAttribute(RegistryConstants.ATT_SINGLETON));
        if (element.getAttribute(ATT_CLASS) == null) {
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(), 0,
                    "Invalid extension (missing class name): " + element //$NON-NLS-1$
                    , null));
        }
        initializeEnablement();
    }

    private void initializeEnablement() {
        IConfigurationElement[] enablements = element
                .getChildren(TAG_ENABLEMENT);
        if (enablements.length == 0)
            return;

        try {
            condition = ExpressionConverter.getDefault()
                    .perform(enablements[0]);
        } catch (CoreException e) {
            Logger.log(e, "Failed to convert expression: " + enablements[0]); //$NON-NLS-1$
        }
    }

    public IBranchHook createHook(IBranchPart branch) {
        if (failsEnablement(branch))
            return null;

        if (singleton) {
            return getSingletonInstance();
        }
        return createInstance();
    }

    private IBranchHook createInstance() {
        try {
            return (IBranchHook) element.createExecutableExtension(ATT_CLASS);
        } catch (CoreException e) {
            Logger.log(e, "Failed to create branch hook: " //$NON-NLS-1$
                    + element.getAttribute(ATT_CLASS));
        }
        return null;
    }

    private IBranchHook getSingletonInstance() {
        if (singletonInstance == null) {
            singletonInstance = createInstance();
            if (singletonInstance == null) {
                if (NullInstance == null)
                    NullInstance = new NullBranchHook();
                singletonInstance = NullInstance;
            }
        }
        return singletonInstance;
    }

    private boolean failsEnablement(IBranchPart branch) {
        if (condition == null)
            return false;

        IEvaluationContext context = BranchPolicyManager
                .createBranchEvaluationContext(branch);
        try {
            EvaluationResult result = condition.evaluate(context);
            return result != EvaluationResult.TRUE;
        } catch (CoreException e) {
            Logger.log(e, "Evaluation Failed: " + branch); //$NON-NLS-1$
        }
        return true;
    }

}