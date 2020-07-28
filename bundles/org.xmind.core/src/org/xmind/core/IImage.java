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
package org.xmind.core;

public interface IImage extends IAdaptable, ITopicComponent {

    String TOP = "top"; //$NON-NLS-1$

    String LEFT = "left"; //$NON-NLS-1$

    String BOTTOM = "bottom"; //$NON-NLS-1$

    String RIGHT = "right"; //$NON-NLS-1$

    int UNSPECIFIED = -1;

    String getSource();

    String getAlignment();

    int getWidth();

    int getHeight();

    void setSource(String source);

    void setAlignment(String alignment);

    void setSize(int width, int height);

    void setWidth(int width);

    void setHeight(int height);

}