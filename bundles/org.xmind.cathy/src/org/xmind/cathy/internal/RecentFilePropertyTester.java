package org.xmind.cathy.internal;

import java.net.URI;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.editor.IEditorHistory;

public class RecentFilePropertyTester extends PropertyTester {

    private static final String P_ISPINNED = "isPinned"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        Assert.isLegal(receiver instanceof URI);
        IEditorHistory editorHistory = PlatformUI.getWorkbench()
                .getService(IEditorHistory.class);
        Assert.isLegal(editorHistory != null);
        return testTag(editorHistory, (URI) receiver, property, expectedValue);
    }

    private boolean testTag(IEditorHistory editorHistory, URI receiver,
            String property, Object expectedValue) {
        if (P_ISPINNED.equals(property)) {
            boolean actualValue = editorHistory.isPinned(receiver);
            if (expectedValue == null || "".equals(expectedValue)) { //$NON-NLS-1$
                return actualValue;
            } else if (expectedValue instanceof Boolean) {
                return ((Boolean) expectedValue) == actualValue;
            } else if (expectedValue instanceof String) {
                return Boolean
                        .parseBoolean((String) expectedValue) == actualValue;
            }
        }
        Assert.isLegal(false, "Unrecognized value: " + expectedValue); //$NON-NLS-1$
        return false;
    }

}
