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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.xmind.core.CoreException;
import org.xmind.core.io.IInputSource;
import org.xmind.core.util.IXMLLoader;

/**
 * @deprecated
 * @author Frank Shaka
 */
@Deprecated
public abstract class XMLLoader implements IXMLLoader {

    private Map<String, Document> loadedDocuments = null;

    /**
     * @deprecated
     */
    @Deprecated
    public Document loadXMLFile(IInputSource source, String entryName)
            throws IOException, CoreException {
        if (loadedDocuments != null) {
            Document document = loadedDocuments.get(entryName);
            if (document != null) {
                return document;
            }
        }

        Document document = doLoadXMLFile(source, entryName);

        if (loadedDocuments == null)
            loadedDocuments = new HashMap<String, Document>();
        loadedDocuments.put(entryName, document);
        return document;
    }

    /**
     * @deprecated
     */
    @Deprecated
    protected abstract Document doLoadXMLFile(IInputSource source,
            String entryName) throws IOException, CoreException;

}