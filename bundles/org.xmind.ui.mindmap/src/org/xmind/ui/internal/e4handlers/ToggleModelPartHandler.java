package org.xmind.ui.internal.e4handlers;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.xmind.ui.internal.e4models.IModelConstants;

public class ToggleModelPartHandler {

    private static final String TAG_EDITOR = "Editor"; //$NON-NLS-1$

    @Inject
    private EModelService modelService;

    @Inject
    private MApplication application;

    @Execute
    public void run(EPartService partService,
            @Optional @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID) String partId,
            @Optional @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PARTSTACK_ID) String partStackId,
            @Optional MToolItem toolItemModel, MApplication appli,
            EModelService modelService) {
        if (partId == null) {
            if (partStackId == null)
                partStackId = "org.xmind.ui.stack.right"; //$NON-NLS-1$

            partId = (String) appli.getContext()
                    .get(IModelConstants.KEY_LAST_OPENED_MODEL_PART_ID);
            if (partId == null) {
                partId = "org.xmind.ui.modelPart.properties"; //$NON-NLS-1$
            }
        }

        MPartDescriptor partDescriptor = null;
        for (MPartDescriptor mp : appli.getDescriptors()) {
            if (partId.equals(mp.getElementId())) {
                partDescriptor = mp;
                break;
            }
        }
        if (partDescriptor == null)
            return;

        List<MPartStack> partStacks = modelService.findElements(appli,
                partStackId, MPartStack.class, null);
        boolean partStackExisted = !partStacks.isEmpty();
        MPartStack partStack = partStackExisted ? partStacks.get(0)
                : createPartStack(modelService, partStackId);

        MPart p = partService.findPart(partId);
        if (p == null) {
            p = partService.createPart(partId);
            partStack.getChildren().add(p);
            partStack.setSelectedElement(p);
            p.setVisible(false);
        }

        if (!partStackExisted) {
            appli.getChildren().get(0).getChildren().add(p);
        }

        if (!p.isVisible()) {
            partStack.setVisible(true);
            p.setVisible(true);
            partService.activate(p, true);
        } else {
            p.setVisible(false);
            partService.hidePart(p);
        }

        appli.getContext().set(IModelConstants.KEY_LAST_OPENED_MODEL_PART_ID,
                partId);

//        boolean toShow = toolItemModel.isSelected();
//        if (toShow) {
//            p.setVisible(true);
//            partService.activate(p, true);
//        } else {
//            p.setVisible(false);
//            partService.hidePart(p);
//        }

    }

    private MPartStack createPartStack(EModelService modelService,
            String partStackId) {
        MPartStack partStack = modelService
                .createModelElement(MPartStack.class);
        partStack.setElementId(partStackId);
        partStack.setVisible(true);
        return partStack;
    }

    @CanExecute
    public boolean canExecute() {
        List<MPart> editors = modelService.findElements(application, null,
                MPart.class, Arrays.asList(TAG_EDITOR));
        if (!editors.isEmpty()) {
            return true;
        }

        return false;
    }

}
