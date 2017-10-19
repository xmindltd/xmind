package org.xmind.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.xmind.core.IIdentifiable;
import org.xmind.core.IImage;
import org.xmind.core.IResourceRef;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.internal.xpath.IAxisProvider;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;

public class CoreAxisProvider implements IAxisProvider {

    private static final String TAG_TOPIC = "topic"; //$NON-NLS-1$
    private static final String TAG_MARKER = "marker"; //$NON-NLS-1$
    private static final String TAG_LABEL = "label"; //$NON-NLS-1$
    private static final String TAG_IMAGE = "image"; //$NON-NLS-1$
    private static final String TAG_EXTENSION = "extension"; //$NON-NLS-1$
    private static final String TAG_CONTENT = "content"; //$NON-NLS-1$
    private static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$

    private static final String ATTR_ID = "id"; //$NON-NLS-1$
    private static final String ATTR_TYPE = "type"; //$NON-NLS-1$
    private static final String ATTR_TITLE = "title"; //$NON-NLS-1$
    private static final String ATTR_FOLDED = "folded"; //$NON-NLS-1$
    private static final String ATTR_HYPERLINK = "hyperlink"; //$NON-NLS-1$
    private static final String ATTR_STRUCTURE_CLASS = "structureClass"; //$NON-NLS-1$
    private static final String ATTR_SOURCE = "source"; //$NON-NLS-1$
    private static final String ATTR_NAME = "name"; //$NON-NLS-1$
    private static final String ATTR_GROUP_ID = "groupId"; //$NON-NLS-1$
    private static final String ATTR_PROVIDER = "provider"; //$NON-NLS-1$

    public List<?> getChildNodes(Object node, String name) {
        if (TAG_MARKER.equals(name)) {
            if (node instanceof ITopic)
                return new ArrayList<Object>(((ITopic) node).getMarkerRefs());
        } else if (TAG_LABEL.equals(name)) {
            if (node instanceof ITopic)
                return new ArrayList<Object>(((ITopic) node).getLabels());
        } else if (TAG_IMAGE.equals(name)) {
            if (node instanceof ITopic)
                return Arrays.asList(((ITopic) node).getImage());
        } else if (TAG_TOPIC.equals(name)) {
            if (node instanceof ITopic)
                return ((ITopic) node).getAllChildren();
        } else if (TAG_EXTENSION.equals(name)) {
            if (node instanceof ITopic)
                return ((ITopic) node).getExtensions();
        } else if (TAG_CONTENT.equals(name)) {
            if (node instanceof ITopicExtension)
                return Arrays.asList(((ITopicExtension) node).getContent());
        } else if (TAG_RESOURCE.equals(name)) {
            if (node instanceof ITopicExtension)
                return ((ITopicExtension) node).getResourceRefs();
        } else {
            if (node instanceof ITopicExtensionElement)
                return ((ITopicExtensionElement) node).getChildren(name);
        }
        return Collections.emptyList();
    }

    public Object getParentNode(Object node) {
        if (node instanceof ITopic) {
            ITopic parent = ((ITopic) node).getParent();
            return parent != null ? parent : ((ITopic) node).getOwnedSheet();
        }
        return null;
    }

    public Object getAttribute(Object node, String name) {
        if (ATTR_TYPE.equals(name)) {
            if (node instanceof ITopic)
                return ((ITopic) node).getType();
            if (node instanceof IResourceRef)
                return ((IResourceRef) node).getType();
        } else if (ATTR_ID.equals(name)) {
            if (node instanceof IIdentifiable)
                return ((IIdentifiable) node).getId();
            if (node instanceof IMarkerRef)
                return ((IMarkerRef) node).getMarkerId();
            if (node instanceof IResourceRef)
                return ((IResourceRef) node).getResourceId();
        } else if (ATTR_TITLE.equals(name)) {
            if (node instanceof ITitled)
                return ((ITitled) node).getTitleText();
        } else if (ATTR_FOLDED.equals(name)) {
            if (node instanceof ITopic)
                return Boolean.valueOf(((ITopic) node).isFolded());
        } else if (ATTR_HYPERLINK.equals(name)) {
            if (node instanceof ITopic)
                return ((ITopic) node).getHyperlink();
        } else if (ATTR_STRUCTURE_CLASS.equals(name)) {
            if (node instanceof ITopic)
                return ((ITopic) node).getStructureClass();
        } else if (ATTR_SOURCE.equals(name)) {
            if (node instanceof IImage)
                return ((IImage) node).getSource();
        } else if (ATTR_GROUP_ID.equals(name)) {
            if (node instanceof IMarkerRef)
                return getMarkerGroupId((IMarkerRef) node);
        } else if (ATTR_NAME.equals(name)) {
            if (node instanceof IMarkerRef)
                return ((IMarkerRef) node).getDescription();
        } else if (ATTR_PROVIDER.equals(name)) {
            if (node instanceof ITopicExtension)
                return ((ITopicExtension) node).getProviderName();
        } else {
            if (node instanceof ITopicExtensionElement)
                return ((ITopicExtensionElement) node).getAttribute(name);
        }
        return null;
    }

    public String getTextContent(Object node) {
        if (node instanceof ITopicExtensionElement)
            return ((ITopicExtensionElement) node).getTextContent();
        if (node instanceof String)
            return (String) node;
        return null;
    }

    private String getMarkerGroupId(IMarkerRef mr) {
        IMarker m = mr.getMarker();
        if (m == null)
            return null;
        IMarkerGroup g = m.getParent();
        if (g == null)
            return null;
        return g.getId();
    }

}
