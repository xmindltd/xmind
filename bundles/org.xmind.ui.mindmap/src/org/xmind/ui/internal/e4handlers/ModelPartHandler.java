
package org.xmind.ui.internal.e4handlers;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.xmind.ui.internal.e4models.IModelConstants;

public class ModelPartHandler {

    @Execute
    public void run(EPartService partService,
            @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PART_ID) String partId,
            @Optional @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PAGE_ID) String pageId,
            @Optional @Named(IModelConstants.KEY_MODEL_PART_COMMAND_PARAMETER_PARTSTACK_ID) String partStackId,
            MApplication appli, EModelService modelService) {
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
        }

        if (pageId != null) {
            p.getPersistedState().put(
                    IModelConstants.KEY_MODEL_PART_CURRENT_PAGE_ID, pageId);
        }
        appli.getContext().set(IModelConstants.KEY_LAST_OPENED_MODEL_PART_ID,
                partId);

        if (!partStackExisted) {
            appli.getChildren().get(0).getChildren().add(p);
        }

        partStack.setVisible(true);
        p.setVisible(true);

        modelService.bringToTop(p);
        partService.activate(p, true);
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
    public boolean canExecute(MApplication app, EModelService modelService) {
        List<MPart> editors = modelService.findElements(app, null, MPart.class,
                Arrays.asList(IModelConstants.TAG_EDITOR));
        if (!editors.isEmpty()) {
            return true;
        }

        return false;
    }
}
