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
package org.xmind.ui.internal.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.AbstractViewer;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.graphicalpolicy.AbstractGraphicalPolicy;
import org.xmind.gef.graphicalpolicy.AbstractStyleSelector;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.PartRegistry;
import org.xmind.gef.service.IShadowService;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchStyleSelector;
import org.xmind.ui.internal.mindmap.BranchPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class BranchDummy {

    private static class DummyBranchStructure extends AbstractBranchStructure {

        private static DummyBranchStructure instance = new DummyBranchStructure();

        @Override
        protected void doFillPlusMinus(IBranchPart branch,
                IPlusMinusPart plusMinus, LayoutInfo info) {
        }

        @Override
        protected void doFillSubBranches(IBranchPart branch,
                List<IBranchPart> subBranches, LayoutInfo info) {
        }

    }

    private static class DummyStyleSelector extends AbstractStyleSelector
            implements IBranchStyleSelector {

        private IBranchPart sourceBranch;

        private Map<String, String> overridedStyles = null;

        public DummyStyleSelector(IBranchPart sourceBranch) {
            this.sourceBranch = sourceBranch;
        }

        public String getAutoValue(IGraphicalPart part, String key,
                IStyleValueProvider defaultValueProvider) {
            if (overridedStyles != null && overridedStyles.containsKey(key))
                return overridedStyles.get(key);

            if (Styles.RotateAngle.equals(key))
                return Double.toString(0);
            return sourceBranch.getBranchPolicy().getStyleSelector(sourceBranch)
                    .getAutoValue(sourceBranch, key, defaultValueProvider);
        }

        public String getUserValue(IGraphicalPart part, String key) {
            if (overridedStyles != null && overridedStyles.containsKey(key))
                return overridedStyles.get(key);

            if (Styles.RotateAngle.equals(key))
                return Double.toString(0);
            return sourceBranch.getBranchPolicy().getStyleSelector(sourceBranch)
                    .getUserValue(sourceBranch, key);
        }

        public String getStyleValue(IGraphicalPart part, String key,
                IStyleValueProvider defaultValueProvider) {
            if (overridedStyles != null && overridedStyles.containsKey(key))
                return overridedStyles.get(key);

            if (Styles.RotateAngle.equals(key))
                return Double.toString(0);
            return sourceBranch.getBranchPolicy().getStyleSelector(sourceBranch)
                    .getStyleValue(sourceBranch, key, defaultValueProvider);
        }

        public void flushStyleCaches(IBranchPart branch) {
        }

        public void setOverridedStyle(String key, String value) {
            if (overridedStyles == null)
                overridedStyles = new HashMap<String, String>();
            overridedStyles.put(key, value);
        }

        public void removeOverridedStyle(String key) {
            if (overridedStyles != null) {
                overridedStyles.remove(key);
            }
        }

    }

    private static class DummyBranchPolicy extends AbstractGraphicalPolicy
            implements IBranchPolicy {

        private IBranchPart sourceBranch;

        public DummyBranchPolicy(IBranchPart sourceBranch) {
            this.sourceBranch = sourceBranch;
        }

        protected IStructure createDefaultStructureAlgorithm() {
            return DummyBranchStructure.instance;
        }

        protected IStyleSelector createDefaultStyleSelector() {
            return new DummyStyleSelector(sourceBranch);
        }

        public void flushStructureCache(IBranchPart branch, boolean ancestors,
                boolean descendants) {
        }

        public boolean isPropertyModifiable(IBranchPart branch,
                String propertyKey) {
            return false;
        }

        public boolean isPropertyModifiable(IBranchPart branch,
                String propertyKey, String secondaryKey) {
            return false;
        }

        public void postDeactivate(IBranchPart branch) {
        }

    }

    private IGraphicalViewer viewer;

    private IBranchPart branch;

    private ITopic topic;

    public BranchDummy(IGraphicalViewer viewer, boolean newTopic) {
        this(viewer, null, newTopic);
    }

    public BranchDummy(IGraphicalViewer viewer, IBranchPart sourceBranch) {
        this(viewer, sourceBranch, false);
    }

    private BranchDummy(IGraphicalViewer viewer, IBranchPart sourceBranch,
            boolean newTopic) {
        this.viewer = viewer;
        create(sourceBranch, newTopic);
        pack(sourceBranch);
    }

    private void create(final IBranchPart sourceBranch, boolean newTopic) {
        topic = createDummyTopic(sourceBranch, newTopic);

        if (sourceBranch != null || !newTopic) {
            PartRegistry partRegistry = viewer.getPartRegistry();
            if (viewer instanceof AbstractViewer)
                ((AbstractViewer) viewer).setPartRegistry(null);
            BranchPart branchPart = new BranchPart();
            if (sourceBranch != null)
                branchPart.setOverrideBranchType(sourceBranch.getBranchType());
            branch = branchPart;
            branch.setModel(topic);
            branch.setParent(viewer.getRootPart());
            addBranchView();
            branch.addNotify();
            branch.getStatus().activate();
            removeShadow();
            if (sourceBranch != null)
                ((BranchPart) branch).setGraphicalPolicy(
                        new DummyBranchPolicy(sourceBranch));
            if (viewer instanceof AbstractViewer)
                ((AbstractViewer) viewer).setPartRegistry(partRegistry);
        } else {
            IPart topicPart = viewer.findPart(topic);
            branch = MindMapUtils.findBranch(topicPart);
            branch.getFigure().setEnabled(false);
        }

        branch.refresh();
    }

    private void removeShadow() {
        IShadowService shadowService = (IShadowService) viewer
                .getService(IShadowService.class);
        if (shadowService != null) {
            shadowService.removeShadow(branch.getTopicPart().getFigure());
        }
    }

    private void pack(IBranchPart sourceBranch) {
        pack();
        if (sourceBranch != null) {
            ((IReferencedFigure) branch.getFigure())
                    .setReference(((IReferencedFigure) sourceBranch.getFigure())
                            .getReference());
        }
    }

    public void pack() {
        IFigure figure = branch.getFigure();
        figure.setSize(figure.getPreferredSize());
    }

    private ITopic createDummyTopic(IBranchPart sourceBranch,
            boolean newTopic) {
        ITopic topic;
        if (sourceBranch == null && newTopic) {
            ITopic centralTopic = (ITopic) viewer.getAdapter(ITopic.class);
            topic = centralTopic.getOwnedWorkbook().createTopic();
            centralTopic.add(topic, ITopic.DETACHED);
        } else if (sourceBranch != null) {
            ITopic sourceTopic = sourceBranch.getTopic();
            topic = sourceTopic.getOwnedWorkbook().createTopic();
            topic.setTitleText(sourceTopic.getTitleText());
            topic.setStyleId(sourceTopic.getStyleId());
            topic.setTitleWidth(sourceTopic.getTitleWidth());
        } else {
            topic = Core.getWorkbookBuilder().createWorkbook().createTopic();
        }
        return topic;
    }

    public IBranchPart getBranch() {
        return branch;
    }

    public ITopic getTopic() {
        return topic;
    }

    public void dispose() {
        if (topic != null) {
            ITopic parent = topic.getParent();
            if (parent != null) {
                parent.remove(topic);
                branch = null;
            } else {
                if (branch != null) {
                    branch.getStatus().deactivate();
                    branch.removeNotify();
                    removeBranchView();
                    branch.setParent(null);
                    branch = null;
                }
            }
            topic = null;
        }

    }

    private void addBranchView() {
        Layer layer = viewer.getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            layer.add(branch.getFigure());
        }
    }

    private void removeBranchView() {
        IFigure figure = branch.getFigure();
        if (figure.getParent() != null) {
            figure.getParent().remove(figure);
        }
    }

    public void setStyle(String key, String value) {
        IStyleSelector ss = branch.getBranchPolicy().getStyleSelector(branch);
        if (ss instanceof DummyStyleSelector) {
            ((DummyStyleSelector) ss).setOverridedStyle(key, value);
        } else {
            IStyleSheet styleSheet = topic.getOwnedWorkbook().getStyleSheet();
            IStyle style = styleSheet.findStyle(topic.getStyleId());
            if (style == null) {
                style = styleSheet.createStyle(IStyle.TOPIC);
                styleSheet.addStyle(style, IStyleSheet.NORMAL_STYLES);
                topic.setStyleId(style.getId());
            }
            style.setProperty(key, value);
        }
    }

    public void removeOverridedStyle(String key) {
        IStyleSelector ss = branch.getBranchPolicy().getStyleSelector(branch);
        if (ss instanceof DummyStyleSelector) {
            ((DummyStyleSelector) ss).removeOverridedStyle(key);
        } else {
            setStyle(key, null);
        }
    }

}