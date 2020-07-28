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
package org.xmind.gef.ui.actions;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.ActionFactory;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.internal.ActionMessages;

public class RedoAction extends CommandStackAction {

    public RedoAction(IGraphicalEditor editor) {
        super(editor);
        setId(ActionFactory.REDO.getId());
        setActionDefinitionId(ActionFactory.REDO.getCommandId());
    }

    public void run() {
        if (getCommandStack() == null || isDisposed())
            return;

        getCommandStack().redo();
    }

    protected void update() {
        ICommandStack cs = getCommandStack();
        boolean canRedo = cs != null && cs.canRedo();

        setEnabled(canRedo);

        String label = null;
        if (canRedo && cs != null) {
            label = cs.getRedoLabel();
        }

        if (label == null) {
            setText(ActionMessages.RedoText);
            setToolTipText(ActionMessages.RedoTooltip);
        } else {
            setText(NLS.bind(ActionMessages.RedoTextFormat, label));
            setToolTipText(NLS.bind(ActionMessages.RedoTooltipFormat, label));
        }
    }
}