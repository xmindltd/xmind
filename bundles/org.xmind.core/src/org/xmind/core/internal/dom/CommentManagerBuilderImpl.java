package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.TAG_COMMENTS;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.comment.ICommentManager;
import org.xmind.core.internal.CommentManagerBuilder;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.io.IInputSource;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.IXMLLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CommentManagerBuilderImpl extends CommentManagerBuilder implements
        ErrorHandler {

    private DocumentBuilder getDocumentCreator() {
        DocumentBuilder documentCreator = null;
        try {
            documentCreator = DOMUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        return documentCreator;
    }

    private DocumentBuilder getDocumentLoader() throws CoreException {
        DocumentBuilder documentLoader = null;
        try {
            documentLoader = DOMUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_PARSER, e);
        }
        documentLoader.setErrorHandler(this);
        return documentLoader;
    }

    private Document createDocument() {
        return getDocumentCreator().newDocument();
    }

    public ICommentManager createCommentManager() {
        Document impl = createDocument();
        DOMUtils.createElement(impl, TAG_COMMENTS);
        CommentManagerImpl commentManager = new CommentManagerImpl(impl);
        return commentManager;
    }

    public ICommentManager loadFromStream(InputStream stream)
            throws IOException, CoreException {
        DocumentBuilder loader = getDocumentLoader();
        Document doc = parse(loader, stream);
        return createCommentManager(doc);
    }

    public ICommentManager loadFromInputSource(IInputSource source,
            IXMLLoader xmlLoader) throws IOException, CoreException {
        Document doc = xmlLoader.loadXMLFile(source,
                ArchiveConstants.COMMENTS_XML);
        return createCommentManager(doc);
    }

    private ICommentManager createCommentManager(Document doc) {
        CommentManagerImpl commentManager = new CommentManagerImpl(doc);
//        init(commentManager);
        return commentManager;
    }

    private Document parse(DocumentBuilder loader, InputStream stream)
            throws IOException, CoreException {
        try {
            return loader.parse(stream);
        } catch (SAXException e) {
            throw new CoreException(Core.ERROR_FAIL_PARSING_XML);
        } catch (IOException e) {
            throw e;
        }
    }

    public void error(SAXParseException exception) throws SAXException {
    }

    public void fatalError(SAXParseException exception) throws SAXException {
    }

    public void warning(SAXParseException exception) throws SAXException {
    }

}
