package org.xmind.ui.internal.e4handlers;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.osgi.framework.FrameworkUtil;
import org.xmind.ui.internal.e4models.IModelConstants;

public class DialogPartHandler {

    private static final String DIALOG_PREFIX = "DIALOG:"; //$NON-NLS-1$
    private static final String DIALOG_PART_SHARED_LIBRARIES = "org.xmind.ui.dialogPart.sharedLibraries"; //$NON-NLS-1$

    private static final int DEFAULT_DIALOG_Y = 0;
    private static final int DEFAULT_DIALOG_X = 0;
    private static final int DEFAULT_DIALOG_WIDTH = 800;
    private static final int DEFAULT_DIALOG_HEIGHT = 600;

    @Execute
    public void run(EPartService ps, MApplication appli,
            EModelService modelService,
            @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID) String partId,
            @Optional @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PAGE_ID) String pageId) {
        if (partId == null)
            return;

        MPartDescriptor partDescriptor = null;
        for (MPartDescriptor mp : appli.getDescriptors()) {
            if (partId.equals(mp.getElementId())) {
                partDescriptor = mp;
                break;
            }
        }
        if (partDescriptor == null)
            return;

        List<MDialog> existingDialogs = modelService.findElements(appli,
                DIALOG_PREFIX + partId, MDialog.class, null);
        boolean dialogExisted = !existingDialogs.isEmpty();
        MDialog dialogModel = dialogExisted ? existingDialogs.get(0)
                : createDialog(modelService, partDescriptor, partId);
        configDialog(dialogModel, partDescriptor);

        MPart p = ps.findPart(partId);
        if (p == null) {
            p = ps.createPart(partId);
            dialogModel.getChildren().add(p);
            dialogModel.setSelectedElement(p);
        }

        if (!dialogExisted) {
            appli.getChildren().get(0).getWindows().add(dialogModel);
        }

        if (pageId != null) {
            p.getPersistedState().put(
                    IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID, pageId);
        }

        p.setVisible(true);

        modelService.bringToTop(p);
        ps.activate(p, true);

    }

    private MDialog createDialog(EModelService modelService,
            MPartDescriptor partDescriptor, String partId) {
        String contributorURI = "platform:/plugin/" //$NON-NLS-1$
                + FrameworkUtil.getBundle(getClass()).getSymbolicName();

        MDialog dialogModel = modelService.createModelElement(MDialog.class);
        dialogModel.setElementId(DIALOG_PREFIX + partId);
        dialogModel.setLabel(partDescriptor.getLocalizedLabel());
        dialogModel.setContributorURI(contributorURI);

        String dialogStyle = partDescriptor.getPersistedState()
                .get(IPresentationEngine.STYLE_OVERRIDE_KEY);
        dialogModel.getPersistedState()
                .put(IPresentationEngine.STYLE_OVERRIDE_KEY, dialogStyle);
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

    private boolean isNone(String value) {
        return value == null || "".equals(value); //$NON-NLS-1$
    }

    private int getDigitalValue(String value, int defaultValue) {
        return isNone(value) ? defaultValue : Integer.valueOf(value);
    }

    @CanExecute
    public boolean canExecute(MApplication app, EModelService modelService,
            @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID) String partId) {
        if (DIALOG_PART_SHARED_LIBRARIES.equals(partId)) {
            return true;
        }

        List<MPart> editors = modelService.findElements(app, null, MPart.class,
                Arrays.asList(IModelConstants.TAG_EDITOR));
        if (!editors.isEmpty()) {
            return true;
        }

        return false;
    }

}
