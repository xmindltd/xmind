/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
package org.xmind.gef.ui.editor;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IInteractiveMessage extends IMessageProvider {

    List<IAction> getActions();

}
