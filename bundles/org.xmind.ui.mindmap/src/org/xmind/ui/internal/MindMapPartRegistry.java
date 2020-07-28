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
package org.xmind.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.xmind.gef.part.IPart;
import org.xmind.gef.part.PartRegistry;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.IViewerModel;

/**
 * 
 * @author MANGOSOFT
 * @deprecated
 */
public class MindMapPartRegistry extends PartRegistry {

    private Map<Object, Object> realModelToViewerModel = new HashMap<Object, Object>();

    public void register(Object model, IPart p) {
        super.register(model, p);
        if (model instanceof IViewerModel) {
            IViewerModel wm = (IViewerModel) model;
            Class<? extends IPart> partType = wm.getPartType();
            if (isPartSelectable(partType)) {
                model = wm.getRealModel();
                realModelToViewerModel.put(model, wm);
            }
        } else if (model instanceof IMindMap) {
            IMindMap viewerInput = (IMindMap) model;
            Object sheet = viewerInput.getSheet();
            realModelToViewerModel.put(sheet, model);
        }
    }

    public void unregister(Object model, IPart p) {
        if (model instanceof IViewerModel) {
            IViewerModel wm = (IViewerModel) model;
            Class<? extends IPart> partType = wm.getPartType();
            if (isPartSelectable(partType)) {
                model = wm.getRealModel();
                realModelToViewerModel.remove(model);
            }
        } else if (model instanceof IMindMap) {
            IMindMap viewerInput = (IMindMap) model;
            Object sheet = viewerInput.getSheet();
            realModelToViewerModel.remove(sheet);
        }
        super.unregister(model, p);
    }

    public IPart getPartByModel(Object model) {
        IPart part = super.getPartByModel(model);
        if (part == null) {
            model = toViewerModel(model);
            if (model != null) {
                part = super.getPartByModel(model);
            }
        }
        return part;
    }

    private Object toViewerModel(Object model) {
        return realModelToViewerModel.get(model);
    }

    private boolean isPartSelectable(Class<? extends IPart> partType) {
        return ITopicPart.class.isAssignableFrom(partType)
                || IRelationshipPart.class.isAssignableFrom(partType)
                || IBoundaryPart.class.isAssignableFrom(partType)
                //|| ISummaryPart.class.isAssignableFrom(partType)
                || IMarkerPart.class.isAssignableFrom(partType)
                || IImagePart.class.isAssignableFrom(partType);
    }

}