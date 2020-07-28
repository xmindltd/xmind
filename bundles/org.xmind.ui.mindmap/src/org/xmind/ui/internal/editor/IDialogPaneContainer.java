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

package org.xmind.ui.internal.editor;

/**
 * @author Frank Shaka
 * 
 */
public interface IDialogPaneContainer {

    /**
     * Opens the dialog with the given dialog pane.
     * 
     * @param pane
     * @return
     */
    void open(IDialogPane pane);

    /**
     * Closes the currently opened dialog.
     * 
     * @return <code>true</code> if the dialog has been successfully closed.
     */
    boolean close();

    /**
     * Closes the currently opened dialog and set the return code to the given
     * value.
     * 
     * @param returnCode
     */
    void close(int returnCode);

    /**
     * Returns <code>true</code> if this container is showing.
     * 
     * @return
     */
    boolean isOpen();
}
