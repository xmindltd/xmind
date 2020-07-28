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
package org.xmind.ui.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.RGB;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerVariation;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGReference;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageUtils;

public class MarkerImageDescriptor extends ImageDescriptor {
    public static final String RESOURCE_URL_PREFIX = "platform:/plugin/org.xmind.ui.resources/markers/"; //$NON-NLS-1$
    private static ImageDescriptor ErrorImage = null;

    private IMarker marker;

    private IMarkerRef markerRef;

    private String markerId;

    private RGB background;

    private boolean createByStream = true;

    private int maxWidth;

    private int maxHeigh;

    protected MarkerImageDescriptor(IMarker marker, int maxWidth,
            int maxHeigh) {
        this.marker = marker;
        this.markerRef = null;
        this.markerId = marker.getId();
        this.maxWidth = maxWidth;
        this.maxHeigh = maxHeigh;
    }

    protected MarkerImageDescriptor(IMarkerRef markerRef, int maxWidth,
            int maxHeigh) {
        this.marker = null;
        this.markerRef = markerRef;
        this.markerId = markerRef.getMarkerId();
        this.maxWidth = maxWidth;
        this.maxHeigh = maxHeigh;
    }

    @Override
    public Image createImage(boolean returnMissingImageOnError, Device device) {
        Image image = null;
        try {
            image = new Image(device, new ImageDataProvider() {
                @Override
                public ImageData getImageData(int zoom) {
                    return MarkerImageDescriptor.this.getImageData(zoom);
                }
            });
        } catch (SWTException e) {
            if (e.code != SWT.ERROR_INVALID_IMAGE) {
                throw e;
            }
        } catch (IllegalArgumentException e) {
            // fall through
        }
        if (image == null && returnMissingImageOnError) {
            try {
                image = new Image(device, DEFAULT_IMAGE_DATA);
            } catch (SWTException nextException) {
                return null;
            }
        }
        return image;
    }

    @Override
    public ImageData getImageData() {

        String svgPath = getMarker() == null ? null : getMarker().getSVGPath();

        boolean createImageDataByStream = createByStream
                || (svgPath == null || "".equals(svgPath)); //$NON-NLS-1$

        if (createImageDataByStream)
            return createImageDataByStream(100);
        else {
            return createImageDataBySVG(100);
        }
    }

    private ImageData getImageData(int zoom) {
        if (zoom > 100) {
            String svgPath = getMarker() == null ? null
                    : getMarker().getSVGPath();

            boolean createImageDataByStream = createByStream
                    || (svgPath == null || "".equals(svgPath)); //$NON-NLS-1$

            if (createImageDataByStream)
                return createImageDataByStream(200);
            else {
                return createImageDataBySVG(200);
            }
        } else {
            return getImageData();
        }
    }

    private ImageData createImageDataBySVG(int zoom) {
        int width = zoom / 100 * maxWidth;
        int height = zoom / 100 * maxHeigh;
        String filePath = RESOURCE_URL_PREFIX + getMarker().getSVGPath();
        SVGImageData data = new SVGReference(filePath).getSVGData();
        return data.createImage(new Dimension(width, height), background);
    }

