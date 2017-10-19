/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.branch;

import java.util.HashSet;
import java.util.Set;

import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ICacheManager;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class ParentValueProvider {

    private static final String CACHE_PARENT_VALUE_PROVIDER = "org.xmind.ui.branchCache.parentValueProvider"; //$NON-NLS-1$

    private static final String PREFIX = "org.xmind.ui.branchCache.style."; //$NON-NLS-1$

    private static final String USER_PREFIX = "org.xmind.ui.branchCache.style.user."; //$NON-NLS-1$

    private IBranchPart branch;

    private Set<String> cachedKeys = null;

    private ParentValueProvider(IBranchPart branch) {
        this.branch = branch;
    }

    public String getParentValue(String key) {
        String cacheKey = PREFIX + key;
        String value = (String) MindMapUtils.getCache(branch, cacheKey);
        if (value == null) {
            value = getParentCachedValue(branch, key, cacheKey);
            if (value == null)
                value = Styles.NULL;
            MindMapUtils.setCache(branch, cacheKey, value);
            if (cachedKeys == null)
                cachedKeys = new HashSet<String>();
            cachedKeys.add(cacheKey);
        }
        if (Styles.NULL.equals(value))
            return null;
        return value;
    }

    private String getParentCachedValue(IBranchPart branch, String key,
            String cacheKey) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            ParentValueProvider parentProvider = getValueProvider(parent);
            return parentProvider.getCachedValue(parent, key, cacheKey);
        }
        return null;
    }

    private String getCachedValue(IBranchPart branch, String key,
            String cacheKey) {
        String userCacheKey = USER_PREFIX + key;
        String value = (String) MindMapUtils.getCache(branch, userCacheKey);
        if (value == null) {
            value = getValue(branch, key, cacheKey);
            if (value == null)
                value = Styles.NULL;
            MindMapUtils.setCache(branch, userCacheKey, value);
            if (cachedKeys == null)
                cachedKeys = new HashSet<String>();
            cachedKeys.add(userCacheKey);
        }
        if (Styles.NULL.equals(value))
            return null;
        return value;
    }

    protected String getValue(IBranchPart branch, String key, String cacheKey) {
        IStyleSelector ss = branch.getBranchPolicy().getStyleSelector(branch);
        String value;
        value = ss.getStyleValue(branch, key);
        if (value != null)
            return value;
        return getParentCachedValue(branch, key, cacheKey);
    }

    public static ParentValueProvider getValueProvider(IBranchPart branch) {
        ParentValueProvider valueProvider = (ParentValueProvider) MindMapUtils
                .getCache(branch, CACHE_PARENT_VALUE_PROVIDER);
        if (valueProvider == null) {
            valueProvider = new ParentValueProvider(branch);
            MindMapUtils.setCache(branch, CACHE_PARENT_VALUE_PROVIDER,
                    valueProvider);
        }
        return valueProvider;
    }

    public static void flush(IBranchPart branch) {
        ICacheManager cacheManager = MindMapUtils.getCacheManager(branch);
        ParentValueProvider valueProvider = (ParentValueProvider) cacheManager
                .getCache(CACHE_PARENT_VALUE_PROVIDER);
        if (valueProvider != null) {
            if (valueProvider.cachedKeys != null) {
                for (String cachedKey : valueProvider.cachedKeys) {
                    cacheManager.flush(cachedKey);
                }
            }
        }
        cacheManager.flush(CACHE_PARENT_VALUE_PROVIDER);
    }

}