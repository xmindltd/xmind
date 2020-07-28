/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.image.ExportAreaProvider;

/**
 * @author Frank Shaka
 * 
 */
public class MaxPixelsExportAreaProvider extends ExportAreaProvider {

    /**
     * @param sourceArea
     * @param constrainedWidth
     * @param constrainedHeight
     * @param margins
     */
    public MaxPixelsExportAreaProvider(Rectangle sourceArea,
            int constrainedWidth, int constrainedHeight, Insets margins) {
        super(sourceArea, constrainedWidth, constrainedHeight, margins);
    }

    protected void adjustExportArea() {
        exportArea.expand(margins);
        int wHint = constrainedWidth > 0 ? constrainedWidth : exportArea.width;
        int hHint = constrainedHeight > 0 ? constrainedHeight
                : exportArea.height;
        int maxPixels = wHint * hHint;
        int oldPixels = exportArea.width * exportArea.height;
        if (oldPixels > 0 && oldPixels > maxPixels) {
            scale = Math.sqrt(maxPixels * 1.0d / oldPixels);
            exportArea.scale(scale);
        }
    }
}
