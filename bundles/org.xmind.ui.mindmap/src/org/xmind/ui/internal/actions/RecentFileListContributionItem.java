package org.xmind.ui.internal.actions;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.MindMapUI;

public class RecentFileListContributionItem extends CompoundContributionItem
        implements IWorkbenchContribution {

    private IServiceLocator serviceLocator;

    public RecentFileListContributionItem() {
    }

    public RecentFileListContributionItem(String id) {
        super(id);
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        List<IContributionItem> items = new ArrayList<IContributionItem>();

        fillItems(items);

        IContributionItem[] itemArray = new IContributionItem[items.size()];
        items.toArray(itemArray);
        return itemArray;
    }

    private void fillItems(List<IContributionItem> items) {
        if (serviceLocator == null)
            return;

        int itemsToShow = WorkbenchPlugin.getDefault().getPreferenceStore()
                .getInt(IPreferenceConstants.RECENT_FILES);
        if (itemsToShow <= 0)
            return;

        URI[] inputURIs = MindMapUI.getEditorHistory()
                .getRecentInputURIs(itemsToShow);
        if (inputURIs.length == 0)
            return;

        Map<URI, String> labels = new HashMap<URI, String>(inputURIs.length);
        FilePathParser.calculateFileURILabels(inputURIs, labels);

        for (int index = 0; index < inputURIs.length; index++) {
            URI inputURI = inputURIs[index];
            items.add(makeHistoryCommandItem(inputURI, index,
                    labels.get(inputURI)));
        }
    }

    private IContributionItem makeHistoryCommandItem(URI item, int index,
            String label) {
        String indexStr = Integer.toString(index + 1);
        String id = "org.xmind.ui.file.recent." + indexStr; //$NON-NLS-1$
        CommandContributionItemParameter parameter = new CommandContributionItemParameter(
                serviceLocator, id, MindMapCommandConstants.OPEN_WORKBOOK,
                CommandContributionItem.STYLE_PUSH);

        if (label == null) {
            label = item.toString();
        }
        parameter.label = String.format("&%s %s", indexStr, label); //$NON-NLS-1$
        Map<Object, Object> params = new HashMap<Object, Object>();
        params.put(MindMapCommandConstants.OPEN_WORKBOOK_PARAM_URI,
                item.toString());
        parameter.parameters = params;
        return new CommandContributionItem(parameter);
    }

    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

}
