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
package org.xmind.ui.color;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.RGB;

public interface IColorSelection extends ISelection {

    public static final int AUTO = 1;

    public static final int NONE = 2;

    public static final int CUSTOM = 3;

    int getType();

    RGB getColor();

    boolean isAutomatic();

    boolean isNone();

    boolean isCustom();

}