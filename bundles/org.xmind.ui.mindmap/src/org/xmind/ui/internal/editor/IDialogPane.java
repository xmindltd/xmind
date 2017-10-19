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

package org.xmind.ui.internal.editor;

import org.eclipse.ui.part.IPage;

/**
 * @author Frank Shaka
 * 
 */
public interface IDialogPane extends IPage {

    /**
     * Standard return code constant (value 0) indicating that the window was
     * opened.
     * 
     * @see #open
     */
    public static final int OK = 0;

    /**
     * Standard return code constant (value 1) indicating that the window was
     * canceled.
     * 
     * @see #open
     */
    public static final int CANCEL = 1;

    int getReturnCode();

    void setReturnCode(int code);

    void init(IDialogPaneContainer paneContainer);

    void setFocus();

}
