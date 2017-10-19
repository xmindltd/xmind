package org.xmind.core.internal;

import org.xmind.core.marker.IMarkerVariation;

public class MarkerVariation implements IMarkerVariation {

    private String suffix;

    private int applicableWidth;

    private int applicableHeight;

    public MarkerVariation(String suffix, int applicableWidth,
            int applicableHeight) {
        super();
        this.suffix = suffix;
        this.applicableWidth = applicableWidth;
        this.applicableHeight = applicableHeight;
    }

    public String getVariedPath(String path) {
        int extLength = 0;
        if (path.endsWith(".png") || path.endsWith(".jpg") //$NON-NLS-1$ //$NON-NLS-2$
                || path.endsWith(".gif") || path.endsWith("bmp")) { //$NON-NLS-1$//$NON-NLS-2$
            extLength = 4;
        } else if (path.endsWith(".jpeg")) { //$NON-NLS-1$
            extLength = 5;
        }
        if (extLength > 0)
            return path.substring(0, path.length() - extLength) + suffix
                    + path.substring(path.length() - extLength);
        return path + suffix + path.substring(path.length() - extLength);
    }

    public boolean isApplicable(int widthHint, int heightHint) {
        return widthHint <= applicableWidth && heightHint <= applicableHeight;
    }

}
