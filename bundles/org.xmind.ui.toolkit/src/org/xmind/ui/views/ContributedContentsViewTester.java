package org.xmind.ui.views;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.internal.expressions.Expressions;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;

@SuppressWarnings("restriction")
public class ContributedContentsViewTester extends PropertyTester {

    private static final String P_IS_CONTRIBUTED = "isContributed"; //$NON-NLS-1$

    private static final String P_CONTRIBUTING_PART_ID = "contributingPartId"; //$NON-NLS-1$

    private static final String P_CONTRIBUTING_PART_CAN_ADAPT_TO = "contributingPageCanAdaptTo"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {

        Assert.isLegal(receiver instanceof IWorkbenchPart,
                "Receiver is NOT an instance of 'org.eclipse.ui.IWorkbenchPart': " //$NON-NLS-1$
                        + receiver);

        IWorkbenchPart part = (IWorkbenchPart) receiver;
        IContributedContentsView contributedView = getContributedContentsView(
                part);
        IWorkbenchPart contributingPart = contributedView == null ? null
                : contributedView.getContributingPart();

        if (P_IS_CONTRIBUTED.equals(property)) {
            return testBooleanValue(contributedView != null, expectedValue);
        } else if (P_CONTRIBUTING_PART_ID.equals(property)) {
            return testStringValue(
                    contributingPart == null ? null
                            : contributingPart.getSite().getId(),
                    expectedValue);
        } else if (P_CONTRIBUTING_PART_CAN_ADAPT_TO.equals(property)) {
            return testAdapter(contributedView, expectedValue instanceof String
                    ? (String) expectedValue : null);
        }

        Assert.isLegal(false, "Unrecognized property: " + property); //$NON-NLS-1$

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

    private static boolean testBooleanValue(boolean actualValue,
            Object expectedValue) {
        if (expectedValue == null || "".equals(expectedValue)) //$NON-NLS-1$
            return actualValue;
        if (expectedValue instanceof String)
            return Boolean.parseBoolean((String) expectedValue) == actualValue;
        if (expectedValue instanceof Boolean)
            return ((Boolean) expectedValue).booleanValue() == actualValue;

        Assert.isLegal(false, "Unrecognized expected value: " + expectedValue); //$NON-NLS-1$

        return false;
    }

    private static IContributedContentsView getContributedContentsView(
            IWorkbenchPart part) {
        if (part instanceof IContributedContentsView)
            return (IContributedContentsView) part;

        IContributedContentsView ccv = part
                .getAdapter(IContributedContentsView.class);
        if (ccv != null)
            return ccv;

        return Platform.getAdapterManager().getAdapter(part,
                IContributedContentsView.class);
    }

    private static boolean testAdapter(Object obj, String adapterTypeName) {
        if (obj == null || adapterTypeName == null)
            return false;

        Object adapted = null;
        IAdapterManager manager = Platform.getAdapterManager();
        if (Expressions.isInstanceOf(obj, adapterTypeName)) {
            adapted = obj;
        } else {
            if (manager.hasAdapter(obj, adapterTypeName)) {
                adapted = manager.getAdapter(obj, adapterTypeName);
            } else {
                // if the adapter manager doesn't have an adapter contributed,
                // try to see if the variable itself implements IAdaptable
                if (obj instanceof IAdaptable) {
                    Class<?> typeClazz = loadClass(
                            obj.getClass().getClassLoader(), adapterTypeName);
                    if (typeClazz == null) {
                        return false;
                    }
                    adapted = ((IAdaptable) obj).getAdapter(typeClazz);
                }
                if (adapted == null) {
                    // all attempts failed, return false
                    return false;
                }
            }
        }
        return adapted != null;
    }

    private static Class<?> loadClass(ClassLoader loader, String name) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
    }
}
