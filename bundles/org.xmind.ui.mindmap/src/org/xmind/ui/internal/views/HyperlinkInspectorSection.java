package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

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
import org.xmind.gef.IGraphicalViewer;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class HyperlinkInspectorSection extends InspectorContentSection
        implements ICoreEventListener {

    private ICoreEventRegister register;

    private List<ITopic> topicsWithHyperlink;

    private List<ITopic> allTopics;

    public HyperlinkInspectorSection() {
        setTitle(NLS.bind(Messages.HyperlinkInspectorSection_title, 0));
    }

    @Override
    protected void refreshList() {
        super.refreshList();

        if (topicsWithHyperlink != null)
            setTitle(NLS.bind(Messages.HyperlinkInspectorSection_title,
                    topicsWithHyperlink.size()));
        else
            setTitle(NLS.bind(Messages.HyperlinkInspectorSection_title, 0));

    }

    @Override
    protected Object[] getAllPropertyContents(IGraphicalViewer viewer) {
        List<ITopic> topicsWithHyperlink = null;
        List<ITopic> allTopics = null;
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet != null) {
            allTopics = getAllTopics();
            topicsWithHyperlink = getTopicsWithHyperlink(allTopics);
        }

        setLabelRef(topicsWithHyperlink, allTopics);

        if (topicsWithHyperlink != null)
            return topicsWithHyperlink.toArray();
        return new Object[0];
    }

    private void setLabelRef(List<ITopic> topicsWithHyperlink,
            List<ITopic> allTopics) {
        if (topicsWithHyperlink == this.topicsWithHyperlink
                && allTopics == this.allTopics)
            return;

        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        this.topicsWithHyperlink = topicsWithHyperlink;
        this.allTopics = allTopics;
        if (allTopics != null) {
            register = new CoreEventRegister(getCurrentWorkbook(), this);
            register.register(Core.ModifyTime);
        }
    }

    private List<ITopic> getTopicsWithHyperlink(List<ITopic> all) {
        List<ITopic> topics = null;

        for (ITopic topic : all) {
            if (hasHyperlink(topic)) {
                if (topics == null)
                    topics = new ArrayList<ITopic>();
                topics.add(topic);
            }
        }

        return topics;
    }

    private boolean hasHyperlink(ITopic topic) {
        return topic.getHyperlink() != null
                && !topic.getHyperlink().startsWith("xap:") //$NON-NLS-1$
                && !topic.getHyperlink().startsWith("file:"); //$NON-NLS-1$
    }

    @Override
    protected Image getPropertyInspectorImage(Object element) {
        return MindMapUI.getImages().get(IMindMapImages.HYPERLINK, true)
                .createImage();
    }

    @Override
    protected String getPropertyInspectorText(Object element) {
        if (element instanceof ITopic)
            return ((ITopic) element).getTitleText().replaceAll(
                    "\r\n|\r|\n", " ") //$NON-NLS-1$ //$NON-NLS-2$
                    + ContentListViewer.SEP + ((ITopic) element).getHyperlink();

        return null;
    }

    public String getPropertyInspectorHyperlink(Object element) {
        if (element instanceof ITopic) {
            return ((ITopic) element).getHyperlink();
        }
        return null;
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

    @Override
    protected void handleDispose() {
        if (register != null) {
            register.unregisterAll();
            register = null;
        }
    }
}
