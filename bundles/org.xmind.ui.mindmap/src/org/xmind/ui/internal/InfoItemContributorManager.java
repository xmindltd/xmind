package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.ui.mindmap.IInfoItemContributor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class InfoItemContributorManager extends RegistryReader {

    private static final InfoItemContributorManager instance = new InfoItemContributorManager();

    public static final String LABEL_ID = "org.xmind.ui.infoItem.label"; //$NON-NLS-1$

    public static final String NOTES_ID = "org.xmind.ui.infoItem.notes"; //$NON-NLS-1$

    public static final String HYPERLINK_ID = "org.xmind.ui.infoItem.hyperlink"; //$NON-NLS-1$

    public static final String TASKINFO_ID = "org.xmind.ui.infoItem.taskInfo"; //$NON-NLS-1$

    private List<IInfoItemContributor> contributors = null;

    private List<IInfoItemContributor> bothContributors = null;

    private InfoItemContributorManager() {
    }

    public List<IInfoItemContributor> getContributors() {
        ensureLoaded();
        return contributors;
    }

    public List<IInfoItemContributor> getBothContributors() {
        return bothContributors;
    }

    private void ensureLoaded() {
        if (contributors != null && bothContributors != null)
            return;
        lazyLoad();
        if (contributors == null)
            contributors = Collections.emptyList();
        if (bothContributors == null)
            bothContributors = Collections.emptyList();
    }

    private void lazyLoad() {
        if (Platform.isRunning()) {
            readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                    RegistryConstants.EXT_INFOITMES);
        }
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (RegistryConstants.TAG_INFOITEM.equals(name)) {
            readInfoItem(element);
            return true;
        }
        return false;
    }

    private void readInfoItem(IConfigurationElement element) {
        IInfoItemContributor contributor;
        try {
            contributor = new InfoItemContributorProxy(element);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load info item: " + element); //$NON-NLS-1$
            return;
        }

        String modes = contributor.getAvailableModes();
        if (modes.contains(DOMConstants.VAL_ICONMODE)
                && modes.contains(DOMConstants.VAL_CARDMODE)) {
            if (bothContributors == null)
                bothContributors = new ArrayList<IInfoItemContributor>();
            bothContributors.add(contributor);
        } else {
            if (contributors == null)
                contributors = new ArrayList<IInfoItemContributor>();
            contributors.add(contributor);
        }
    }

    public static InfoItemContributorManager getInstance() {
        return instance;
    }

}
