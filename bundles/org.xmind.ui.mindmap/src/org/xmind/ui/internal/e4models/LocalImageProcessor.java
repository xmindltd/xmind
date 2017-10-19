package org.xmind.ui.internal.e4models;

import java.util.Map;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class LocalImageProcessor {

    private static final String CONTRIBUTION_URI = "bundleclass://org.xmind.ui.mindmap/org.xmind.ui.internal.e4models.LocalImageModelPage"; //$NON-NLS-1$

    @Execute
    public void execute(MApplication application, EModelService modelService) {
        MPartDescriptor partDescriptor = null;
        for (MPartDescriptor mp : application.getDescriptors()) {
            if (ImagePart.PART_ID.equals(mp.getElementId())) {
                partDescriptor = mp;
                break;
            }
        }
        if (partDescriptor == null)
            return;

        // Model Pages
        Map<String, String> persistedState = partDescriptor.getPersistedState();
        String lastUris = persistedState
                .get(MultiPageModelPart.PERSISTED_STATE_PAGES_CONTRIBUTIONURI);
        String newUris = (lastUris == null || lastUris.equals("")) //$NON-NLS-1$
                ? CONTRIBUTION_URI : lastUris + "," + CONTRIBUTION_URI;  //$NON-NLS-1$
        persistedState.put(
                MultiPageModelPart.PERSISTED_STATE_PAGES_CONTRIBUTIONURI,
                newUris);
    }

}
