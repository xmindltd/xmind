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
package org.xmind.ui.internal.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class AllMarkersMenu extends CompoundContributionItem
        implements IWorkbenchContribution {

    private IServiceLocator serviceLocator;

    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        List<IContributionItem> items = new ArrayList<IContributionItem>();

        if (serviceLocator != null) {
            fillItems(items,
                    MindMapUI.getResourceManager().getSystemMarkerSheet());
        }

        return items.toArray(new IContributionItem[items.size()]);
    }

    private void fillItems(List<IContributionItem> items,
            IMarkerSheet markerSheet) {
        for (final IMarkerGroup group : markerSheet.getMarkerGroups()) {
            MenuManager groupMenu = new MenuManager(group.getName(), "#" //$NON-NLS-1$
                    + group.getId());
            if (!group.isHidden()) {
                fillGroup(group, groupMenu);
                items.add(groupMenu);
            }
        }
    }

    private void fillGroup(IMarkerGroup group, IMenuManager groupMenu) {
        for (IMarker marker : group.getMarkers()) {
            if (!marker.isHidden()) {
                groupMenu.add(makeMarkerCommandContributionItem(marker));
            }
        }
    }

    private IContributionItem makeMarkerCommandContributionItem(
            IMarker marker) {
        CommandContributionItemParameter parameter = new CommandContributionItemParameter(
                serviceLocator, "addMarker." + marker.getId(), //$NON-NLS-1$
                MindMapCommandConstants.ADD_MARKER,
                CommandContributionItem.STYLE_PUSH);
        parameter.label = marker.getName();
        parameter.icon = MarkerImageDescriptor.createFromMarker(marker);
        Map<Object, Object> params = new HashMap<Object, Object>();
        params.put(MindMapCommandConstants.ADD_MARKER_PARAM_MARKER_ID,
                marker.getId());
        parameter.parameters = params;
        return new CommandContributionItem(parameter);
    }

}