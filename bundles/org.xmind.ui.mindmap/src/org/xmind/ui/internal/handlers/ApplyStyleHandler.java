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
package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * The command handler that sends a 'modify style' request with the first
 * selected style to the current active editor.
 * 
 * @author Frank Shaka
 */
public class ApplyStyleHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        applyStyleToEditor(MindMapHandlerUtil.findStyle(event),
                MindMapHandlerUtil.findContributingEditor(event));
        return null;
    }

    private void applyStyleToEditor(IStyle style, IEditorPart editor) {
        if (style == null || editor == null)
            return;

        IViewer viewer = MindMapUIPlugin.getAdapter(editor, IViewer.class);
        if (viewer == null)
            return;

        EditDomain editDomain = viewer.getEditDomain();
        if (editDomain == null)
            return;

        editDomain.handleRequest(new Request(MindMapUI.REQ_MODIFY_STYLE)
                .setViewer(viewer).setDomain(editDomain)
                .setParameter(MindMapUI.PARAM_RESOURCE, style));
    }

}
