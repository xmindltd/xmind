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
package org.xmind.gef.draw2d.graphics;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Pattern;

/**
 * @author Brian Sun
 */
public class GradientPattern extends Pattern {

    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public Color color1;
    public Color color2;
    public int alpha1;
    public int alpha2;

    /**
     * 
     * @param device
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param color1
     * @param alpha1
     * @param color2
     * @param alpha2
     */
    public GradientPattern(Device device, float x1, float y1, float x2,
            float y2, Color color1, int alpha1, Color color2, int alpha2) {
        super(device, GraphicsUtils.isCarbonSnowLeopard() ? (int) x1 : x1,
                GraphicsUtils.isCarbonSnowLeopard() ? (int) y1 : y1,
                GraphicsUtils.isCarbonSnowLeopard() ? (int) x2 : x2,
                GraphicsUtils.isCarbonSnowLeopard() ? (int) y2 : y2, color1,
                alpha1, color2, alpha2);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color1 = color1;
        this.alpha1 = alpha1;
        this.color2 = color2;
        this.alpha2 = alpha2;
    }

}