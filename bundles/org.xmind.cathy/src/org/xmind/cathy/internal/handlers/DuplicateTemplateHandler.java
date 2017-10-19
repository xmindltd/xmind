package org.xmind.cathy.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.utils.ResourceUtils;
import org.xmind.ui.mindmap.ITemplate;

public class DuplicateTemplateHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        List<ITemplate> templates = new ArrayList<ITemplate>();
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof ITemplate) {
                templates.add((ITemplate) obj);
            }
        }
        ResourceUtils.duplicateTemplates(templates);
        return null;
    }

}
