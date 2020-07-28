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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.ShadowFigure;

public class ShadowLayer extends BaseLayer {

    private Map<Object, IFigure> shadows = new HashMap<Object, IFigure>();

    public void add(IFigure child, Object constraint, int index) {
        super.add(child, constraint, index);
        if (constraint != null)
            shadows.put(constraint, child);
    }

    public IFigure addShadow(IFigure source) {
        return addShadow(source, 0x24);
    }

    public IFigure addShadow(IFigure source, int alpha) {
        removeShadow(source);
        ShadowFigure shadow = new ShadowFigure();
        shadow.setSource(source);
        shadow.setAlpha(alpha);
        shadow.setOffset(4, 4);
        add(shadow, source);
        return shadow;
    }

    public void removeShadow(IFigure key) {
        IFigure shadow = shadows.remove(key);
        if (shadow != null) {
            if (shadow instanceof ShadowFigure) {
                ((ShadowFigure) shadow).setSource(null);
            }
            remove(shadow);
        }
    }

    public IFigure getShadow(IFigure key) {
        return shadows.get(key);
    }

}