package org.xmind.cathy.internal.dashboard;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.MindMapUI;

public class TemplatePropertyTester extends PropertyTester {

    private static final String PROP_SYSTEM = "system"; //$NON-NLS-1$

    private static final String PROP_USER = "user"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        Assert.isLegal(receiver instanceof ITemplate,
                "Receiver is not an ITemplate object: " + receiver); //$NON-NLS-1$

        ITemplate template = (ITemplate) receiver;

        if (PROP_SYSTEM.equals(property)) {
            return MindMapUI.getResourceManager().isSystemTemplate(template);
        } else if (PROP_USER.equals(property)) {
            return MindMapUI.getResourceManager().isUserTemplate(template);
        }

        Assert.isLegal(false, "Unrecognized property: " + property); //$NON-NLS-1$

        return false;
    }

}
