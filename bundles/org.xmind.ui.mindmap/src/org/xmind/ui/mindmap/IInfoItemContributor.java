package org.xmind.ui.mindmap;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.xmind.core.ITopic;

public interface IInfoItemContributor {

    IAction createAction(ITopicPart topicPart, ITopic topic);

    boolean isModified(ITopicPart topicPart, ITopic topic, IAction action);

    String getContent(ITopic topic);

    String getId();

    String getDefaultMode();

    String getAvailableModes();

    String getCardLabel();

    String getSVGFilePath(ITopic topic, IAction action);

    boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart);

    void fillContextMenu(IInfoItemPart part);

    void topicActivated(IInfoPart infoPart);

    void topicDeactivated(IInfoPart infoPart);

    void topicActivated(ITopicPart topicPart);

    void topicDeactivated(ITopicPart topicPart);

    List<IAction> getPopupMenuActions(ITopicPart topicPart, ITopic topic);

}
