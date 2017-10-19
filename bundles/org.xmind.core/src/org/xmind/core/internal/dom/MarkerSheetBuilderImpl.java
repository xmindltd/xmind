/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_SHEET;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.internal.MarkerSheetBuilder;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResourceProvider;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.DOMUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MarkerSheetBuilderImpl extends MarkerSheetBuilder
        implements ErrorHandler {

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

    public IMarkerSheet createMarkerSheet(
            IMarkerResourceProvider resourceProvider) {
        Document impl = createDocument();
        DOMUtils.createElement(impl, TAG_MARKER_SHEET);
        MarkerSheetImpl sheet = new MarkerSheetImpl(impl, resourceProvider);
        return sheet;
    }

    private Document createDocument() {
        return getDocumentCreator().newDocument();
    }

    public IMarkerSheet loadFromStream(InputStream stream,
            IMarkerResourceProvider resourceProvider)
                    throws IOException, CoreException {
        DocumentBuilder loader = getDocumentLoader();
        Document doc = parse(loader, stream);
        return createMarkerSheet(doc, resourceProvider);
    }

    protected MarkerSheetImpl createMarkerSheet(Document doc,
            IMarkerResourceProvider resourceProvider) {
        MarkerSheetImpl sheet = new MarkerSheetImpl(doc, resourceProvider);
        init(sheet);
        return sheet;
    }

//    public IMarkerSheet loadFromInputSource(IInputSource source,
//            IXMLLoader xmlLoader, IMarkerResourceProvider resourceProvider)
//                    throws IOException, CoreException {
//        Document doc = xmlLoader.loadXMLFile(source, PATH_MARKER_SHEET);
//        return createMarkerSheet(doc, resourceProvider);
//    }

    private void init(MarkerSheetImpl sheet) {
        for (IMarkerGroup group : sheet.getMarkerGroups()) {
            initGroup(group);
        }
    }

    private void initGroup(IMarkerGroup group) {
        for (IMarker marker : group.getMarkers()) {
            initMarker(marker);
        }
    }

    private void initMarker(IMarker marker) {
    }

    private Document parse(DocumentBuilder loader, InputStream stream)
            throws IOException, CoreException {
        try {
            return loader.parse(stream);
        } catch (SAXException e) {
            throw new CoreException(Core.ERROR_FAIL_PARSING_XML, e);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException ignore) {
            }
        }
    }

    public void error(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception);
    }

    public void warning(SAXParseException exception) throws SAXException {
        Core.getLogger().log(exception);
    }

    public void loadProperties(InputStream stream, IMarkerSheet sheet)
            throws IOException, CoreException {
        Properties p = new Properties();
        p.load(stream);
        ((MarkerSheetImpl) sheet).setProperties(p);
    }

}