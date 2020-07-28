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

import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.ui.internal.mindmap.SheetPart;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IViewerModel;
import org.xmind.ui.util.Logger;

public class MindMapPartFactory implements IPartFactory {

    public MindMapPartFactory() {
    }

    public IPart createPart(IPart parent, Object model) {
        IPart p = null;
        if (model instanceof IViewerModel) {
            p = createSpecificPart(parent,
                    ((IViewerModel) model).getPartType(), model);
        } else if (model instanceof IMindMap) {
            p = new SheetPart();
            p.setModel(model);
        }
        return p;
    }

    protected IPart createSpecificPart(IPart parent,
            Class<? extends IPart> partType, Object model) {
        try {
            IPart part = (IPart) partType.newInstance();
            part.setModel(model);
            return part;
        } catch (Exception e1) {
            Logger
                    .log(
                            e1,
                            "Unknown part type when creating part for: " + model); //$NON-NLS-1$
        }
        return null;
    }

}