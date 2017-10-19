package org.xmind.cathy.internal;

import org.eclipse.jface.internal.InternalPolicy;

@SuppressWarnings("restriction")
public class ConstantsHacker {

    private ConstantsHacker() {
    }

    public static void hack() {
        org.eclipse.ui.internal.WorkbenchMessages.WizardHandler_menuLabel = WorkbenchMessages.ConstantsHacker_WizardHandler_menuLabel;

        // Enable loading ***@2x.png by URLImageDescriptor
        InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x = true;
    }

}
