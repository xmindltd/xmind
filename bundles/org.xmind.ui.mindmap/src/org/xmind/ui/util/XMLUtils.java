package org.xmind.ui.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUtils {

    private static final ErrorHandler NULL_ERROR_HANDLER = new ErrorHandler() {

        public void warning(SAXParseException exception) throws SAXException {
        }

        public void fatalError(SAXParseException exception)
                throws SAXException {
        }

        public void error(SAXParseException exception) throws SAXException {
        }

    };

    public static DocumentBuilder getDefaultDocumentBuilder()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setAttribute(
                    "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
                    true);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl", //$NON-NLS-1$
                    true);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(NULL_ERROR_HANDLER);
        return documentBuilder;
    }

    public static DocumentBuilder getSvgDocumentBuilder()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setAttribute(
                    "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
                    true);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl", //$NON-NLS-1$
                    true);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }

        /// settings for svg
        try {
            factory.setFeature("http://xml.org/sax/features/namespaces", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature("http://xml.org/sax/features/validation", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        factory.setValidating(false);
        ///

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(NULL_ERROR_HANDLER);
        return documentBuilder;
    }

}
