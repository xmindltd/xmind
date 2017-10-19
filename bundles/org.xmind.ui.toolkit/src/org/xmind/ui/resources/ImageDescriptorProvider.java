package org.xmind.ui.resources;

import org.eclipse.jface.resource.ImageDescriptor;

public interface ImageDescriptorProvider {

    default ImageDescriptor getImageDescriptor(Object element) {
        return null;
    }

}
