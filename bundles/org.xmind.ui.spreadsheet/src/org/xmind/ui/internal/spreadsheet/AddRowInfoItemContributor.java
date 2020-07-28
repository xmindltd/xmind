package org.xmind.ui.internal.spreadsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.ArraySourceProvider;
import org.xmind.gef.EditDomain;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CreateTopicCommand;
import org.xmind.ui.commands.ModifyLabelCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.spreadsheet.structures.Chart2;
import org.xmind.ui.internal.spreadsheet.structures.Row2;
import org.xmind.ui.internal.spreadsheet.structures.SpreadsheetColumnStructure;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public class AddRowInfoItemContributor extends AbstractInfoItemContributor {

    private static class AddRowAction extends Action {

        private IViewer viewer;

        private ITopic chartTopic;

        public AddRowAction(IViewer viewer, ITopic chartTopic) {
            this.viewer = viewer;
            this.chartTopic = chartTopic;
        }

        public void run() {
            EditDomain domain = viewer.getEditDomain();
            if (domain == null)
                return;

            ICommandStack cs = domain.getCommandStack();
            if (cs == null)
                return;

            IPart part = viewer.findPart(chartTopic);
            IBranchPart branch = MindMapUtils.findBranch(part);
            if (branch == null)
                return;

            IStructure sa = branch.getBranchPolicy().getStructure(branch);
            if (sa instanceof SpreadsheetColumnStructure) {
                SpreadsheetColumnStructure ca = (SpreadsheetColumnStructure) sa;
                Chart2 chart = ca.getChart(branch);
                String newRowTitle = createRowHead(branch, chart);

                List<Command> cmds = new ArrayList<Command>();
                List<ITopic> children = chartTopic.getChildren(ITopic.ATTACHED);
                ISourceProvider colProvider;
                IWorkbook workbook = chartTopic.getOwnedWorkbook();
                int childrenSize;
                if (children.isEmpty()) {
                    CreateTopicCommand createCol = new CreateTopicCommand(
                            workbook);
                    cmds.add(createCol);
                    colProvider = createCol;
                    cmds.add(createSetTitleTextCommand(chartTopic.isRoot(),
                            chartTopic.getChildren(ITopic.ATTACHED).size(),
                            colProvider));
                    cmds.add(new AddTopicCommand(colProvider, chartTopic));
                    childrenSize = 0;
                } else {
                    ITopic colTopic = children.get(0);
                    colProvider = new ArraySourceProvider(colTopic);
                    childrenSize = colTopic.getChildren(ITopic.ATTACHED).size();
                }
                CreateTopicCommand createCell = new CreateTopicCommand(
                        workbook);
                cmds.add(createCell);
                cmds.add(createSetTitleTextCommand(false, childrenSize,
                        createCell));
                cmds.add(new ModifyLabelCommand(createCell,
                        Collections.singletonList(newRowTitle)));
                cmds.add(new AddTopicCommand(createCell, colProvider));
                cs.execute(new CompoundCommand(Messages.Command_AddRow, cmds));
                viewer.setSelection(
                        new StructuredSelection(createCell.getSource()));
            }
        }

        private Command createSetTitleTextCommand(boolean isRoot,
                int childrenSize, ISourceProvider sourceProvider) {
            String newTitle;
            int index = childrenSize + 1;
            if (isRoot) {
                newTitle = NLS.bind(MindMapMessages.TitleText_MainTopic, index);
            } else {
                newTitle = NLS.bind(MindMapMessages.TitleText_Subtopic, index);
            }
            return new ModifyTitleTextCommand(sourceProvider, newTitle);
        }

        private String createRowHead(IBranchPart branch, Chart2 chart) {
            int numRows = chart.getNumValidRows();
            String newColumnHead = NLS.bind(Messages.Label_pattern,
                    numRows + 1);
            while (containsRowHead(branch, chart, newColumnHead)) {
                numRows++;
                String newName = NLS.bind(Messages.Label_pattern, numRows + 1);
                if (newColumnHead.equals(newName))
                    break;
                newColumnHead = newName;
            }
            return newColumnHead;
        }

        private boolean containsRowHead(IBranchPart branch, Chart2 chart,
                String newColumnTitle) {
            for (Row2 row : chart.getRows()) {
                if (newColumnTitle.equals(row.getHead().toString()))
                    return true;
            }
            return false;
        }
    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        IBranchPart branch = MindMapUtils.findBranch(topicPart);
        if (branch != null) {
            IViewer viewer = branch.getSite().getViewer();
            if (viewer != null) {
                if (isStructureAlgorithmId(branch,
                        Spreadsheet.SPREADSHEET_STRUCTURE_COLUMN_ID)) {
                    return new AddRowAction(viewer, topic);
                }
            }
        }
        return null;
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        return "platform:/plugin/org.xmind.ui.spreadsheet/icons/add_row.svg"; //$NON-NLS-1$
    }

    private boolean isStructureAlgorithmId(IBranchPart branch,
            String expectedValue) {
        String id = (String) MindMapUtils.getCache(branch,
                IBranchPolicy.CACHE_STRUCTURE_ID);
//        if (id == null)
//            return expectedValue != null;
        return id != null && id.equals(expectedValue);
    }

    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
    }

    protected void handleTopicEvent(IInfoPart infoPart, CoreEvent event) {
    }

    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
    }

}
