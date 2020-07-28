/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.CloneHandler;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.LazyPasswordBasedEncryptor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.MindMapImporter;

public class XMind2008Importer extends MindMapImporter {

    public XMind2008Importer(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_IMPORT,
                UserDataConstants.IMPORT_FROM_X_MIND2008);
        try {
            IStorage storage = createStorage();
            try {
                Core.getWorkbookBuilder().setEntryStreamNormalizer(
                        new LazyPasswordBasedEncryptor());
                IWorkbook sourceWorkbook = Core.getWorkbookBuilder()
                        .loadFromPath(getSourcePath(), storage, null);

                new CloneHandler()
                        .withWorkbooks(sourceWorkbook, getTargetWorkbook())
                        .copyWorkbookContents();
            } finally {
                storage.clear();
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        postBuilded();
    }

    private IStorage createStorage() {
        String tempFile = Core.getIdFactory().createId()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        String tempLocation = Core.getWorkspace()
                .getTempDir("workbooks" + "/" + tempFile); //$NON-NLS-1$ //$NON-NLS-2$
        File tempDir = new File(tempLocation);
        return new DirectoryStorage(tempDir);
    }

}
