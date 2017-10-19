package org.xmind.ui.internal.views;

import java.net.MalformedURLException;
import java.net.URL;
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

public class ImageInspectorSection extends InspectorContentSection implements
        ICoreEventListener {

    private static final String FILE_PROTOCOL = "file"; //$NON-NLS-1$

    private ICoreEventRegister register;

    private List<ITopic> topicsWithImage;

    private List<ITopic> allTopics;

    public ImageInspectorSection() {
        setTitle(NLS.bind(Messages.ImageInspectorSection_title, 0));
    }

    @Override
    protected Object[] getAllPropertyContents(IGraphicalViewer viewer) {
        List<ITopic> topicsWithImage = null;
        List<ITopic> allTopics = null;
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet != null) {
            allTopics = getAllTopics();
            topicsWithImage = getTopicsWithImage(allTopics);
        }

        setLabelRef(topicsWithImage, allTopics);

        if (topicsWithImage != null)
            return topicsWithImage.toArray();
        return new Object[0];
    }

    private void setLabelRef(List<ITopic> topicsWithImage,
            List<ITopic> allTopics) {
        if (topicsWithImage == this.topicsWithImage
                && allTopics == this.allTopics)
            return;

        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        this.topicsWithImage = topicsWithImage;
        this.allTopics = allTopics;
        if (allTopics != null) {
            register = new CoreEventRegister(getCurrentWorkbook(), this);
            register.register(Core.ModifyTime);
        }
    }

    private List<ITopic> getTopicsWithImage(List<ITopic> all) {
        List<ITopic> topics = null;

        for (ITopic topic : all) {
            if (hasImage(topic)) {
                if (topics == null)
                    topics = new ArrayList<ITopic>();
                topics.add(topic);
            }
        }

        return topics;
    }

    private boolean hasImage(ITopic topic) {
        return topic.getImage().getSource() != null;
    }

    @Override
    protected Image getPropertyInspectorImage(Object element) {
        if (element instanceof ITopic) {
            String source = ((ITopic) element).getImage().getSource();
            if (HyperlinkUtils.isAttachmentURL(source)) {
                String path = HyperlinkUtils.toAttachmentPath(source);
                ImageDescriptor image = MindMapUI.getImages().getFileIcon(path,
                        true);

                if (image != null)
                    return image.createImage();
            } else {
                URL url = checkFileURL(source);
                ImageDescriptor imageDescriptor = null;
                if (url != null)
                    imageDescriptor = ImageDescriptor.createFromURL(url);
                if (imageDescriptor != null)
                    return imageDescriptor.createImage();
            }
        }

        return super.getPropertyInspectorImage(element);
    }

    private URL checkFileURL(String source) {
        try {
            URL url = new URL(source);
            if (FILE_PROTOCOL.equalsIgnoreCase(url.getProtocol()))
                return url;
        } catch (MalformedURLException e) {
        }
        return null;
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
    protected void refreshImageList() {
        if (topicsWithImage != null)
            setTitle(NLS.bind(Messages.ImageInspectorSection_title,
                    topicsWithImage.size()));
        else
            setTitle(NLS.bind(Messages.ImageInspectorSection_title, 0));

        reflow();
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
