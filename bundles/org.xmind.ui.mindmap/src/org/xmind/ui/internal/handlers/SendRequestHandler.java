package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;

public abstract class SendRequestHandler extends AbstractHandler
        implements IExecutableExtension {

    public static final class ToActiveEditor extends SendRequestHandler {

        @Override
        protected IViewer getViewer(ExecutionEvent event)
                throws ExecutionException {
            IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
            return editor == null ? null
                    : MindMapUIPlugin.getAdapter(editor, IViewer.class);
        }

    }

    public static final class ToActivePart extends SendRequestHandler {

        @Override
        protected IViewer getViewer(ExecutionEvent event)
                throws ExecutionException {
            IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
            return part == null ? null
                    : MindMapUIPlugin.getAdapter(part, IViewer.class);
        }

    }

    public static final class ToContributingEditor extends SendRequestHandler {

        @Override
        protected IViewer getViewer(ExecutionEvent event)
                throws ExecutionException {
            IEditorPart editor = MindMapHandlerUtil
                    .findContributingEditor(event);
            return editor == null ? null
                    : MindMapUIPlugin.getAdapter(editor, IViewer.class);
        }

    }

    private String requestType;

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        if (data instanceof String) {
            this.requestType = (String) data;
        }
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        collectUsage();
        sendRequest(this.requestType, getViewer(event));
        return null;
    }

    protected abstract IViewer getViewer(ExecutionEvent event)
            throws ExecutionException;

    private static void sendRequest(String requestType, IViewer viewer) {
        if (requestType == null || viewer == null)
            return;

        EditDomain editDomain = viewer.getEditDomain();
        if (editDomain == null)
            return;

        editDomain.handleRequest(requestType, viewer);
    }

    private void collectUsage() {
        if (requestType == null)
            return;

        if (MindMapUI.REQ_CREATE_CALLOUT.equals(requestType)) {
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_INSERT,
                    UserDataConstants.INSERT_CALLOUT);
        } else if (MindMapUI.REQ_CREATE_RELATIONSHIP.equals(requestType)) {
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_INSERT,
                    UserDataConstants.INSERT_RELATIONSHIP);
        }
    }

}
