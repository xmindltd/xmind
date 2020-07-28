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
package org.xmind.ui.internal.layers;

import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.StackLayout;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.ScalableFreeformLayeredPane;

public class DefaultLayeredPane extends FreeformLayeredPane {

    private ScalableFreeformLayeredPane scalableLayeredPane;

    public DefaultLayeredPane() {
        setLayoutManager(new StackLayout());
        addLayers();
    }

    protected void addLayers() {
        scalableLayeredPane = new ScalableFreeformLayeredPane();
        add(scalableLayeredPane, GEF.LAYERS_SCALABLE);

        addScalableLayers();

        //TODO add other layers
        add(new FeedbackLayer(), GEF.LAYER_FEEDBACK);

    }

    protected void addScalableLayers() {
        addScalableLayer(new BackgroundLayer(), GEF.LAYER_BACKGROUND);
        addScalableLayer(new ShadowLayer(), GEF.LAYER_SHADOW);
        addScalableLayer(new ContentsLayer(), GEF.LAYER_CONTENTS);
        addScalableLayer(new PresentationLayer(), GEF.LAYER_PRESENTATION);
    }

    public void addScalableLayer(IFigure layer, Object key) {
        scalableLayeredPane.add(layer, key);
    }

    public void addScalableLayer(IFigure layer, Object key, int index) {
        scalableLayeredPane.add(layer, key, index);
    }

    public void addLayerBefore(Layer layer, Object key, Object before) {
        if (scalableLayeredPane.getLayer(before) != null) {
            scalableLayeredPane.addLayerBefore(layer, key, before);
            return;
        }
        super.addLayerBefore(layer, key, before);
    }

    public Layer getLayer(Object key) {
        Layer layer = scalableLayeredPane.getLayer(key);
        if (layer != null)
            return layer;
        return super.getLayer(key);
    }

    public void removeLayer(Object key) {
        Layer layer = scalableLayeredPane.getLayer(key);
        if (layer != null) {
            scalableLayeredPane.removeLayer(key);
            return;
        }
        super.removeLayer(key);
    }

    public ScalableFreeformLayeredPane getScalableLayeredPane() {
        return scalableLayeredPane;
    }

}