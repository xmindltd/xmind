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
package org.xmind.ui.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xmind.core.ITopic;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;
import org.xmind.ui.internal.ModelCacheManager;

public class ModifyFoldedCommand extends ModifyCommand {

    public ModifyFoldedCommand(ITopic source, boolean newFolded) {
        super(source, Boolean.valueOf(newFolded));
    }

    public ModifyFoldedCommand(Collection<? extends ITopic> sources,
            boolean newFolded) {
        super(sources, Boolean.valueOf(newFolded));
    }

    public ModifyFoldedCommand(ISourceProvider sourceProvider,
            boolean newFolded) {
        super(sourceProvider, Boolean.valueOf(newFolded));
    }

    protected Object getValue(Object source) {
        if (source instanceof ITopic) {
            return Boolean.valueOf(((ITopic) source).isFolded());
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ITopic && value instanceof Boolean) {
            ((ITopic) source).setFolded(((Boolean) value).booleanValue());
        }
    }

    @Override
    protected void setNewValues() {
        List<Object> sourcesToChange = filterSameValueSources(getSources(),
                false);
        List<Object> rootParents = filterRootParents(sourcesToChange);
        for (Object source : getSources()) {
            if (!rootParents.contains(source)) {
                ModelCacheManager.getInstance().setCache(source,
                        ModelCacheManager.MODEL_CACHE_DELAYLAYOUT,
                        Boolean.TRUE);
                setValue(source, getNewValue());
                ModelCacheManager.getInstance().flush(source,
                        ModelCacheManager.MODEL_CACHE_DELAYLAYOUT);
            }
        }

        for (Object rootParent : rootParents) {
            setValue(rootParent, getNewValue());
        }
    }

    @Override
    protected void setOldValues() {
        List<Object> sourcesToChange = filterSameValueSources(getSources(),
                true);
        List<Object> rootParents = filterRootParents(sourcesToChange);
        for (Object source : getSources()) {
            if (!rootParents.contains(source)) {
                ModelCacheManager.getInstance().setCache(source,
                        ModelCacheManager.MODEL_CACHE_DELAYLAYOUT,
                        Boolean.TRUE);
                setValue(source, getOldValue(source));
                ModelCacheManager.getInstance().flush(source,
                        ModelCacheManager.MODEL_CACHE_DELAYLAYOUT);
            }
        }

        for (Object rootParent : rootParents) {
            setValue(rootParent, getOldValue(rootParent));
        }
    }

    private List<Object> filterSameValueSources(List<Object> sources,
            boolean oldValueOrNewValue) {
        ArrayList<Object> result = new ArrayList<Object>(sources.size());
        for (Object topic : sources) {
            if (topic instanceof ITopic) {
                Object value = oldValueOrNewValue ? getOldValue(topic)
                        : getNewValue();
                boolean folded = ((ITopic) topic).isFolded();
                if (value instanceof Boolean && !value.toString()
                        .equals(Boolean.valueOf(folded).toString()))
                    result.add(topic);
            }
        }
        return result;
    }

    private List<Object> filterRootParents(List<Object> topics) {
        ArrayList<Object> result = new ArrayList<Object>(topics.size());
        for (Object topic : topics) {
            if (topic instanceof ITopic
                    && !branchContains((ITopic) topic, topics)) {
                result.add(topic);
            }
        }
        return result;
    }

    private boolean branchContains(ITopic topic, List<Object> topics) {
        boolean contains = false;
        ITopic parent = topic.getParent();
        if (parent != null) {
            contains = topics.contains(parent);
            if (!contains) {
                contains = branchContains(parent, topics);
            }
        }
        return contains;
    }
}