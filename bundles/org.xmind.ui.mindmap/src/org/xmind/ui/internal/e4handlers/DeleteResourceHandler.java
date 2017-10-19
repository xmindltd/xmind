
package org.xmind.ui.internal.e4handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;

public class DeleteResourceHandler {
    private IContextRunnable deleteRunnable;

    @Execute
    public void execute() {
        if (deleteRunnable != null) {
            deleteRunnable.run();
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext context) {
        deleteRunnable = E4Utils.getContextRunnable(context,
                IModelConstants.KEY_MODEL_PART_DELETE);
        return deleteRunnable != null && deleteRunnable.canExecute(context,
                IModelConstants.KEY_MODEL_PART_DELETE);
    }

}
