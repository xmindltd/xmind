package org.xmind.ui.wizards;

import java.net.URI;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface ISaveWizard {

    public static class SaveWizardNotAvailable extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = -4861252992795175238L;

        public SaveWizardNotAvailable() {
        }

        public SaveWizardNotAvailable(String message) {
            super(message);
        }

    }

    URI askForTargetURI(ISaveContext context, SaveOptions options)
            throws SaveWizardNotAvailable;

    /**
     *
     * @param context
     * @param options
     * @return >=0, or <0 = not available
     */
    int getPriorityFor(ISaveContext context, SaveOptions options);

}
