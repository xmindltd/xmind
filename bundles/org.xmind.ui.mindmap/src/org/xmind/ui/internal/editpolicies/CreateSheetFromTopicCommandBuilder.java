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
package org.xmind.ui.internal.editpolicies;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.xmind.core.ICloneData;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.dom.WorkbookUtilsImpl;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.CreateSheetCommand;
import org.xmind.ui.commands.ModifyTopicHyperlinkCommand;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.StyleUtils;

/**
 * @author karelun huang
 * 
 */
public class CreateSheetFromTopicCommandBuilder extends CommandBuilder {

    private ITopic sourceTopic;

    public CreateSheetFromTopicCommandBuilder(IViewer viewer,
            ICommandStack commandStack, ITopic sourceTopic) {
        super(viewer, commandStack);
        this.sourceTopic = sourceTopic;
    }

    public void run() {
        IWorkbook workbook = sourceTopic.getOwnedWorkbook();
        CreateSheetCommand createCommand = new CreateSheetCommand(workbook,
                sourceTopic);
        add(createCommand, true);

        ISheet newSheet = (ISheet) createCommand.getSource();
        if (newSheet == null)
            return;

        ITopic newCentralTopic = newSheet.getRootTopic();
        String newHyperlink = newCentralTopic.getHyperlink();
        if (newHyperlink == null) {
            add(new ModifyTopicHyperlinkCommand(newCentralTopic,
                    HyperlinkUtils.toInternalURL(sourceTopic)), false);
        }
        add(new ModifyTopicHyperlinkCommand(sourceTopic,
                HyperlinkUtils.toInternalURL(newCentralTopic)), false);

        ISheet sourceSheet = sourceTopic.getOwnedSheet();
        if (sourceSheet != null) {
            String themeId = sourceSheet.getThemeId();
            IStyle theme = themeId == null ? null : sourceSheet
                    .getOwnedWorkbook().getStyleSheet().findStyle(themeId);
            if (theme == null)
                theme = MindMapUI.getResourceManager().getDefaultTheme();
            StyleUtils.setTheme(newSheet, theme);
        }

        ICloneData cloneData = createCommand.getCloneData();

//      newSheet.setTitleText(NLS.bind(MindMapMessages.TitleText_Sheet,
//      workbook.getSheets().size()));
        newSheet.setTitleText(sourceTopic.getTitleText());

        String newStructure = newStructure(sourceTopic);
        if (newStructure != null)
            newCentralTopic.setStructureClass(newStructure);

        Set<IRelationship> sourceRelationships = sourceSheet.getRelationships();
        if (sourceRelationships.size() > 0)
            cloneRelationships(workbook, newSheet, cloneData,
                    sourceRelationships);
    }

    private void cloneRelationships(IWorkbook workbook, ISheet newSheet,
            ICloneData cloneData, Set<IRelationship> sourceRelationships) {
        Iterator<IRelationship> iter = sourceRelationships.iterator();
        while (iter.hasNext()) {
            IRelationship r = iter.next();
            cloneRelationship(workbook, newSheet, cloneData, r);
        }
    }

    private void cloneRelationship(IWorkbook workbook, ISheet newSheet,
            ICloneData cloneData, IRelationship sourceRelationship) {
        String source1Id = sourceRelationship.getEnd1Id();
        String source2Id = sourceRelationship.getEnd2Id();
        String target1Id = source1Id == null ? null : cloneData.getString(
                ICloneData.WORKBOOK_COMPONENTS, source1Id);
        String target2Id = source2Id == null ? null : cloneData.getString(
                ICloneData.WORKBOOK_COMPONENTS, source2Id);
        if (target1Id == null || target2Id == null)
            return;

        Object target1 = workbook.getElementById(target1Id);
        Object target2 = workbook.getElementById(target2Id);
        if (target1 instanceof IRelationshipEnd
                && target2 instanceof IRelationshipEnd) {
            ICloneData cloned = WorkbookUtilsImpl.clone(workbook,
                    Arrays.asList(sourceRelationship), cloneData);
            IRelationship newRelationship = (IRelationship) cloned
                    .get(sourceRelationship);
            if (newRelationship != null) {
                newSheet.addRelationship(newRelationship);
            }
        }
    }

    private String newStructure(ITopic topic) {
        String structure = topic.getStructureClass();
        if (structure != null) {
            if ("org.xmind.ui.map.floating".equals(structure)) {//$NON-NLS-1$
                structure = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
            } else if ("org.xmind.ui.map.floating.clockwise".equals(structure)) { //$NON-NLS-1$
                structure = "org.xmind.ui.map.clockwise"; //$NON-NLS-1$
            } else if ("org.xmind.ui.map.floating.anticlockwise".equals(structure)) { //$NON-NLS-1$
                structure = "org.xmind.ui.map.anticlockwise"; //$NON-NLS-1$
            }
        }
        return structure;
    }
}
