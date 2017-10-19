/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/

package org.xmind.ui.viewers;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * @author Frank Shaka
 * 
 */
public abstract class AbstractSnapSliderContentProvider implements
        ISliderContentProvider {

    protected static final double DEFAULT_TOLERANCE = 0.015;

    private static class SnapData {
        Object value;
        double positiveTolerance;
        double negativeTolerance;

        public SnapData(Object value, double positiveTolerance,
                double negativeTolerance) {
            super();
            this.value = value;
            this.positiveTolerance = positiveTolerance;
            this.negativeTolerance = negativeTolerance;
        }

    }

    private SortedMap<Double, SnapData> snaps = new TreeMap<Double, SnapData>();

    protected void addSnap(double ratio, Object value) {
        addSnap(ratio, value, DEFAULT_TOLERANCE, -DEFAULT_TOLERANCE);
    }

    /**
     * 
     * @param ratio
     * @param value
     * @param positiveTolerance
     *            a positive value
     * @param negativeTolerance
     *            a negative value
     */
    protected void addSnap(double ratio, Object value,
            double positiveTolerance, double negativeTolerance) {
        snaps.put(Double.valueOf(ratio), new SnapData(value, positiveTolerance,
                negativeTolerance));
    }

    protected void addSnap(double ratio, Object value, double tolerance) {
        addSnap(ratio, value, tolerance, -tolerance);
    }

    protected void removeSnap(double ratio) {
        snaps.remove(Double.valueOf(ratio));
    }

    protected void clearSnaps() {
        snaps.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.viewers.ISliderContentProvider#getRatio(java.lang.Object,
     * java.lang.Object)
     */
    public double getRatio(Object input, Object value) {
        Double ratio = checkSnappedRatio(value);
        if (ratio != null)
            return ratio.doubleValue();
        return getOtherRatio(input, value);
    }

    protected Double checkSnappedRatio(Object value) {
        if (value != null) {
            for (Entry<Double, SnapData> entry : snaps.entrySet()) {
                if (value.equals(entry.getValue().value)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    protected abstract double getOtherRatio(Object input, Object value);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.viewers.ISliderContentProvider#getValue(java.lang.Object,
     * double)
     */
    public Object getValue(Object input, double ratio) {
        Object value = checkSnappedValue(ratio);
        if (value != null)
            return value;
        return getOtherValue(input, ratio);
    }

    /**
     * @param input
     * @param value
     * @return
     */
    protected abstract Object getOtherValue(Object input, double ratio);

    /**
     * @param ratio
     * @return
     */
    protected Object checkSnappedValue(double ratio) {
        Iterator<Entry<Double, SnapData>> it = snaps.entrySet().iterator();
        Entry<Double, SnapData> next = it.hasNext() ? it.next() : null;
        if (next == null)
            return null;

        Entry<Double, SnapData> prev = null;
        Entry<Double, SnapData> current = null;
        do {
            prev = current;
            current = next;
            next = it.hasNext() ? it.next() : null;
            if (isSnapped(ratio, current.getKey().doubleValue(), current
                    .getValue().positiveTolerance,
                    current.getValue().negativeTolerance, prev == null ? null
                            : prev.getKey(), next == null ? null : next
                            .getKey())) {
                return current.getValue().value;
            }
        } while (next != null);
        return null;
    }

    /**
     * @param r0
     * @param r1
     * @param r2
     * @return
     */
    private boolean isSnapped(double r, double r0, double t1, double t2,
            Double r1, Double r2) {
        double min = Math.max(r0 + t2, r1 == null ? 0 : r1.doubleValue());
        double max = Math.min(r0 + t1, r2 == null ? 1 : r2.doubleValue());
        return r == r0 || (r > min && r < max);
    }

}
