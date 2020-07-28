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
package org.xmind.core.style;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;

public interface IStyleSheet extends IAdaptable {

    /**
     * Group name for normal styles.
     */
    String NORMAL_STYLES = "normal-styles"; //$NON-NLS-1$

    /**
     * Group name for master styles.
     */
    String MASTER_STYLES = "master-styles"; //$NON-NLS-1$

    /**
     * Group name for automatic styles.
     */
    String AUTOMATIC_STYLES = "automatic-styles"; //$NON-NLS-1$

    /**
     * 
     * @param styleId
     * @return
     */
    IStyle findStyle(String styleId);

    /**
     * 
     * @return
     */
    Set<IStyle> getAllStyles();

    /**
     * 
     * @param groupName
     * @return
     */
    Set<IStyle> getStyles(String groupName);

    /**
     * 
     * @param style
     * @param groupName
     */
    void addStyle(IStyle style, String groupName);

    /**
     * 
     * @param style
     * @return
     */
    String findOwnedGroup(IStyle style);

    /**
     * 
     * @param style
     */
    void removeStyle(IStyle style);

    /**
     * 
     * @return
     */
    IStyleSheet getParentSheet();

    /**
     * 
     * @param parent
     */
    void setParentSheet(IStyleSheet parent);

    /**
     * 
     * @param type
     * @return
     */
    IStyle createStyle(String type);

    /**
     * 
     * @return
     */
    boolean isEmpty();

    /**
     * 
     * @param style
     * @return
     */
    IStyle importStyle(IStyle style);

    /**
     * 
     * 
     * @param out
     * @throws IOException
     * @throws CoreException
     * @deprecated Save with owner workbook
     */
    void save(OutputStream out) throws IOException, CoreException;

}