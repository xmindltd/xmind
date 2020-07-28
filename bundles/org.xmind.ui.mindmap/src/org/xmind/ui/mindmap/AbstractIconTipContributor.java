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
package org.xmind.ui.mindmap;

import org.eclipse.swt.widgets.Display;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.ui.util.MindMapUtils;

public abstract class AbstractIconTipContributor implements IIconTipContributor {

    private static final String CACHE_ICONTIP_EVENT_REG = "org.xmind.ui.cache.icontip.eventReg"; //$NON-NLS-1$

    private static int NUMBER = 0;

    private final String regCacheKey;

    protected AbstractIconTipContributor() {
        regCacheKey = CACHE_ICONTIP_EVENT_REG + NUMBER;
        NUMBER++;
    }

    public void fillContextMenu(IIconTipPart part) {
    }

    public void topicActivated(final ITopicPart topicPart) {
        ICacheManager cacheManager = (ICacheManager) topicPart
                .getAdapter(ICacheManager.class);
        if (cacheManager != null) {
            ITopic topic = topicPart.getTopic();
            ICoreEventRegister register = new CoreEventRegister(topic,
                    new ICoreEventListener() {
                        public void handleCoreEvent(final CoreEvent event) {
                            Display.getDefault().syncExec(new Runnable() {
                                public void run() {
                                    handleTopicEvent(topicPart, event);
                                }
                            });
                        }
                    });
            registerTopicEvent(topicPart, topic, register);
            if (register.hasRegistration()) {
                cacheManager.setCache(regCacheKey, register);
            }
        }
    }

    public void topicDeactivated(ITopicPart topicPart) {
        Object cache = MindMapUtils.flushCache(topicPart, regCacheKey);
        if (cache instanceof ICoreEventRegister) {
            ((ICoreEventRegister) cache).unregisterAll();
        }
    }

    protected abstract void registerTopicEvent(ITopicPart topicPart,
            ITopic topic, ICoreEventRegister register);

    protected abstract void handleTopicEvent(ITopicPart topicPart,
            CoreEvent event);

}