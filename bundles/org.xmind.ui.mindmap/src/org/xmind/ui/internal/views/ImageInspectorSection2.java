package org.xmind.ui.internal.views;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.viewers.ImageListViewer;

public class ImageInspectorSection2 extends InspectorSection implements
        ICoreEventListener {

    private static final String FILE_PROTOCOL = "file"; //$NON-NLS-1$

    private ICoreEventRegister register;

    private ImageListViewer list;

    List<ITopic> topicsWithImage;

    private List<ITopic> allTopics;

    public ImageInspectorSection2() {
        setTitle(NLS.bind(Messages.ImageInspectorSection_title, 0));
    }

    @Override
    protected Composite createContent(Composite parent) {
        Composite composite = super.createContent(parent);

        list = new ImageListViewer(composite, SWT.RESIZE);
        list.setContentProvider(new InspectorContentProvider());
        list.setLabelProvider(new InspectorLabelProvider());

        list.getControl().addListener(SWT.FocusOut, new Listener() {
            public void handleEvent(Event event) {
                list.setSelection(StructuredSelection.EMPTY);
            }
        });

        return composite;
    }

    @Override
    protected void refreshImageList() {
        if (list == null || list.getControl() == null)
            return;
        if (list.getInput() != getContributingViewer())
            list.setInput(getContributingViewer());

        list.refresh();

        if (topicsWithImage != null)
            setTitle(NLS.bind(Messages.ImageInspectorSection_title,
                    topicsWithImage.size()));
        else
            setTitle(NLS.bind(Messages.ImageInspectorSection_title, 0));

        reflow();
    }

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
    protected String getPropertyInspectorText(Object element) {
        return super.getPropertyInspectorText(element);
    }

    protected Image getPropertyInspectorImage(Object element) {
        if (element instanceof ITopic) {
            String source = ((ITopic) element).getImage().getSource();
            if (source != null) {
                return getImage(((ITopic) element), source);
            }
        }
        return null;
    }

    private Image getImage(ITopic topic, String source) {
        ImageDescriptor imageDescriptor = null;
        if (HyperlinkUtils.isAttachmentURL(source)) {
            String path = HyperlinkUtils.toAttachmentPath(source);
            imageDescriptor = AttachmentImageDescriptor.createFromEntryPath(
                    topic.getOwnedWorkbook(), path);
        } else {
            URL url = checkFileURL(source);
            if (url != null)
                imageDescriptor = ImageDescriptor.createFromURL(url);
        }
        if (imageDescriptor != null)
            return imageDescriptor.createImage();

        return null;
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

    public void handleCoreEvent(CoreEvent event) {
        Control c = getControl();
        if (c == null || c.isDisposed())
            return;

        c.getDisplay().syncExec(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    @Override
    protected void handleDispose() {
        if (register != null) {
            register.unregisterAll();
            register = null;
        }
    }

}
