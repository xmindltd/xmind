package org.xmind.ui.internal.svgsupport;

import org.eclipse.jface.resource.DeviceResourceDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Pattern;
import org.xmind.gef.draw2d.graphics.GradientPattern;

/**
 * 
 * @author Enki Xiong
 *
 */
public class PatternResourceDescriptor extends DeviceResourceDescriptor {
    private float x1, x2, y1, y2;
    private Color start, end;
    private int startOpacity, endOpacity;

    public PatternResourceDescriptor(float x1, float y1, float x2, float y2,
            int foreOpacity, Color foreground, int backOpacity,
            Color background) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.startOpacity = foreOpacity;
        this.start = foreground;
        this.endOpacity = backOpacity;
        this.end = background;
    }

    @Override
    public Object createResource(Device device) {
        return new GradientPattern(device, x1, y1, x2, y2, start, startOpacity,
                end, endOpacity);
    }

    @Override
    public void destroyResource(Object pattern) {
        if (pattern instanceof Pattern)
            ((Pattern) pattern).dispose();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PatternResourceDescriptor) {
            PatternResourceDescriptor desc = (PatternResourceDescriptor) obj;
            if (this.x1 == desc.x1 && this.x2 == desc.x2 && this.y1 == desc.y1
                    && this.y2 == desc.y2
                    && this.startOpacity == desc.startOpacity
                    && this.endOpacity == desc.endOpacity)
                if (this.start != null && this.start.equals(desc.start)
                        && this.end != null && this.end.equals(desc.end))
                    return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Float.floatToIntBits(x1);
        result = 31 * result + Float.floatToIntBits(x2);
        result = 31 * result + Float.floatToIntBits(y1);
        result = 31 * result + Float.floatToIntBits(y2);
        result = 31 * result + startOpacity;
        result = 31 * result + start.hashCode();
        result = 31 * result + endOpacity;
        result = 31 * result + end.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PattrnResourceDescriptor:(" + " start color:" + start //$NON-NLS-1$ //$NON-NLS-2$
                + " end color:" + end + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
