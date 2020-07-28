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
package org.xmind.ui.internal.mindmap;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.IViewer;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.TopicContextService;

/**
 * @author Karelun Huang
 * 
 */
public class MindMapTopicContextService extends TopicContextService {

    private IGraphicalEditorPage page;

    /**
     * @param viewer
     */
    public MindMapTopicContextService(IGraphicalEditorPage page,
            IViewer viewer) {
        super(viewer);
        this.page = page;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.TopicContextService#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ISelectionProvider.class) {
            return page.getParentEditor().getSite().getSelectionProvider();
        }
        if (adapter == Shell.class)
            return page.getControl().getShell();
        if (adapter == Control.class)
            return getViewer().getControl();
        Object o = getViewer().getAdapter(adapter);
        if (o != null)
            return o;
        o = page.getAdapter(adapter);
        if (o != null)
            return o;
        o = page.getParentEditor().getAdapter(adapter);
        if (o != null)
            return o;
        return null;
    }

}
