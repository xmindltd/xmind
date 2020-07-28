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
package org.xmind.gef.draw2d;

public interface IUseTransparency {

    // Transparency
    public static final int DONT_USE_ALPHA = -1;

    /**
     * @return the alpha
     */
    public abstract int getMainAlpha();

    /**
     * @param alpha
     *            the alpha to set
     */
    public abstract void setMainAlpha(int alpha);

    /**
     * @return the childrenAlpha
     */
    public abstract int getSubAlpha();

    /**
     * @param alpha
     *            the childrenAlpha to set
     */
    public abstract void setSubAlpha(int alpha);

}