package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.xmind.core.style.IStyle;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.tools.StyleCopyPasteTool;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;

public class PasteStyleHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        pasteStyle(MindMapHandlerUtil.findContributingEditor(event));
        return null;
    }

    private void pasteStyle(IWorkbenchPart part) {
        if (part == null)
            return;

        IStyle sourceStyle = StyleCopyPasteTool.getInstance().getSourceStyle();
        if (sourceStyle == null)
            return;

        IViewer viewer = MindMapUIPlugin.getAdapter(part, IViewer.class);
        if (viewer == null)
            return;

        EditDomain editDomain = viewer.getEditDomain();
        if (editDomain == null)
            return;

        String bg = sourceStyle.getProperty(Styles.Background);
        if (bg != null && !"".equals(bg)) { //$NON-NLS-1$
            sourceStyle.setProperty(Styles.Background, ""); //$NON-NLS-1$
        }

        editDomain.handleRequest(new Request(MindMapUI.REQ_MODIFY_STYLE)
                .setViewer(viewer).setDomain(editDomain)
                .setParameter(MindMapUI.PARAM_RESOURCE, sourceStyle));
    }

}
