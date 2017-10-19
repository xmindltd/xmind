
package org.xmind.ui.internal.e4handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.style.IStyle;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.e4models.IModelPartContext;
import org.xmind.ui.internal.e4models.ThemesPart;
import org.xmind.ui.internal.resourcemanager.ResourceManagerDialogPart;
import org.xmind.ui.internal.resourcemanager.ThemeResourceManagerViewer;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.internal.views.CategorizedThemeViewer;
import org.xmind.ui.mindmap.MindMapUI;

public class SetDefaultThemeHandler {

    private IStyle styleToSetDefault;

    @Execute
    public void execute(IEclipseContext context) {
        if (styleToSetDefault != null) {
            MindMapUI.getResourceManager()
                    .setDefaultTheme(styleToSetDefault.getId());
            Runnable refreshRunnable = E4Utils.getContextRunnable(context,
                    IModelConstants.KEY_MODEL_PART_REFRESH_PAGE);
            if (refreshRunnable != null) {
                refreshRunnable.run();
            }
            IEclipseContext parentContext = context.getParent();
            if (parentContext != null) {
                IModelPartContext modelContext = parentContext
                        .get(IModelPartContext.class);
                if (modelContext instanceof ResourceManagerDialogPart) {
                    ResourceManagerDialogPart dialogPart = (ResourceManagerDialogPart) modelContext;
                    ISelectionProvider selectionProvider = dialogPart
                            .getAdapter(ISelectionProvider.class);
                    if (selectionProvider instanceof ThemeResourceManagerViewer)
                        ((ThemeResourceManagerViewer) selectionProvider)
                                .selectDefault();
                }
                if (modelContext instanceof ThemesPart) {
                    ThemesPart themesPart = (ThemesPart) modelContext;
                    ISelectionProvider selectionProvider = themesPart
                            .getAdapter(ISelectionProvider.class);
                    if (selectionProvider instanceof CategorizedThemeViewer) {
                        ((CategorizedThemeViewer) selectionProvider)
                                .selectDefault();
                    }
                }
            }
            styleToSetDefault = null;
        }
    }

    @CanExecute
    public boolean canExecute(IEclipseContext context) {
        Object selection = context.get(IServiceConstants.ACTIVE_SELECTION);
        if (!(selection instanceof IStructuredSelection)
                || ((IStructuredSelection) selection).size() != 1)
            return false;
        Object ele = ((IStructuredSelection) selection).getFirstElement();
        if (!(ele instanceof IStyle))
            return false;
        IStyle style = (IStyle) ele;
        if (!IStyle.THEME.equals(style.getType()))
            return false;
        styleToSetDefault = style;
        return true;
    }

}
