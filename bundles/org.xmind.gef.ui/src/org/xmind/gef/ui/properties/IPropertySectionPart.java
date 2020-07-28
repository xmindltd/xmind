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
package org.xmind.gef.ui.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.xmind.gef.ui.editor.IGraphicalEditor;

public interface IPropertySectionPart {

    void init(IPropertyPartContainer container, IGraphicalEditor editor);

    /**
     * Clients are allowed to set layout on the parent.
     */
    void createControl(Composite parent);

    void dispose();

    void refresh();

    void setFocus();

    void setSelection(ISelection selection);

    String getTitle();

}