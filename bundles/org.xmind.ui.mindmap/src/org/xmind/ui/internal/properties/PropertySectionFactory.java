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
package org.xmind.ui.internal.properties;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CLASS;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_NAME;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_ENABLED_WHEN;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.gef.ui.properties.IPropertySectionPart;
import org.xmind.gef.ui.properties.PropertySectionPart;
import org.xmind.ui.util.Logger;

public class PropertySectionFactory {

    private IConfigurationElement element;

    private String id;

    private Expression enablementExpression;

    private String preferredTitle;

    /* package */PropertySectionFactory(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.id = element.getAttribute(ATT_ID);
        this.preferredTitle = element.getAttribute(ATT_NAME);
        if (PropertySectionContributorManager.getClassValue(element, ATT_CLASS) == null) {
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(), 0,
                    "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                    null));
        }
        initializeEnablement();
    }

    private void initializeEnablement() {
        IConfigurationElement[] enabledWhens = element
                .getChildren(TAG_ENABLED_WHEN);
        if (enabledWhens.length == 0)
            return;

        IConfigurationElement[] enablements = enabledWhens[0].getChildren();
        if (enablements.length == 0)
            return;

        try {
            enablementExpression = ExpressionConverter.getDefault().perform(
                    enablements[0]);
        } catch (CoreException e) {
            Logger.log(e, "Failed to convert expression: " + id); //$NON-NLS-1$
        }
    }

    public String getId() {
        return id;
    }

    public boolean isApplicableTo(Object object) {
        if (enablementExpression == null)
            return true;
        try {
            EvaluationResult result = enablementExpression
                    .evaluate(new EvaluationContext(null, object));
            return result == EvaluationResult.TRUE;
        } catch (CoreException e) {
            Logger.log(e, "Evaluation failed on: " + id); //$NON-NLS-1$
        }
        return false;
    }

    public boolean isEnabledOn(Object[] objects) {
        for (Object object : objects) {
            if (!isApplicableTo(object))
                return false;
        }
        return true;
    }

    public IPropertySectionPart createSection() {
        try {
            IPropertySectionPart section = (IPropertySectionPart) element
                    .createExecutableExtension(ATT_CLASS);
            if (section.getTitle() == null
                    && section instanceof PropertySectionPart) {
                ((PropertySectionPart) section).setTitle(preferredTitle);
            }
            return section;
        } catch (CoreException e) {
            Logger.log(e, "Failed to create property section: " + id); //$NON-NLS-1$
        }
        return null;
    }

}