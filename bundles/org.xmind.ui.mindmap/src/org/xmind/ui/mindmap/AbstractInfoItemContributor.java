package org.xmind.ui.mindmap;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public abstract class AbstractInfoItemContributor
        implements IInfoItemContributor {

    private static final String CACHE_INFORITEM_EVENT_REG = "org.xmind.ui.cache.inforItem.eventReg"; //$NON-NLS-1$

    private static int NUMBER = 0;

    private final String regCacheKey;

    public AbstractInfoItemContributor() {
        regCacheKey = CACHE_INFORITEM_EVENT_REG + NUMBER;
        NUMBER++;
    }

    public boolean isModified(ITopicPart topicPart, ITopic topic,
            IAction action) {
        return true;
    }

    public void fillContextMenu(IInfoItemPart part) {
    }

    public String getId() {
        return null;
    }

    public String getDefaultMode() {
        return null;
    }

    public String getAvailableModes() {
        return null;
    }

    public String getCardLabel() {
        return null;
    }

    public String getContent(ITopic topic) {
        return null;
    }

    public boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart) {
        return false;
    }

    protected boolean isIconTipOnly(ITopicPart part) {
        IBranchPart branch = MindMapUtils.findBranch(part);
        if (branch != null) {
            IStyleSelector ss = StyleUtils.getStyleSelector(branch);
            String value = ss.getStyleValue(branch, Styles.IconTipOnly);
            return Boolean.TRUE.toString().equals(value);
        }
        return false;
    }

    public void topicActivated(final IInfoPart infoPart) {
        ICacheManager cacheManager = (ICacheManager) infoPart
                .getAdapter(ICacheManager.class);
        if (cacheManager != null) {
            final ITopic topic = infoPart.getTopic();
            ICoreEventRegister register = new CoreEventRegister(topic,
                    new ICoreEventListener() {
                        public void handleCoreEvent(final CoreEvent event) {
                            Display.getDefault().syncExec(new Runnable() {
                                public void run() {
                                    handleTopicEvent(infoPart, event);
                                }
                            });
                        }
                    });
            registerTopicEvent(infoPart.getTopicPart(), topic, register);
            if (register.hasRegistration()) {
                cacheManager.setCache(regCacheKey, register);
            }
        }
    }

    public void topicDeactivated(IInfoPart infoPart) {
        Object cache = MindMapUtils.flushCache(infoPart, regCacheKey);
        if (cache instanceof ICoreEventRegister) {
            ((ICoreEventRegister) cache).unregisterAll();
        }
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

    protected abstract void handleTopicEvent(IInfoPart infoPart,
            CoreEvent event);

    protected abstract void handleTopicEvent(ITopicPart topicPart,
            CoreEvent event);

    public List<IAction> getPopupMenuActions(ITopicPart topicPart,
            ITopic topic) {
        return Collections.emptyList();
    }

}