    private ImageData createImageDataByStream(int zoom) {
        InputStream in = getStream(zoom);
        ImageData result = null;
        if (in != null) {
            try {
                result = new ImageData(in);
                result = performScale(result);
            } catch (SWTException e) {
                Logger.log(e, "Unable to create image from marker: [" //$NON-NLS-1$
                        + this.markerId + "] " //$NON-NLS-1$
                        + (marker != null ? marker.getResourcePath() : "")); //$NON-NLS-1$
                // if (e.code != SWT.ERROR_INVALID_IMAGE) {
                //  throw e;
                //  // fall through otherwise
                // }
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    //System.err.println(getClass().getName()+".getImageData(): "+
                    //  "Exception while closing InputStream : "+e);
                }
            }
        }
        if (result == null) {
            result = getErrorImage().getImageData();
        }
        return result;
    }

    private ImageData performScale(ImageData result) {
        if (maxWidth >= 0 || maxHeigh >= 0) {
            double hScale = (double) result.width / maxWidth;
            double vScale = (double) result.height / maxHeigh;
            boolean shouldScaleWidth = hScale > 1;
            boolean shouldScaleHeight = vScale > 1;
            if (shouldScaleWidth || shouldScaleHeight) {
                int w, h;
                if (hScale > vScale) {
                    w = maxWidth;
                    h = (int) (result.height / hScale);
                    if (h == 0)
                        h = 1;
                } else {
                    w = (int) (result.width / vScale);
                    h = maxHeigh;
                    if (w == 0)
                        w = 1;
                }
                result = result.scaledTo(w, h);
            }
        }
        return result;
    }

    private IMarker getMarker() {
        if (marker == null) {
            if (markerRef != null) {
                return markerRef.getMarker();
            }
        }
        return marker;
    }

    private InputStream getStream(int zoom) {
        IMarker m = getMarker();
        if (m == null)
            return null;

        IMarkerResource res = m.getResource();
        if (res == null)
            return null;

        IMarkerVariation variation = getMarkerVariation(res);

        InputStream in = null;
        try {
            if (variation == null) {
                in = res.openInputStream(zoom);
            } else {
                in = res.openInputStream(variation, zoom);
                if (in == null)
                    in = res.openInputStream(zoom);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (in == null)
            return null;

        return new BufferedInputStream(in);
    }

    private IMarkerVariation getMarkerVariation(IMarkerResource res) {
        for (IMarkerVariation variation : res.getVariations()) {
            if (variation.isApplicable(maxWidth, maxHeigh)) {
                return variation;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MarkerImageDescriptor))
            return false;
        MarkerImageDescriptor that = (MarkerImageDescriptor) obj;
        if ((this.maxWidth < 0 ? that.maxWidth >= 0
                : this.maxWidth != that.maxWidth)
                || (this.maxHeigh < 0 ? that.maxHeigh >= 0
                        : this.maxHeigh != that.maxHeigh))
            return false;
        IMarker thisMarker = this.getMarker();
        return thisMarker != null && thisMarker.equals(that.getMarker());
    }

    @Override
    public int hashCode() {
        return markerId.hashCode();
    }

    @Override
    public String toString() {
        return "MarkerImageDescriptor(marker=" + markerId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static ImageDescriptor getErrorImage() {
        if (ErrorImage == null) {
            ErrorImage = ImageUtils.createErrorImage(MindMapUI.DEF_MARKER_WIDTH,
                    MindMapUI.DEF_MARKER_HEIGHT);
        }
        return ErrorImage;
    }

    public static ImageDescriptor createFromMarker(IMarker marker) {
        if (marker == null)
            return getErrorImage();
        return new MarkerImageDescriptor(marker, -1, -1);
    }

    public static ImageDescriptor createFromMarkerRef(IMarkerRef markerRef) {
        if (markerRef == null)
            return getErrorImage();
        return new MarkerImageDescriptor(markerRef, -1, -1);
    }

    public static ImageDescriptor createFromMarker(IMarker marker, int maxWidth,
            int maxHeigh) {
        if (marker == null)
            return getErrorImage();
        return new MarkerImageDescriptor(marker, maxWidth, maxHeigh);
    }

    public static ImageDescriptor createFromMarker(IMarker marker, int maxWidth,
            int maxHeigh, RGB background) {
        MarkerImageDescriptor descriptor = (MarkerImageDescriptor) createFromMarker(
                marker, maxWidth, maxHeigh);
        descriptor.setBackground(background);
        return descriptor;
    }

    public static ImageDescriptor createFromMarker(IMarker marker, int maxWidth,
            int maxHeigh, boolean createByStream) {
        if (createByStream)
            return createFromMarker(marker, maxWidth, maxHeigh);

        MarkerImageDescriptor descriptor = (MarkerImageDescriptor) createFromMarker(
                marker, maxWidth, maxHeigh);
        descriptor.setCreateByStream(createByStream);

        return descriptor;
    }

    public static ImageDescriptor createFromMarkerRef(IMarkerRef markerRef,
            int maxWidth, int maxHeigh) {
        if (markerRef == null)
            return getErrorImage();
        return new MarkerImageDescriptor(markerRef, maxWidth, maxHeigh);
    }

    private void setBackground(RGB background) {
        this.background = background;
    }

    private void setCreateByStream(boolean createByStream) {
        this.createByStream = createByStream;
    }

}
