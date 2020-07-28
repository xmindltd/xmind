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
package org.xmind.ui.internal.mindmap;

import static org.xmind.core.ISheetSettings.INFO_ITEM;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.jface.action.IAction;
import org.xmind.core.Core;
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.actions.ActionRegistry;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.ui.internal.InfoItemContributorManager;
import org.xmind.ui.internal.decorators.InformationDecorator;
import org.xmind.ui.internal.figures.InformationFigure;
import org.xmind.ui.internal.layouts.InformationLayout;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IInfoItemContributor;
import org.xmind.ui.mindmap.IInfoItemPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.ITopicPart;

public class InfoPart extends MindMapPartBase implements IInfoPart {

    private List<IInfoItemPart> infoItemIcons = null;

    private List<InfoItemContentPart> infoItemContents = null;

    private IActionRegistry actionRegistry = null;

    private List<IAction> actionList = null;

    private ChildSorter sorter = new ChildSorter(this);

    public InfoPart() {
        setDecorator(InformationDecorator.getInstance());
    }

    protected IFigure createFigure() {
        return new InformationFigure();
    }

    public List<IInfoItemPart> getInfoItemIcons() {
        if (infoItemIcons == null)
            infoItemIcons = new ArrayList<IInfoItemPart>();
        return infoItemIcons;
    }

    public void addInfoItemIcon(IInfoItemPart infoItem) {
        getInfoItemIcons().add(infoItem);
        sorter.sort(getInfoItemIcons());
    }

    public void removeInfoItemIcon(IInfoItemPart infoItem) {
        getInfoItemIcons().remove(infoItem);
        sorter.sort(getInfoItemIcons());
    }

    public List<InfoItemContentPart> getInfoItemContents() {
        if (infoItemContents == null)
            infoItemContents = new ArrayList<InfoItemContentPart>();
        return infoItemContents;
    }

    public void addInfoItemContent(InfoItemContentPart infoItem) {
        getInfoItemContents().add(infoItem);
        sorter.sort(getInfoItemContents());
    }

    public void removeInfoItemContent(InfoItemContentPart infoItem) {
        getInfoItemContents().remove(infoItem);
        sorter.sort(getInfoItemContents());
    }

    public IBranchPart getOwnedBranch() {
        if (getParent() instanceof IBranchPart)
            return ((IBranchPart) getParent());
        return null;
    }

    public ITopic getTopic() {
        return (ITopic) super.getRealModel();
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof BranchPart) {
            return ((BranchPart) getParent()).getTopicPart();
        }
        return null;
    }

    public boolean hasActions() {
        return actionList != null && !actionList.isEmpty();
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            if (branch.getInfoPart() == this) {
                branch.setinfoPart(null);
            }
        }
        super.setParent(parent);
        if (getParent() instanceof BranchPart) {
            BranchPart branch = (BranchPart) getParent();
            branch.setinfoPart(this);
        }
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == IActionRegistry.class)
            return getActionRegistry();
        if (adapter == InfoItemIcon.class)
            return null;
        return super.getAdapter(adapter);
    }

    private IActionRegistry getActionRegistry() {
        if (actionRegistry == null) {
            actionRegistry = new ActionRegistry();
            IViewer viewer = getSite().getViewer();
            if (viewer != null) {
                actionRegistry.setParent((IActionRegistry) viewer
                        .getProperties().get(IMindMapViewer.VIEWER_ACTIONS));
            }
        }
        return actionRegistry;
    }

    @Override
    protected LayoutManager createLayoutManager() {
        return new InformationLayout(this);
    }

    @Override
    protected Object[] getModelChildren(Object model) {
        List<Object> list = new ArrayList<Object>();

        ITopic topic = getTopic();
        addinfoItem(topic, list);
        return list.toArray();
    }

    private void addinfoItem(ITopic topic, List<Object> list) {
        if (actionList != null && !actionList.isEmpty())
            actionList.clear();

        List<IInfoItemContributor> contributors = InfoItemContributorManager
                .getInstance().getBothContributors();

        if (contributors.isEmpty())
            return;

        ISheet sheet = topic.getOwnedSheet();
        if (sheet != null) {
            for (IInfoItemContributor c : contributors) {
                if (!c.isCardModeAvailable(topic, getTopicPart()))
                    continue;

                String infoItemMode = null;
                String type = c.getId();
                if (type != null && !"".equals(type)) { //$NON-NLS-1$
                    List<ISettingEntry> entries = sheet.getSettings()
                            .getEntries(INFO_ITEM);
                    for (ISettingEntry entry : entries) {
                        String t = entry.getAttribute(ISheetSettings.ATTR_TYPE);
                        if (type.equals(t))
                            infoItemMode = entry
                                    .getAttribute(ISheetSettings.ATTR_MODE);
                    }
                }

                if (infoItemMode == null || "".equals(infoItemMode)) //$NON-NLS-1$
                    infoItemMode = c.getDefaultMode();
                if (DOMConstants.VAL_CARDMODE.equals(infoItemMode)) {
                    IAction action = c.createAction(getTopicPart(), topic);
                    if (action != null) {
                        if (actionList == null)
                            actionList = new ArrayList<IAction>();
                        actionList.add(action);
                        list.add(new InfoItemIcon(topic, c, action));
                        list.add(new InfoItemContent(topic, c,
                                c.getContent(topic)));
                    }
                }
            }
        }
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.Labels);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.Labels.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    update();
                }
            });
        } else {
            super.handleCoreEvent(event);
        }
    }

    public void refresh() {
        super.refresh();
        for (InfoItemContentPart infoItem : getInfoItemContents())
            infoItem.refresh();

        for (IInfoItemPart infoItem : getInfoItemIcons())
            infoItem.refresh();
    }

    @Override
    public void update() {
        super.update();
        for (InfoItemContentPart infoItem : getInfoItemContents())
            infoItem.update();

        for (IInfoItemPart infoItem : getInfoItemIcons())
            infoItem.update();

        IFigure figure = getFigure();
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        for (IInfoItemContributor infoItemCont : InfoItemContributorManager
                .getInstance().getBothContributors()) {
            infoItemCont.topicActivated(this);
        }
    }

    @Override
    protected void onDeactivated() {
        for (IInfoItemContributor infoItemCont : InfoItemContributorManager
                .getInstance().getBothContributors()) {
            infoItemCont.topicDeactivated(this);
        }
        super.onDeactivated();
    }

    @Override
    protected void reorderChild(IPart child, int index) {
        super.reorderChild(child, index);
        if (getInfoItemContents().contains(child))
            sorter.sort(getInfoItemContents());

        if (getInfoItemIcons().contains(child))
            sorter.sort(getInfoItemIcons());
        update();
    }

}