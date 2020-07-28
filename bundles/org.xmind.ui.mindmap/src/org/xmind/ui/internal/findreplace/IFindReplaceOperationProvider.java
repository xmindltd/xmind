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
package org.xmind.ui.internal.findreplace;

import org.eclipse.swt.graphics.Font;

/**
 * @author Frank Shaka
 */
public interface IFindReplaceOperationProvider {

    int PARAM_NONE = 0;

    int PARAM_CASE_SENSITIVE = 1;

    int PARAM_WHOLE_WORD = 1 << 1;

    int PARAM_FORWARD = 1 << 2;

    int PARAM_BACKWARD = 1 << 3;

    int PARAM_CURRENT_MAP = 1 << 4;

    int PARAM_WORKBOOK = 1 << 5;

    int PARAM_ALL = 1 << 16;

    String getContextName();

    String getContextName(int maxWidth, Font font);

    /**
     * Finds the next object containing the specified string.
     * 
     * @param toFind
     *            the string to find
     * @return <code>true</code> if this operation ends with success
     */
    boolean find(String toFind);

    /**
     * Replaces the specified string in the next object containing that string
     * with the another string.
     * 
     * @param toFind
     *            the string to find
     * @param toReplaceWith
     *            the string to replace with
     * @return <code>true</code> if this operation ends with success
     */
    boolean replace(String toFind, String toReplaceWith);

    /**
     * Checks whether the 'Find' operation is available with the specified
     * string.
     * 
     * @param toFind
     *            the string to find
     * @return <code>true</code> if 'find' is available
     */
    boolean canFind(String toFind);

    /**
     * Checks whether the 'Replace' operation is available.
     * 
     * @param toFind
     *            the string to find
     * @param toReplaceWith
     *            the string to replace with
     * @return <code>true</code> if 'replace' is available
     */
    boolean canReplace(String toFind, String toReplaceWith);

    boolean canFindAll(String toFind);

    boolean canReplaceAll(String toFind, String toReplaceWith);

    int getParameter();

    void setParameter(int op, boolean value);

    void setParameter(int parameter);

    boolean understandsPatameter(int parameter);

}