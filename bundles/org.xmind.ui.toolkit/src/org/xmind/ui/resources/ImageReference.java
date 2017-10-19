package org.xmind.ui.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Image references are used to cache SWT Image objects to share across the
 * entire application to reduce memory/handle usage, which is useful when a
 * large image is shown in two or more places within one application, e.g. in a
 * duplicated editor.
 * <p>
 * The first call to any image reference's <code>getImage()</code> method will
 * create the underlying Image resource. Any subsequent calls to any image
 * reference's <code>getImage()</code> will re-use the created image to avoid
 * creating duplicated operating system resource.
 * </p>
 * <p>
 * <b>IMPORTANT: It's client's responsibility to dispose an image reference when
 * it's not in service any more, but remember NOT to dispose a referenced image
 * object on your own!</b> An image's resource will be automatically released
 * when all references registered with it are disposed.
 * </p>
 * <p>
 * Note that an image reference relies on an ImageDescriptor object to describe
 * an image to registered with, so make sure two ImageDescriptor objects
 * <i>equals</i> each other if they describe the same image.
 * </p>
 * 
 * @author Frank Shaka &lt;frank@xmind.net&gt;
 */
public class ImageReference {

    /**
     * An image cache holds the cached image and all its references.
     */
    private static class ImageCache {

        private ImageDescriptor descriptor;

        private Device device;

        private boolean returnMissingImageOnError;

        private Image image = null;

        private List<ImageReference> refs = new ArrayList<ImageReference>();

        public ImageCache(ImageDescriptor descriptor,
                boolean returnMissingImageOnError, Device device) {
            this.descriptor = descriptor;
            this.device = device;
            this.returnMissingImageOnError = returnMissingImageOnError;
        }

        public void register(ImageReference ref) {
            synchronized (this) {
                refs.add(ref);
            }
        }

        public void unregister(ImageReference ref) {
            synchronized (this) {
                refs.remove(ref);
                if (refs.isEmpty()) {
                    disposeImage();
                }
            }
        }

        private void disposeImage() {
            Image oldImage = this.image;
            this.image = null;
            if (oldImage != null) {
                oldImage.dispose();
            }
        }

        public synchronized Image getImage() {
            if (image == null || image.isDisposed()) {
                try {
                    image = descriptor.createImage(returnMissingImageOnError,
                            device);
                } catch (SWTException e) {
                    image = null;
                }
            }
            return image;
        }

    }

    /**
     * The global cache registry.
     */
    private static Map<ImageDescriptor, ImageCache> caches = new HashMap<ImageDescriptor, ImageCache>();

    /**
     * The corresponding cache object.
     */
    private ImageCache cache;

    /**
     * The dispose state of this reference.
     */
    private boolean disposed = false;

    /**
     * Create a new image reference and register it with an image described by
     * an ImageDescriptor. This constructor must be called when there's at least
     * one SWT display instance available.
     * 
     * @param descriptor
     *            describes the image to register with
     * @param returnMissingImageOnError
     *            a flag that determines if a default image is returned on error
     * @exception IllegalStateException
     *                if there's no SWT display available
     */
    public ImageReference(ImageDescriptor descriptor,
            boolean returnMissingImageOnError) {
        this(descriptor, returnMissingImageOnError, findAvailableDevice());
    }

    /**
     * Create a new image reference and register it with an image described by
     * an ImageDescriptor.
     * 
     * @param descriptor
     *            describes the image to register with
     * @param returnMissingImageOnError
     *            a flag that determines if a default image is returned on error
     * @param device
     *            the device on which the image will be created
     */
    public ImageReference(ImageDescriptor descriptor,
            boolean returnMissingImageOnError, Device device) {
        this.cache = getImageCache(descriptor, returnMissingImageOnError,
                device);
        this.cache.register(this);
    }

    /**
     * Returns the image, which will be created on the first call.
     * 
     * @return the cached image
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_GRAPHIC_DISPOSED - if the reference has been
     *                disposed</li>
     *                </ul>
     */
    public Image getImage() {
        if (disposed)
            throw new SWTException(SWT.ERROR_GRAPHIC_DISPOSED);
        return cache.getImage();
    }

    /**
     * Returns the object describing the registered image.
     * 
     * @return the image descriptor
     */
    public ImageDescriptor getImageDescriptor() {
        return cache.descriptor;
    }

    /**
     * Determines whether a default image will be returned on error
     * 
     * @return <code>true</code> if a default image will be returned on error,
     *         <code>false</code> otherwise
     */
    public boolean returnsMissingImageOnError() {
        return cache.returnMissingImageOnError;
    }

    /**
     * Returns the device on which the image will be created.
     * 
     * @return the device on which the image will be created
     */
    public Device getDevice() {
        return cache.device;
    }

    /**
     * Returns <code>true</code> if the image reference has been disposed, and
     * <code>false</code> otherwise.
     * <p>
     * This method gets the dispose state for the image reference. When an image
     * reference has been disposed, it is an error to invoke any other method
     * (except {@link #dispose()}) using the image reference.
     * 
     * @return <code>true</code> when the image reference is disposed and
     *         <code>false</code> otherwise
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Unregisters this reference and, if there's no references then registered
     * with the image, the image's related operating system resource will be
     * released. Applications must dispose of all references which they
     * allocate.
     * <p>
     * This method does nothing if the reference is already disposed.
     */
    public void dispose() {
        synchronized (this) {
            if (disposed)
                return;
            disposed = true;
            cache.unregister(this);
        }
    }

    /**
     * Finds or create an image cache for an image reference to register.
     * 
     * @param descriptor
     *            describes the image to register with
     * @param returnMissingImageOnError
     *            a flag that determines if a default image is returned on error
     * @param device
     *            the device on which the image will be created
     * @return an image cache corresponding to the image descriptor
     */
    private synchronized static ImageCache getImageCache(
            ImageDescriptor descriptor, boolean returnMissingImageOnError,
            Device device) {
        ImageCache cache = caches.get(descriptor);
        if (cache == null) {
            cache = new ImageCache(descriptor, returnMissingImageOnError,
                    device);
            caches.put(descriptor, cache);
        }
        return cache;
    }

    /**
     * Find an available device to create image with.
     * 
     * @return a device found to create image with
     * @exception IllegalStateException
     *                if there's no SWT display available
     */
    private static Device findAvailableDevice() {
        Device device = Display.getCurrent();
        if (device == null) {
            device = Display.getDefault();
        }
        if (device == null)
            throw new IllegalStateException("No display available"); //$NON-NLS-1$
        return device;
    }
}
