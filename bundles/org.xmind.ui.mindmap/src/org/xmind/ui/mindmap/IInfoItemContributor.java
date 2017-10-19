package org.xmind.ui.mindmap;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.xmind.core.ITopic;

public interface IInfoItemContributor {

    IAction createAction(ITopicPart topicPart, ITopic topic);

    String getContent(ITopic topic);

    String getId();

    String getDefaultMode();

    String getAvailableModes();

    String getCardLabel();

    boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart);

    void fillContextMenu(IInfoItemPart part);

    void topicActivated(IInfoPart infoPart);

    void topicDeactivated(IInfoPart infoPart);

    void topicActivated(ITopicPart topicPart);

    void topicDeactivated(ITopicPart topicPart);

    List<IAction> getPopupMenuActions(ITopicPart topicPart, ITopic topic);

}
