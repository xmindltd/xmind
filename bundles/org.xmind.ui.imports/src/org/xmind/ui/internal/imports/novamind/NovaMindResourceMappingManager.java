package org.xmind.ui.internal.imports.novamind;

import org.xmind.core.io.ResourceMappingManager;

/**
 * @author lyn
 */

public class NovaMindResourceMappingManager {

    private static ResourceMappingManager instance = null;

    public static ResourceMappingManager getInstance() {
        if (instance == null) {
            try {
                instance = ResourceMappingManager.createInstance(
                        NovaMindResourceMappingManager.class, "mappings.xml"); //$NON-NLS-1$
            } catch (Exception e) {
                instance = ResourceMappingManager
                        .createEmptyInstance("NovaMind"); //$NON-NLS-1$
            }
        }
        return instance;
    }

}
