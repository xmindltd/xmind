package org.xmind.ui.mindmap;

import java.util.List;

public interface INumberSeparatorManager {

    List<INumberSeparatorDescriptor> getDescriptors();

    INumberSeparatorDescriptor getDescriptor(String separatorId);

    INumberSeparator getSeparator(String separatorId);

    String getSeparatorText(String separatorId);

}
