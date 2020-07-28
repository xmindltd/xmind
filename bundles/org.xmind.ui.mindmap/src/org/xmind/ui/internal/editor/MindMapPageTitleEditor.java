/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
package org.xmind.ui.internal.editor;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.core.ISheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ISourceTool;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.tools.SheetTitleEditTool;
import org.xmind.ui.mindmap.MindMapUI;

public class MindMapPageTitleEditor {

    private static final int MIN_EDITOR_WIDTH = 50;

    private final CTabFolder tabFolder;

    private final IGraphicalEditor editor;

    public MindMapPageTitleEditor(CTabFolder tabFolder,
            MindMapEditor mindmapEditor) {
        super();
        this.tabFolder = tabFolder;
        this.editor = mindmapEditor;
        hookControl(tabFolder);
    }

    protected void hookControl(CTabFolder tabFolder) {
        Listener eventHandler = new Listener() {

            @Override
            public void handleEvent(Event event) {
                doHandleEvent(event);
            }
        };
        tabFolder.addListener(SWT.MouseDoubleClick, eventHandler);
        tabFolder.addListener(SWT.MouseDown, eventHandler);
    }

    private void doHandleEvent(Event event) {
        if (event.type == SWT.MouseDoubleClick) {
            startEditing(new Point(event.x, event.y));
        }
        if (event.type == SWT.MouseDown) {
            if (tabFolder.isFocusControl()) {
                startEditing(new Point(event.x, event.y));
            }
        }
    }

    /**
     * @param mouseLocation
     */
    private void startEditing(Point mouseLocation) {
        if (mouseLocation == null)
            return;

        CTabItem item = tabFolder.getItem(mouseLocation);
        if (item == null)
            return;

        startEditing(tabFolder.indexOf(item));
    }

    /**
     * @param pageIndex
     */
    public void startEditing(int pageIndex) {
        IGraphicalEditorPage page = editor.getPage(pageIndex);
        if (page == null)
            return;

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null)
            return;
        EditDomain editDomain = page.getEditDomain();
        if (editDomain == null)
            return;

        ISheet sheet = viewer.getAdapter(ISheet.class);
        if (sheet == null)
            return;

        IPart part = viewer.findPart(sheet);
        if (part == null || !(part instanceof IGraphicalEditPart))
            return;

        IGraphicalEditPart sourcePart = (IGraphicalEditPart) part;

        ITool tool = editDomain.getTool(MindMapUI.TOOL_EDIT_SHEET_TITLE);
        if (tool == null)
            return;

        if (tool instanceof ISourceTool) {
            ((ISourceTool) tool).setSource(sourcePart);
        }

        if (tool instanceof SheetTitleEditTool) {
            CTabItem item = tabFolder.getItem(pageIndex);
            Rectangle itemBounds = item.getBounds();
            Rectangle editorBounds = new Rectangle(itemBounds.x, itemBounds.y,
                    Math.max(MIN_EDITOR_WIDTH, itemBounds.width),
                    itemBounds.height);
            ((SheetTitleEditTool) tool).setTextEditorParameters(tabFolder,
                    editorBounds);
        }

        editDomain.setActiveTool(MindMapUI.TOOL_EDIT_SHEET_TITLE);

        if (tool != editDomain.getActiveTool())
            return;

        tool.handleRequest(new Request(GEF.REQ_EDIT).setViewer(viewer)
                .setTargets(Arrays.asList(sourcePart)));
    }

}
