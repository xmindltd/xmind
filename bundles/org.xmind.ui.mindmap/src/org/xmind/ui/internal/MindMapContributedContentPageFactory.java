package org.xmind.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.outline.MindMapOutlinePage;
import org.xmind.ui.internal.properties.MindMapPropertySheetPage;
import org.xmind.ui.internal.views.WorkbookMetadataPage;
import org.xmind.ui.internal.views.WorkbookOverviewPage;
import org.xmind.ui.internal.views.WorkbookRevisionsPage;
import org.xmind.ui.views.IContributedContentPageFactory;

public class MindMapContributedContentPageFactory
        implements IContributedContentPageFactory {

    public static final String INSPECTOR_PAGE_TYPE_OVERVIEW = "org.xmind.ui.views.overview"; //$NON-NLS-1$

    public static final String INSPECTOR_PAGE_TYPE_REVISIONS = "org.xmind.ui.views.revisions"; //$NON-NLS-1$

    public static final String INSPECTOR_PAGE_TYPE_METADATA = "org.xmind.ui.views.metadata"; //$NON-NLS-1$

    public IPage createInspectorPage(String pageType, IWorkbenchPart part) {
        if (part instanceof IGraphicalEditor) {
            if (INSPECTOR_PAGE_TYPE_OUTLINE.equals(pageType))
                return new MindMapOutlinePage((IGraphicalEditor) part,
                        SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
            if (INSPECTOR_PAGE_TYPE_PROPERTIES.equals(pageType))
                return new MindMapPropertySheetPage((IGraphicalEditor) part);
            if (INSPECTOR_PAGE_TYPE_OVERVIEW.equals(pageType))
                return new WorkbookOverviewPage((IGraphicalEditor) part);
            if (INSPECTOR_PAGE_TYPE_REVISIONS.equals(pageType))
                return new WorkbookRevisionsPage((IGraphicalEditor) part);
            if (INSPECTOR_PAGE_TYPE_METADATA.equals(pageType))
                return new WorkbookMetadataPage((IEditorPart) part);
        }
        return null;
    }

}
