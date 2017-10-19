package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

/**
 * 
 * @author Shawn Liu
 * @since 3.6.50
 */
public class ShareOptionRegistry {

    private List<ShareOption> options = null;

    ShareOptionRegistry() {
    }

    public List<ShareOption> getOptions() {
        ensureLoaded();
        return Collections.unmodifiableList(options);
    }

    /**
     * @return
     */
    public boolean hasOptions() {
        ensureLoaded();
        return !options.isEmpty();
    }

    public ShareOption getOptionById(String id) {
        Assert.isLegal(id != null);
        ensureLoaded();
        for (ShareOption option : options) {
            if (id.equals(option.getId())) {
                return option;
            }
        }
        return null;
    }

    public List<ShareOption> getOptionsByCategory(String category) {
        Assert.isLegal(category != null);
        ensureLoaded();
        List<ShareOption> result = new ArrayList<ShareOption>();
        for (ShareOption option : options) {
            if (category.equals(option.getCategory()))
                result.add(option);
        }
        return result;
    }

    private void ensureLoaded() {
        if (options != null)
            return;

        lazyLoad();

        if (options == null)
            options = Collections.emptyList();
    }

    private void lazyLoad() {
        IExtensionPoint extPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(MindMapUI.PLUGIN_ID,
                        RegistryConstants.EXT_SHARE_OPTIONS);
        Assert.isNotNull(extPoint);
        for (IConfigurationElement ele : extPoint.getConfigurationElements()) {
            if (RegistryConstants.TAG_OPTION.equals(ele.getName())) {
                readShareOption(ele);
            }
        }
    }

    private void readShareOption(IConfigurationElement element) {
        ShareOption descriptor;
        try {
            descriptor = new ShareOption(element);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load share item: " + element); //$NON-NLS-1$
            return;
        }
        if (options == null) {
            options = new ArrayList<ShareOption>();
        }
        options.add(descriptor);
    }

}
