package org.xmind.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.ITopic;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;

public class TopicBranchAdapterFactory implements IAdapterFactory {

    private Class<?>[] LIST = new Class<?>[] { IBranchPart.class };

    public Class<?>[] getAdapterList() {
        return LIST;
    }

    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adapterType == IBranchPart.class) {
            if (adaptableObject instanceof ITopic) {
                return adapterType.cast(
                        findBranchPartInActivePart((ITopic) adaptableObject));
            }
        }
        return null;
    }

    private IBranchPart findBranchPartInActivePart(ITopic topic) {
        if (!PlatformUI.isWorkbenchRunning())
            return null;

        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return null;

        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return null;

        IWorkbenchPart wp = page.getActivePart();
        if (wp == null)
            return null;

        IViewer viewer = wp.getAdapter(IViewer.class);
        if (viewer == null)
            return null;

        IPart part = viewer.findPart(topic);
        if (part == null)
            return null;

        if (part instanceof IBranchPart)
            return (IBranchPart) part;

        return part.getAdapter(IBranchPart.class);
    }

}
