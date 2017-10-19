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
package org.xmind.ui.mindmap;

import org.eclipse.jface.action.IAction;

public interface IProtocolManager {

    IProtocolDescriptor findProtocolDescriptor(String uri);

    /**
     * 
     * @param uri
     * @return
     */
    IProtocol findProtocol(String uri);

    /**
     * 
     * @param context
     * @param uri
     * @return
     */
    IAction createOpenHyperlinkAction(Object context, String uri);

    /**
     * 
     * @param context
     * @param uri
     * @return
     */
    boolean isHyperlinkModifiable(Object context, String uri);

}