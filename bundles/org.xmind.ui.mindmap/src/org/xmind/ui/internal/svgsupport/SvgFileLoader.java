package org.xmind.ui.internal.svgsupport;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmind.ui.util.XMLUtils;
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
                Document document = XMLUtils.getSvgDocumentBuilder()
                        .parse(stream);

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
