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

public interface IMiniBarContributor extends IAdaptable {

    void init(IMiniBar bar, IGraphicalEditor editor);

    void dispose();

    /**
     * 
     * @param page
     * @deprecated Use
     *             {@link IGraphicalEditor#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)}
     *             to listen to active page change events.
     */
    void setActivePage(IGraphicalEditorPage page);

}