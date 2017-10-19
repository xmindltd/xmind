package org.xmind.gef.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xmind.gef.command.ICommandStack;

public class CommandStackPropertyTester extends PropertyTester {

    private static final String P_CAN_UNDO = "canUndo"; //$NON-NLS-1$

    private static final String P_CAN_REDO = "canRedo"; //$NON-NLS-1$

    private static final String P_IS_DIRTY = "isDirty"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        Assert.isLegal(receiver instanceof ICommandStack);

        ICommandStack stack = (ICommandStack) receiver;
        if (P_CAN_UNDO.equals(property)) {
            return testBooleanValue(stack.canUndo(), expectedValue);
        } else if (P_CAN_REDO.equals(property)) {
            return testBooleanValue(stack.canRedo(), expectedValue);
        } else if (P_IS_DIRTY.equals(property)) {
            return testBooleanValue(stack.isDirty(), expectedValue);
        }

        Assert.isLegal(false, "Unrecognized property: " + property); //$NON-NLS-1$

        return false;
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

}
