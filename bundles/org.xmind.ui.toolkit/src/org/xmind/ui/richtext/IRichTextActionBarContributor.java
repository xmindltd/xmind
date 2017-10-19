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
package org.xmind.ui.richtext;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;

/**
 * @author Frank Shaka
 */
public interface IRichTextActionBarContributor {

    void init(IRichTextEditViewer viewer);

    void fillToolBar(IToolBarManager toolbar);

    void fillMenu(IMenuManager menu);

    void fillContextMenu(IMenuManager menu);

    void selectionChanged(ISelection selection, boolean enabled);

    void dispose();

}