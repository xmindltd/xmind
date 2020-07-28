/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.IViewer;

/**
 * 
 * @author frankshaka
 * @deprecated Use org.xmind.ui.toolkit plugin's ImageReference instead
 */
public class ImageRegistryService extends AbstractViewerService implements
        IImageRegistryService {

    private class ImageCache {

        ImageDescriptor descriptor;

        Image image;

        private List<Object> references = null;

        public ImageCache(ImageDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public Image getImage(boolean returnMissingImageOnError,
                Object reference) {
            if (image == null) {
                try {
                    image = descriptor.createImage(returnMissingImageOnError,
                            getDevice());
                } catch (Exception e) {
                    image = getErrorImageCreator().createImage();
                } catch (SWTError e) {
                    image = getErrorImageCreator().createImage();
                }
            }
            if (reference != null) {
                if (references == null)
                    references = new ArrayList<Object>();
                references.add(reference);
            }
            return image;
        }

        public void decreaseRef(Object reference) {
            if (references != null && reference != null) {
                references.remove(reference);
                if (!hasReferences()) {
                    dispose();
                }
            }
        }

        public void dispose() {
            if (image != null) {
                image.dispose();
                image = null;
            }
            references = null;
        }

        public boolean hasReferences() {
            return references != null && !references.isEmpty();
        }
    }

    private Map<ImageDescriptor, ImageCache> caches = new HashMap<ImageDescriptor, ImageCache>(
            30);

    private Device device;

    private ImageDescriptor errorImageDescriptor = null;

    public ImageRegistryService(IViewer viewer) {
        super(viewer);
    }

    public ImageRegistryService(IViewer viewer,
            ImageDescriptor errorImageDescriptor) {
        super(viewer);
        setErrorImageDescriptor(errorImageDescriptor);
    }

    public void setErrorImageDescriptor(ImageDescriptor errorImageDescriptor) {
        this.errorImageDescriptor = errorImageDescriptor;
    }

    public ImageDescriptor getErrorImageDescriptor() {
        return errorImageDescriptor;
    }

    protected void activate() {
    }

    protected void deactivate() {
    }

    public void decreaseRef(ImageDescriptor imageDescriptor, Object reference) {
        ImageCache cache = caches.get(imageDescriptor);
        if (cache != null) {
            cache.decreaseRef(reference);
            if (!cache.hasReferences()) {
                caches.remove(imageDescriptor);
            }
        }
    }

    public Image getImage(ImageDescriptor imageDescriptor,
            boolean returnMissingImageOnError, Object reference) {
        if (imageDescriptor == null) {
            if (!returnMissingImageOnError)
                return null;
            imageDescriptor = getErrorImageCreator();
        }
        ImageCache cache = caches.get(imageDescriptor);
        if (cache == null) {
            cache = new ImageCache(imageDescriptor);
            caches.put(imageDescriptor, cache);
        }
        return cache.getImage(returnMissingImageOnError, reference);
    }

    private ImageDescriptor getErrorImageCreator() {
        if (errorImageDescriptor != null)
            return errorImageDescriptor;
        return ImageDescriptor.getMissingImageDescriptor();
    }

    private Device getDevice() {
        if (device == null) {
            Control control = getControl();
            if (control != null && !control.isDisposed()) {
                device = control.getDisplay();
            }
            if (device == null) {
                device = Display.getCurrent();
            }
        }
        return device;
    }

    public void dispose() {
        for (ImageCache cache : caches.values()) {
            cache.dispose();
        }
        caches.clear();
        super.dispose();
    }

}