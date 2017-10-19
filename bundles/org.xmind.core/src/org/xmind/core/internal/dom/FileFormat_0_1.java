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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_VERSION;

import java.io.IOException;

import org.xmind.core.CoreException;
import org.xmind.core.internal.compatibility.FileFormat;
import org.xmind.core.io.IInputSource;
import org.xmind.core.util.DOMUtils;

public class FileFormat_0_1 extends FileFormat {

    private static final String VERSION = "0.1"; //$NON-NLS-1$

    private static final String CONTENTS_XML = "contents.xml"; //$NON-NLS-1$

//    private static final String PATH_PICTURES = "Pictures"; //$NON-NLS-1$

    public FileFormat_0_1(DeserializerImpl deserializer) {
        super(deserializer);
    }

    public boolean identifies() throws CoreException, IOException {
        IInputSource inputSource = deserializer.getWorkbookStorage()
                .getInputSource();
        return inputSource.hasEntry(CONTENTS_XML)
                && inputSource.isEntryAvailable(CONTENTS_XML);
    }

    public WorkbookImpl load() throws CoreException, IOException {
        WorkbookImpl wb = new WorkbookImpl(deserializer.createDocument(),
                deserializer.getManifest());
        DOMUtils.setAttribute(wb.getWorkbookElement(), ATTR_VERSION, VERSION);

        //TODO load workbook content from old-formatted file

        return wb;
    }

}