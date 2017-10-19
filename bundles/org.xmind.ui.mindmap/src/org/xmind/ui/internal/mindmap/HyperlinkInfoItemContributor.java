package org.xmind.ui.internal.mindmap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.ModifyHyperlinkAction;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IHyperlinked;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;

public class HyperlinkInfoItemContributor extends AbstractInfoItemContributor {

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        String hyperlink = topic.getHyperlink();
        if (hyperlink == null)
            return null;

        IAction action = MindMapUI.getProtocolManager()
                .createOpenHyperlinkAction(topicPart, hyperlink);
        if (action != null) {
            action.setId(MindMapActionFactory.OPEN_HYPERLINK.getId());
        }

        return action;
    }

    public boolean isModified(ITopicPart topicPart, ITopic topic,
            IAction action) {
        if (!(action instanceof IHyperlinked)) {
            return true;
        }

        String hyperlink = topic.getHyperlink();
        String hyperlink2 = ((IHyperlinked) action).getHyperlink();

        return (hyperlink == null && hyperlink2 != null)
                || (hyperlink != null && !hyperlink.equals(hyperlink2));
    }

    public String getContent(ITopic topic) {
        return topic.getHyperlink();
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        String hyperlink = topic.getHyperlink();
        if (hyperlink == null || action == null)
            return null;

        Object element = HyperlinkUtils.findElement(hyperlink,
                topic.getOwnedWorkbook());
        String filePath = "platform:/plugin/org.xmind.ui.mindmap/icons/"; //$NON-NLS-1$
        if (element != null && element instanceof ITopic) {
            String type = ((ITopic) element).getType();
            if (ITopic.ROOT.equals(type)) {
                return filePath + "link_central_topic.svg"; //$NON-NLS-1$
            }
            if (ITopic.SUMMARY.equals(type)) {
                return filePath + "link_summary.svg"; //$NON-NLS-1$
            }
            if (ITopic.DETACHED.equals(type)) {
                return filePath + "link_floating_topic.svg"; //$NON-NLS-1$
            }
            if (ITopic.CALLOUT.equals(type)) {
                return filePath + "link_callout.svg"; //$NON-NLS-1$
            }
            ITopic parent = ((ITopic) element).getParent();
            if (parent != null && parent.isRoot()) {
                return filePath + "link_main_topic.svg"; //$NON-NLS-1$
            }
            return filePath + "link_subtopic.svg"; //$NON-NLS-1$
        } else if (isLinkToWeb(hyperlink)) {
            ImageDescriptor descriptor = action.getImageDescriptor();
            ImageDescriptor hyperlinkDescriptor = MindMapUI.getImages()
                    .get(IMindMapImages.HYPERLINK, true);
            if (descriptor != null && descriptor.equals(hyperlinkDescriptor))
                return filePath + "hyperlink.svg"; //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart) {
        return isLinkToWeb(topic.getHyperlink()) && !isIconTipOnly(topicPart);
    }

    private boolean isLinkToWeb(String hyperlink) {
        return hyperlink != null && !hyperlink.startsWith("xap:") //$NON-NLS-1$
                && !hyperlink.startsWith("xmind:") //$NON-NLS-1$
                && !hyperlink.startsWith("file:"); //$NON-NLS-1$
    }

    @Override
    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
        register.register(Core.TopicHyperlink);
        register.setNextSupport((ICoreEventSupport) topic.getOwnedWorkbook()
                .getAdapter(ICoreEventSupport.class));
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
        register.register(Core.TitleText);
    }

    @Override
    protected void handleTopicEvent(IInfoPart infoPart, CoreEvent event) {
        if (Core.TopicAdd.equals(event.getType())
                || Core.TopicRemove.equals(event.getType())
                || Core.TitleText.equals(event.getType())) {
            String hyperlink = infoPart.getTopicPart().getTopic()
                    .getHyperlink();
            if (HyperlinkUtils.isInternalURL(hyperlink)) {
                Object target = HyperlinkUtils.findElement(hyperlink,
                        infoPart.getTopicPart().getTopic().getOwnedWorkbook());
                if (target == event.getTarget()) {
                    infoPart.refresh();
                    infoPart.getTopicPart().refresh();
                }
                if (Core.TitleText.equals(event.getType()) && target != null) {
                    infoPart.refresh();
                    infoPart.getTopicPart().refresh();
                }
            }
        } else {
            infoPart.refresh();
            infoPart.getTopicPart().refresh();
        }
    }

    @Override
    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
        if (Core.TopicAdd.equals(event.getType())
                || Core.TopicRemove.equals(event.getType())
                || Core.TitleText.equals(event.getType())) {
            String hyperlink = topicPart.getTopic().getHyperlink();
            if (HyperlinkUtils.isInternalURL(hyperlink)) {
                Object target = HyperlinkUtils.findElement(hyperlink,
                        topicPart.getTopic().getOwnedWorkbook());
                if (target == event.getTarget()) {
                    topicPart.refresh();
                }
            }
        } else {
            topicPart.refresh();
        }
    }

    public void removeHyperlink(ITopic topic) {
        IGraphicalEditorPage page = getPage();
        if (page == null)
            return;

        Request request = new Request(MindMapUI.REQ_MODIFY_HYPERLINK)
                .setParameter(GEF.PARAM_TEXT, null);
        request.setViewer(page.getViewer());

        EditDomain domain = page.getEditDomain();
        if (domain != null) {
            domain.handleRequest(request);
        }

    }

    @Override
    public List<IAction> getPopupMenuActions(ITopicPart topicPart,
            final ITopic topic) {
        IGraphicalEditorPage page = getPage();
        if (page == null)
            return Collections.emptyList();
        List<IAction> actions = new ArrayList<IAction>();

        String url = topic.getHyperlink();
        if (!HyperlinkUtils.isAttachmentURL(url)) {
            IAction modifyHyperlinkAction = new ModifyHyperlinkAction(page);

            modifyHyperlinkAction.setText(MindMapMessages.InfoItem_Modify_text);
            modifyHyperlinkAction.setImageDescriptor(null);
            actions.add(modifyHyperlinkAction);
        } else {
            IAction saveAttchmentAsAction = new Action(
                    MindMapMessages.InfoItem_SaveAttachment_text) {
                @Override
                public void run() {
                    saveAttachmentAs(topic);
                }
            };
            actions.add(saveAttchmentAsAction);
        }

        IAction deleteHyperlinkAction = new Action(
                MindMapMessages.InfoItem_Delete_text) {
            @Override
            public void run() {
                HyperlinkInfoItemContributor.this.removeHyperlink(topic);
            }
        };
        deleteHyperlinkAction.setId("org.xmind.ui.removeHyperlink"); //$NON-NLS-1$
        deleteHyperlinkAction.setImageDescriptor(null);
        actions.add(deleteHyperlinkAction);

        return actions;
    }

    private IGraphicalEditorPage getPage() {

        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return null;

        IGraphicalEditor editor = (IGraphicalEditor) window.getActivePage()
                .getActiveEditor();
        if (editor == null)
            return null;
        return editor.getActivePageInstance();
    }

    private Object saveAttachmentAs(ITopic sourceTopic) {

        if (sourceTopic == null)
            return null;

        String url = sourceTopic.getHyperlink();
        if (!HyperlinkUtils.isAttachmentURL(url))
            return null;

        final String entryPath = HyperlinkUtils.toAttachmentPath(url);
        final IFileEntry entry = sourceTopic.getOwnedWorkbook().getManifest()
                .getFileEntry(entryPath);
        if (entry == null)
            return null;

        final InputStream is = entry.getInputStream();
        if (is == null)
            return null;

        try {
            String ext = FileUtils.getExtension(entryPath);
            FileDialog dialog = new FileDialog(
                    Display.getCurrent().getActiveShell(), SWT.SAVE);

            dialog.setFilterExtensions(new String[] { "*" + ext, "*.*" }); //$NON-NLS-1$//$NON-NLS-2$    
            String extension = ext;
            if (ext != null) {
                Program p = Program.findProgram(ext);
                if (p != null) {
                    extension = p.getName();
                }
            }
            dialog.setFilterNames(
                    new String[] { extension, NLS.bind("{0} (*.*)", //$NON-NLS-1$
                            DialogMessages.AllFilesFilterName) });
            String name = sourceTopic.getTitleText();
            if (name != null && !name.endsWith(ext)) {
                name += ext;
            }
            if (name != null) {
                dialog.setFileName(name);
            }
            dialog.setOverwrite(true);
            final String targetPath = dialog.open();
            if (targetPath == null)
                return null;

            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    FileOutputStream os = new FileOutputStream(targetPath);
                    FileUtils.transfer(is, os, true);
                }
            });
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

        return null;
    }

}
