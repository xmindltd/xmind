package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.ITemplateGroup;

public class TemplateGroup implements ITemplateGroup {

    private String name;

    private List<ITemplate> templates;

    public TemplateGroup(String name) {
        this.name = name;
    }

    public TemplateGroup(String name, List<ITemplate> templates) {
        this(name);
        this.templates = templates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ITemplate> getTemplates() {
        if (templates == null)
            templates = new ArrayList<ITemplate>();
        return templates;
    }

    public void setTemplates(List<ITemplate> templates) {
        this.templates = templates;
    }

}
