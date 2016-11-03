package org.xmind.ui.internal.imports.opml;

import static org.xmind.ui.internal.imports.opml.OpmlConstants.ATT_TEXT;
import static org.xmind.ui.internal.imports.opml.OpmlConstants.ATT_TYPE;
import static org.xmind.ui.internal.imports.opml.OpmlConstants.ATT_URL;
import static org.xmind.ui.internal.imports.opml.OpmlConstants.TAG_BODY;
import static org.xmind.ui.internal.imports.opml.OpmlConstants.TYPE_HYPERLINK;
import static org.xmind.ui.internal.imports.opml.OpmlConstants.TYPE_NOTE;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.INotes;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.wizards.MindMapImporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class OpmlImporter extends MindMapImporter implements ErrorHandler {

    private ISheet sheet;

    public OpmlImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    @Override
    public void build() throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .increase("ImportFromOpmlCount"); //$NON-NLS-1$
        InputStream in = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
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
        sheet = targetWorkbook.createSheet();
        sheet.setTitleText("sheet1"); //$NON-NLS-1$
        NodeList nodelist = rootElement.getChildNodes();
        Element bodyEle = (Element) nodelist.item(1);
        if (TAG_BODY.equals(bodyEle.getTagName())) {
            loadAll(bodyEle);
        }
        addTargetSheet(sheet);
    }

    private void loadAll(Element bodyEle) {
        NodeList nodelist = bodyEle.getChildNodes();
        if (nodelist.getLength() > 1)
            return;

        Node child = nodelist.item(0);
        if (child instanceof Element) {
            Element rootEle = (Element) child;
            ITopic rootTopic = sheet.getRootTopic();
            dealRootTopic(rootEle, rootTopic);
            loadTopic(rootEle, rootTopic);
        }
    }

    private void dealRootTopic(Element ele, ITopic rootTopic) {
        rootTopic.setTitleText(att(ele, ATT_TEXT));
    }

    private void loadTopic(Element ele, ITopic parent) {
        pushInMap(ele, parent);
        if (TYPE_HYPERLINK.equals(att(ele, ATT_TYPE)))
            loadHyperLink(ele, parent);
        NodeList children = ele.getChildNodes();
        int lenght = children.getLength();
        for (int index = 0; index < lenght; index++) {
            Node childNode = children.item(index);
            if (childNode instanceof Element) {
                Element child = (Element) childNode;
                String type = att(child, ATT_TYPE);
                if (TYPE_NOTE.equals(type))
                    loadNotes(child, parent);
                else {
                    ITopic topic = dealTopic(child, parent);
                    loadTopic(child, topic);
                }
            }
        }
    }

    private ITopic dealTopic(Element ele, ITopic parent) {
        ITopic topic = getTargetWorkbook().createTopic();
        topic.setTitleText(att(ele, ATT_TEXT));
        parent.add(topic, ITopic.ATTACHED);
        return topic;
    }

    private void pushInMap(Element ele, ITopic parent) {
        parent.setTitleText(att(ele, ATT_TEXT));
    }

    private String att(Element ele, String attTag) {
        if (ele.hasAttribute(attTag))
            return ele.getAttribute(attTag);
        return null;
    }

    private void loadHyperLink(Element ele, ITopic topic) {
        String link = att(ele, ATT_URL);
        if (link != null && isLinkToWeb(link)) {
            topic.setHyperlink(link);
        }
    }

    private void loadNotes(Element ele, ITopic parent) {
        String text = att(ele, ATT_TEXT);
        if (text != null && !text.trim().equals("")) { //$NON-NLS-1$
            IPlainNotesContent notesContent = (IPlainNotesContent) getTargetWorkbook()
                    .createNotesContent(INotes.PLAIN);
            notesContent.setTextContent(text);
            parent.getNotes().setContent(INotes.PLAIN, notesContent);
        }
    }

    private boolean isLinkToWeb(String hyperlink) {
        if (hyperlink.contains("www.") || hyperlink.contains(".com") //$NON-NLS-1$//$NON-NLS-2$
                || hyperlink.contains(".cn") || hyperlink.contains(".org") //$NON-NLS-1$//$NON-NLS-2$
                || hyperlink.contains(".cc") || hyperlink.contains(".net") //$NON-NLS-1$//$NON-NLS-2$
                || hyperlink.contains(".ren")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    private void checkInterrupted() throws InterruptedException {
        if (getMonitor().isCanceled())
            throw new InterruptedException();
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
