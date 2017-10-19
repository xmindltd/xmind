package org.xmind.ui.internal.svgsupport;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SvgFileLoader {

    private static SvgFileLoader instance;

    private SvgFileLoader() {
    }

    public String loadSvgFile(String svgFilePath) {
        String prefix = "platform:/plugin/"; //$NON-NLS-1$
        try {
            URL url = new URL(prefix + svgFilePath);
            return loadSvgFile(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ""; //$NON-NLS-1$
    }

    public String loadSvgFile(URL url) {
        InputStream stream = null;
        try {
            stream = url.openStream();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(true);
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
                Document document = factory.newDocumentBuilder().parse(stream);

                Element element = document.getDocumentElement();

                NodeList ps = element.getElementsByTagName("path"); //$NON-NLS-1$
                Element svgPath = (Element) ps.item(0);
                return svgPath.getAttribute("d"); //$NON-NLS-1$
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                    stream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    public static SvgFileLoader getInstance() {
        if (instance == null)
            instance = new SvgFileLoader();
        return instance;
    }

}
