/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.wizards;

import org.eclipse.swt.graphics.Image;
import org.xmind.core.INamed;
import org.xmind.core.marker.IMarker;
import org.xmind.ui.util.MarkerImageDescriptor;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

public class MarkerGroupLabelProvider extends ImageCachedLabelProvider {

    public String getText(Object element) {
        if (element instanceof INamed)
            return ((INamed) element).getName();
        return super.getText(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.viewers.ImageCachedLabelProvider#createImage(java.lang.Object
     * )
     */
    protected Image createImage(Object element) {
        if (element instanceof IMarker) {
            try {
                return MarkerImageDescriptor.createFromMarker(
                        (IMarker) element, 16, 16).createImage(false);
            } catch (Throwable e) {
            }
        }
        return null;
    }
}