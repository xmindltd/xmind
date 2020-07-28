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
package org.xmind.ui.viewers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Frank Shaka
 */
public abstract class ImageCachedLabelProvider extends LabelProvider {

    private Map<Object, Image> map = new HashMap<Object, Image>();

    public void disposeImage(Object element) {
        Image img = unmapImage(element);
        if (img != null) {
            img.dispose();
        }
    }

    protected Image unmapImage(Object element) {
        return map.remove(element);
    }

    public Image getImage(Object element) {
        Image img = map.get(element);
        if (img == null) {
            img = createImage(element);
            if (img != null)
                map.put(element, img);
        }
        return img;
    }

    protected abstract Image createImage(Object element);

    public void dispose() {
        clearCaches();
        super.dispose();
    }

    public void clearCaches() {
        for (Object element : map.keySet().toArray()) {
            disposeImage(element);
        }
        map.clear();
    }

}