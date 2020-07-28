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
package org.xmind.ui.internal.handlers;

/**
 * @author Frank Shaka
 */
public interface IMindMapCommandConstants {

    /*
     * ======================= Command Ids =========================
     */

    /**
     * Id for command "Apply" in category "Style" (value is
     * <code>org.xmind.ui.command.style.apply</code>).
     */
    public static final String STYLE_APPLY = "org.xmind.ui.command.style.apply"; //$NON-NLS-1$

    /**
     * Id for command "Rename" in category "Style" (value is
     * <code>org.xmind.ui.command.style.rename</code>).
     */
    public static final String STYLE_RENAME = "org.xmind.ui.command.style.rename"; //$NON-NLS-1$

    /*
     * ===================== Command Parameters ====================
     */

    /**
     * Id for parameter "Style URI" or "Marker URI" (value is
     * <code>org.xmind.ui.resource.uri</code>).
     */
    public static final String RESOURCE_URI = "org.xmind.ui.resource.uri"; //$NON-NLS-1$

    /*
     * 
     */

    /**
     * 
     */
    public static final String SOURCE_VIEWER = "org.xmind.ui.command.sourceViewer"; //$NON-NLS-1$

}
