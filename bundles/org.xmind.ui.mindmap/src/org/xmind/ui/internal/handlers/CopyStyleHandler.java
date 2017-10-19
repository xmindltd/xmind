package org.xmind.ui.internal.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.Property;
import org.xmind.gef.IViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.tools.StyleCopyPasteTool;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.util.MindMapUtils;

public class CopyStyleHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        copyStyle(HandlerUtil.getCurrentSelection(event),
                MindMapHandlerUtil.findContributingEditor(event));
        return null;
    }

    private void copyStyle(ISelection selection, IWorkbenchPart part) {
        if (selection == null || !(selection instanceof IStructuredSelection)
                || selection.isEmpty())
            return;

        for (Object element : ((IStructuredSelection) selection).toList()) {
            if (element instanceof IWorkbookComponent
                    && element instanceof IStyled) {
                copyStyle((IStyled) element, part);
                break;
            }
        }
    }

    private void copyStyle(IStyled element, IWorkbenchPart part) {
        StyleCopyPasteTool tool = StyleCopyPasteTool.getInstance();
        IStyleSheet styleSheet = ((IWorkbookComponent) element)
                .getOwnedWorkbook().getStyleSheet();

        IStyle style = null;
        String styleId = element.getStyleId();
        if (styleId != null) {
            style = styleSheet.findStyle(styleId);
            if (style != null) {
                IStyle defaultStyle = getDefaultStyle(element, part);
                if (defaultStyle != null) {
                    setProperties(style, defaultStyle);
                }
            }
        }
        if (style == null) {
            style = getDefaultStyle(element, part);
        }
        if (style == null) {
            return;
        }

        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
        IStyle importStyle = workbook.getStyleSheet().importStyle(style);
        tool.setSourceStyle(importStyle);
    }

    private void setProperties(IStyle style, IStyle defaultStyle) {
        Iterator<Property> ps = defaultStyle.properties();
        while (ps.hasNext()) {
            Property next = ps.next();
            if (style.getProperty(next.key) == null) {
                style.setProperty(next.key, next.value);
            }
        }
    }

    private IStyle getDefaultStyle(IStyled element, IWorkbenchPart part) {
        IViewer viewer = MindMapUIPlugin.getAdapter(part, IViewer.class);
        IMindMap mindMap = MindMapUIPlugin.getAdapter(viewer, IMindMap.class);
        if (mindMap == null)
            return null;

        String family = MindMapUtils.getFamily(element, mindMap);
        if (family == null)
            return null;

        IStyle theme = mindMap.getSheet().getTheme();
        if (theme == null)
            return null;

        return theme.getDefaultStyle(family);
    }

}
