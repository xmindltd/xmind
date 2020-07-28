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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.internal.fishbone.Fishbone;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.util.MindMapUtils;

public class SouthEastRotated extends AbstractSubFishboneDirection {

    public SouthEastRotated() {
        super(Fishbone.RotateAngle - 180.0d, true, false, false, WEST, WEST);
    }

    public ISubDirection getSubDirection() {
        return SE;
    }

    public void fillFishboneData(IBranchPart branch, FishboneData data,
            IPrecisionTransformer h, PrecisionRotator r, double spacing,
            List<IBranchPart> subbranches) {
        PrecisionPoint origin = h.getOrigin();
        PrecisionInsets hTopicIns = h.ti(data.topicRefIns);
        PrecisionRectangle hBranchBounds = h.ti(data.branchRefIns).getBounds(
                origin);
        PrecisionRectangle rhBranchBounds = h.ti(data.rBranchRefIns).getBounds(
                origin);

        double sin = r.sin();
        double ctg = r.cos() / r.sin();

        PrecisionPoint joint = origin.getTranslated(0,//-hTopicIns.left,
                hTopicIns.bottom);
        IPlusMinusPart plusMinus = branch.getPlusMinus();
        if (plusMinus != null) {
            joint.x -= plusMinus.getFigure().getPreferredSize().width;
        }

        IInsertion insertion = (IInsertion) MindMapUtils.getCache(branch,
                IInsertion.CACHE_INSERTION);
        int insIndex = insertion == null ? -1 : insertion.getIndex();
        Dimension insSize = insertion == null ? null : insertion.getSize();
        double insHeight = insSize == null ? 0 : insSize.height;

        IStructure structure = branch.getBranchPolicy().getStructure(branch);
        BoundaryLayoutHelper helper = ((SubFishboneStructure) structure)
                .getBoundaryLayoutHelper(branch);

        for (int i = 0; i < subbranches.size(); i++) {
            IBranchPart subBranch = subbranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();

            PrecisionInsets hChildBranchIns;
            PrecisionInsets rhChildBranchIns;
//            PrecisionInsets hChildBorder = h.ti(new PrecisionInsets(
//                    subBranchFigure.getInsets()));
            Insets ins = helper.getInsets(subBranch);
//            PrecisionInsets preciseIns = h.ri(new PrecisionInsets(ins));
            PrecisionInsets hChildBorder = h.ti(new PrecisionInsets(ins));
            PrecisionInsets rhChildBorder = r.ti(hChildBorder);

            IStructure bsa = subBranch.getBranchPolicy()
                    .getStructure(subBranch);
            if (bsa instanceof SubFishboneStructure) {
                SubFishboneStructure sfsa = (SubFishboneStructure) bsa;
                FishboneData subData = sfsa.getCastedData(subBranch)
                        .getFishboneData();
                hChildBranchIns = h.ti(subData.branchRefIns);
                rhChildBranchIns = h.ti(subData.rBranchRefIns);
            } else {
                PrecisionInsets childBranchNormal = new PrecisionInsets(
                        ((IReferencedFigure) subBranchFigure)
                                .getReferenceDescription());
                hChildBranchIns = h.ti(childBranchNormal);
                rhChildBranchIns = r.ti(hChildBranchIns);
            }

            double dy = rhChildBranchIns.top;
            double dx1 = Math.abs((hChildBorder.top + hChildBranchIns.top)
                    / sin);
            if (insIndex >= 0 && i == insIndex) {
                dx1 += Math.abs(insHeight / sin);
            }

            double dx2 = Math.abs(dy * ctg);
            double dx = dx1 + dx2;

            PrecisionPoint hChildRef = joint.getTranslated(-dx, dy + 3);
            PrecisionPoint rhChildRef = r.rp(hChildRef);

            data.addChildOffset(subBranch, h.rp(rhChildRef));

            hBranchBounds.union(rhChildBranchIns.getBounds(hChildRef).expand(
                    rhChildBorder));
            rhBranchBounds.union(hChildBranchIns.getBounds(rhChildRef).expand(
                    hChildBorder));
            double dx3 = Math
                    .abs((hChildBorder.bottom + hChildBranchIns.bottom) / sin);
            joint.x -= dx1 + dx3 + spacing;
        }
        data.branchRefIns = h.rr(hBranchBounds).getInsets(origin);
        data.rBranchRefIns = h.rr(rhBranchBounds).getInsets(origin);
    }

