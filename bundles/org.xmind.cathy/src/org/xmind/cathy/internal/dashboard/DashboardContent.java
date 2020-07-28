package org.xmind.cathy.internal.dashboard;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.ui.internal.dashboard.pages.IDashboardPage;
import org.xmind.ui.tabfolder.MTabFolder;
import org.xmind.ui.tabfolder.MTabItem;
import org.xmind.ui.util.XMLUtils;

public class DashboardContent {

    private static final String CONTENT_URI = "platform:/plugin/org.xmind.cathy/dashboard/dashboard.xml"; //$NON-NLS-1$
    private static final String NLS_PATH_BASE = "dashboard/dashboard"; //$NON-NLS-1$

    private static final String TAG_DASHBOARD = "dashboard"; //$NON-NLS-1$
    private static final String TAG_PAGE = "page"; //$NON-NLS-1$
    private static final String TAG_SPACE = "space"; //$NON-NLS-1$
    private static final String TAG_COMMAND = "command"; //$NON-NLS-1$
    private static final String TAG_PARAMETER = "parameter"; //$NON-NLS-1$
    private static final String TAG_ITEM = "item"; //$NON-NLS-1$
    private static final String ATTR_ID = "id"; //$NON-NLS-1$
    private static final String ATTR_LABEL = "label"; //$NON-NLS-1$
    private static final String ATTR_TOOLTIP = "tooltip"; //$NON-NLS-1$
    private static final String ATTR_ICON_URI = "iconURI"; //$NON-NLS-1$
    private static final String ATTR_CONTRIBUTION_URI = "contributionURI"; //$NON-NLS-1$
    private static final String ATTR_COMMAND_ID = "commandId"; //$NON-NLS-1$
    private static final String ATTR_NAME = "name"; //$NON-NLS-1$
    private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
    private static final String ATTR_WIDTH = "width"; //$NON-NLS-1$
    private static final String ATTR_COLOR = "color"; //$NON-NLS-1$

    private static final String VAL_FILL = "fill"; //$NON-NLS-1$

    private static final String DATA_ID = "org.xmind.ui.dashboard.itemId"; //$NON-NLS-1$
    private static final String DATA_PARAMETERS = "org.xmind.ui.dashboard.commandParameters"; //$NON-NLS-1$

    private static final String STATE_SELECTED_PAGE_ID = "selectedPageId"; //$NON-NLS-1$

    private final DashboardPart part;

    private final MTabFolder tabFolder;

    private Properties nlsProperties = new Properties();

