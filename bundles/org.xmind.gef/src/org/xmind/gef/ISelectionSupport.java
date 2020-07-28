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
package org.xmind.gef;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.part.IPart;

public interface ISelectionSupport {

    ISelection getModelSelection();

    List<IPart> getPartSelection();

    IPart findSelectablePart(Object element);

    boolean isSelectable(IPart p);

    void setSelection(ISelection selection, boolean reveal);

    void selectSingle(Object element);

    void deselect(Object element);

    void appendSelection(Object element);

    void appendSelection(List<?> elements);

    void deselectAll(List<?> elements);

    void selectAll(List<?> elements);

    void deselectAll();

    void selectAll();

    void refresh();

}