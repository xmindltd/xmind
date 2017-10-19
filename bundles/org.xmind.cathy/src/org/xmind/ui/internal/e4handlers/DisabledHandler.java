package org.xmind.ui.internal.e4handlers;

import java.util.Map;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * A special handler that can never be executed, typically used as a 'disabler'
 * for a command.
 * 
 * @author Frank Shaka
 * @since 3.6.0
 */
public class DisabledHandler implements IElementUpdater {

    /**
     * An element updater delegate that performs the real updating job.
     */
    private IElementUpdater updater;

    /**
     * A boolean indicating that the handler is currently updating an UIElement.
     */
    private boolean updating = false;

    /**
     * Constructs a new instance of this class.
     * 
     * @param updater
     *            an element updater to perform the real updating job, typically
     *            the real handler for this command
     */
    public DisabledHandler(IElementUpdater updater) {
        this.updater = updater;
    }

    public void updateElement(UIElement element, Map parameters) {
        // check if we're currently updating to break infinite recursion
        if (updating)
            return;

        if (updater != null) {
            updating = true;
            updater.updateElement(element, parameters);
            updating = false;
        }
    }

    /**
     * Will always return <code>false</code>.
     * 
     * @return
     */
    @CanExecute
    public boolean canExecute() {
        return false;
    }

    @Execute
    public void execute() {
        // do nothing
    }

}