    public DashboardContent(final DashboardPart part,
            final MTabFolder tabFolder) {
        this.part = part;
        this.tabFolder = tabFolder;
        loadFromDefaultLocation();
        tabFolder.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Object page = event.item.getData();
                if (page instanceof IDashboardPage) {
                    handlePageSelected((IDashboardPage) page);
                }
            }
        });
        tabFolder.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                MTabItem item = tabFolder.getSelection();
                String pageId = (String) item.getData(DATA_ID);
                if (pageId != null) {
                    part.setPersistedState(STATE_SELECTED_PAGE_ID, pageId);
                }
            }
        });
    }

    private void loadFromDefaultLocation() {
        // load NLS properties
        nlsProperties = CathyPlugin.getDefault()
                .loadNLSProperties(NLS_PATH_BASE);

        // load content xml
        try {
            URL docURL = new URL(CONTENT_URI);
            loadFromURL(docURL);
        } catch (Exception e) {
            CathyPlugin.log(e,
                    "Failed to load dashboard content from " + CONTENT_URI); //$NON-NLS-1$
        }

        // set primary selection
        MTabItem primarySelection = findPrimarySelection();
        tabFolder.setSelection(primarySelection);

        IDashboardPage page = getDashboardPage(primarySelection);
        if (page != null) {
            if (page.getControl() == null || page.getControl().isDisposed()) {
                page.createControl(this.tabFolder.getBody());
                primarySelection.setControl(page.getControl());
            }
            handlePageSelected(page);
        }
    }

    private void loadFromURL(URL docURL) throws Exception {
        InputStream docStream = docURL.openStream();
        try {
            Document doc = XMLUtils.getDefaultDocumentBuilder()
                    .parse(docStream);

            Element rootElement = doc.getDocumentElement();
            if (rootElement == null
                    || !TAG_DASHBOARD.equals(rootElement.getNodeName()))
                throw new IllegalArgumentException(
                        "No 'dashboard' element in " + CONTENT_URI); //$NON-NLS-1$

            readElement(rootElement, tabFolder);
        } finally {
            docStream.close();
        }
    }

    private void readElement(Element element, Object parent) throws Exception {
        String tagName = element.getTagName();
        Object item = parent;
        if (parent instanceof MTabFolder) {
            if (TAG_PAGE.equals(tagName)) {
                item = readPage(element, (MTabFolder) parent);
            } else if (TAG_COMMAND.equals(tagName)) {
                item = readCommand(element, (MTabFolder) parent);
            } else if (TAG_SPACE.equals(tagName)) {
                item = readSeparator(element, (MTabFolder) parent);
            } else if (TAG_ITEM.equals(tagName)) {
                item = readSimpleItem(element, (MTabFolder) parent);
            }
        } else if (parent instanceof MTabItem) {
            if (TAG_PARAMETER.equals(tagName)) {
                readCommandParameter(element, (MTabItem) parent);
            }
        }

        readChildren(element.getChildNodes(), item);
    }

    private void readChildren(NodeList children, Object parent)
            throws Exception {
        int length = children.getLength();
        for (int i = 0; i < length; i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                readElement((Element) childNode, parent);
            }
        }
    }

    private MTabItem readPage(Element element, MTabFolder tabFolder)
            throws Exception {
        String id = element.getAttribute(ATTR_ID);
        if (id == null || "".equals(id)) //$NON-NLS-1$
            throw new IllegalArgumentException("No id for page."); //$NON-NLS-1$

        String contributionURI = element.getAttribute(ATTR_CONTRIBUTION_URI);
        if (contributionURI == null
                || !contributionURI.startsWith("bundleclass://")) //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "Invalid contributionURI: " + contributionURI); //$NON-NLS-1$
        String[] contributionPaths = contributionURI.substring(14).split("/"); //$NON-NLS-1$
        if (contributionPaths.length != 2)
            throw new IllegalArgumentException(
                    "Invalid contributionURI: " + contributionURI); //$NON-NLS-1$
        String bundleId = contributionPaths[0];
        String className = contributionPaths[1];
        Class<?> cls;
        try {
            Bundle bundle = Platform.getBundle(bundleId);
            if (bundle == null)
                throw new ClassNotFoundException();
            cls = bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            // ignore errors caused contribution not found
            return null;
        }

        Object contribution = ContextInjectionFactory.make(cls,
                part.getContext());
        if (!(contribution instanceof IDashboardPage))
            throw new IllegalArgumentException(
                    "Invalid contribution type: " + contribution); //$NON-NLS-1$

        final IDashboardPage page = (IDashboardPage) contribution;
        page.setContext(part);

        String label = readLabel(element);
        page.setTitle(label);

        ImageDescriptor icon = readIcon(element);
        page.setImageDescriptor(icon);

        // add side-bar tab
        MTabItem item = new MTabItem(tabFolder, SWT.RADIO);

        item.setText(page.getTitle());
        String tooltip = readTooltip(element);
        item.setTooltipText(tooltip);
        item.setImage(page.getImage());

//        page.createControl(this.tabFolder.getBody());
//        item.setControl(page.getControl());

        item.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                Control pageControl = page.getControl();
                if (pageControl != null) {
                    pageControl.dispose();
                }
                page.dispose();
            }
        });

        item.setData(page);
        item.setData(DATA_ID, id);

        return item;
    }

    private MTabItem readCommand(Element element, MTabFolder tabFolder)
            throws Exception {
        String id = element.getAttribute(ATTR_ID);
        if (id == null || "".equals(id)) //$NON-NLS-1$
            throw new IllegalArgumentException("No id for command."); //$NON-NLS-1$
        final String commandId = element.getAttribute(ATTR_COMMAND_ID);
        if (commandId == null || "".equals(commandId)) //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "No command id found for command"); //$NON-NLS-1$

        String label = readLabel(element);
        String tooltip = readTooltip(element);
        ImageDescriptor icon = readIcon(element);

        MTabItem item = new MTabItem(tabFolder, SWT.PUSH);

        String width = element.getAttribute(ATTR_WIDTH);
        if (width != null && !"".equals(width)) { //$NON-NLS-1$
            try {
                int widthValue = Integer.parseInt(width, 10);
                item.setWidth(widthValue);
            } catch (NumberFormatException e) {
            }
        }

        item.setText(label);
        item.setTooltipText(tooltip);

        final Image iconImage = icon == null ? null : icon.createImage();
        item.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                iconImage.dispose();
            }
        });
        item.setImage(iconImage);

        item.setData(DATA_ID, id);
        item.setData(DATA_PARAMETERS, new Properties());

        item.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                Properties commandParameters = (Properties) event.widget
                        .getData(DATA_PARAMETERS);
                if (commandParameters != null) {
                    for (Entry<Object, Object> en : commandParameters
                            .entrySet()) {
                        parameters.put((String) en.getKey(), en.getValue());
                    }
                }
                part.executeCommand(commandId, parameters);
            }
        });
        return item;
    }

    private MTabItem readSimpleItem(Element element, MTabFolder tabFolder)
            throws Exception {
        String id = element.getAttribute(ATTR_ID);
        if (id == null || "".equals(id)) //$NON-NLS-1$
            throw new IllegalArgumentException("No id for page."); //$NON-NLS-1$

        String label = readLabel(element);
        String tooltip = readTooltip(element);
        ImageDescriptor icon = readIcon(element);

        MTabItem item = new MTabItem(tabFolder, SWT.SIMPLE);
        item.setText(label);
        item.setTooltipText(tooltip);
        final Image iconImage = icon.createImage();
        item.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                iconImage.dispose();
            }
        });
        item.setImage(iconImage);

        item.setData(DATA_ID, id);

        return item;
    }

    private MTabItem readSeparator(Element element, MTabFolder tabFolder)
            throws Exception {
        String id = element.getAttribute(ATTR_ID);
        if (id == null || "".equals(id)) //$NON-NLS-1$
            throw new IllegalArgumentException("No id for page."); //$NON-NLS-1$

        MTabItem item = new MTabItem(tabFolder, SWT.SEPARATOR);

        item.setData(DATA_ID, id);

        String width = element.getAttribute(ATTR_WIDTH);
        if (VAL_FILL.equals(width)) {
            item.setWidth(SWT.SEPARATOR_FILL);
        } else if (width != null && !"".equals(width)) { //$NON-NLS-1$
            try {
                int widthValue = Integer.parseInt(width, 10);
                item.setWidth(widthValue);
            } catch (NumberFormatException e) {
            }
        }

        String color = element.getAttribute(ATTR_COLOR);
        if (color != null && !"".equals(color)) //$NON-NLS-1$
            item.setColor(color);

        return item;
    }

    private void readCommandParameter(Element element, MTabItem item) {
        String name = element.getAttribute(ATTR_NAME);
        String value = element.getAttribute(ATTR_VALUE);
        if (name == null || "".equals(name) //$NON-NLS-1$
                || value == null || "".equals(value)) //$NON-NLS-1$
            return;

        Properties parameters = (Properties) item.getData(DATA_PARAMETERS);
        if (parameters == null)
            return;

        parameters.put(name, value);
    }

    private ImageDescriptor readIcon(Element element)
            throws MalformedURLException {
        String iconURI = element.getAttribute(ATTR_ICON_URI);
        ImageDescriptor icon = (iconURI == null || "".equals(iconURI)) //$NON-NLS-1$
                ? null : ImageDescriptor.createFromURL(new URL(iconURI));
        return icon;
    }

    private String readLabel(Element element) {
        String label = element.getAttribute(ATTR_LABEL);
        if (label.startsWith("%")) { //$NON-NLS-1$
            String nativeLabel = nlsProperties.getProperty(label.substring(1));
            if (nativeLabel != null) {
                label = nativeLabel;
            }
        }
        return label;
    }

    private String readTooltip(Element element) {
        String tooltip = element.getAttribute(ATTR_TOOLTIP);
        if (tooltip.startsWith("%")) { //$NON-NLS-1$
            String nativeTooltip = nlsProperties
                    .getProperty(tooltip.substring(1));
            if (nativeTooltip != null) {
                tooltip = nativeTooltip;
            }
        }
        return tooltip;
    }

    private MTabItem findPrimarySelection() {
        String lastPageId = part.getPersistedState(STATE_SELECTED_PAGE_ID);
        if (lastPageId != null) {
            MTabItem item = getItemById(lastPageId);
            if (item != null)
                return item;
        }

        int itemCount = tabFolder.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            MTabItem item = tabFolder.getItem(i);
            if ((item.getStyle() & SWT.RADIO) != 0) {
                return item;
            }
        }
        return null;
    }

    public MTabItem getItemById(String pageId) {
        if (pageId == null)
            return null;
        int itemCount = tabFolder.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            MTabItem item = tabFolder.getItem(i);
            if (pageId.equals(item.getData(DATA_ID)))
                return item;
        }
        return null;
    }

    public String getItemId(MTabItem item) {
        return (String) item.getData(DATA_ID);
    }

    public IDashboardPage getDashboardPage(MTabItem item) {
        Object data = item.getData();
        return data instanceof IDashboardPage ? (IDashboardPage) data : null;
    }

    protected void handlePageSelected(final IDashboardPage page) {
        ISelectionProvider selectionProvider = CathyPlugin.getAdapter(page,
                ISelectionProvider.class);
        part.setSelectionProvider(selectionProvider);
    }

}
