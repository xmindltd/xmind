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
package org.xmind.ui.internal.actions;

import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.mindmap.MindMapUI;

public class NewFromMoreTemplateAction extends BaseNewFromTemplateAction {

    public NewFromMoreTemplateAction(IWorkbenchWindow window) {
        super(window);
        setId("org.xmind.ui.newFromTemplate"); //$NON-NLS-1$
        setText(MindMapMessages.NewFromTemplate_text);
        setToolTipText(MindMapMessages.NewFromTemplate_toolTip);
        setActionDefinitionId("org.xmind.ui.command.newFromTemplate"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.actions.BaseNewFromTemplateAction#getTemplateStream
     * (org.eclipse.swt.widgets.Shell)
     */
    protected InputStream getTemplateStream(Shell shell) throws Exception {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(
                new String[] { "*" + MindMapUI.FILE_EXT_TEMPLATE }); //$NON-NLS-1$
        dialog.setFilterNames(
                new String[] { DialogMessages.TemplateFilterName });
        String path = dialog.open();
        if (path == null)
            return null;
        return new FileInputStream(path);
    }

}
