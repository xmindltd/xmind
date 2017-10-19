package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.ui.mindmap.MindMapUI;

public class AttachmentsInspectorSection extends InspectorContentSection
        implements ICoreEventListener {

    private ICoreEventRegister register;

    private List<ITopic> topicsWithAttachment;

    private List<ITopic> allTopics;

    public AttachmentsInspectorSection() {
        setTitle(NLS.bind(Messages.AttachmentsInspectorSection_title, 0));
    }

    @Override
    protected Object[] getAllPropertyContents(IGraphicalViewer viewer) {
        List<ITopic> topicsWithAttachment = null;
        List<ITopic> allTopics = null;
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);

        if (sheet != null) {
            allTopics = getAllTopics();
            topicsWithAttachment = getTopicwithAttachment(allTopics);
        }

        setLabelRef(topicsWithAttachment, allTopics);

        if (topicsWithAttachment != null)
            return topicsWithAttachment.toArray();
        return new Object[0];
    }

    private void setLabelRef(List<ITopic> topicsWithAttachment,
            List<ITopic> allTopics) {
        if (topicsWithAttachment == this.topicsWithAttachment
                && allTopics == this.allTopics)
            return;

        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        this.topicsWithAttachment = topicsWithAttachment;
        this.allTopics = allTopics;
        if (allTopics != null) {
            register = new CoreEventRegister(getCurrentWorkbook(), this);
            register.register(Core.ModifyTime);
        }
    }

    private List<ITopic> getTopicwithAttachment(List<ITopic> all) {
        List<ITopic> topics = null;

        for (ITopic topic : all) {
            if (hasAttachment(topic)) {
                if (topics == null)
                    topics = new ArrayList<ITopic>();
                topics.add(topic);
            }
        }

        return topics;
    }

    private boolean hasAttachment(ITopic topic) {
        return topic.getHyperlink() != null
                && topic.getHyperlink().startsWith("xap:"); //$NON-NLS-1$
    }

    @Override
    protected Image getPropertyInspectorImage(Object element) {
        if (element instanceof ITopic) {
            String path = HyperlinkUtils.toAttachmentPath(((ITopic) element)
                    .getHyperlink());

            ImageDescriptor image = MindMapUI.getImages().getFileIcon(path,
                    true);

            if (image != null)
                return image.createImage();

        }

        return super.getPropertyInspectorImage(element);
    }

    @Override
    protected String getPropertyInspectorText(Object element) {
        if (element instanceof ITopic) {
            return ((ITopic) element).getTitleText().replaceAll(
                    "\r\n|\r|\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    @Override
    protected void refreshList() {
        super.refreshList();

        if (topicsWithAttachment == null)
            setTitle(NLS.bind(Messages.AttachmentsInspectorSection_title, 0));
        else
            setTitle(NLS.bind(Messages.AttachmentsInspectorSection_title,
                    topicsWithAttachment.size()));
    }

    protected void handleDispose() {
        if (register != null) {
            register.unregisterAll();
            register = null;
        }
    }

    public void handleCoreEvent(final CoreEvent event) {
        Control c = getControl();
        if (c == null || c.isDisposed())
            return;

        c.getDisplay().syncExec(new Runnable() {
            public void run() {
                refreshList(event);
            }
        });
    }

    protected void refreshList(CoreEvent event) {
        refreshList();
        getControl().getParent().layout();
    }

}
