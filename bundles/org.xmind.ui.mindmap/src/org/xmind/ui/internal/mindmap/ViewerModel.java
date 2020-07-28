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
package org.xmind.ui.internal.mindmap;

import org.eclipse.core.runtime.IAdaptable;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IViewerModel;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class ViewerModel implements IViewerModel {

    private Class<? extends IPart> partType;

    private Object realModel;

    public ViewerModel(Class<? extends IPart> partType, Object realModel) {
        this.partType = partType;
        this.realModel = realModel;
    }

    public Class<? extends IPart> getPartType() {
        return partType;
    }

    public Object getRealModel() {
        return realModel;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof ViewerModel))
            return false;
        ViewerModel that = (ViewerModel) obj;
        return this.partType == that.partType
                && this.realModel.equals(that.realModel);
    }

    public int hashCode() {
        return partType.hashCode() ^ realModel.hashCode();
    }

    public String toString() {
        return realModel + "^" + partType.getSimpleName(); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(realModel.getClass()))
            return realModel;
        if (realModel instanceof IAdaptable)
            return ((IAdaptable) realModel).getAdapter(adapter);
        if (realModel instanceof org.xmind.core.IAdaptable) {
            return ((org.xmind.core.IAdaptable) realModel).getAdapter(adapter);
        }
        return null;
    }

}