    @Override
    public void fillFishboneExtraData(IBranchPart branch, FishboneData data,
            IPrecisionTransformer h, PrecisionRotator r, double spacing,
            boolean extraBranch) {
        if (!extraBranch)
            return;
        List<IBranchPart> calloutBranches = branch.getCalloutBranches();
        PrecisionPoint origin = h.getOrigin();

        PrecisionInsets hTopicIns = h.ti(data.topicRefIns);
//        PrecisionRectangle hTopicBounds = hTopicIns.getBounds(origin);
        PrecisionRectangle rhTopicBounds = h.ti(data.rTopicRefIns).getBounds(
                origin);
        double tan = r.sin() / r.cos();

        double bottomLineX = hTopicIns.bottom / r.sin();
        double bottomLineY = 0;

        double topLineX = -hTopicIns.top / r.sin();
        double topLineY = 0;

        PrecisionRectangle hBranchBounds = h.ti(data.branchRefIns).getBounds(
                origin);
        PrecisionRectangle rhBranchBounds = h.ti(data.rBranchRefIns).getBounds(
                origin);

        for (int i = 0; i < calloutBranches.size(); i++) {
            IBranchPart calloutBranch = calloutBranches.get(i);
            Dimension calloutTopicSize = calloutBranch.getTopicPart()
                    .getFigure().getPreferredSize();
            IReferencedFigure calloutBranchFigure = (IReferencedFigure) calloutBranch
                    .getFigure();

            PrecisionInsets hChildBranchIns;
            PrecisionInsets rhChildBranchIns;

            IStructure structure = calloutBranch.getBranchPolicy()
                    .getStructure(calloutBranch);
            if (structure instanceof SubFishboneStructure) {
                FishboneData calloutData = ((SubFishboneStructure) structure)
                        .getCastedData(calloutBranch).getFishboneData();
                hChildBranchIns = h.ti(calloutData.branchRefIns);
                rhChildBranchIns = h.ti(calloutData.rBranchRefIns);
            } else {
                PrecisionInsets childBranchNormal = new PrecisionInsets(
                        calloutBranchFigure.getReferenceDescription());
                hChildBranchIns = h.ti(childBranchNormal);
                rhChildBranchIns = r.ti(hChildBranchIns);
            }

            org.xmind.core.util.Point position = calloutBranch.getTopic()
                    .getPosition();
            if (position == null)
                position = new org.xmind.core.util.Point();
            boolean originPosition = position.x == 0 && position.y == 0;

            double dx = originPosition ? -(0.5 * hTopicIns.getHeight()
                    / r.sin() + calloutTopicSize.width / 2) : position.x;
            double dy = originPosition ? 10 + calloutTopicSize.height
                    : position.y;

            PrecisionPoint rhChildRef = origin.getTranslated(h.tp(dx, dy));
            PrecisionRectangle hChildBounds = hChildBranchIns
                    .getBounds(rhChildRef);

            double distTop = (-tan * (hChildBounds.getTopRight().x - topLineX)
                    + topLineY - hChildBounds.getTopRight().y)
                    / Math.sqrt(tan * tan + 1);
            double distBottom = (-tan
                    * (hChildBounds.getTopRight().x - bottomLineX)
                    + bottomLineY - hChildBounds.getTopRight().y)
                    / Math.sqrt(tan * tan + 1);
            double distLimit = hChildBounds.y - rhTopicBounds.y;

            if (distLimit < 0) {
                rhChildRef = rhChildRef.getTranslated(0, -distLimit);
                hChildBounds = hChildBranchIns.getBounds(rhChildRef);
            }

            if (distBottom > 0) {
                rhChildRef = rhChildRef.getTranslated(-distBottom / r.sin(), 0);
                hChildBounds = hChildBranchIns.getBounds(rhChildRef);
            }

            if (hChildBounds.toDraw2DRectangle().touches(
                    rhTopicBounds.toDraw2DRectangle())
                    && distTop > 0) {
                rhChildRef = rhChildRef.getTranslated(-distTop / r.sin(), 0);
            }

            PrecisionPoint hChildRef = r.tp(rhChildRef);
            data.addChildOffset(calloutBranch, h.rp(rhChildRef));

            hBranchBounds.union(rhChildBranchIns.getBounds(hChildRef));
            rhBranchBounds.union(hChildBranchIns.getBounds(rhChildRef));
        }
        data.branchRefIns = h.rr(hBranchBounds).getInsets(origin);
        data.rBranchRefIns = h.rr(rhBranchBounds).getInsets(origin);
    }

}