package org.xmind.ui;

import java.util.Properties;

public interface IPreSaveInteractiveFeedback {

    String INTERACTIVE_RESULT = "org.xmind.ui.preSaveInteractiveResult"; //$NON-NLS-1$

    /**
     * Custom
     */
    String INTERACTIVE_FEEDBACK_1 = "org.xmind.ui.preSaveInteractive.feedback.1"; //$NON-NLS-1$
    String INTERACTIVE_FEEDBACK_2 = "org.xmind.ui.preSaveInteractive.feedback.2"; //$NON-NLS-1$
    String INTERACTIVE_FEEDBACK_3 = "org.xmind.ui.preSaveInteractive.feedback.3"; //$NON-NLS-1$
    String INTERACTIVE_FEEDBACK_4 = "org.xmind.ui.preSaveInteractive.feedback.4"; //$NON-NLS-1$
    String INTERACTIVE_FEEDBACK_5 = "org.xmind.ui.preSaveInteractive.feedback.5"; //$NON-NLS-1$

    Properties interactiveFeedback();

}
