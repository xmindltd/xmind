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
package org.xmind.ui.internal.branch;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_ENABLEMENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchPolicyCategoryDescriptor;
import org.xmind.ui.branch.IBranchPolicyDescriptor;
import org.xmind.ui.branch.IBranchPolicyManager;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.branch.IBranchStyleValueProvider;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

/**
 * This class is not intended to be instantiate by clients. Call
 * {@link org.xmind.ui.mindmap.MindMapUI#getBranchPolicyManager()} to obtain an
 * instance of this class.
 * 
 * @author MANGOSOFT
 */
public class BranchPolicyManager extends RegistryReader implements
        IBranchPolicyManager {

    private static final String V_CALLOUT_BRANCHES = "calloutBranches"; //$NON-NLS-1$

    private static final String DEFAULT_BRANCH_POLICY_ID = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$

    private static final String V_PARENT_BRANCH = "parentBranch"; //$NON-NLS-1$

    private static final String V_SUB_BRANCHES = "subBranches"; //$NON-NLS-1$

    private static final String V_SUMMARY_BRANCHES = "summaryBranches"; //$NON-NLS-1$

    private static final String V_BOUNDARIES = "boundaries"; //$NON-NLS-1$

    private static final String V_SUMMARIES = "summaries"; //$NON-NLS-1$

    private static final String V_TOPIC = "topic"; //$NON-NLS-1$

    static IEvaluationContext createBranchEvaluationContext(IBranchPart branch) {
        EvaluationContext context = new EvaluationContext(null, branch);
        IBranchPart parentBranch = branch.getParentBranch();
        context.addVariable(V_PARENT_BRANCH,
                parentBranch == null ? IEvaluationContext.UNDEFINED_VARIABLE
                        : parentBranch);
        context.addVariable(V_SUB_BRANCHES, branch.getSubBranches());
        context.addVariable(V_CALLOUT_BRANCHES, branch.getCalloutBranches());
        context.addVariable(V_SUMMARY_BRANCHES, branch.getSummaryBranches());
        context.addVariable(V_BOUNDARIES, branch.getBoundaries());
        context.addVariable(V_SUMMARIES, branch.getSummaries());
        context.addVariable(V_TOPIC, branch.getTopic());
        return context;
    }

    private List<IBranchPolicyDescriptor> policyList = null;

    private Map<String, BranchPolicy> policyMap = null;

    private Map<String, StructureDescriptor> structureMap = null;

    private Map<String, BranchPropertyTesterProxy> testerMap = null;

    private Map<String, ContributedStyleValueProvider> valueProviderMap = null;

    private IBranchPolicy defaultBranchPolicy = null;

    private List<IBranchPolicyCategoryDescriptor> categoryList = null;

    private Map<String, BranchPolicyCategoryDescriptor> categoryMap = null;

    /**
     * This class is not intended to be instantiate by clients.
     */
    public BranchPolicyManager() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.IBranchPolicyRegistry#getDescriptors()
     */
    public List<IBranchPolicyDescriptor> getBranchPolicyDescriptors() {
        ensureLoaded();
        return policyList;
    }

    public List<IBranchPolicyCategoryDescriptor> getBranchPolicyCategoryDescriptors() {
        ensureLoaded();
        return categoryList;
    }

    private Map<String, BranchPolicy> getPolicyMap() {
        ensureLoaded();
        return policyMap;
    }

    private Map<String, StructureDescriptor> getStructureMap() {
        ensureLoaded();
        return structureMap;
    }

    private Map<String, BranchPolicyCategoryDescriptor> getCategoryMap() {
        ensureLoaded();
        return categoryMap;
    }

    public IStructureDescriptor getStructureDescriptor(String id) {
        if (id != null) {
            StructureDescriptor structure = getStructureMap().get(id);
            if (structure != null)
                return structure;
        }
        return DefaultStructureDescriptor.getInstance();
    }

    public IBranchPropertyTester getPropertyTester(String id) {
        ensureLoaded();
        return testerMap.get(id);
    }

    public IBranchStyleValueProvider getValueProvider(String id) {
        ensureLoaded();
        return valueProviderMap.get(id);
    }

    private BranchPolicy getPolicy(String id) {
        return getPolicyMap().get(id);
    }

    public IBranchPolicyCategoryDescriptor getBranchPolicyCategoryDescriptor(
            String categoryId) {
        if (categoryId != null) {
            return getCategoryMap().get(categoryId);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.IBranchPolicyRegistry#getDescriptor(java
     * .lang.String)
     */
    public IBranchPolicyDescriptor getBranchPolicyDescriptor(String id) {
        return getPolicy(id);
    }

    public IBranchPolicy getBranchPolicy(String id) {
        BranchPolicy policy = getPolicy(id);
        if (policy == null) {
            policy = getPolicy(DEFAULT_BRANCH_POLICY_ID);
            if (policy == null)
                return getDefaultBranchPolicy();
        }
        return policy;
    }

    public IBranchPolicy getDefaultBranchPolicy() {
        if (defaultBranchPolicy == null) {
            defaultBranchPolicy = new DefaultBranchPolicy(this);
        }
        return defaultBranchPolicy;
    }

    public String calculateBranchPolicyId(IBranchPart branch,
            String prefferedPolicyId) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            String parentId = parent.getBranchPolicyId();
            BranchPolicy parentPolicy = getPolicy(parentId);
            if (parentPolicy != null
                    && parentPolicy
                            .canOverride(createBranchEvaluationContext(branch)))
                return parentId;
        }

        if (prefferedPolicyId != null) {
            BranchPolicy policy = getPolicy(prefferedPolicyId);
            if (policy != null && policy.isApplicableTo(branch))
                return prefferedPolicyId;
        }
        if (parent != null && parent.getSubBranches().contains(branch))
            return parent.getBranchPolicyId();
        if (parent != null && parent.getCalloutBranches().contains(branch))
            return parent.getBranchPolicyId();
        return DEFAULT_BRANCH_POLICY_ID;
    }

    public List<IBranchPolicyDescriptor> getApplicableBranchPolicyDescriptors(
            IBranchPart branch) {
        if (branch == null)
            return Collections.emptyList();
        ArrayList<IBranchPolicyDescriptor> list = new ArrayList<IBranchPolicyDescriptor>(
                getBranchPolicyDescriptors());
        IEvaluationContext context = createBranchEvaluationContext(branch);
        for (Iterator<IBranchPolicyDescriptor> it = list.iterator(); it
                .hasNext();) {
            if (!((BranchPolicy) it.next()).isApplicableTo(context)) {
                it.remove();
            }
        }
        return list;
    }

    private void ensureLoaded() {
        if (policyMap == null || policyList == null || structureMap == null
                || testerMap == null || valueProviderMap == null
                || categoryList == null || categoryMap == null)
            lazyLoad();
        if (policyList == null)
            policyList = Collections.emptyList();
        if (policyMap == null)
            policyMap = Collections.emptyMap();
        if (structureMap == null)
            structureMap = Collections.emptyMap();
        if (testerMap == null)
            testerMap = Collections.emptyMap();
        if (valueProviderMap == null)
            valueProviderMap = Collections.emptyMap();
        if (categoryList == null)
            categoryList = Collections.emptyList();
        if (categoryMap == null)
            categoryMap = Collections.emptyMap();
    }

    private void lazyLoad() {
        if (Platform.isRunning()) {
            readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                    RegistryConstants.EXT_BRANCH_POLICIES);
        }
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (RegistryConstants.TAG_BRANCH_POLICY.equals(name)) {
            readBranchPolicy(element);
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_STRUCTURE.equals(name)) {
            readStructureAlgorithm(element);
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_PROPERTY_TESTER.equals(name)) {
            readPropertyTester(element);
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_STYLE_VALUE_PROVIDER.equals(name)) {
            readStyleValueProvider(element);
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_ADDITIONAL_STRUCTURES.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_ADDITIONAL_STRUCTURE.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_BRANCH_HOOK.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_STYLE_SELECTOR.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_OVERRIDED_STYLE.equals(name)) {
            readElementChildren(element);
            return true;
//        } else if (RegistryConstants.TAG_INHERITED_STYLE.equals(name)) {
//            readElementChildren(element);
//            return true;
        } else if (RegistryConstants.TAG_UNMODIFIABLE_PROPERTIES.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_UNMODIFIABLE_PROPERTY.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_LAYER.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_STRUCTURE_CACHES.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_STRUCTURE_CACHE.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_OVERRIDE.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_ADVISOR.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (TAG_ENABLEMENT.equals(name)) {
            return true;
        } else if (RegistryConstants.TAG_BRANCH_POLICY_CATEGORY.equals(name)) {
            readBranchPolicyCategory(element);
            readElementChildren(element);
            return true;
        }
        return false;
    }

    private void readBranchPolicy(IConfigurationElement element) {
        try {
            BranchPolicy descriptor = new BranchPolicy(this, element);
            registerBranchPolicy(descriptor);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load branch policy: " //$NON-NLS-1$
                    + element.toString());
        }
    }

    private void readStructureAlgorithm(IConfigurationElement element) {
        try {
            StructureDescriptor descriptor = new StructureDescriptor(element);
            registryStructureAlgorithm(descriptor);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load structure algorithm: " //$NON-NLS-1$
                    + element.toString());
        }
    }

    private void readPropertyTester(IConfigurationElement element) {
        try {
            BranchPropertyTesterProxy proxy = new BranchPropertyTesterProxy(
                    element);
            registerPropertyTester(proxy);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load branch property tester: " //$NON-NLS-1$ 
                    + element.toString());
        }
    }

    private void readStyleValueProvider(IConfigurationElement element) {
        try {
            ContributedStyleValueProvider valueProvider = new ContributedStyleValueProvider(
                    element);
            registerValueProvider(valueProvider);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load style value provider: " //$NON-NLS-1$
                    + element.toString());
        }
    }

    private void readBranchPolicyCategory(IConfigurationElement element) {
        try {
            BranchPolicyCategoryDescriptor descriptor = new BranchPolicyCategoryDescriptor(
                    element);
            registerBranchPolicyCategory(descriptor);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load branch policy category: " //$NON-NLS-1$
                    + element.toString());
        }
    }

    private void registerBranchPolicy(BranchPolicy descriptor) {
        if (policyList == null)
            policyList = new ArrayList<IBranchPolicyDescriptor>();
        policyList.add(descriptor);
        if (policyMap == null)
            policyMap = new HashMap<String, BranchPolicy>();
        policyMap.put(descriptor.getId(), descriptor);
    }

    private void registryStructureAlgorithm(StructureDescriptor descriptor) {
        if (structureMap == null)
            structureMap = new HashMap<String, StructureDescriptor>();
        structureMap.put(descriptor.getId(), descriptor);
    }

    private void registerPropertyTester(BranchPropertyTesterProxy tester) {
        if (testerMap == null)
            testerMap = new HashMap<String, BranchPropertyTesterProxy>();
        testerMap.put(tester.getId(), tester);
    }

    private void registerValueProvider(
            ContributedStyleValueProvider valueProvider) {
        if (valueProviderMap == null)
            valueProviderMap = new HashMap<String, ContributedStyleValueProvider>();
        valueProviderMap.put(valueProvider.getId(), valueProvider);
    }

    private void registerBranchPolicyCategory(
            BranchPolicyCategoryDescriptor descriptor) {
        if (categoryList == null)
            categoryList = new ArrayList<IBranchPolicyCategoryDescriptor>();
        categoryList.add(descriptor);
        if (categoryMap == null)
            categoryMap = new HashMap<String, BranchPolicyCategoryDescriptor>();
        categoryMap.put(descriptor.getId(), descriptor);
    }

    /**
     * Start the registry reading process using the supplied plugin ID and
     * extension point.
     * 
     * @param registry
     *            the registry to read from
     * @param pluginId
     *            the plugin id of the extenion point
     * @param extensionPoint
     *            the extension point id
     */
    public void readRegistry(IExtensionRegistry registry, String pluginId,
            String extensionPoint) {
        IExtensionPoint point = registry.getExtensionPoint(pluginId,
                extensionPoint);
        if (point == null) {
            return;
        }
        IExtension[] extensions = point.getExtensions();
        if (extensions.length <= 0)
            return;

//        Comparator<IExtension> comparer = new Comparator<IExtension>() {
//            public int compare(IExtension arg0, IExtension arg1) {
//                String s1 = arg0.getNamespaceIdentifier();
//                String s2 = arg1.getNamespaceIdentifier();
//                return s1.compareToIgnoreCase(s2);
//            }
//        };
//        IExtension myExtension = null;
        int i = 0;
//        for (; i < extensions.length; i++) {
//            IExtension ext = extensions[i];
//            String contributorName = ext.getNamespaceIdentifier();
//            if (contributorName.startsWith("org.xmind.")) { //$NON-NLS-1$
//                myExtension = ext;
//                break;
//            }
//        }
//        if (myExtension != null) {
//            extensions[i] = extensions[0];
//            extensions[0] = myExtension;
//            Arrays.sort(extensions, 1, extensions.length, comparer);
//        } else {
//            Arrays.sort(extensions, comparer);
//        }
        for (i = 0; i < extensions.length; i++) {
            readExtension(extensions[i]);
        }
    }
}