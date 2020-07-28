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

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_ENABLEMENT;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.util.Logger;

public class UnmodifiableProperty {

    private IConfigurationElement element;

    private String primaryKey;

    private String secondaryKey;

    private Expression condition;

    public UnmodifiableProperty(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.primaryKey = element
                .getAttribute(RegistryConstants.ATT_PRIMARY_KEY);
        if (primaryKey == null) {
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(), 0,
                    "Invalid extension (missing primary key): " //$NON-NLS-1$
                            + element, null));
        }
        this.secondaryKey = element
                .getAttribute(RegistryConstants.ATT_SECONDARY_KEY);
        initializeEnablement();
    }

    private void initializeEnablement() {
        IConfigurationElement[] children = element.getChildren(TAG_ENABLEMENT);
        if (children.length == 0)
            return;

        try {
            condition = ExpressionConverter.getDefault().perform(children[0]);
        } catch (CoreException e) {
            Logger.log(e, "Failed to convert expression: " + children[0]); //$NON-NLS-1$
        }
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }

    boolean isApplicableTo(IEvaluationContext context) {
        if (condition == null)
            return false;
        try {
            EvaluationResult result = condition.evaluate(context);
            return result == EvaluationResult.TRUE;
        } catch (CoreException e) {
            Logger.log(e, "Evaluation failed: " + context); //$NON-NLS-1$
            return false;
        }
    }
}