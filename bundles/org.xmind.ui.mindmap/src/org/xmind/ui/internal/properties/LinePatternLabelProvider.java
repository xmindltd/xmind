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
package org.xmind.ui.internal.properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.xmind.ui.resources.ImageDescriptorProvider;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

public class LinePatternLabelProvider extends ImageCachedLabelProvider
        implements ImageDescriptorProvider {

    public String getText(Object element) {
        LinePattern value;
        if (element instanceof LinePattern) {
            value = (LinePattern) element;
        } else if (element instanceof String) {
            value = LinePattern.findByValue((String) element);
        } else {
            value = null;
        }
        if (value != null)
            return value.getName();
        return super.getText(element);
    }

    protected Image createImage(Object element) {
        LinePattern value;
        if (element instanceof LinePattern) {
            value = (LinePattern) element;
        } else if (element instanceof String) {
            value = LinePattern.findByValue((String) element);
        } else {
            value = null;
        }
        if (value != null) {
            ImageDescriptor icon = value.getIcon();
            if (icon != null)
                return icon.createImage(false);
        }
        return null;
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object element) {
        LinePattern value;
        if (element instanceof LinePattern) {
            value = (LinePattern) element;
        } else if (element instanceof String) {
            value = LinePattern.findByValue((String) element);
        } else {
            value = null;
        }
        if (value != null) {
            return value.getIcon();
        }
        return null;
    }

}
