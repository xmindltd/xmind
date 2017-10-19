package org.xmind.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;

public class ShareOptionRegistryPropertyTester extends PropertyTester {

    private static final String PROP_HAS_OPTIONS = "hasOptions"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (PROP_HAS_OPTIONS.equals(property)) {
            return MindMapUIPlugin.getDefault().getShareOptionRegistry()
                    .hasOptions();
        }

        Assert.isLegal(false, "Unrecognized property: " + property); //$NON-NLS-1$

        return false;
    }

}
