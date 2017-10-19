package org.xmind.ui.internal.branch;

import java.util.HashMap;
import java.util.Map;

import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IBranchStyleSelector;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.MindMapStyleSelectorBase;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public abstract class AbstractBranchStyleSelector
        extends MindMapStyleSelectorBase implements IBranchStyleSelector {

    private Map<String, String> inheritedStyleKeys;

    protected void registerInheritedStyleKey(String key, String layer) {
        if (key == null)
            return;
        if (inheritedStyleKeys == null)
            inheritedStyleKeys = new HashMap<String, String>();
        inheritedStyleKeys.put(key, layer);
    }

    @Override
    public String getAutoValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider) {
        String familyName = getFamilyName(part);
        if ((Styles.LineColor.equals(key) || Styles.LineWidth.equals(key))
                && (Styles.FAMILY_MAIN_TOPIC.equals(familyName)
                        || Styles.FAMILY_SUB_TOPIC.equals(familyName)
                        || Styles.FAMILY_SUMMARY_TOPIC.equals(familyName))
                && part instanceof IBranchPart) {

            String multiColor = Styles.LineColor.equals(key)
                    ? StyleUtils.getIndexedBranchLineColor((IBranchPart) part)
                    : null;

            String[] ancestorUserValueAndBranchType = getAncestorUserValueAndBranchType(
                    part, key);
            String ancestorUserValue = ancestorUserValueAndBranchType[0];
            String ancestorBranchType = ancestorUserValueAndBranchType[1];

            if (isValidValue(part, key, multiColor)
                    && MindMapUI.BRANCH_CENTRAL.equals(ancestorBranchType))
                return multiColor;

            if (isValidValue(part, key, ancestorUserValue))
                return ancestorUserValue;
            if (isValidValue(part, key, multiColor))
                return multiColor;

        }

        return super.getAutoValue(part, key, defaultValueProvider);
    }

    private String[] getAncestorUserValueAndBranchType(IGraphicalPart part,
            String key) {
        String[] ancestorUserValueAndBranchType = new String[2];

        if (!(part instanceof IBranchPart))
            return ancestorUserValueAndBranchType;

        IBranchPart parentBranchPart = ((IBranchPart) part).getParentBranch();
        if (parentBranchPart == null)
            return ancestorUserValueAndBranchType;

        IStyleSelector styleSelector = parentBranchPart.getBranchPolicy()
                .getStyleSelector(parentBranchPart);
        if (styleSelector == null)
            return ancestorUserValueAndBranchType;

        String parentUserValue = styleSelector.getUserValue(parentBranchPart,
                key);
        if (isValidValue(parentBranchPart, key, parentUserValue)) {
            ancestorUserValueAndBranchType[0] = parentUserValue;
            ancestorUserValueAndBranchType[1] = parentBranchPart
                    .getBranchType();
            return ancestorUserValueAndBranchType;
        }

        return getAncestorUserValueAndBranchType(parentBranchPart, key);
    }

    protected String getThemeStyleValue(IGraphicalPart part, String familyName,
            String key) {
        if ((Styles.LineColor.equals(key) || Styles.LineWidth.equals(key))
                && (Styles.FAMILY_MAIN_TOPIC.equals(familyName)
                        || Styles.FAMILY_SUB_TOPIC.equals(familyName)
                        || Styles.FAMILY_SUMMARY_TOPIC.equals(familyName))
                && part instanceof IBranchPart) {
            String value = super.getThemeStyleValue(part, familyName, key);
            if (isValidValue(part, key, value))
                return value;
        }
        if (inheritedStyleKeys != null && inheritedStyleKeys.containsKey(key)) {
            if (part instanceof IBranchPart) {
                String value = ParentValueProvider
                        .getValueProvider((IBranchPart) part)
                        .getParentValue(key);
                if (value != null)
                    return value;
            }
        }
        return super.getThemeStyleValue(part, familyName, key);
    }

    protected String getLayeredProperty(IGraphicalPart part, String layerName,
            String familyName, String key) {
        if (part instanceof IBranchPart && inheritedStyleKeys != null
                && layerName.equals(inheritedStyleKeys.get(key))) {
            String value = getLayeredProperty(part,
                    Styles.LAYER_BEFORE_PARENT_VALUE, familyName, key);
            if (isValidValue(part, key, value)) {
                return getCheckedValue(value);
            }
            value = ParentValueProvider.getValueProvider((IBranchPart) part)
                    .getParentValue(key);
            if (isValidValue(part, key, value))
                return getCheckedValue(value);
        }
        return null;
    }

    @Override
    public String getFamilyName(IGraphicalPart part) {
        if (part instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) part;
            String branchType = branch.getBranchType();
            if (MindMapUI.BRANCH_CENTRAL.equals(branchType))
                return Styles.FAMILY_CENTRAL_TOPIC;
            if (MindMapUI.BRANCH_MAIN.equals(branchType))
                return Styles.FAMILY_MAIN_TOPIC;
            if (MindMapUI.BRANCH_CALLOUT.equals(branchType))
                return Styles.FAMILY_CALLOUT_TOPIC;
            if (MindMapUI.BRANCH_FLOATING.equals(branchType))
                return Styles.FAMILY_FLOATING_TOPIC;
            if (MindMapUI.BRANCH_SUMMARY.equals(branchType))
                return Styles.FAMILY_SUMMARY_TOPIC;
        }
        return Styles.FAMILY_SUB_TOPIC;
    }

    public void flushStyleCaches(IBranchPart branch) {
        ParentValueProvider.flush(branch);
    }

}
