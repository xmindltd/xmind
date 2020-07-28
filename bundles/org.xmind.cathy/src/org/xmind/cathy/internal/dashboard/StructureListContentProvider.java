package org.xmind.cathy.internal.dashboard;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.util.XMLUtils;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

public class StructureListContentProvider
        implements IStructuredContentProvider {

    public static final String CONTENT_URI = "platform:/plugin/org.xmind.cathy/dashboard/new/structures.xml"; //$NON-NLS-1$
    public static final String NLS_PATH_BASE = "dashboard/new/structures"; //$NON-NLS-1$

    private static final String TAG_STRUCTURE_LIST = "structure-list"; //$NON-NLS-1$
    private static final String TAG_STRUCTURE = "structure"; //$NON-NLS-1$
    private static final String ATTR_ID = "id"; //$NON-NLS-1$
    private static final String ATTR_NAME = "name"; //$NON-NLS-1$
    private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
    private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
    private static final String ATTR_ICON_HEIGHT = "icon-height"; //$NON-NLS-1$
    private static final String ATTR_ICON_WIDTH = "icon-width"; //$NON-NLS-1$

    public static final class ContentSource {

        public final String contentURI;

        public final String nlsPathBase;

        public ContentSource(String contentURI, String nlsPathBase) {
            this.contentURI = contentURI;
            this.nlsPathBase = nlsPathBase;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof ContentSource))
                return false;
            ContentSource that = (ContentSource) obj;
            return (this.contentURI == that.contentURI
                    || (this.contentURI != null
                            && this.contentURI.equals(that.contentURI)))
                    && (this.nlsPathBase == that.nlsPathBase
                            || (this.nlsPathBase != null && this.nlsPathBase
                                    .equals(that.nlsPathBase)));
        }

        @Override
        public int hashCode() {
            int x = 37;
            if (contentURI != null) {
                x = x ^ contentURI.hashCode();
            }
            if (nlsPathBase != null) {
                x = x ^ nlsPathBase.hashCode();
            }
            return x;
        }

    }

    static class StructureDescriptor {
        private String id;
        private String value;
        private String name;
        private ImageDescriptor icon;

        public StructureDescriptor(String id, String value, String name,
                ImageDescriptor icon) {
            super();
            Assert.isNotNull(id);
            Assert.isNotNull(value);
            this.id = id;
            this.value = value;
            this.name = name;
            this.icon = icon;
        }

        public int hashCode() {
            return id.hashCode();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public ImageDescriptor getIcon() {
            return this.icon;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof StructureDescriptor))
                return false;
            StructureDescriptor that = (StructureDescriptor) obj;
            return this.id.equals(that.id);
        }

    }

    static class StructureListLabelProvider extends ImageCachedLabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof StructureDescriptor)
                return ((StructureDescriptor) element).getName();
            return super.getText(element);
        }

        @Override
        protected Image createImage(Object element) {
            if (element instanceof StructureDescriptor) {
                ImageDescriptor icon = ((StructureDescriptor) element)
                        .getIcon();
                if (icon != null)
                    return icon.createImage();
            }
            return null;
        }
    }

    private ContentSource source = null;

    private List<StructureDescriptor> structureDescriptors = new ArrayList<StructureListContentProvider.StructureDescriptor>();

    private Dimension iconSizeHints = new Dimension();

    public void dispose() {
        structureDescriptors.clear();
        source = null;
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        ContentSource newSource = toContentSource(newInput);
        if (newSource == source
                || (newSource != null && newSource.equals(source)))
            return;

        source = newSource;
        structureDescriptors.clear();
        iconSizeHints = new Dimension();

        if (source != null) {
            readTemplatesFromSource(source);
        }

        if (viewer instanceof GalleryViewer) {
            ((GalleryViewer) viewer).getProperties()
                    .set(GalleryViewer.FrameContentSize, iconSizeHints);
        }
    }

    private ContentSource toContentSource(Object input) {
        ContentSource newSource;
        if (input instanceof ContentSource) {
            newSource = (ContentSource) input;
        } else if (input instanceof String) {
            newSource = new ContentSource((String) input, null);
        } else {
            newSource = null;
        }
        return newSource;
    }

    public Object[] getElements(Object inputElement) {
        ContentSource inputSource = toContentSource(inputElement);
        if (inputSource == source
                || (inputSource != null && inputSource.equals(source))) {
            return structureDescriptors.toArray();
        }
        return new Object[0];
    }

    private void readTemplatesFromSource(ContentSource source) {
        Properties nlsProperties;
        if (source.nlsPathBase != null) {
            nlsProperties = CathyPlugin.getDefault()
                    .loadNLSProperties(source.nlsPathBase);
        } else {
            nlsProperties = new Properties();
        }

        if (source.contentURI != null) {
            try {
                URL contentURL = new URL(source.contentURI);
                URL locatedURL = FileLocator.find(contentURL);
                if (locatedURL != null)
                    contentURL = locatedURL;
                InputStream contentStream = contentURL.openStream();
                try {
                    Document doc = XMLUtils.getDefaultDocumentBuilder()
                            .parse(contentStream);
                    readElement(doc.getDocumentElement(), nlsProperties);
                } finally {
                    contentStream.close();
                }
            } catch (Exception e) {
                CathyPlugin.log(e,
                        "Failed to load content for structure list from: " //$NON-NLS-1$
                                + source.contentURI);
            }
        }

    }

    private void readElement(Element element, Properties nlsProperties) {
        String tagName = element.getTagName();
        if (TAG_STRUCTURE.equals(tagName)) {
            readTemplate(element, nlsProperties);
        } else if (TAG_STRUCTURE_LIST.equals(tagName)) {
            readGlobalAttributes(element);
        }

        readChildren(element.getChildNodes(), nlsProperties);
    }

    private void readChildren(NodeList children, Properties nlsProperties) {
        int length = children.getLength();
        for (int i = 0; i < length; i++) {
            Node node = children.item(i);
            if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                readElement((Element) node, nlsProperties);
            }
        }
    }

    private void readTemplate(Element element, Properties nlsProperties) {
        String id = element.getAttribute(ATTR_ID);
        String name = element.getAttribute(ATTR_NAME);
        String iconURI = element.getAttribute(ATTR_ICON);
        String structureClass = element.getAttribute(ATTR_VALUE);

        if (id == null || "".equals(id)) //$NON-NLS-1$
            throw new IllegalArgumentException("Missing 'id' attribute"); //$NON-NLS-1$

        if (name == null) {
            name = ""; //$NON-NLS-1$
        } else if (name.startsWith("%")) { //$NON-NLS-1$
            String nativeName = nlsProperties.getProperty(name.substring(1));
            if (nativeName != null) {
                name = nativeName;
            }
        }

        ImageDescriptor icon;
        if (iconURI == null) {
            icon = null;
        } else {
            try {
                icon = ImageDescriptor.createFromURL(new URL(iconURI));
            } catch (MalformedURLException e) {
                CathyPlugin.log(e, "Invalid icon URI: '" + iconURI + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                icon = null;
            }
        }

        StructureDescriptor structureDescriptor = new StructureDescriptor(id,
                structureClass, name, icon);
        structureDescriptors.add(structureDescriptor);
    }

    private void readGlobalAttributes(Element element) {
        String iconWidth = element.getAttribute(ATTR_ICON_WIDTH);
        String iconHeight = element.getAttribute(ATTR_ICON_HEIGHT);

        if (iconWidth != null && iconHeight != null) {
            try {
                int width = Integer.parseInt(iconWidth, 10);
                int height = Integer.parseInt(iconHeight, 10);
                iconSizeHints.width = width;
                iconSizeHints.height = height;
            } catch (NumberFormatException e) {
            }
        }
    }

    public static final ContentSource getDefaultInput() {
        return new ContentSource(CONTENT_URI, NLS_PATH_BASE);
    }

}
