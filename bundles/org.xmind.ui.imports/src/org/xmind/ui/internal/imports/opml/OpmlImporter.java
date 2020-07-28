package org.xmind.ui.internal.imports.opml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmind.core.INotes;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.util.DOMUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.util.XMLUtils;
import org.xmind.ui.wizards.MindMapImporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class OpmlImporter extends MindMapImporter
        implements ErrorHandler, OpmlConstants {

    private ISheet targetSheet;

    public OpmlImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    @Override
    public void build() throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_IMPORT,
                UserDataConstants.IMPORT_FROM_OPML);

        InputStream in = null;
        try {
            DocumentBuilder builder = XMLUtils.getDefaultDocumentBuilder();
            builder.setErrorHandler(this);

            in = new FileInputStream(getSourcePath());
            Document doc = builder.parse(in);

            checkInterrupted();
            Element rootElement = doc.getDocumentElement();

            loadSheet(rootElement);

        } catch (Throwable e) {
            throw new InvocationTargetException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        postBuilded();
    }

    private void loadSheet(Element rootElement) throws InterruptedException {
        checkInterrupted();

        IWorkbook targetWorkbook = getTargetWorkbook();
        targetSheet = targetWorkbook.createSheet();

        Element bodyEle = child(rootElement, TAG_BODY);
        if (bodyEle != null) {
            loadContents(bodyEle);
        }

        Element headEle = child(rootElement, TAG_HEAD);
        if (headEle != null) {
            loadSheetProperties(headEle);
        }

        addTargetSheet(targetSheet);
    }

    private void loadContents(Element bodyEle) throws InterruptedException {
        checkInterrupted();

        Element rootTopicEle = child(bodyEle, TAG_OUTLINE);
        if (rootTopicEle != null) {
            ITopic rootTopic = targetSheet.getRootTopic();
            loadTopicContent(rootTopicEle, rootTopic);
        }

        Iterator<Element> children = children(bodyEle, TAG_OUTLINE);
        while (children.hasNext()) {
            Element next = children.next();
            if (next != rootTopicEle) {
                loadFloatingTopic(next, targetSheet.getRootTopic());
            }
        }
    }

    private void loadTopicContent(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        topic.setTitleText(attr(topicEle, ATTR_TEXT));

        if (TYPE_LINK.equals(attr(topicEle, ATTR_TYPE)))
            loadHyperlink(topicEle, topic);

        Iterator<Element> children = children(topicEle, TAG_OUTLINE);
        while (children.hasNext()) {
            Element next = children.next();
            String type = attr(next, ATTR_TYPE);
            if (TYPE_NOTE.equals(type)) {
                loadNotes(next, topic);
            } else {
                loadSubTopic(next, topic);
            }
        }
    }

    private void loadHyperlink(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        String link = attr(topicEle, ATTR_URL);
        if (link != null && isLinkToWeb(link)) {
            String hyperlink = link;
            if (!(hyperlink.startsWith("http://") //$NON-NLS-1$
                    || hyperlink.startsWith("https://"))) { //$NON-NLS-1$
                hyperlink = "http://" + hyperlink; //$NON-NLS-1$
            }
            topic.setHyperlink(link);
        }
    }

    private void loadNotes(Element topicEle, ITopic topic)
            throws InterruptedException {
        checkInterrupted();

        String text = attr(topicEle, ATTR_TEXT);
        if (text != null && !text.trim().equals("")) { //$NON-NLS-1$
            IPlainNotesContent notesContent = (IPlainNotesContent) getTargetWorkbook()
                    .createNotesContent(INotes.PLAIN);
            notesContent.setTextContent(text);
            topic.getNotes().setContent(INotes.PLAIN, notesContent);
        }
    }

    private void loadSubTopic(Element topicEle, ITopic parent)
            throws InterruptedException {
        checkInterrupted();

        ITopic subTopic = getTargetWorkbook().createTopic();
        parent.add(subTopic, ITopic.ATTACHED);
        loadTopicContent(topicEle, subTopic);
    }

    private void loadFloatingTopic(Element floatingTopicEle, ITopic parent)
            throws InterruptedException {
        checkInterrupted();

        ITopic subTopic = getTargetWorkbook().createTopic();
        parent.add(subTopic, ITopic.DETACHED);
        loadTopicContent(floatingTopicEle, subTopic);
    }

    private void loadSheetProperties(Element headEle)
            throws InterruptedException {
        checkInterrupted();

        Element titleEle = child(headEle, TAG_TITLE);
        if (titleEle != null) {
            targetSheet.setTitleText(titleEle.getTextContent());
        } else {
            targetSheet.setTitleText(getSuggestedSheetTitle());
        }
    }

    private void checkInterrupted() throws InterruptedException {
        if (getMonitor().isCanceled())
            throw new InterruptedException();
    }

    private static Element child(Element parentEle, String childTag) {
        return children(parentEle, childTag).next();
    }

    private static Iterator<Element> children(final Element parentEle,
            final String childTag) {
        return new Iterator<Element>() {

            String tag = DOMUtils.getLocalName(childTag);

            Iterator<Element> it = DOMUtils.childElementIter(parentEle);

            Element next = findNext();

            public void remove() {
            }

            private Element findNext() {
                while (it.hasNext()) {
                    Element ele = it.next();
                    if (DOMUtils.getLocalName(ele.getTagName())
                            .equalsIgnoreCase(tag)) {
                        return ele;
                    }
                }
                return null;
            }

            public Element next() {
                Element result = next;
                next = findNext();
                return result;
            }

            public boolean hasNext() {
                return next != null;
            }
        };
    }

    private static String attr(Element ele, String attName) {
        if (ele == null || attName == null)
            return null;

        if (ele.hasAttribute(attName))
            return ele.getAttribute(attName);

        attName = DOMUtils.getLocalName(attName);
        NamedNodeMap atts = ele.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node att = atts.item(i);
            if (attName.equalsIgnoreCase(
                    DOMUtils.getLocalName(att.getNodeName()))) {
                return att.getNodeValue();
            }
        }
        return null;
    }

    private static boolean isLinkToWeb(String urlOrBookmark) {
        if (urlOrBookmark.contains("www.") || urlOrBookmark.contains(".com") //$NON-NLS-1$ //$NON-NLS-2$
                || urlOrBookmark.contains(".cn") //$NON-NLS-1$
                || urlOrBookmark.contains(".org") //$NON-NLS-1$
                || urlOrBookmark.contains(".cc") //$NON-NLS-1$
                || urlOrBookmark.contains(".net")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    public void warning(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void error(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

}
