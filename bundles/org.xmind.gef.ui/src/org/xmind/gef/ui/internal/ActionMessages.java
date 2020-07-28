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
package org.xmind.gef.ui.internal;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {

    public static String UndoText;
    public static String UndoTooltip;
    public static String UndoTextFormat;
    public static String UndoTooltipFormat;
    public static String RedoText;
    public static String RedoTooltip;
    public static String RedoTextFormat;
    public static String RedoTooltipFormat;

    static {
        NLS.initializeMessages(
                "org.xmind.gef.ui.internal.messages", ActionMessages.class); //$NON-NLS-1$
    }

}