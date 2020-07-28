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
/**
 * 
 */
package org.xmind.ui.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * @author Frank Shaka
 */
public class DeleteStyleHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        deleteSelectedStyles(event);
        return null;
    }

    private void deleteSelectedStyles(ExecutionEvent event) {
        List<IStyle> deletableStyles = MindMapHandlerUtil.findStyles(event,
                MindMapHandlerUtil.MATCH_MODIFIABLE);
        if (deletableStyles.isEmpty())
            return;

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window != null
                && !confirmDeletingStyles(window.getShell(), deletableStyles))
            return;

        deleteStyles(deletableStyles);
    }

    protected void deleteStyles(List<IStyle> styles) {
        boolean isTheme = false;
        for (IStyle style : styles) {
            if (IStyle.THEME.equals(style.getType()))
                isTheme = true;
            IStyleSheet sheet = style.getOwnedStyleSheet();
            ElementRegistry elementRegistry = (ElementRegistry) sheet
                    .getAdapter(ElementRegistry.class);
            elementRegistry.unregister(style);
            sheet.removeStyle(style);
        }
        if (isTheme)
            MindMapUI.getResourceManager().saveUserThemeSheet();
        else
            MindMapUI.getResourceManager().saveUserStyleSheet();
    }

    private boolean confirmDeletingStyles(Shell parentShell,
            List<IStyle> styles) {
        StringBuilder sb = new StringBuilder(styles.size() * 10);
        boolean isTheme = false;
        for (IStyle style : styles) {
            if (IStyle.THEME.equals(style.getType()))
                isTheme = true;
            if (sb.length() > 0) {
                sb.append(',');
                sb.append(' ');
            }
            sb.append('\'');
            sb.append(style.getName());
            sb.append('\'');
        }
        String styleNames = sb.toString();
        return MessageDialog.openConfirm(parentShell,
                NLS.bind(MindMapMessages.DeleteStyles_MessageDialog_title,
                        isTheme ? MindMapMessages.DeleteStyle_MessageDialog_themes
                                : MindMapMessages.DeleteStyle_MessageDialog_styles),
                NLS.bind(
                        MindMapMessages.DeleteStyle_MessageDialog_description,
                        styleNames));
    }

}
