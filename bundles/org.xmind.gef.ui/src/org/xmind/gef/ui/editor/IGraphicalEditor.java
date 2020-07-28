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

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.ui.IEditorPart;
import org.xmind.gef.command.ICommandStack;

public interface IGraphicalEditor extends IEditorPart, IPageChangeProvider {

    void addPage(IGraphicalEditorPage page);

    void removePage(IGraphicalEditorPage page);

    IGraphicalEditorPage getPage(int pageIndex);

    int getPageCount();

    int findPage(IGraphicalEditorPage page);

//    boolean isDirty();

//    void forceDirty();

//    void fireDirty();

    void setCommandStack(ICommandStack commandStack);

    ICommandStack getCommandStack();

    int getActivePage();

    void setActivePage(int index);

    IGraphicalEditorPage getActivePageInstance();

    IGraphicalEditorPage[] getPages();

    IGraphicalEditorPage findPage(Object pageInput);

    IGraphicalEditorPage ensurePageVisible(Object pageInput);

    boolean navigateTo(Object pageInput, Object... elements);

    void movePageTo(int oldIndex, int newIndex);

    String getPageText(int index);

    void setPageText(int index, String text);

//    void addPageClosedListener(IPageClosedListener listener);
//
//    void removePageClosedListener(IPageClosedListener listener);
//
}