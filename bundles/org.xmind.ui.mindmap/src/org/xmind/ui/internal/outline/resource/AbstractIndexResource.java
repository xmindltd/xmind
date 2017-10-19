package org.xmind.ui.internal.outline.resource;

import java.util.List;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.IWorkbookRef;

public abstract class AbstractIndexResource implements IOutlineResource {

    protected void collectResourceForWorkbook(IWorkbookRef workbookRef) {
        if (workbookRef == null)
            return;

        List<ISheet> sheets = workbookRef.getWorkbook().getSheets();
        for (ISheet sheet : sheets) {
            collectResourceForSheet(sheet);
        }
    }

    protected void collectResourceForSheet(ISheet sheet) {
        ITopic rootTopic = sheet.getRootTopic();
        collectResourceForTopic(rootTopic);
        collectResourceForParentTopic(rootTopic);
    }

    protected void collectResourceForParentTopic(ITopic parentTopic) {
        for (ITopic child : parentTopic.getAllChildren()) {
            collectResourceForTopic(child);
            collectResourceForParentTopic(child);
        }
    }

    protected abstract void collectResourceForTopic(ITopic topic);
}
