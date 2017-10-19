package org.xmind.ui;

/**
 * @deprecated
 */
@Deprecated
public interface IPreSaveInteractiveProvider {

    /**
     * Interactive Type
     */
    /**
     * @deprecated
     */
    @Deprecated
    int TYPE_PRE_SAVE_AS = 1 << 1;
    /**
     * @deprecated
     */
    @Deprecated
    int TYPE_PRE_SAVE = 1 << 2;

    /**
     * Returned Instruction
     */
    /**
     * @deprecated
     */
    @Deprecated
    String INSTRUCTION_PROMOTE = "org.xmind.ui.preSaveInteractiveProvider.instrction.promote"; //$NON-NLS-1$
    /**
     * @deprecated
     */
    @Deprecated
    String INSTRUCTION_END = "org.xmind.ui.preSaveInteractiveProvider.instrction.end"; //$NON-NLS-1$

    /**
     * @deprecated
     */
    @Deprecated
    IPreSaveInteractiveFeedback executeInteractive(Object source,
            int interactiveType);

}
