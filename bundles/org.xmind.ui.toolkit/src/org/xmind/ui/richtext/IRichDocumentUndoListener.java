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
package org.xmind.ui.richtext;


/**
 * @author Frank Shaka
 */
public interface IRichDocumentUndoListener {
    
    /**
     * The document is involved in an undo-related change.  Notify listeners 
     * with an event describing the change.
     * 
     * @param event the document undo event that describes the particular notification
     */
    void documentUndoNotification(RichDocumentUndoEvent event);


}