package org.xmind.ui.internal.mindmap;

import org.eclipse.jface.action.IAction;
import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.IInfoItemContributor;

public class InfoItemIcon extends ViewerModel {

    private IInfoItemContributor contributor;

    private IAction action;

    public InfoItemIcon(ITopic topic, IInfoItemContributor contributor,
            IAction action) {
        super(InfoItemIconPart.class, topic);
        this.contributor = contributor;
        this.action = action;
    }

    public IInfoItemContributor getContributor() {
        return contributor;
    }

    public IAction getAction() {
        return action;
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
        if (obj == null || !(obj instanceof InfoItemIcon))
            return false;
        InfoItemIcon that = (InfoItemIcon) obj;
        return super.equals(obj) && that.contributor == this.contributor
                && that.action == this.action;
    }

}
