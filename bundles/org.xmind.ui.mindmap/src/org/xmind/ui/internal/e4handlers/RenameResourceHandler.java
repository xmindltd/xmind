
package org.xmind.ui.internal.e4handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;

public class RenameResourceHandler {

    private IContextRunnable renameRunnable;

    @Execute
    public void execute() {
        if (renameRunnable != null) {
            renameRunnable.run();
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext context) {
        renameRunnable = E4Utils.getContextRunnable(context,
                IModelConstants.KEY_MODEL_PART_RENAME);
        return renameRunnable != null && renameRunnable.canExecute(context,
                IModelConstants.KEY_MODEL_PART_RENAME);
    }

}
