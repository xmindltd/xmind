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
package org.xmind.gef.internal.image;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Frank Shaka
 */
class MinRiskColorReplacingPolicy implements IColorReplacingPolicy {
    private static class ColorReplacing {
        RGB source;
        RGB replacement;
        int risk;

        public ColorReplacing(RGB source, RGB replacement, int risk) {
            this.source = source;
            this.replacement = replacement;
            this.risk = risk;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "source=" + source + ",replacement=" + replacement + ",risk=" + risk; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private Map<RGB, ColorReplacing> replacings;
//    private Map<RGB, Map<RGB, Integer>> riskCache;
    private int[][] riskCache;
    private Set<RGB> retainedColors;

    /**
     * @see cn.brainy.gef.image.IColorReplacingPolicy#getReplacingColors(int,
     *      java.util.Map)
     */
    public RGB[] getReplacingColors(int numMaxColors,
            final Map<RGB, Integer> colorOccurrences) {
        /* Sort colors by occurrences */
        Set<RGB> sorter = new TreeSet<RGB>(new Comparator<RGB>() {
            public int compare(RGB o1, RGB o2) {
                int x = colorOccurrences.get(o2) - colorOccurrences.get(o1);
                return x == 0 ? 1 : x;
            }
        });
        sorter.addAll(colorOccurrences.keySet());

        List<RGB> sortedColors = new ArrayList<RGB>(sorter);

        replacings = new HashMap<RGB, ColorReplacing>();

        int t = 0;
        for (int i = 0; i < sortedColors.size(); i++) {
            RGB c1 = sortedColors.get(i);
            ColorReplacing rep = createMinRiskReplacing(c1,
                    sortedColors.subList(0, i));
            replacings.put(c1, rep);
            if ((i >> 10) > t) {
                t = i >> 10;
            }
        }

        Set<RGB> sorter2 = new TreeSet<RGB>(new Comparator<RGB>() {
            /**
             * @see java.util.Comparator#compare(java.lang.Object,
             *      java.lang.Object)
             */
            public int compare(RGB o1, RGB o2) {
                int delta = replacings.get(o2).risk
                        * (int) Math.sqrt(colorOccurrences.get(o2))
                        - replacings.get(o1).risk
                        * (int) Math.sqrt(colorOccurrences.get(o1));
                return delta == 0 ? 1 : delta;
            }
        });
        sorter2.addAll(sortedColors);
        sortedColors = new ArrayList<RGB>(sorter2);
        List<RGB> results = sortedColors.subList(0, numMaxColors);

        for (RGB c : results) {
            replacings.put(c, new ColorReplacing(c, c, 0));
        }

        for (RGB c : sortedColors.subList(numMaxColors, sortedColors.size())) {
            replacings.put(c, createMinRiskReplacing(c, results));
        }

        retainedColors = new HashSet<RGB>(results);
        return results.toArray(new RGB[results.size()]);
    }

    /**
     * @see cn.brainy.gef.image.IColorReplacingPolicy#getReplacedColor(org.eclipse.swt.graphics.RGB)
     */
    public RGB getReplacedColor(RGB source) {
        ColorReplacing replacing = replacings.get(source);
        RGB result = replacing.replacement;
        if (!retainedColors.contains(result)) {
            result = getReplacedColor(result);
            replacing.replacement = result;
        }
        return result;
    }

    public ColorReplacing createMinRiskReplacing(RGB src, List<RGB> colors) {
        RGB resultColor = src;
        int minRisk = Integer.MAX_VALUE;
        for (RGB toTest : colors) {
            int risk = getColorReplacingRisk2(src, toTest);
            if (risk < minRisk) {
                minRisk = risk;
                resultColor = toTest;
            }
        }
        return new ColorReplacing(src, resultColor, minRisk);
    }

    protected int getRisk(RGB src, RGB dest) {
//        Map<RGB, Integer> riskMap = getRiskMap( src );
//        Integer risk = riskMap.get( dest );
//        if ( risk == null ) {
//            risk = getInteger( getColorReplacingRisk( src, dest ) );
//            riskMap.put( dest, risk );
//        }
//        return risk;
        int[] rc = getRiskCache(src);
        int index = dest.hashCode();
        int risk = rc[index];
        if (risk < 0) {
            risk = getColorReplacingRisk2(src, dest);
            rc[index] = risk;
        }
        return risk;
    }

    private int[] getRiskCache(RGB src) {
        int index = src.hashCode();
        int[] rc = getRiskCache()[index];
        if (rc == null) {
            rc = new int[16777215];
            for (int i = 0; i < rc.length; i++) {
                rc[i] = -1;
            }
            getRiskCache()[index] = rc;
        }
        return rc;
    }

    private int[][] getRiskCache() {
        if (riskCache == null)
            riskCache = new int[16777215][];
        return riskCache;
    }

//    private Map<RGB, Integer> getRiskMap( RGB src ) {
//        Map<RGB, Integer> map = getRiskCache().get( src );
//        if ( map == null ) {
//            map = new HashMap<RGB, Integer>();
//            getRiskCache().put( src, map );
//        }
//        return map;
//    }

//    private Map<RGB, Map<RGB, Integer>> getRiskCache() {
//        if ( riskCache == null )
//            riskCache = new HashMap<RGB, Map<RGB, Integer>>();
//        return riskCache;
//    }

    public static int getColorReplacingRisk(RGB c1, RGB c2) {
        int deltaRed = c1.red - c2.red;
        int deltaGreen = c1.green - c2.green;
        int deltaBlue = c1.blue - c2.blue;
        return deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue
                * deltaBlue;
    }

    public static int getColorReplacingRisk2(RGB c1, RGB c2) {
        int deltaRed = c1.red - c2.red;
        int deltaGreen = c1.green - c2.green;
        int deltaBlue = c1.blue - c2.blue;
        return Math.abs(deltaRed) + Math.abs(deltaGreen) + Math.abs(deltaBlue);
    }

}