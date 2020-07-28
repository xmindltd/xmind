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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IListRenderer {

    int STATE_NONE = 0;
    int STATE_SELECTED = 1 << 0;

    IListLayout getListLayout(MListViewer viewer);

    Control createListItemForElement(MListViewer viewer, Composite parent,
            Object element);

    void updateListItem(MListViewer viewer, Object element, Control item);

    int getListItemState(MListViewer viewer, Control item);

    void setListItemState(MListViewer viewer, Control item, int state);

}
