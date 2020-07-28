/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.mindmap;

import org.eclipse.jface.action.IAction;
import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.IIconTipContributor;
import org.xmind.ui.mindmap.IInfoItemContributor;

public class IconTip extends ViewerModel {

    private IIconTipContributor contributor;

    private IInfoItemContributor infoItemContributor;

    private IAction action;

    public IconTip(ITopic topic, IIconTipContributor contributor, IAction action) {
        super(IconTipPart.class, topic);
        this.contributor = contributor;
        this.action = action;
    }

    public IconTip(ITopic topic, IInfoItemContributor contributor,
            IAction action) {
        super(IconTipPart.class, topic);
        this.infoItemContributor = contributor;
        this.action = action;
    }

    public IIconTipContributor getContributor() {
        return contributor;
    }

    public IInfoItemContributor getInfoItemContributor() {
        return infoItemContributor;
    }

    public IAction getAction() {
        return action;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ITopic.class)
            return getRealModel();
        return super.getAdapter(adapter);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof IconTip))
            return false;
        IconTip that = (IconTip) obj;
        return super.equals(obj) && that.contributor == this.contributor
                && that.action == this.action
                && that.infoItemContributor == this.infoItemContributor;
    }

}