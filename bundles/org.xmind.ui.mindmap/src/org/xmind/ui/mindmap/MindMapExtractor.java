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
package org.xmind.ui.mindmap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;

public class MindMapExtractor {

    private static final String SUBDIR_EXPORT = "export"; //$NON-NLS-1$

    private static String DefaultDirectory = null;

    private ISheet sourceSheet;

    private ITopic sourceTopic;

    private Collection<IRelationship> sourceRels;

    private IWorkbook result;

    private String tempLocation;

    public MindMapExtractor(IMindMapViewer viewer) {
        this(viewer, newTempLocation());
    }

    public MindMapExtractor(IMindMapViewer viewer, String tempLocation) {
        this.sourceSheet = viewer.getSheet();
        this.sourceTopic = viewer.getCentralTopic();
        List<IRelationshipPart> relParts = viewer.getSheetPart()
                .getRelationships();
        this.sourceRels = new ArrayList<IRelationship>(relParts.size());
        for (IRelationshipPart relPart : relParts) {
            this.sourceRels.add(relPart.getRelationship());
        }
        this.tempLocation = tempLocation;
    }

    public IWorkbook extract() {
        if (result == null) {
            result = Core.getWorkbookBuilder().createWorkbook();
            result.getMarkerSheet().setParentSheet(
                    MindMapUI.getResourceManager().getUserMarkerSheet());
            result.setTempLocation(tempLocation);

            ICloneData cloneResult = result.clone(Arrays.asList(sourceSheet));

            ISheet newSheet = (ISheet) cloneResult.get(sourceSheet);
            result.addSheet(newSheet);
            result.removeSheet(result.getPrimarySheet());

            String newTopicId = cloneResult.getString(
                    ICloneData.WORKBOOK_COMPONENTS, sourceTopic.getId());
            ITopic newRootTopic = result.findTopic(newTopicId);
            if (newRootTopic != null)
                newSheet.replaceRootTopic(newRootTopic);

            Set<String> newRelIds = new HashSet<String>(sourceRels.size());
            for (IRelationship sr : sourceRels) {
                String newRelId = cloneResult.getString(
                        ICloneData.WORKBOOK_COMPONENTS, sr.getId());
                if (newRelId != null)
                    newRelIds.add(newRelId);
            }

            Set<IRelationship> newRels = new HashSet<IRelationship>(
                    newSheet.getRelationships());
            for (IRelationship r : newRels) {
                if (!newRelIds.contains(r.getId())) {
                    newSheet.removeRelationship(r);
                }
            }
        }
        return result;
    }

    public void delete() {
        FileUtils.delete(new File(tempLocation));
    }

    private static String newTempLocation() {
        if (DefaultDirectory == null) {
            DefaultDirectory = Core.getWorkspace().getTempDir(SUBDIR_EXPORT);
        }
        String fileName = Core.getIdFactory().createId()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        return new File(DefaultDirectory, fileName).getAbsolutePath();
    }

}