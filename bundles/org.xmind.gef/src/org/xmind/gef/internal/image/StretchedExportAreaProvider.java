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

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.image.ExportAreaProvider;

public class StretchedExportAreaProvider extends ExportAreaProvider {

    public StretchedExportAreaProvider(Rectangle sourceArea,
            int constrainedWidth, int constrainedHeight, Insets margins) {
        super(sourceArea, constrainedWidth, constrainedHeight, margins);
    }

    protected void adjustExportArea() {
        int width = super.constrainedWidth;
        int height = super.constrainedHeight;
        if (width >= 0 && height >= 0) {
            if (width > 0 && height > 0) {
                if (exportArea.width > 0 && exportArea.height > 0) {
                    double contentWidth = width - margins.getWidth();
                    double contentHeight = height - margins.getHeight();
                    if (contentWidth < 0) {
                        contentWidth = 0;
                        margins.left = margins.right = 0;
                    }
                    if (contentHeight < 0) {
                        contentHeight = 0;
                        margins.top = margins.bottom = 0;
                    }
                    double sx = contentWidth / exportArea.width;
                    double sy = contentHeight / exportArea.height;
                    if (sx > sy) {
                        scale = sy;
                        exportArea.scale(scale).expand(margins);
                        exportArea.x -= (width - exportArea.width) / 2;
                    } else {
                        scale = sx;
                        exportArea.scale(scale).expand(margins);
                        exportArea.y -= (height - exportArea.height) / 2;
                    }
                }
            } else {
                scale = 0;
                margins.top = margins.left = margins.bottom = margins.right = 0;
                exportArea.setLocation(0, 0).setSize(0, 0);
            }
        } else if (width >= 0) {
            if (exportArea.width > 0) {
                double contentWidth = width - margins.getWidth();
                if (contentWidth < 0) {
                    contentWidth = 0;
                    margins.left = margins.right = 0;
                }
                scale = contentWidth / exportArea.width;
                double contentHeight = exportArea.height * scale;
                height = (int) Math.ceil(contentHeight + margins.getHeight());
                exportArea.scale(scale).expand(margins);
                exportArea.x -= (width - exportArea.width) / 2;
            } else {
                scale = 0;
                height = margins.getHeight();
                exportArea.setLocation(0, 0).setSize(0, 0);
            }
        } else if (height >= 0) {
            if (exportArea.height > 0) {
                double contentHeight = height - margins.getHeight();
                if (contentHeight < 0) {
                    contentHeight = 0;
                    margins.top = margins.bottom = 0;
                }
                scale = contentHeight / exportArea.height;
                double contentWidth = exportArea.width * scale;
                width = (int) Math.ceil(contentWidth + margins.getWidth());
                exportArea.scale(scale).expand(margins);
                exportArea.y -= (height - exportArea.height) / 2;
            } else {
                scale = 0;
                width = margins.getWidth();
                exportArea.setLocation(0, 0).setSize(0, 0);
            }
        } else {
            width = exportArea.width + margins.getWidth();
            height = exportArea.height + margins.getHeight();
            exportArea.x -= margins.left;
            exportArea.y -= margins.top;
        }
        exportArea.setSize(width, height);
    }

}