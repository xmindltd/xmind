package org.xmind.cathy.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.ui.IWorkbenchWindow;

public class WindowPropertyTester extends PropertyTester {

    private static final String P_SHOWING_DASHBOARD = "showingDashboard"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        Assert.isLegal(receiver instanceof IWorkbenchWindow);

        IWorkbenchWindow window = (IWorkbenchWindow) receiver;
        MWindow windowModel = window.getService(MWindow.class);
        Assert.isLegal(windowModel != null);

        if (P_SHOWING_DASHBOARD.equals(property)) {
            return testTag(windowModel, ICathyConstants.TAG_SHOW_DASHBOARD,
                    expectedValue);
        }

        Assert.isLegal(false, "Unrecognized property: " + property); //$NON-NLS-1$

        return false;
    }

    private boolean testTag(MWindow windowModel, String tag,
            Object expectedValue) {
        boolean actualValue = windowModel.getTags().contains(tag);
        if (expectedValue == null || "".equals(expectedValue)) { //$NON-NLS-1$
            return actualValue;
        } else if (expectedValue instanceof Boolean) {
            return ((Boolean) expectedValue) == actualValue;
        } else if (expectedValue instanceof String) {
            return Boolean.parseBoolean((String) expectedValue) == actualValue;
        }
        Assert.isLegal(false, "Unrecognized value: " + expectedValue); //$NON-NLS-1$
        return false;
    }

}
