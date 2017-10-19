package org.xmind.ui.internal.mindmap;

import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.IInfoItemContributor;

public class InfoItemContent extends ViewerModel {

    private IInfoItemContributor contributor;

    private String content;

    public InfoItemContent(ITopic topic, IInfoItemContributor contributor,
            String content) {
        super(InfoItemContentPart.class, topic);
        this.contributor = contributor;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public IInfoItemContributor getContributor() {
        return contributor;
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ITopic.class)
            return getRealModel();
        return super.getAdapter(adapter);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof InfoItemContent))
            return false;
        InfoItemContent that = (InfoItemContent) obj;
        return super.equals(obj) && that.contributor == this.contributor
                && ((that.content == null && this.content == null)
                        || (that.content != null
                                && that.content.equals(this.content)));
    }

}
