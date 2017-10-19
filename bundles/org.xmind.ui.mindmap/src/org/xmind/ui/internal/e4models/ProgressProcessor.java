package org.xmind.ui.internal.e4models;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.framework.FrameworkUtil;

public class ProgressProcessor {

    private static final String DIALOG_PREFIX = "DIALOG:"; //$NON-NLS-1$

    private static final String CUSTOM_LOCATION_KEY = "customLocation"; //$NON-NLS-1$

    private static final int DEFAULT_DIALOG_Y = 0;

    private static final int DEFAULT_DIALOG_X = 0;

    private static final int DEFAULT_DIALOG_WIDTH = 600;

    private static final int DEFAULT_DIALOG_HEIGHT = 700;

    @Execute
    public void execute(EModelService modelService, MApplication application) {
        String partId = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$
        String partStackId = "org.xmind.ui.stack.progress"; //$NON-NLS-1$

        //create dialog model
        MPartDescriptor partDescriptor = null;
        for (MPartDescriptor mp : application.getDescriptors()) {
            if (partId.equals(mp.getElementId())) {
                partDescriptor = mp;
                break;
            }
        }
        if (partDescriptor == null) {
            return;
        }

        List<MDialog> existingDialogs = modelService.findElements(application,
                DIALOG_PREFIX + partId, MDialog.class, null);
        boolean dialogExisted = !existingDialogs.isEmpty();
        MDialog dialogModel = dialogExisted ? existingDialogs.get(0)
                : createDialog(modelService, partDescriptor, partId);
        dialogModel.setToBeRendered(false);
        if (!dialogExisted) {
            application.getChildren().get(0).getWindows().add(dialogModel);
        }

        //create part stack
        List<MPartStack> partStacks = modelService.findElements(dialogModel,
                partStackId, MPartStack.class, null);
        boolean partStackExisted = !partStacks.isEmpty();
        MPartStack partStack = partStackExisted ? partStacks.get(0)
                : createPartStack(modelService, partStackId);
        if (!partStackExisted) {
            dialogModel.getChildren().add(partStack);
        }

        //create part
        List<MPart> parts = modelService.findElements(partStack, partId,
                MPart.class, null);
        boolean partExisted = !parts.isEmpty();
        MPart part = partExisted ? parts.get(0) : null;
        if (part == null) {
            part = modelService.createModelElement(MPart.class);
            part.setElementId(partId);
            part.setContributionURI(partDescriptor.getContributionURI());
            partStack.getChildren().add(part);
            partStack.setSelectedElement(part);
        }

    }

    private MDialog createDialog(EModelService modelService,
            MPartDescriptor partDescriptor, String partId) {
        String contributorURI = "platform:/plugin/" //$NON-NLS-1$
                + FrameworkUtil.getBundle(getClass()).getSymbolicName();

        MDialog dialogModel = modelService.createModelElement(MDialog.class);
        dialogModel.setElementId(DIALOG_PREFIX + partId);
        dialogModel.setLabel(partDescriptor.getLabel());
        dialogModel.setContributorURI(contributorURI);

        String dialogStyle = partDescriptor.getPersistedState()
                .get(IPresentationEngine.STYLE_OVERRIDE_KEY);
        dialogModel.getPersistedState()
                .put(IPresentationEngine.STYLE_OVERRIDE_KEY, dialogStyle);

        configDialog(dialogModel, partDescriptor);

        return dialogModel;
    }

    private void configDialog(MDialog dialogModel,
            MPartDescriptor partDescriptor) {

        String location = dialogModel.getPersistedState()
                .get(IModelConstants.KEY_DIALOG_PART_CUSTOM_LOCATION);
        if (location == null || location.equals("")) { //$NON-NLS-1$
            location = partDescriptor.getPersistedState()
                    .get(IModelConstants.KEY_DIALOG_PART_CUSTOM_LOCATION);
        }
        location = location == null ? "" : location; //$NON-NLS-1$
        String[] locations = location.split(","); //$NON-NLS-1$

        if (locations.length < 4) {
            String[] tempLocations = new String[4];
            for (int i = 0; i < locations.length; i++)
                tempLocations[i] = locations[i];
            locations = tempLocations;
        }

        int dialogX = getDigitalValue(locations[0], DEFAULT_DIALOG_X);
        int dialogY = getDigitalValue(locations[1], DEFAULT_DIALOG_Y);
        int dialogW = getDigitalValue(locations[2], DEFAULT_DIALOG_WIDTH);
        int dialogH = getDigitalValue(locations[3], DEFAULT_DIALOG_HEIGHT);

        dialogModel.setX(dialogX);
        dialogModel.setY(dialogY);
        dialogModel.setWidth(dialogW);
        dialogModel.setHeight(dialogH);

        dialogModel.getPersistedState()
                .put(IModelConstants.KEY_DIALOG_PART_CUSTOM_LOCATION, location);
    }

    private int getDigitalValue(String value, int defaultValue) {
        return isNone(value) ? defaultValue : Integer.valueOf(value);
    }

    private boolean isNone(String value) {
        return value == null || "".equals(value); //$NON-NLS-1$
    }

    private MPartStack createPartStack(EModelService modelService,
            String partStackId) {
        MPartStack partStack = modelService
                .createModelElement(MPartStack.class);
        partStack.setElementId(partStackId);
        partStack.setVisible(true);
        partStack.getTags().add(IModelConstants.TAG_X_STACK);
        return partStack;
    }

}
