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
package org.xmind.ui.mindmap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xmind.core.Core;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.CloneHandler;

public class MindMapExtractor {

    private static final String SUBDIR_EXPORT = "export"; //$NON-NLS-1$

    private static String DefaultDirectory = null;

    private ISheet sourceSheet;

    private ITopic sourceTopic;

    private Collection<IRelationship> sourceRels;

    private IWorkbook result;

    private IStorage tempStorage;

    public MindMapExtractor(IMindMapViewer viewer) {
        this(viewer, newTempStorage());
    }

    public MindMapExtractor(IMindMapViewer viewer, String tempLocation) {
        this(viewer, new DirectoryStorage(new File(tempLocation)));
    }

    public MindMapExtractor(IMindMapViewer viewer, IStorage tempStorage) {
        this.sourceSheet = viewer.getSheet();
        this.sourceTopic = viewer.getCentralTopic();
        List<IRelationshipPart> relParts = viewer.getSheetPart()
                .getRelationships();
        this.sourceRels = new ArrayList<IRelationship>(relParts.size());
        for (IRelationshipPart relPart : relParts) {
            this.sourceRels.add(relPart.getRelationship());
        }
        this.tempStorage = tempStorage;
    }

    public IWorkbook extract() throws IOException {
        if (result == null) {
            result = Core.getWorkbookBuilder().createWorkbook(tempStorage);
            result.getMarkerSheet().setParentSheet(
                    MindMapUI.getResourceManager().getSystemMarkerSheet());
            CloneHandler cloner = new CloneHandler()
                    .withWorkbooks(sourceSheet.getOwnedWorkbook(), result);
            ISheet newSheet = (ISheet) cloner.cloneObject(sourceSheet);
            result.addSheet(newSheet);
            result.removeSheet(result.getPrimarySheet());
        }
        return result;
    }

    public void delete() {
        tempStorage.clear();
    }

    @Deprecated
    public String getTempLocation() {
        if (tempStorage instanceof DirectoryStorage)
            return ((DirectoryStorage) tempStorage).getFullPath();
        return null;
    }

    /**
     * @return the tempStorage
     */
    public IStorage getTempStorage() {
        return tempStorage;
    }

    private static IStorage newTempStorage() {
        if (DefaultDirectory == null) {
            DefaultDirectory = Core.getWorkspace().getTempDir(SUBDIR_EXPORT);
        }
        String fileName = Core.getIdFactory().createId()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        File dir = new File(DefaultDirectory, fileName);
        dir.mkdirs();
        return new DirectoryStorage(dir);
    }

}
