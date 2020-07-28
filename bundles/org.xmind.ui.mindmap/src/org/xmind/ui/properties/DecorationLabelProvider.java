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
package org.xmind.ui.properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageDescriptorProvider;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

public class DecorationLabelProvider extends ImageCachedLabelProvider
        implements ImageDescriptorProvider {

    public String getText(Object element) {
        IDecorationDescriptor descriptor;
        if (element instanceof IDecorationDescriptor) {
            descriptor = (IDecorationDescriptor) element;
        } else if (element instanceof String) {
            descriptor = MindMapUI.getDecorationManager()
                    .getDecorationDescriptor((String) element);
        } else {
            descriptor = null;
        }
        return descriptor != null ? descriptor.getName()
                : super.getText(element);
    }

    protected Image createImage(Object element) {
        IDecorationDescriptor descriptor;
        if (element instanceof IDecorationDescriptor) {
            descriptor = (IDecorationDescriptor) element;
        } else if (element instanceof String) {
            descriptor = MindMapUI.getDecorationManager()
                    .getDecorationDescriptor((String) element);
        } else {
            descriptor = null;
        }
        if (descriptor != null) {
            ImageDescriptor icon = descriptor.getIcon();
            if (icon != null)
                return icon.createImage(false);
        }
        return null;
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object element) {
        IDecorationDescriptor descriptor;
        if (element instanceof IDecorationDescriptor) {
            descriptor = (IDecorationDescriptor) element;
        } else if (element instanceof String) {
            descriptor = MindMapUI.getDecorationManager()
                    .getDecorationDescriptor((String) element);
        } else {
            descriptor = null;
        }
        if (descriptor != null) {
            return descriptor.getIcon();
        }
        return null;
    }
}
