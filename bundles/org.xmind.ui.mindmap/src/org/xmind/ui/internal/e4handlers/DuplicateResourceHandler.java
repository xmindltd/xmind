
package org.xmind.ui.internal.e4handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;

public class DuplicateResourceHandler {
    private IContextRunnable duplicateRunnable;

    @Execute
    public void execute() {
        if (duplicateRunnable != null) {
            duplicateRunnable.run();
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext context) {
        duplicateRunnable = E4Utils.getContextRunnable(context,
                IModelConstants.KEY_MODEL_PART_DUPLICATE);
        return duplicateRunnable != null && duplicateRunnable
                .canExecute(context, IModelConstants.KEY_MODEL_PART_DUPLICATE);
    }

}
