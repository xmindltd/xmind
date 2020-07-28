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

public abstract class PropertySectionPart implements IPropertySectionPart {

    private IPropertyPartContainer container;

    private IGraphicalEditor editor;

    private ISelection currentSelection;

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (container != null)
            container.updateSectionTitle(this);
    }

    public void init(IPropertyPartContainer container, IGraphicalEditor editor) {
        this.container = container;
        this.editor = editor;
    }

    public IGraphicalEditor getContributedEditor() {
        return editor;
    }

    public IPropertyPartContainer getContainer() {
        return container;
    }

    /**
     * Clients are allowed to set layout on the parent.
     */
    public abstract void createControl(Composite parent);

    public void dispose() {
        setSelection(null);
    }

    public void refresh() {
    }

    public abstract void setFocus();

    protected ISelection getCurrentSelection() {
        return currentSelection;
    }

    public void setSelection(ISelection selection) {
        if (this.currentSelection != null) {
            unhookSelection(this.currentSelection);
        }
        this.currentSelection = selection;
        if (selection != null) {
            hookSelection(selection);
        }
    }

    protected void hookSelection(ISelection selection) {
    }

    protected void unhookSelection(ISelection selection) {
    }

}