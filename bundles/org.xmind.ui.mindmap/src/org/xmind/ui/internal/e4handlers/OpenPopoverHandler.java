package org.xmind.ui.internal.e4handlers;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.e4models.IModelConstants;

@SuppressWarnings("restriction")
public class OpenPopoverHandler {

    @Inject
    private EModelService modelService;

    @Inject
    private IEclipseContext context;

    /**
     * Marker only.
     */
    @Execute
    public void run() {
        if (modelService == null || context == null)
            return;

        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .trackView(UserDataConstants.VIEW_MARKER_POPOVER);
        MApplication appModel = context.get(MApplication.class);
        MUIElement markerDirectItem = modelService
                .find(IModelConstants.TOOLITEM_ID_MARKER_POPOVER, appModel);

        if (markerDirectItem instanceof MContribution) {
            MContribution mContri = (MContribution) markerDirectItem;

            IEclipseContext windowContext = modelService
                    .getContainingContext(markerDirectItem);
            IEclipseContext staticContext = EclipseContextFactory
                    .create("MarkerPopover-Static-Context"); //$NON-NLS-1$
            populateModelInterfaces(markerDirectItem, staticContext,
                    markerDirectItem.getClass().getInterfaces());

            Object markerModelObject = mContri.getObject();
            if (markerModelObject == null) {
                mContri.setObject(windowContext.get(IContributionFactory.class)
                        .create(mContri.getContributionURI(), windowContext));
            }

            ContextInjectionFactory.invoke(markerModelObject, Execute.class,
                    windowContext, staticContext, windowContext);
        }
    }

    private static void populateModelInterfaces(Object modelObject,
            IEclipseContext context, Class<?>[] interfaces) {
        for (Class<?> intf : interfaces) {
            context.set(intf.getName(), modelObject);

            populateModelInterfaces(modelObject, context, intf.getInterfaces());
        }
    }

    @CanExecute
    public boolean canExecute(EModelService modelService, MApplication app) {
        List<MPart> editors = modelService.findElements(app, null, MPart.class,
                Arrays.asList(IModelConstants.TAG_EDITOR));
        if (!editors.isEmpty()) {
            return true;
        }

        return false;
    }

}
