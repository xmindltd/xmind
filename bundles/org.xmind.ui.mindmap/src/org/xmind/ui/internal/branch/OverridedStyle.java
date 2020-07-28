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

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_KEY;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_NAME;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_VALUE;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_ENABLEMENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.ui.branch.IBranchStyleValueProvider;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.Logger;

public class OverridedStyle {

    private BranchPolicyManager manager;

    private IConfigurationElement element;

    private String key;

    private String value;

    private List<String> layers;

    private IBranchStyleValueProvider valueProvider;

    private boolean triedLoadingValueProvider;

    private Expression condition;

    public OverridedStyle(BranchPolicyManager manager,
            IConfigurationElement element) throws CoreException {
        this.manager = manager;
        this.element = element;
        this.key = element.getAttribute(ATT_KEY);
        if (key == null)
            throw new CoreException(
                    new Status(IStatus.ERROR, element.getNamespaceIdentifier(),
                            0, "Invalid extension (missing style key)", //$NON-NLS-1$
                            null));
        this.value = element.getAttribute(ATT_VALUE);
        initializeLayers();
        initializeEnablement();
    }

    private void initializeLayers() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_LAYER);
        if (children.length > 0) {
            for (IConfigurationElement child : children) {
                String layerName = child.getAttribute(ATT_NAME);
                if (layerName != null) {
                    if (layers == null)
                        layers = new ArrayList<String>();
                    layers.add(layerName);
                }
            }
        }
        if (layers == null)
            layers = Collections.emptyList();
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

    public List<String> getLayers() {
        return layers;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return value;
    }

    public boolean isOnLayer(String layerName) {
        if (layers.isEmpty()) {
            return Styles.LAYER_BEFORE_DEFAULT_VALUE.equals(layerName);
        }
        return layers.contains(layerName);
    }

    public boolean isApplicableTo(IBranchPart branch) {
        if (condition == null)
            return true;
        return isApplicableTo(
                BranchPolicyManager.createBranchEvaluationContext(branch));
    }

    boolean isApplicableTo(IEvaluationContext context) {
        if (condition == null)
            return true;
        try {
            EvaluationResult result = condition.evaluate(context);
            return result == EvaluationResult.TRUE;
        } catch (CoreException e) {
            Logger.log(e, "Evaluation failed: " + context); //$NON-NLS-1$
        }
        return false;
    }

    public String getValue(IBranchPart branch, String layerName) {
        IBranchStyleValueProvider valueProvider = getValueProvider();
        if (valueProvider != null) {
            return valueProvider.getValue(branch, layerName, key);
        }
        return getDefaultValue();
    }

    private IBranchStyleValueProvider getValueProvider() {
        if (valueProvider == null && !triedLoadingValueProvider) {
            String valueProviderId = element
                    .getAttribute(RegistryConstants.ATT_VALUE_PROVIDER_ID);
            if (valueProviderId != null) {
                valueProvider = manager.getValueProvider(valueProviderId);
            }
            triedLoadingValueProvider = true;
        }
        return valueProvider;
    }
}