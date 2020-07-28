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
package org.xmind.core.internal.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponentRefManager;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;

/**
 * 
 * @author Frank Shaka
 */
public class WorkbookComponentRefCounter implements
        IWorkbookComponentRefManager, ICoreEventSource {

    private static final Collection<String> EMPTY = Collections.emptySet();

    private WorkbookImpl workbook;

    private Map<String, Collection<String>> source2targets;

    private Map<String, Collection<String>> target2sources;

    public WorkbookComponentRefCounter(WorkbookImpl workbook) {
        this.workbook = workbook;
    }

    public IWorkbook getWorkbook() {
        return workbook;
    }

    public void increaseRef(String source, String target) {
        if (source2targets == null)
            source2targets = new HashMap<String, Collection<String>>();
        Collection<String> targets = source2targets.get(source);
        if (targets == null) {
            targets = new HashSet<String>();
            source2targets.put(source, targets);
        }
        targets.add(target);

        if (target2sources == null)
            target2sources = new HashMap<String, Collection<String>>();
        Collection<String> sources = target2sources.get(target);
        if (sources == null) {
            sources = new HashSet<String>();
            target2sources.put(target, sources);
        }
        sources.add(target);
    }

    public void decreaseRef(String source, String target) {
        if (source2targets != null) {
            Collection<String> targets = source2targets.get(source);
            if (targets != null) {
                targets.remove(target);
                if (targets.isEmpty())
                    source2targets.remove(source);
            }
        }
        if (target2sources != null) {
            Collection<String> sources = target2sources.get(target);
            if (sources != null) {
                sources.remove(source);
                if (sources.isEmpty())
                    target2sources.remove(target);
            }
        }
    }

    public Collection<String> getSources() {
        return source2targets == null ? EMPTY : source2targets.keySet();
    }

    public Collection<String> getSources(String target) {
        if (target2sources != null) {
            Collection<String> sources = target2sources.get(target);
            if (sources != null)
                return sources;
        }
        return EMPTY;
    }

    public Collection<String> getTargets() {
        return target2sources == null ? EMPTY : target2sources.keySet();
    }

    public Collection<String> getTargets(String source) {
        if (source2targets != null) {
            Collection<String> targets = source2targets.get(source);
            if (targets != null)
                return targets;
        }
        return EMPTY;
    }

//    protected void postIncreaseRef(String resourceId, Object resource) {
//        if (this.oldValue != null) {
//            Collection<String> oldValue = this.oldValue;
//            Collection<String> newValue = new ArrayList<String>(
//                    getCountedRefs());
//            this.oldValue = null;
//            fireValueChange(Core.ResourceRefs, oldValue, newValue);
//        }
//    }
//
//    public void decreaseRef(String resourceId) {
//        oldValue = new ArrayList<String>(getCountedRefs());
//        super.decreaseRef(resourceId);
//    }
//
//    protected void postDecreaseRef(String resourceId, Object resource) {
//        if (this.oldValue != null) {
//            Collection<String> oldValue = this.oldValue;
//            Collection<String> newValue = new ArrayList<String>(
//                    getCountedRefs());
//            this.oldValue = null;
//            fireValueChange(Core.ResourceRefs, oldValue, newValue);
//        }
//    }

    public ICoreEventSupport getCoreEventSupport() {
        return workbook.getCoreEventSupport();
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

//    private void fireValueChange(String eventType, Object oldValue,
//            Object newValue) {
//        getCoreEventSupport().dispatchValueChange(this, eventType, oldValue,
//                newValue);
//    }

//    public void loadTopicHyperlink() {
//        List<ISheet> sheets = workbook.getSheets();
//        for (ISheet sheet : sheets) {
//            ITopic rootTopic = sheet.getRootTopic();
//            loadTopic(rootTopic);
//        }
//    }
//
//    private void loadTopic(ITopic topic) {
//        String hyperlink = topic.getHyperlink();
//        if (hyperlink != null && hyperlink.startsWith("xmind:#")) { //$NON-NLS-1$
//            String targetId = hyperlink.substring(7);
//            registryTopicLinks(targetId, topic.getId());
//        }
//        List<ITopic> children = topic.getAllChildren();
//        if (children != null && !children.isEmpty()) {
//            for (ITopic child : children)
//                loadTopic(child);
//        }
//    }
//
////    public void modifyTargetLink(String oldTargetId, String newTargetId) {
////        List<String> list = registry.get(oldTargetId);
////        if (list == null || list.isEmpty())
////            return;
////        registry.put(newTargetId, list);
////    }
//
////    public void modifyTopicLinks(String oldTargetId, String newTargetId,
////            String sourceId) {
////        if (oldTargetId == null && newTargetId != null) { //add TopicHyprelink , or change WebHyperlink/FileHyperlink to TopicHyperlink
////            registryTopicLinks(newTargetId, sourceId);
////        } else if (oldTargetId != null && newTargetId == null) {//remove TopicHyperlink , or change TopicHyperlink to WebHyperlink/FileHyperlink
////            List<String> list = registry.get(oldTargetId);
////            if (list != null && !list.isEmpty())
////                list.remove(sourceId);
////        } else if (oldTargetId != null && newTargetId != null) {//modify TopicHyperlink (change TopicHyperlink to another TopicHyperlink)
////            List<String> list = registry.get(oldTargetId);
////            if (list != null && !list.isEmpty())
////                list.remove(sourceId);
////            registryTopicLinks(newTargetId, sourceId);
////        }
////    }
//
//    public void registryTopicLinks(String targetId, String sourceId) {
//        List<String> list = registry.get(targetId);
//        if (list == null) {
//            List<String> newList = new ArrayList<String>();
//            newList.add(sourceId);
//            registry.put(targetId, newList);
//        } else
//            list.add(sourceId);
//    }
//
//    public List<ITopic> getLinkTopics(String targetId) {
//        List<String> list = registry.get(targetId);
//        if (list != null && !list.isEmpty()) {
//            return toTopics(list);
//        }
//        return null;
//    }
//
////    public void removeTopicLinks(String targetId) {
////        registry.remove(targetId);
////    }
//
//    public String getOldTargetId(ITopic topic) {
//        String id = topic.getId();
//        for (Entry<String, List<String>> entry : registry.entrySet()) {
//            List<String> values = entry.getValue();
//            for (String value : values) {
//                if (id.equals(value))
//                    return entry.getKey();
//            }
//        }
//        return null;
//    }
//
//    public void setNewTargetId(ITopic topic, String newTargetId) {
//        String oldTargetId = getOldTargetId(topic);
//        String sourceId = topic.getId();
//        removeTopicLink(oldTargetId, sourceId);
//        if (newTargetId != null)
//            registryTopicLinks(newTargetId, sourceId);
//    }
//
//    private void removeTopicLink(String oldTargetId, String sourceId) {
//        if (registry != null) {
//            List<String> list = registry.get(oldTargetId);
//            if (list != null && !list.isEmpty()) {
//                list.remove(sourceId);
//            }
//            if (list == null || list.isEmpty())
//                registry.remove(oldTargetId);
//        }
//    }
//
//    private List<ITopic> toTopics(List<String> list) {
//        List<ITopic> topics = new ArrayList<ITopic>();
//        for (String id : list) {
//            ITopic topic = getWorkbook().findTopic(id);
//            topics.add(topic);
//        }
//        return topics;
//    }

}
