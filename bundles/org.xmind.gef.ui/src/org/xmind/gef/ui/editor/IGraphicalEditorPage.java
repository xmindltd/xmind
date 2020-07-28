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
package org.xmind.gef.ui.editor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IInputProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IDisposable2;
import org.xmind.gef.IGraphicalViewer;

/**
 * @author Brian Sun
 */
public interface IGraphicalEditorPage extends IDisposable2, IAdaptable,
        IInputProvider {

    /**
     * A viewer property to set/get this editor page instance to/from its nested
     * viewer via {@link org.xmind.gef.IViewer#getProperties()}.
     */
    String VIEWER_EDITOR_PAGE = "editorPage"; //$NON-NLS-1$

    String getPageTitle();

    void setPageTitle(String title);

    void updatePageTitle();

    int getIndex();

    void setIndex(int index);

    void init(IGraphicalEditor parent, Object input);

    IGraphicalEditor getParentEditor();

    boolean isActive();

    void setActive(boolean active);

    EditDomain getEditDomain();

    void setEditDomain(EditDomain editDomain);

    IGraphicalViewer getViewer();

    void createPageControl(Composite parent);

    Control getControl();

    void setFocus();

    boolean isFocused();

    ISelectionProvider getSelectionProvider();

    Object getInput();

}