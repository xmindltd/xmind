package org.xmind.ui.internal.views;

import java.io.File;
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
import org.xmind.gef.IGraphicalViewer;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.internal.protocols.FileProtocol;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class ExternalFilesInspectorSection extends InspectorContentSection
        implements ICoreEventListener {

    private ICoreEventRegister register;

    private List<ITopic> topicsWithExternalFile;

    private List<ITopic> allTopics;

    public ExternalFilesInspectorSection() {
        setTitle(NLS.bind(Messages.ExternalFilesInspectorSection_title, 0));
    }

    @Override
    protected Object[] getAllPropertyContents(IGraphicalViewer viewer) {
        List<ITopic> topicsWithExternalFile = null;
        List<ITopic> allTopics = null;
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet != null) {
            allTopics = getAllTopics();
            topicsWithExternalFile = getTopicwithAttachment(allTopics);
        }

        setLabelRef(topicsWithExternalFile, allTopics);

        if (topicsWithExternalFile != null)
            return topicsWithExternalFile.toArray();
        return new Object[0];
    }

    private void setLabelRef(List<ITopic> topicsWithExternalFile,
            List<ITopic> allTopics) {
        if (this.topicsWithExternalFile == topicsWithExternalFile
                && allTopics == this.allTopics)
            return;

        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        this.topicsWithExternalFile = topicsWithExternalFile;
        this.allTopics = allTopics;
        if (allTopics != null) {
            register = new CoreEventRegister(getCurrentWorkbook(), this);
            register.register(Core.ModifyTime);
        }
    }

    private List<ITopic> getTopicwithAttachment(List<ITopic> all) {
        List<ITopic> topics = null;

        for (ITopic topic : all) {
            if (hasExternalFile(topic)) {
                if (topics == null)
                    topics = new ArrayList<ITopic>();
                topics.add(topic);
            }
        }

        return topics;
    }

    private boolean hasExternalFile(ITopic topic) {
        return topic.getHyperlink() != null
                && topic.getHyperlink().startsWith("file:"); //$NON-NLS-1$
    }

    @Override
    protected Image getPropertyInspectorImage(Object element) {
        if (element instanceof ITopic) {
            String path = FilePathParser
                    .toPath(((ITopic) element).getHyperlink());

            path = FileProtocol.getAbsolutePath(element, path);
//            if (FilePathParser.isPathRelative(path)) {
//                IWorkbook workbook = ((ITopic) element).getOwnedWorkbook();
//                if (workbook != null) {
//                    String base = workbook.getFile();
//                    if (base != null) {
//                        base = new File(base).getParent();
//                        if (base != null) {
//                            path = FilePathParser.toAbsolutePath(base, path);
//                        }
//                    }
//                }
//                path = FilePathParser.toAbsolutePath(
//                        FilePathParser.ABSTRACT_FILE_BASE, path);
//            }

            File file = new File(path);
            ImageDescriptor image = MindMapUI.getImages().getFileIcon(path,
                    true);
            if (image == null) {
                if (file.isDirectory()) {
                    image = MindMapUI.getImages().get(IMindMapImages.OPEN,
                            true);
                } else {
                    image = MindMapUI.getImages()
                            .get(IMindMapImages.UNKNOWN_FILE, true);
                }
            }

            if (image != null)
                return image.createImage();

        }

        return super.getPropertyInspectorImage(element);
    }

    @Override
    protected String getPropertyInspectorText(Object element) {
        if (element instanceof ITopic) {
            String link = ((ITopic) element).getHyperlink();
            return link.substring(link.lastIndexOf("/") + 1, link.length()) //$NON-NLS-1$
                    .replaceAll("%20", " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    @Override
    protected void refreshList() {
        super.refreshList();
        if (topicsWithExternalFile != null)
            setTitle(NLS.bind(Messages.ExternalFilesInspectorSection_title,
                    topicsWithExternalFile.size()));
        else
            setTitle(NLS.bind(Messages.ExternalFilesInspectorSection_title, 0));
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
