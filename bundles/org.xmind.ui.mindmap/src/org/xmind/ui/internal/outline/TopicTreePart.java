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
package org.xmind.ui.internal.outline;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.ui.internal.protocols.WebProtocol;

public class TopicTreePart extends MindMapTreePartBase {

    public TopicTreePart(ITopic model) {
        super(model);
    }

    public ITopic getTopic() {
        return (ITopic) super.getModel();
    }

    protected Object[] getModelChildren(Object model) {
        List<ITopic> allChildren = getTopic().getAllChildren();

        ArrayList<ITopic> fixedChildren = new ArrayList<ITopic>();
        for (ITopic child : allChildren) {
            if (ITopic.CALLOUT.equals(child.getType())) {
                fixedChildren.add(child);
            }
        }
        allChildren.removeAll(fixedChildren);
        allChildren.addAll(fixedChildren);
        return allChildren.toArray();
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.TitleText);
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
        register.register(Core.TopicHyperlink);
        register.register(WebProtocol.WEB_ICON_EVENT_TYPE);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TitleText.equals(type) || Core.TopicHyperlink.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    update();
                }
            }, false);
        } else if (Core.TopicAdd.equals(type)
                || Core.TopicRemove.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    refresh();
                }
            }, false);
        } else if (WebProtocol.WEB_ICON_EVENT_TYPE.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    setWidgetImage(getImage());
                }
            }, false);
        } else {
            super.handleCoreEvent(event);
        }
    }

}