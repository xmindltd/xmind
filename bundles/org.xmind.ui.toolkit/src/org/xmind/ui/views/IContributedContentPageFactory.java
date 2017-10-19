package org.xmind.ui.views;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;

public interface IContributedContentPageFactory {

    String INSPECTOR_PAGE_TYPE_OUTLINE = "outline"; //$NON-NLS-1$
    String INSPECTOR_PAGE_TYPE_PROPERTIES = "properties"; //$NON-NLS-1$

    IPage createInspectorPage(String pageType, IWorkbenchPart part);

}
