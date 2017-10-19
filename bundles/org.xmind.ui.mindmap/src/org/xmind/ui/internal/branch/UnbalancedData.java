package org.xmind.ui.internal.branch;

import static org.xmind.ui.internal.branch.BaseRadialStructure.CACHE_NUMBER_RIGHT_BRANCHES;

import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class UnbalancedData extends RadialData {

    public final static String STRUCTUREID_UNBALANCED = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
    public final static String EXTENTION_UNBALANCEDSTRUCTURE = "org.xmind.ui.map.unbalanced"; //$NON-NLS-1$
    public final static String EXTENTIONELEMENT_RIGHTNUMBER = "right-number";//$NON-NLS-1$

    public UnbalancedData(IBranchPart branch) {
        super(branch);
    }

    @Override
    public int getNumRight() {
        IBranchPart branch = getBranch();
        Integer num = (Integer) MindMapUtils.getCache(branch,
                CACHE_NUMBER_RIGHT_BRANCHES);
        if (num != null)
            return num.intValue();
        ITopicExtension extension = branch.getTopic()
                .getExtension(EXTENTION_UNBALANCEDSTRUCTURE);
        if (extension == null)
            return super.getNumRight();
        ITopicExtensionElement element = extension.getContent()
                .getCreatedChild(EXTENTIONELEMENT_RIGHTNUMBER);
        String rightNum = element.getTextContent();
        if (rightNum != null) {
            int value = Integer.valueOf(rightNum).intValue();
            if (value < 0) {
                int superRightNum = super.getNumRight();
                element.setTextContent(String.valueOf(superRightNum));
                return superRightNum;
            }
            return value;
        } else {
            int superRightNum = super.getNumRight();
            element.setTextContent(String.valueOf(superRightNum));
            return superRightNum;
        }
    }

}
