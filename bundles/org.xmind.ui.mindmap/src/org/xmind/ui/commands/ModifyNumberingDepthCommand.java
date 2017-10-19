package org.xmind.ui.commands;

import java.util.Collection;

import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyNumberingDepthCommand extends ModifyCommand {

    public ModifyNumberingDepthCommand(ITopic topic, String newDepth) {
        super(topic, newDepth);
    }

    public ModifyNumberingDepthCommand(Collection<? extends ITopic> topics,
            String newDepth) {
        super(topics, newDepth);
    }

    public ModifyNumberingDepthCommand(ISourceProvider topicProvider,
            String newDepth) {
        super(topicProvider, newDepth);
    }

    @Override
    protected Object getValue(Object source) {
        if (source instanceof ITopic)
            return ((ITopic) source).getNumbering().getComputedDepth();
        return null;
    }

    @Override
    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic) {
            ITopic topic = (ITopic) source;
            if (value instanceof String) {
                topic.getNumbering().setDepth((String) value);
            } else if (value == null) {
                topic.getNumbering().setDepth(null);
            }
        }
    }

}
