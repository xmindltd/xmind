/* ******************************************************************************
 * Copyright (c) 2006-2015 XMind Ltd. and others.
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
package org.xmind.ui.viewers;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IListLayout {

    Point computeSize(MListViewer viewer, Composite composite, int wHint,
            int hHint, boolean flushCache);

    void layout(MListViewer viewer, Composite composite, boolean flushCache);

    void itemAdded(MListViewer viewer, Composite composite, Control item);

    void itemRemoved(MListViewer viewer, Composite composite, Control item);

}
