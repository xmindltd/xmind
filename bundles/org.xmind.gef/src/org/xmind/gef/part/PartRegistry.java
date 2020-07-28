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
package org.xmind.gef.part;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Brian Sun
 * @author Frank Shaka <frank@xmind.net>
 */
public class PartRegistry {

    private Map<Object, IPart> modelToPart = new HashMap<Object, IPart>();

    private Map<IPart, Object> partToModel = new HashMap<IPart, Object>();

    public void register(Object model, IPart part) {
        if (part == null || model == null)
            return;
        modelToPart.put(model, part);
        partToModel.put(part, model);
    }

    public void unregister(Object model, IPart part) {
        if (part == null || model == null)
            return;
        IPart part2 = modelToPart.get(model);
        if (part2 == part)
            modelToPart.remove(model);
        Object model2 = partToModel.get(part);
        if (model.equals(model2)) {
            partToModel.remove(part);
        }
    }

    public IPart getPartByModel(Object model) {
        return model == null ? null : modelToPart.get(model);
    }

    public Object getModelByPart(IPart part) {
        return part == null ? null : partToModel.get(part);
    }

    public void clear() {
        modelToPart.clear();
        partToModel.clear();
    }

}