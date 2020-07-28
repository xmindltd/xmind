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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.properties.IPropertyEditingEntry;

/**
 * @author Frank Shaka
 */
public class ResetPropertyHandler extends AbstractHandler {

    private ISelectionProvider selectionProvider;

    public ResetPropertyHandler(ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
    }

    public ResetPropertyHandler() {
        this(null);
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        resetSelectedStyleProperty(event);
        return null;
    }

    protected void resetSelectedStyleProperty(ExecutionEvent event) {
        ISelection selection = selectionProvider != null ? selectionProvider
                .getSelection() : HandlerUtil.getCurrentSelection(event);
        if (selection == null || !(selection instanceof IStructuredSelection))
            return;

        for (Object element : ((IStructuredSelection) selection).toList()) {
            if (element instanceof IPropertyEditingEntry) {
                ((IPropertyEditingEntry) element).resetPropertyValue();
            }
        }
    }

}
