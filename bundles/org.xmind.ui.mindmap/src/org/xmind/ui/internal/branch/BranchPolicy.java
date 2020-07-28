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

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CATEGORY_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_DESCRIPTION;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ICON;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_NAME;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_ENABLEMENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.branch.IBranchPolicyAdvisor;
import org.xmind.ui.branch.IBranchPolicyDescriptor;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ICacheManager;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.MindMapUtils;

class BranchPolicy extends AbstractBranchPolicy implements
        IBranchPolicyDescriptor {

    private static final String SHOULD_NOTIFY_POST_DEACTIVATE = "org.xmind.ui.branch.internal.shouldNotifyPostDeactivate"; //$NON-NLS-1$

    private IConfigurationElement element;

    private ImageDescriptor icon;

    private Expression enablementCondition;

    private Expression overrideCondition;

    private String defaultStructureId;

    private IStructureDescriptor defaultStructure;

    private List<AdditionalStructure> additionalStructures;

    private IBranchPropertyTester propertyTester;

    private boolean triedLoadingPropertyTester;

    private BranchHookFactory branchHookFactory;

    private boolean triedLoadingBranchHook;

    private Set<String> structureCacheKeys;

    private Map<String, Map<String, List<UnmodifiableProperty>>> unmodifiableProperties;

    private IBranchPolicyAdvisor advisor;

    private boolean triedLoadingAdvisor;

    private Expression advisorCondition;

    public BranchPolicy(BranchPolicyManager manager,
            IConfigurationElement element) throws CoreException {
        super(manager, element.getAttribute(ATT_ID));
        this.element = element;
        load();
    }

    private void load() throws CoreException {
        defaultStructureId = element
                .getAttribute(RegistryConstants.ATT_DEFAULT_STRUCTURE_ID);
        if (defaultStructureId == null)
            throw new CoreException(new Status(IStatus.ERROR,
                    element.getNamespaceIdentifier(), 0,
                    "Invalid extension (missing default structure id): " //$NON-NLS-1$
                            + getId(), null));
        initializeEnablement();
        initializeOverride();
    }

    private void initializeEnablement() {
        IConfigurationElement[] elements = element
                .getChildren(IWorkbenchRegistryConstants.TAG_ENABLEMENT);
        if (elements.length == 0)
            return;

        try {
            enablementCondition = ExpressionConverter.getDefault().perform(
                    elements[0]);
        } catch (CoreException e) {
            Logger.log(e, "Unable to convert enablement expression:" + getId()); //$NON-NLS-1$
        }
    }

    private void initializeOverride() {
        IConfigurationElement[] overrideElements = element
                .getChildren(RegistryConstants.TAG_OVERRIDE);
        if (overrideElements.length == 0)
            return;

        IConfigurationElement[] elements = overrideElements[0]
                .getChildren(IWorkbenchRegistryConstants.TAG_ENABLEMENT);
        if (elements.length == 0)
            return;

        try {
            overrideCondition = ExpressionConverter.getDefault().perform(
                    elements[0]);
        } catch (CoreException e) {
            Logger.log(e, "Unable to convert override expression:" + getId()); //$NON-NLS-1$
        }
    }

    public String getId() {
        return getPolicyId();
    }

    public ImageDescriptor getIcon() {
        if (icon == null) {
            icon = createIcon();
        }
        return icon;
    }

    private ImageDescriptor createIcon() {
        String iconName = element.getAttribute(ATT_ICON);
        if (iconName != null) {
            String plugId = element.getNamespaceIdentifier();
            return AbstractUIPlugin.imageDescriptorFromPlugin(plugId, iconName);
        }
        return null;
    }

    public String getName() {
        return element.getAttribute(ATT_NAME);
    }

    public String getDescription() {
        return element.getAttribute(ATT_DESCRIPTION);
    }

    public String getCategoryId() {
        return element.getAttribute(ATT_CATEGORY_ID);
    }

    public String getName(IBranchPart branch) {
        return getName();
    }

    public ImageDescriptor getIcon(IBranchPart branch) {
        return getIcon();
    }

    public boolean isApplicableTo(IBranchPart branch) {
        if (enablementCondition == null)
            return true;
        return isApplicableTo(BranchPolicyManager
                .createBranchEvaluationContext(branch));
    }

    boolean isApplicableTo(IEvaluationContext context) {
        if (enablementCondition == null)
            return true;
        try {
            EvaluationResult result = enablementCondition.evaluate(context);
            return result == EvaluationResult.TRUE;
        } catch (CoreException e) {
            Logger.log(e, "Evaluation failed on: " + getId()); //$NON-NLS-1$
        }
        return false;
    }

    boolean canOverride(IEvaluationContext context) {
        if (overrideCondition == null)
            return false;
        try {
            EvaluationResult result = overrideCondition.evaluate(context);
            return result == EvaluationResult.TRUE;
        } catch (CoreException e) {
            Logger.log(e, "Evaluation failed on: " + getId()); //$NON-NLS-1$
        }
        return false;
    }

    private IStructureDescriptor getDefaultStructure() {
        if (defaultStructure == null) {
            defaultStructure = manager
                    .getStructureDescriptor(defaultStructureId);
        }
        return defaultStructure;
    }

    public String getDefaultStructureId() {
        return defaultStructureId;
    }

    private List<AdditionalStructure> getAdditionalStructures() {
        if (additionalStructures == null) {
            loadAdditionalStructures();
            if (additionalStructures == null) {
                additionalStructures = Collections.emptyList();
            }
        }
        return additionalStructures;
    }

    private void loadAdditionalStructures() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_ADDITIONAL_STRUCTURES);
        if (children.length == 0)
            return;

        children = children[0]
                .getChildren(RegistryConstants.TAG_ADDITIONAL_STRUCTURE);
        for (IConfigurationElement child : children) {
            readAdditionalStructure(child);
        }
    }

    private void readAdditionalStructure(IConfigurationElement element) {
        try {
            AdditionalStructure additionalStructure = new AdditionalStructure(
                    manager, element);
            registerAdditionStructure(additionalStructure);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load additional structure: " //$NON-NLS-1$
                    + element);
        }
    }

    private void registerAdditionStructure(
            AdditionalStructure additionalStructure) {
        if (additionalStructures == null)
            additionalStructures = new ArrayList<AdditionalStructure>();
        additionalStructures.add(additionalStructure);
    }

    private BranchHookFactory getBranchHookFactory() {
        if (branchHookFactory == null && !triedLoadingBranchHook) {
            loadBranchHookFactories();
            triedLoadingBranchHook = true;
        }
        return branchHookFactory;
    }

    private void loadBranchHookFactories() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_BRANCH_HOOK);
        if (children.length == 0)
            return;

        readBranchHookFactory(children[0]);
    }

    private void readBranchHookFactory(IConfigurationElement element) {
        try {
            branchHookFactory = new BranchHookFactory(element);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load branch hook: " + element); //$NON-NLS-1$
        }
    }

    protected void activateBranch(IBranchPart branch) {
        super.activateBranch(branch);
        ICacheManager cm = MindMapUtils.getCacheManager(branch);
        if (cm != null) {
            cm.setValueProvider(IBranchPropertyTester.CACHE_PROPERTY_TESTER,
                    this);
            cm.flush(IBranchPropertyTester.CACHE_PROPERTY_TESTER);
        }
        if (shouldNotifyAdvisor(branch)) {
            IBranchPolicyAdvisor advisor = getAdvisor();
            if (advisor != null) {
                advisor.postActivate(branch, this);
            }
        }
    }

    protected void deactivateBranch(IBranchPart branch) {
        boolean shouldNotifyAdvisor = shouldNotifyAdvisor(branch);
//        if (shouldNotifyAdvisor) {
//            IBranchPolicyAdvisor advisor = getAdvisor();
//            if (advisor != null) {
//                advisor.deactivate(branch, this);
//            }
//        }
        ICacheManager cm = MindMapUtils.getCacheManager(branch);
        if (cm != null) {
            cm.flush(IBranchPropertyTester.CACHE_PROPERTY_TESTER);
            cm.removeValueProvider(IBranchPropertyTester.CACHE_PROPERTY_TESTER);
        }
        super.deactivateBranch(branch);
        if (cm != null) {
            cm.setCache(SHOULD_NOTIFY_POST_DEACTIVATE,
                    Boolean.valueOf(shouldNotifyAdvisor));
        }
    }

    private boolean shouldNotifyAdvisor(IBranchPart branch) {
        if (getAdvisor() != null) {
            if (advisorCondition != null) {
                IEvaluationContext context = BranchPolicyManager
                        .createBranchEvaluationContext(branch);
                try {
                    return advisorCondition.evaluate(context) == EvaluationResult.TRUE;
                } catch (CoreException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected IBranchHook createHook(IBranchPart branch) {
        BranchHookFactory factory = getBranchHookFactory();
        if (factory != null)
            return factory.createHook(branch);
        return null;
    }

    public void postDeactivate(IBranchPart branch) {
        super.postDeactivate(branch);
        Object shouldUsePostDeactivator = MindMapUtils.getCache(branch,
                SHOULD_NOTIFY_POST_DEACTIVATE);
        if (shouldUsePostDeactivator instanceof Boolean
                && ((Boolean) shouldUsePostDeactivator).booleanValue()) {
            IBranchPolicyAdvisor deactivator = getAdvisor();
            if (deactivator != null) {
                deactivator.postDeactivate(branch, this);
            }
        }
        MindMapUtils.flushCache(branch, SHOULD_NOTIFY_POST_DEACTIVATE);
    }

    protected void flushStructureCache(IBranchPart branch) {
        super.flushStructureCache(branch);
        for (String cacheKey : getStructureCacheKeys()) {
            MindMapUtils.flushCache(branch, cacheKey);
        }
    }

    private Set<String> getStructureCacheKeys() {
        if (structureCacheKeys == null) {
            IConfigurationElement[] children = element
                    .getChildren(RegistryConstants.TAG_STRUCTURE_CACHES);
            if (children.length > 0) {
                children = children[0]
                        .getChildren(RegistryConstants.TAG_STRUCTURE_CACHE);
                if (children.length > 0) {
                    for (IConfigurationElement child : children) {
                        readStructureCache(child);
                    }
                }
            }
            if (structureCacheKeys == null)
                structureCacheKeys = Collections.emptySet();
        }
        return structureCacheKeys;
    }

    private void readStructureCache(IConfigurationElement element) {
        String key = element.getAttribute(IWorkbenchRegistryConstants.ATT_KEY);
        if (key != null) {
            if (structureCacheKeys == null)
                structureCacheKeys = new HashSet<String>();
            structureCacheKeys.add(key);
        }
    }

    private IBranchPropertyTester getPropertyTester() {
        if (propertyTester == null && !triedLoadingPropertyTester) {
            String propertyTesterId = element
                    .getAttribute(RegistryConstants.ATT_PROPERTY_TESTER_ID);
            if (propertyTesterId != null) {
                propertyTester = manager.getPropertyTester(propertyTesterId);
            }
            triedLoadingPropertyTester = true;
        }
        return propertyTester;
    }

    protected IStructure createDefaultStructureAlgorithm() {
        return getDefaultStructure().getAlgorithm();
    }

    protected IStyleSelector createDefaultStyleSelector() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_STYLE_SELECTOR);
        if (children.length > 0) {
            return new ContributedBranchStyleSelector(manager, children[0]);
        }
        return super.createDefaultStyleSelector();
    }

    protected boolean isUnmodifiableProperty(IBranchPart branch,
            String primaryKey, String secondaryKey) {
        Map<String, Map<String, List<UnmodifiableProperty>>> unmoProps = getUnmodifiableProperties();
        if (!unmoProps.isEmpty()) {
            Map<String, List<UnmodifiableProperty>> map = unmoProps
                    .get(primaryKey);
            if (map != null) {
                if (secondaryKey == null)
                    secondaryKey = Styles.NULL;
                List<UnmodifiableProperty> list = map.get(secondaryKey);
                if (list != null) {
                    IEvaluationContext context = BranchPolicyManager
                            .createBranchEvaluationContext(branch);
                    for (UnmodifiableProperty unmoProp : list) {
                        if (unmoProp.isApplicableTo(context)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public Map<String, Map<String, List<UnmodifiableProperty>>> getUnmodifiableProperties() {
        if (unmodifiableProperties == null) {
            readUnmodifiableProperties();
            if (unmodifiableProperties == null)
                unmodifiableProperties = Collections.emptyMap();
        }
        return unmodifiableProperties;
    }

    private void readUnmodifiableProperties() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_UNMODIFIABLE_PROPERTIES);
        if (children.length == 0)
            return;

        children = children[0]
                .getChildren(RegistryConstants.TAG_UNMODIFIABLE_PROPERTY);
        for (IConfigurationElement child : children) {
            loadUnmodifiableProperty(child);
        }
    }

    private void loadUnmodifiableProperty(IConfigurationElement element) {
        try {
            UnmodifiableProperty unmodifiableProperty = new UnmodifiableProperty(
                    element);
            registerUnmodifiableProperty(unmodifiableProperty);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load unmodifiable property: " //$NON-NLS-1$ 
                    + element);
        }
    }

    private void registerUnmodifiableProperty(
            UnmodifiableProperty unmodifiableProperty) {
        String primaryKey = unmodifiableProperty.getPrimaryKey();
        String secondaryKey = unmodifiableProperty.getSecondaryKey();
        if (secondaryKey == null) {
            secondaryKey = Styles.NULL;
        }
        if (unmodifiableProperties == null)
            unmodifiableProperties = new HashMap<String, Map<String, List<UnmodifiableProperty>>>();
        Map<String, List<UnmodifiableProperty>> map = unmodifiableProperties
                .get(primaryKey);
        if (map == null) {
            map = new HashMap<String, List<UnmodifiableProperty>>();
            unmodifiableProperties.put(primaryKey, map);
        }
        List<UnmodifiableProperty> list = map.get(secondaryKey);
        if (list == null) {
            list = new ArrayList<UnmodifiableProperty>();
            map.put(secondaryKey, list);
        }
        list.add(unmodifiableProperty);
    }

    public Object getValue(IPart part, String key) {
        if (IBranchPropertyTester.CACHE_PROPERTY_TESTER.equals(key)) {
            return getPropertyTester();
        }
        return super.getValue(part, key);
    }

    protected String calcAdditionalStructureId(IBranchPart branch,
            IBranchPart parent) {
        List<AdditionalStructure> additionalStructures = getAdditionalStructures();
        if (!additionalStructures.isEmpty()) {
            IEvaluationContext context = BranchPolicyManager
                    .createBranchEvaluationContext(branch);
            for (AdditionalStructure additionalStructure : additionalStructures) {
                if (additionalStructure.isApplicableTo(context)) {
                    return additionalStructure.getStructureId();
                }
            }
        }
        return null;
    }

    private IBranchPolicyAdvisor getAdvisor() {
        if (advisor == null && !triedLoadingAdvisor) {
            triedLoadingAdvisor = true;
            IConfigurationElement[] children = element
                    .getChildren(RegistryConstants.TAG_ADVISOR);
            if (children.length > 0) {
                IConfigurationElement advisorElement = children[0];
                String att = advisorElement
                        .getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
                if (att != null) {
                    try {
                        advisor = (IBranchPolicyAdvisor) advisorElement
                                .createExecutableExtension(RegistryConstants.ATT_CLASS);
                        loadAdvisorCondition(advisorElement);
                    } catch (CoreException e) {
                        Logger.log(e, "Failed to create Post Deactivator: " //$NON-NLS-1$
                                + getId());
                    }
                }
            }
        }
        return advisor;
    }

    private void loadAdvisorCondition(IConfigurationElement pdElement) {
        IConfigurationElement[] enablements = pdElement
                .getChildren(TAG_ENABLEMENT);
        if (enablements.length == 0)
            return;

        try {
            advisorCondition = ExpressionConverter.getDefault().perform(
                    enablements[0]);
        } catch (CoreException e) {
            Logger.log(e, "Failed to convert Post Deactivator expression: " //$NON-NLS-1$
                    + getId());
        }
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof BranchPolicy))
            return false;
        BranchPolicy that = (BranchPolicy) obj;
        return this.element == that.element;
    }

}