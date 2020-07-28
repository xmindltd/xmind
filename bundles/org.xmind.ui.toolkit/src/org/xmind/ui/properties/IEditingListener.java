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

/**
 * An editing listener listens for editing events such as editing finished or
 * canceled.
 * 
 * @author Frank Shaka
 * @see PropertyEditor#addEditingListener(IEditingListener)
 */
public interface IEditingListener {

    /**
     * Notifies that the editing is finished and the current value should be
     * applied to the desired object.
     */
    void editingFinished();

    /**
     * Notifies that the editing is canceled and everything should be rolled
     * back to the original state.
     */
    void editingCanceled();

}
