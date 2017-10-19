package org.xmind.ui.internal;

import static org.xmind.core.ISheetSettings.INFO_ITEM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.core.ITopic;
import org.xmind.ui.internal.mindmap.IconTip;
import org.xmind.ui.internal.mindmap.TopicPart;
import org.xmind.ui.mindmap.IIconTipContributor;
import org.xmind.ui.mindmap.IInfoItemContributor;

public class TopicInfoItemManager {

    private TopicPart topicPart;

    private Map<IInfoItemContributor, IconTip> contributorToModel = new HashMap<IInfoItemContributor, IconTip>();

    public TopicInfoItemManager(TopicPart topicPart) {
        this.topicPart = topicPart;
    }

    public List<IconTip> getIconTips() {
        List<IconTip> iconTips = new ArrayList<IconTip>();
        ITopic topic = topicPart.getTopic();

        List<IIconTipContributor> contributors = IconTipContributorManager
                .getInstance().getContributors();
        if (!contributors.isEmpty()) {
            for (IIconTipContributor contributor : contributors) {
                IAction action = contributor.createAction(topicPart, topic);
                if (action != null) {
                    iconTips.add(new IconTip(topic, contributor, action));
                }
            }
        }

        List<IInfoItemContributor> contributors2 = InfoItemContributorManager
                .getInstance().getContributors();
        if (!contributors2.isEmpty()) {
            for (IInfoItemContributor contributor : contributors2) {

                IconTip oldIconTip = contributorToModel.get(contributor);
                if (oldIconTip != null) {
                    if (!contributor.isModified(topicPart, topic,
                            oldIconTip.getAction())) {
                        iconTips.add(oldIconTip);
                        continue;
                    }
                }

                IconTip iconTip = null;
                IAction action = contributor.createAction(topicPart, topic);
                if (action != null) {
                    iconTip = new IconTip(topic, contributor, action);
                    iconTips.add(iconTip);
                }

                if (iconTip != null) {
                    contributorToModel.put(contributor, iconTip);
                } else {
                    contributorToModel.remove(contributor);
                }
            }
        }

        List<IInfoItemContributor> bothContributors = InfoItemContributorManager
                .getInstance().getBothContributors();
        if (!bothContributors.isEmpty()) {
            ISheet sheet = topic.getOwnedSheet();
            if (sheet != null) {
                for (IInfoItemContributor contributor : bothContributors) {

                    IconTip oldIconTip = contributorToModel.get(contributor);
                    if (oldIconTip != null) {
                        if (!contributor.isModified(topicPart, topic,
                                oldIconTip.getAction())) {
                            iconTips.add(oldIconTip);
                            continue;
                        }
                    }

                    IconTip iconTip = null;
                    String infoItemMode = null;
                    String type = contributor.getId();
                    if (type != null && !"".equals(type)) { //$NON-NLS-1$
                        List<ISettingEntry> entries = sheet.getSettings()
                                .getEntries(INFO_ITEM);
                        for (ISettingEntry entry : entries) {
                            String t = entry
                                    .getAttribute(ISheetSettings.ATTR_TYPE);
                            if (type.equals(t))
                                infoItemMode = entry
                                        .getAttribute(ISheetSettings.ATTR_MODE);
                        }
                    }

                    if (infoItemMode == null || "".equals(infoItemMode)) //$NON-NLS-1$
                        infoItemMode = contributor.getDefaultMode();
                    if (ISheetSettings.MODE_ICON.equals(infoItemMode)
                            || !contributor.isCardModeAvailable(topic,
                                    topicPart)) {
                        IAction action = contributor.createAction(topicPart,
                                topic);
                        if (action != null) {
                            iconTip = new IconTip(topic, contributor, action);
                            iconTips.add(iconTip);
                        }
                    }

                    if (iconTip != null) {
                        contributorToModel.put(contributor, iconTip);
                    } else {
                        contributorToModel.remove(contributor);
                    }
                }
            }
        }

        return iconTips;
    }

    public void topicDeactivated() {
        contributorToModel.clear();
    }

}
