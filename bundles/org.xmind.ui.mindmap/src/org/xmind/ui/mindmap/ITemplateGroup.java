package org.xmind.ui.mindmap;

import java.util.List;

public interface ITemplateGroup {

    String getName();

    void setName(String name);

    List<ITemplate> getTemplates();

    void setTemplates(List<ITemplate> templates);

}
