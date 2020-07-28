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
package org.xmind.gef.service;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.gef.IGraphicalViewer;

public abstract class AbstractShadowService extends GraphicalViewerService
        implements IShadowService {

    private IFigure layer;

    private int alpha = 0x30;

    private Dimension offset = new Dimension(7, 7);

    public AbstractShadowService(IGraphicalViewer viewer) {
        super(viewer);
    }

    protected void activate() {
    }

    protected void deactivate() {
    }

    public void addShadow(IFigure source) {
        addShadow(source, alpha, offset);
    }

    public void addShadow(IFigure source, int alpha) {
        addShadow(source, alpha, offset);
    }

    public void addShadow(IFigure source, Dimension offset) {
        addShadow(source, alpha, offset);
    }

    public int getDefaultAlpha() {
        return alpha;
    }

    public Dimension getDefaultOffset() {
        return offset;
    }

    public void setDefaultAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setDefaultOffset(Dimension offset) {
        this.offset.setSize(offset);
    }

    public IFigure getLayer() {
        return layer;
    }

    public void setLayer(IFigure layer) {
        this.layer = layer;
    }

}