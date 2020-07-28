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

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.gef.ui.editor.IGraphicalEditor;

public interface IPropertyPagePart extends IPropertyPartContainer {

    void init(IPropertyPartContainer container, IGraphicalEditor editor);

    void createControl(Composite parent);

    Control getControl();

    String getTitle();

    void dispose();

    void setSelection(ISelection selection);

    void setFocus();

    List<IPropertySectionPart> getSections();

}