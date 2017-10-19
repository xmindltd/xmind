package org.xmind.ui.commands;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.command.SourceCommand;
import org.xmind.ui.internal.branch.UnbalancedData;

public class ModifyRightNumberOfUnbalancedStructureCommand extends
        SourceCommand {
    private ITopic topic = null;
    private String preNum;
    private int postNum;

    public ModifyRightNumberOfUnbalancedStructureCommand(ITopic topic,
            String preNum, int postNum) {
        Assert.isNotNull(topic);
        this.topic = topic;
        this.preNum = preNum;
        this.postNum = postNum;
    }

    public void redo() {
        ITopicExtension extension = topic
                .createExtension(UnbalancedData.EXTENTION_UNBALANCEDSTRUCTURE);
        ITopicExtensionElement element = extension.getContent()
                .getCreatedChild(UnbalancedData.EXTENTIONELEMENT_RIGHTNUMBER);
        element.setTextContent(String.valueOf(postNum));
        fireForceStructureChange();
        super.redo();
    }

    public void undo() {
        ITopicExtension extension = topic
                .createExtension(UnbalancedData.EXTENTION_UNBALANCEDSTRUCTURE);
        ITopicExtensionElement element = extension.getContent()
                .getCreatedChild(UnbalancedData.EXTENTIONELEMENT_RIGHTNUMBER);
        element.setTextContent(preNum);
        fireForceStructureChange();
        super.undo();
    }

    private void fireForceStructureChange() {
        if (!(topic instanceof ICoreEventSource))
            return;

        ICoreEventSource source = (ICoreEventSource) topic;
        String eventType = Core.StructureClass;
        String structureClass = topic.getStructureClass();
        source.getCoreEventSupport()
                .dispatch(
                        source,
                        new CoreEvent(source, eventType, structureClass,
                                structureClass));
    }

}
