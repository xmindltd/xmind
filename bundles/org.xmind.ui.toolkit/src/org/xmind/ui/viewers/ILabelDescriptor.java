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
/**
 * 
 */
package org.xmind.ui.viewers;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A lightweight label provider.
 * 
 * @author Frank Shaka
 */
public interface ILabelDescriptor {

    /**
     * Returns the text representation of the given element.
     * 
     * @param element
     * @return a {@link String} representing the given element
     */
    String getText(Object element);

    /**
     * Returns the image representation of the given element.
     * 
     * @param element
     * @return an {@link ImageDescriptor}, or <code>null</code>
     */
    ImageDescriptor getImage(Object element);

    /**
     * Returns the foreground color representation of the given element.
     * 
     * @param element
     * @return a {@link ColorDescriptor}, or <code>null</code>
     */
    ColorDescriptor getForeground(Object element);

    /**
     * Returns the background color representation of the given element.
     * 
     * @param element
     * @return a {@link ColorDescriptor}, or <code>null</code>
     */
    ColorDescriptor getBackground(Object element);

    /**
     * Returns the font representation of the given element.
     * 
     * @param element
     * @return a {@link FontDescriptor}, or <code>null</code>
     */
    FontDescriptor getFont(Object element);

}
