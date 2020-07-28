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
package org.xmind.ui.internal.workbench;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

public class Util {

    private Util() {
    }

    public static Point getInitialWindowSize() {
        Display display = Display.getCurrent();
        Rectangle clientArea = getClientArea(display);
        return new Point(Math.min(clientArea.width, 1300),
                Math.min(clientArea.height, 800));
    }

    public static Rectangle getClientArea(Display display) {
        Monitor monitor = display.getPrimaryMonitor();
        if (monitor != null) {
            return monitor.getClientArea();
        }
        return display.getClientArea();
    }

}