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
package org.xmind.gef.image;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

public class ExportAreaProvider implements IExportAreaProvider {

    protected final Rectangle sourceArea;

    protected final int constrainedWidth;

    protected final int constrainedHeight;

    protected final Insets margins;

    protected Rectangle exportArea = null;

    protected double scale = -1;

    public ExportAreaProvider(Rectangle sourceArea, int constrainedWidth,
            int constrainedHeight, Insets margins) {
        this.sourceArea = sourceArea == null ? null : new Rectangle(sourceArea);
        this.constrainedWidth = constrainedWidth;
        this.constrainedHeight = constrainedHeight;
        this.margins = margins == null ? new Insets() : new Insets(margins);
    }

    public Insets getMargins() {
        return margins;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IExportAreaProvider#getExportArea()
     */
    public Rectangle getExportArea() {
        if (exportArea == null) {
            if (sourceArea != null) {
                exportArea = new Rectangle(sourceArea);
                adjustExportArea();
            } else {
                exportArea = new Rectangle();
            }
        }
        return exportArea;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.image.IExportAreaProvider#getScale()
     */
    public double getScale() {
        getExportArea();
        return scale;
    }

    protected void adjustExportArea() {
        exportArea.expand(margins);
    }

}