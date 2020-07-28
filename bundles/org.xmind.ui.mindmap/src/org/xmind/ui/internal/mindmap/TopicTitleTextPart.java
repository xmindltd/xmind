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

import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.decorators.TopicTitleTextDecorator;

public class TopicTitleTextPart extends TitleTextPart {

    public TopicTitleTextPart() {
        setDecorator(TopicTitleTextDecorator.getInstance());
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            TopicPart topic = (TopicPart) getParent();
            if (topic.getTitle() == this) {
                topic.setTitle(null);
            }
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).setTitle(this);
        }
    }

    protected void registerCoreEvents(Object source, ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.TitleWidth);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TitleWidth.equals(type)) {
            update();
        } else if (Core.TitleText.equals(type)) {
            update();
        } else {
            super.handleCoreEvent(event);
        }
    }

//    protected String getDecoratorId() {
//        return "org.xmind.ui.topicTitleTextDecorator"; //$NON-NLS-1$
//    }

}