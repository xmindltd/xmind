
package org.xmind.ui.internal.e4handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.xmind.ui.internal.e4models.IContextRunnable;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;

public class EditResourceHandler {
    private IContextRunnable editRunnable;

    @Execute
    public void execute() {
        if (editRunnable != null) {
            editRunnable.run();
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext context) {
        editRunnable = E4Utils.getContextRunnable(context,
                IModelConstants.KEY_MODEL_PART_EDIT);
        return editRunnable != null && editRunnable.canExecute(context,
                IModelConstants.KEY_MODEL_PART_EDIT);
    }

}
