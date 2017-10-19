package org.xmind.ui.internal.editor;

import java.net.URI;
import java.util.regex.Pattern;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xmind.gef.ui.editor.Editable;
import org.xmind.ui.mindmap.IWorkbookRef;

public class WorkbookRefPropertyTester extends PropertyTester {

    private static final String P_URI = "uri"; //$NON-NLS-1$

    private static final String P_URI_SCHEME = "uriScheme"; //$NON-NLS-1$

    private static final String P_EXIST = "exist"; //$NON-NLS-1$

    public WorkbookRefPropertyTester() {
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        Assert.isLegal(receiver instanceof IWorkbookRef);

        IWorkbookRef workbookRef = (IWorkbookRef) receiver;
        if (P_URI.equals(property)) {
            String uriString = workbookRef.getURI().toString();
            if (expectedValue instanceof String
                    && ((String) expectedValue).startsWith("^")) { //$NON-NLS-1$
                return Pattern.matches((String) expectedValue, uriString);
            }
            return testStringValue(uriString, expectedValue);
        } else if (P_URI_SCHEME.equals(property)) {
            URI uri = workbookRef.getURI();
            return testStringValue(uri == null ? null : uri.getScheme(),
                    expectedValue);
        } else if (P_EXIST.equals(property)) {
            boolean exists = ((Editable) workbookRef).exists();
            if (expectedValue == null || expectedValue.equals("") //$NON-NLS-1$
                    || expectedValue.equals(Boolean.TRUE.toString())) {
                return exists;
            } else if (expectedValue.equals(Boolean.FALSE.toString())) {
                return !exists;
            }
        }

        Assert.isTrue(false, "Unrecognized property: " + property); //$NON-NLS-1$

        return false;
    }

    private static boolean testStringValue(String actualValue,
            Object expectedValue) {
        if ("".equals(expectedValue)) //$NON-NLS-1$
            expectedValue = null;
        else if (expectedValue != null)
            expectedValue = expectedValue.toString();
        return actualValue == expectedValue
                || (actualValue != null && actualValue.equals(expectedValue));
    }

}
