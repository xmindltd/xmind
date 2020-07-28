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
import org.xmind.ui.internal.spreadsheet.structures.Chart;
import org.xmind.ui.internal.spreadsheet.structures.Column;
import org.xmind.ui.internal.spreadsheet.structures.SpreadsheetStructure;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public class AddColumnInfoItemContributor extends AbstractInfoItemContributor {

    private static class AddColumnAction extends Action {

        private IViewer viewer;

        private ITopic chartTopic;

        public AddColumnAction(IViewer viewer, ITopic chartTopic) {
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
            if (sa instanceof SpreadsheetStructure) {
                SpreadsheetStructure ca = (SpreadsheetStructure) sa;
                Chart chart = ca.getChart(branch);
                String newColumnTitle = createColumnHead(branch, chart);

                List<Command> cmds = new ArrayList<Command>();
                List<ITopic> children = chartTopic.getChildren(ITopic.ATTACHED);
                ISourceProvider rowProvider;
                IWorkbook workbook = chartTopic.getOwnedWorkbook();
                int childrenSize;
                if (children.isEmpty()) {
                    CreateTopicCommand createRow = new CreateTopicCommand(
                            workbook);
                    cmds.add(createRow);
                    rowProvider = createRow;
                    cmds.add(createSetTitleTextCommand(chartTopic.isRoot(),
                            chartTopic.getChildren(ITopic.ATTACHED).size(),
                            rowProvider));
//                    cmds.add(new ModifyTitleTextCommand(EMPTY, rowProvider,
//                            "Row"));
                    cmds.add(new AddTopicCommand(rowProvider, chartTopic));
                    childrenSize = 0;
                } else {
                    ITopic rowTopic = children.get(0);
                    rowProvider = new ArraySourceProvider(rowTopic);
                    childrenSize = rowTopic.getChildren(ITopic.ATTACHED).size();
                }
                CreateTopicCommand createCell = new CreateTopicCommand(
                        workbook);
                cmds.add(createCell);
                cmds.add(createSetTitleTextCommand(false, childrenSize,
                        createCell));
                //cmds.add(new ModifyTitleTextCommand(EMPTY, createCell, "Item"));
                cmds.add(new ModifyLabelCommand(createCell,
                        Collections.singletonList(newColumnTitle)));
                cmds.add(new AddTopicCommand(createCell, rowProvider));
                cs.execute(
                        new CompoundCommand(Messages.Command_AddColumn, cmds));
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

        private String createColumnHead(IBranchPart branch, Chart chart) {
            int numCols = chart.getNumValidColumns();
            String newColumnHead = NLS.bind(Messages.Label_pattern,
                    numCols + 1);
            while (containsColumnHead(branch, chart, newColumnHead)) {
                numCols++;
                String newName = NLS.bind(Messages.Label_pattern, numCols + 1);
                if (newColumnHead.equals(newName))
                    break;
                newColumnHead = newName;
            }
            return newColumnHead;
        }

        private boolean containsColumnHead(IBranchPart branch, Chart chart,
                String newColumnTitle) {
            for (Column col : chart.getColumns()) {
                if (newColumnTitle.equals(col.getHead().toString()))
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
                        Spreadsheet.SPREADSHEET_STRUCTURE_ROW_ID)) {
                    return new AddColumnAction(viewer, topic);
                }
            }
        }
        return null;
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        return "platform:/plugin/org.xmind.ui.spreadsheet/icons/add_column.svg"; //$NON-NLS-1$
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
