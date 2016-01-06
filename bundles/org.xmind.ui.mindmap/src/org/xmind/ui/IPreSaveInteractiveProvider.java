package org.xmind.ui;

public interface IPreSaveInteractiveProvider {

    /**
     * Interactive Type
     */
    int TYPE_PRE_SAVE_AS = 1 << 1;
    int TYPE_PRE_SAVE = 1 << 2;

    /**
     * Returned Instruction
     */
    String INSTRUCTION_PROMOTE = "org.xmind.ui.preSaveInteractiveProvider.instrction.promote"; //$NON-NLS-1$
    String INSTRUCTION_END = "org.xmind.ui.preSaveInteractiveProvider.instrction.end"; //$NON-NLS-1$

    IPreSaveInteractiveFeedback executeInteractive(Object source,
            int interactiveType);

}
