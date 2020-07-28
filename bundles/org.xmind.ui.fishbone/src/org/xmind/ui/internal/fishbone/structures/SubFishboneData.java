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
package org.xmind.ui.internal.fishbone.structures;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IRotatableFigure;
import org.xmind.gef.draw2d.IRotatableReferencedFigure;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionHorizontalFlipper;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class SubFishboneData extends BranchStructureData {

    private static final int PADDING = 1;

    private static final double fMinor = 1d;

    private ISubDirection direction;

    private FishboneData data = null;

    private double padding = -1;

    public final IPrecisionTransformer h = new PrecisionHorizontalFlipper();

    public final PrecisionRotator r1 = new PrecisionRotator();

    public final PrecisionRotator r2 = new PrecisionRotator();

    public SubFishboneData(IBranchPart branch, ISubDirection direction) {
        super(branch);
        this.direction = direction;
        r1.setAngle(direction.isRotated() ? direction.getRotateAngle()
                : direction.getSubDirection().getRotateAngle());
        r1.setEnabled(direction.isRotated());
        h.setEnabled(direction.isRightHeaded());
        r2.setAngle(h.isEnabled() ? r1.getAngle() : -r1.getAngle());
    }

    public FishboneData getFishboneData() {
        if (data == null) {
            data = createFishboneData();
        }
        return data;
    }

    public double getPadding() {
        if (padding < 0) {
            padding = calcPadding();
        }
        return padding;
    }

    private double calcPadding() {
        int p = PADDING;
        IStyleSelector ss = getBranch().getBranchPolicy().getStyleSelector(
                getBranch());
        int lineWidth = StyleUtils.getInteger(getBranch(), ss,
                Styles.LineWidth, 1);
        p += lineWidth * 0.5d;
        return p;
    }

    private FishboneData createFishboneData() {
        FishboneData data = new FishboneData();
        ITopicPart topicPart = getBranch().getTopicPart();
        if (topicPart != null) {
            IFigure figure = topicPart.getFigure();
            if (figure instanceof IRotatableReferencedFigure) {
                data.topicRefIns = new PrecisionInsets(
                        ((IRotatableReferencedFigure) figure)
                                .getNormalReferenceDescription());
            }
        }
        ILabelPart label = getBranch().getLabel();
        if (label != null && label.getFigure().isVisible()) {
            IFigure labelFigure = label.getFigure();
            PrecisionDimension size;
            if (labelFigure instanceof IRotatableFigure) {
                size = ((IRotatableFigure) labelFigure).getNormalPreferredSize(
                        -1, -1);
            } else {
                size = new PrecisionDimension(label.getFigure()
                        .getPreferredSize());
            }
            if (data.topicRefIns == null) {
                data.topicRefIns = new PrecisionInsets(0, size.width / 2,
                        size.height, size.width / 2);
            } else {
                data.topicRefIns.left = Math.max(data.topicRefIns.left,
                        size.width / 2);
                data.topicRefIns.right = Math.max(data.topicRefIns.right,
                        size.width / 2);
                data.topicRefIns.bottom += size.height;
            }
        }
        if (data.topicRefIns == null) {
            data.topicRefIns = new PrecisionInsets();
        }
        data.rTopicRefIns = h.ri(r2.ti(h.ti(data.topicRefIns)));

        data.branchRefIns = new PrecisionInsets(data.topicRefIns);
        data.rBranchRefIns = h.ri(r2.ti(h.ti(data.branchRefIns)));

        List<IBranchPart> calloutBranches = getBranch().getCalloutBranches();
        if (!calloutBranches.isEmpty() && !getBranch().isFolded()) {
            this.direction.fillFishboneExtraData(getBranch(), data, h, r2, 0,
                    true);
        }

        if (!getSubBranches().isEmpty() && !getBranch().isFolded()) {
            double spacing = getMinorSpacing() * fMinor;
            this.direction.fillFishboneData(getBranch(), data, h, r2, spacing,
                    getSubBranches());

            double padding = getPadding();
            data.branchRefIns.add(padding);
            data.rBranchRefIns.add(padding);
        }

        return data;
    }

}