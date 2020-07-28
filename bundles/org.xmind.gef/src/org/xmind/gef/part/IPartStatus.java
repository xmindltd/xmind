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
package org.xmind.gef.part;

import org.xmind.gef.status.IStatusMachine;

/**
 * @author Brian Sun
 */
public interface IPartStatus extends IStatusMachine {

    void activate();

    void deactivate();

    boolean isActive();

    void select();

    void deSelect();

    boolean isSelected();

    void preSelect();

    void dePreSelect();

    boolean isPreSelected();

    void setFocus();

    void lostFocus();

    boolean isFocused();

}