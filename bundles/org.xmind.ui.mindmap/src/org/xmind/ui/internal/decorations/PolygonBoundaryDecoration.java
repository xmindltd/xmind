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
package org.xmind.ui.internal.decorations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.decorations.AbstractBoundaryDecoration;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;

public class PolygonBoundaryDecoration extends AbstractBoundaryDecoration {

    private class ValueComparator implements Comparator<ITopicPart> {

        private Map<ITopicPart, Integer> base;

        public ValueComparator(Map<ITopicPart, Integer> base) {
            this.base = base;
        }

        public int compare(ITopicPart t1, ITopicPart t2) {
            if (base.get(t1) > base.get(t2))
                return 1;
            return -1;
        }

    }

    private IBoundaryPart boundary;

    public PolygonBoundaryDecoration() {
        super();
    }

    public PolygonBoundaryDecoration(String id) {
        super(id);
    }

    public PolygonBoundaryDecoration(IBoundaryPart boundary, String id) {
        super(id);
        this.boundary = boundary;
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        List<Point> points = calcPathPoints(box, boundary);

        if (points.size() > 4) {
            shape.moveTo(points.get(0));
            for (int i = 1; i < points.size(); i++)
                shape.lineTo(points.get(i));
            shape.lineTo(points.get(0));
            shape.close();
        } else {
            shape.addRectangle(box);
        }
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        Rectangle bounds = figure.getBounds();
        PrecisionPoint p1 = new PrecisionPoint(bounds.x + bounds.width / 2,
                bounds.y + bounds.height / 2);

        PrecisionPoint p2 = Geometry.getChopBoxLocation(refX, refY,
                getOutlineBox(figure), expansion);

        return calcAnchorLocation(figure, p1, p2);
    }

    private PrecisionPoint calcAnchorLocation(IFigure figure, PrecisionPoint p1,
            PrecisionPoint p2) {
        if (p1.getDistance(p2) < (getLineWidth() == 0 ? 1 : getLineWidth()))
            return p2;

        PrecisionPoint p3 = new PrecisionPoint((p1.x + p2.x) / 2,
                (p1.y + p2.y) / 2);
        if (containsPoint(figure, (float) p3.x, (float) p3.y))
            return calcAnchorLocation(figure, p3, p2);
        else
            return calcAnchorLocation(figure, p1, p3);
    }

    private boolean containsPoint(IFigure figure, float x, float y) {
        checkValidation(figure);
        GC gc = GraphicsUtils.getAdvanced().getGC();
        gc.setLineWidth(getCheckingLineWidth());
        Path shape = new Path(Display.getCurrent());
        sketch(figure, shape, getOutlineBox(figure), FILL);
        boolean ret = shape.contains(x, y, gc, false);
        shape.close();
        shape.dispose();
        return ret;
    }

    protected List<Point> calcPathPoints(Rectangle box,
            IBoundaryPart boundary) {
        List<IBranchPart> enclosingBranches = boundary.getEnclosingBranches();
        IBranchPart branch;
        if (!enclosingBranches.isEmpty())
            branch = enclosingBranches.get(0);
        else
            branch = boundary.getOwnedBranch();

        int childDirection = calcChildDirection(branch);

        List<ITopicPart> topics = collectTopics(boundary);

        List<Point> points = new ArrayList<Point>();
        switch (childDirection) {
        case PositionConstants.WEST:
            collectPointsWestDire(points, topics, box);
            break;
        case PositionConstants.EAST:
            collcetPointsEastDire(points, topics, box);
            break;
        case PositionConstants.SOUTH:
            collectPointsSouthDire(points, topics, box);
            break;
        case PositionConstants.NORTH:
            collectPointsNorthDire(points, topics, box);
            break;
        }

        return points;
    }

    private void collectPointsWestDire(List<Point> points,
            List<ITopicPart> topics, Rectangle box) {
        int lineWidth1 = getLineWidth();
        int lineWidth2 = lineWidth1 > 1 ? lineWidth1 - 1 : lineWidth1;

        Map<ITopicPart, Integer> headMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(headMap, bounds.x);
            if (t != null) {
                if (bounds.y < t.getFigure().getBounds().y) {
                    headMap.remove(t);
                    headMap.put(topic, bounds.x);
                }
            } else {
                headMap.put(topic, bounds.x);
            }
        }

        List<Point> headPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(headMap))
            headPoints.add(topic.getFigure().getBounds().getTopLeft()
                    .getTranslated(-MARGIN_WIDTH + lineWidth2,
                            -MARGIN_HEIGHT + lineWidth2));

        Map<ITopicPart, Integer> tailMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(tailMap, bounds.x);
            if (t != null) {
                if (bounds.bottom() > t.getFigure().getBounds().bottom()) {
                    tailMap.remove(t);
                    tailMap.put(topic, bounds.x);
                }
            } else {
                tailMap.put(topic, bounds.x);
            }
        }
        List<Point> tailPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(tailMap))
            tailPoints.add(topic.getFigure().getBounds().getBottomLeft()
                    .getTranslated(-MARGIN_WIDTH + lineWidth2,
                            MARGIN_HEIGHT - lineWidth2));

        Point tr = box.getTopRight();
        Point br = box.getBottomRight();

        initPoints(points, headPoints.get(0), tr, br, tailPoints.get(0));

        calcPointsWestOrEast(points, tr, br, headPoints, tailPoints);

    }

    private void collcetPointsEastDire(List<Point> points,
            List<ITopicPart> topics, Rectangle box) {
        int lineWidth1 = getLineWidth();
        int lineWidth2 = lineWidth1 > 1 ? lineWidth1 - 1 : lineWidth1;

        Map<ITopicPart, Integer> headMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(headMap, -bounds.right());
            if (t != null) {
                if (bounds.y < t.getFigure().getBounds().y) {
                    headMap.remove(t);
                    headMap.put(topic, -bounds.right());
                }
            } else {
                headMap.put(topic, -bounds.right());
            }
        }

        List<Point> headPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(headMap))
            headPoints.add(topic.getFigure().getBounds().getTopRight()
                    .getTranslated(MARGIN_WIDTH - lineWidth1,
                            -MARGIN_HEIGHT + lineWidth2));

        Map<ITopicPart, Integer> tailMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(tailMap, -bounds.right());
            if (t != null) {
                if (bounds.bottom() > t.getFigure().getBounds().bottom()) {
                    tailMap.remove(t);
                    tailMap.put(topic, -bounds.right());
                }
            } else {
                tailMap.put(topic, -bounds.right());
            }
        }

        List<Point> tailPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(tailMap))
            tailPoints.add(topic.getFigure().getBounds().getBottomRight()
                    .getTranslated(MARGIN_WIDTH - lineWidth1, MARGIN_HEIGHT
                            - (lineWidth1 > 1 ? lineWidth1 - 1 : lineWidth1)));

        Point tl = box.getTopLeft();
        Point bl = box.getBottomLeft();

        initPoints(points, headPoints.get(0), tl, bl, tailPoints.get(0));

        calcPointsWestOrEast(points, tl, bl, headPoints, tailPoints);
    }

    private void calcPointsWestOrEast(List<Point> points, Point crl1,
            Point crl2, List<Point> headPoints, List<Point> tailPoints) {
        int upSize = 2;
        if (headPoints.get(0).y > crl1.y) {
            for (int i = headPoints.size() - 1; i > 0; i--) {
                Point point = headPoints.get(i);
                Point firstPoint = points.get(0);
                Point secPoint = points.get(1);
                if (firstPoint.y == secPoint.y && !firstPoint.equals(secPoint))
                    break;

                if (!secPoint.equals(crl1) && secPoint.y == point.y) {
                    points.remove(1);
                    points.add(1, point);
                } else if (isUpperLine(firstPoint, secPoint, point)) {
                    points.add(1, point);
                    upSize++;
                }
            }
        }
        for (int i = 1; i < upSize - 1; i++) {
            if (!isUpperLine(points.get(i - 1), points.get(i + 1),
                    points.get(i))) {
                points.remove(i--);
                upSize--;
                if (i > 1)
                    i--;
            }
        }

        if (tailPoints.get(0).y < crl2.y) {
            for (int i = tailPoints.size() - 1; i > 0; i--) {
                Point point = tailPoints.get(i);
                Point lastPoint = points.get(points.size() - 1);
                Point penPoint = points.get(points.size() - 2);

                if (lastPoint.y == penPoint.y)
                    break;

                if (!penPoint.equals(crl2) && penPoint.y == point.y) {
                    points.remove(points.size() - 2);
                    points.add(points.size() - 1, point);
                } else if (!isUpperLine(penPoint, lastPoint, point)) {
                    points.add(points.size() - 1, point);
                }
            }
        }
        for (int i = upSize + 1; i < points.size() - 1; i++) {
            if (isUpperLine(points.get(i - 1), points.get(i + 1),
                    points.get(i))) {
                points.remove(i--);
                if (i > upSize)
                    i--;
            }
        }
    }

    private void collectPointsSouthDire(List<Point> points,
            List<ITopicPart> topics, Rectangle box) {
        int lineWidth1 = getLineWidth();
        int lineWidth2 = lineWidth1 > 1 ? lineWidth1 - 1 : lineWidth1;

        Map<ITopicPart, Integer> headMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(headMap, -bounds.bottom());
            if (t != null) {
                if (bounds.x < t.getFigure().getBounds().x) {
                    headMap.remove(t);
                    headMap.put(topic, -bounds.bottom());
                }
            } else {
                headMap.put(topic, -bounds.bottom());
            }
        }

        List<Point> headPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(headMap))
            headPoints.add(topic.getFigure().getBounds().getBottomLeft()
                    .getTranslated(-MARGIN_WIDTH + lineWidth2,
                            MARGIN_HEIGHT - lineWidth2));

        Map<ITopicPart, Integer> tailMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(tailMap, -bounds.bottom());
            if (t != null) {
                if (bounds.right() > t.getFigure().getBounds().right()) {
                    tailMap.remove(t);
                    tailMap.put(topic, -bounds.bottom());
                }
            } else {
                tailMap.put(topic, -bounds.bottom());
            }
        }

        List<Point> tailPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(tailMap))
            tailPoints.add(topic.getFigure().getBounds().getBottomRight()
                    .getTranslated(MARGIN_WIDTH - lineWidth1,
                            MARGIN_HEIGHT - lineWidth2));

        Point tl = box.getTopLeft();
        Point tr = box.getTopRight();

        initPoints(points, headPoints.get(0), tl, tr, tailPoints.get(0));

        calcPointsSouthOrNorth(points, tl, tr, headPoints, tailPoints);
    }

    private void collectPointsNorthDire(List<Point> points,
            List<ITopicPart> topics, Rectangle box) {
        int lineWidth1 = getLineWidth();
        int lineWidth2 = lineWidth1 > 1 ? lineWidth1 - 1 : lineWidth1;
        Map<ITopicPart, Integer> headMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(headMap, bounds.y);
            if (t != null) {
                if (bounds.x < t.getFigure().getBounds().x) {
                    headMap.remove(t);
                    headMap.put(topic, bounds.y);
                }
            } else {
                headMap.put(topic, bounds.y);
            }
        }

        List<Point> headPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(headMap))
            headPoints.add(topic.getFigure().getBounds().getTopLeft()
                    .getTranslated(-MARGIN_WIDTH + lineWidth2,
                            -MARGIN_HEIGHT + lineWidth1));

        Map<ITopicPart, Integer> tailMap = new HashMap<ITopicPart, Integer>();
        for (ITopicPart topic : topics) {
            Rectangle bounds = topic.getFigure().getBounds();
            ITopicPart t = findTopicByValue(tailMap, bounds.y);
            if (t != null) {
                if (bounds.right() > t.getFigure().getBounds().right()) {
                    tailMap.remove(t);
                    tailMap.put(topic, bounds.y);
                }
            } else {
                tailMap.put(topic, bounds.y);
            }
        }

        List<Point> tailPoints = new ArrayList<Point>();
        for (ITopicPart topic : sortTopicPart(tailMap))
            tailPoints.add(topic.getFigure().getBounds().getTopRight()
                    .getTranslated(MARGIN_WIDTH - lineWidth1,
                            -MARGIN_HEIGHT + lineWidth1));

        Point bl = box.getBottomLeft();
        Point br = box.getBottomRight();

        initPoints(points, headPoints.get(0), bl, br, tailPoints.get(0));

        calcPointsSouthOrNorth(points, bl, br, headPoints, tailPoints);
    }

    private void calcPointsSouthOrNorth(List<Point> points, Point crl1,
            Point crl2, List<Point> headPoints, List<Point> tailPoints) {
        int leftSize = 2;
        if (headPoints.get(0).x > crl1.x) {
            for (int i = headPoints.size() - 1; i > 0; i--) {
                Point point = headPoints.get(i);
                Point firstPoint = points.get(0);
                Point secPoint = points.get(1);
                if (firstPoint.x == secPoint.x)
                    break;

                if (!(secPoint.equals(crl1)) && secPoint.x == point.x) {
                    points.remove(1);
                    points.add(1, point);
                } else {
                    if (isLeftLine(firstPoint, secPoint, point)) {
                        points.add(1, point);
                        leftSize++;
                    }
                }
            }
        }
        for (int i = 1; i < leftSize - 1; i++) {
            if (!isLeftLine(points.get(i - 1), points.get(i + 1),
                    points.get(i))) {
                points.remove(i--);
                leftSize--;
                if (i > 1)
                    i--;
            }
        }

        if (tailPoints.get(0).x < crl2.x) {
            for (int i = tailPoints.size() - 1; i > 0; i--) {
                Point point = tailPoints.get(i);
                Point lastPoint = points.get(points.size() - 1);
                Point penPoint = points.get(points.size() - 2);

                if (lastPoint.x == penPoint.x)
                    break;

                if (!penPoint.equals(crl2) && penPoint.x == point.x) {
                    points.remove(points.size() - 2);
                    points.add(points.size() - 1, point);
                } else {
                    if (!isLeftLine(penPoint, lastPoint, point)) {
                        points.add(points.size() - 1, point);
                    }
                }
            }
        }
        for (int i = leftSize + 1; i < points.size() - 1; i++) {
            if (isLeftLine(points.get(i - 1), points.get(i + 1),
                    points.get(i))) {
                points.remove(i--);
                if (i > leftSize)
                    i--;
            }
        }
    }

    private void initPoints(List<Point> points, Point p1, Point p2, Point p3,
            Point p4) {
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);
    }

    private int calcChildDirection(IBranchPart branch) {
        IStructure structure = branch.getBranchPolicy().getStructure(branch);

        if (structure instanceof IBranchStructureExtension)
            return ((IBranchStructureExtension) structure)
                    .getChildTargetOrientation(branch.getParentBranch(),
                            branch);

        return PositionConstants.NONE;
    }

    private List<ITopicPart> collectTopics(IBoundaryPart boundary) {
        List<ITopicPart> topics = new ArrayList<ITopicPart>();

        List<IBranchPart> branches = boundary.getEnclosingBranches();
        if (!branches.isEmpty()) {
            for (IBranchPart branch : branches) {
                topics.add(branch.getTopicPart());
                addAllTopics(branch, topics);
            }
        } else {
            IBranchPart branch = boundary.getOwnedBranch();
            topics.add(branch.getTopicPart());
            addAllTopics(branch, topics);
        }

        return topics;
    }

    private void addAllTopics(IBranchPart branch, List<ITopicPart> topics) {
        for (IBranchPart sub : branch.getSubBranches()) {
            topics.add(sub.getTopicPart());
            addAllTopics(sub, topics);
        }

        for (IBranchPart callout : branch.getCalloutBranches()) {
            topics.add(callout.getTopicPart());
            addAllTopics(callout, topics);
        }

        for (IBranchPart summary : branch.getSummaryBranches()) {
            topics.add(summary.getTopicPart());
            addAllTopics(summary, topics);
        }
    }

    private boolean isUpperLine(Point p1, Point p2, Point p3) {
        if (p1.y < p3.y)
            return false;

        float dx1 = Math.abs(p2.x - p1.x);
        float dy1 = Math.abs(p2.y - p1.y);

        float dx2 = Math.abs(p3.x - p1.x);
        float dy2 = Math.abs(p3.y - p1.y);

        return dy1 / dx1 < dy2 / dx2;
    }

    private boolean isLeftLine(Point p1, Point p2, Point p3) {
        if (p1.x < p3.x)
            return false;

        float dx1 = Math.abs(p2.x - p1.x);
        float dy1 = Math.abs(p2.y - p1.y);

        float dx2 = Math.abs(p3.x - p1.x);
        float dy2 = Math.abs(p3.y - p1.y);

        return dx1 / dy1 < dx2 / dy2;
    }

    private Set<ITopicPart> sortTopicPart(Map<ITopicPart, Integer> topicMap) {
        HashMap<ITopicPart, Integer> map = new HashMap<ITopicPart, Integer>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<ITopicPart, Integer> sortedMap = new TreeMap<ITopicPart, Integer>(
                bvc);

        map.putAll(topicMap);
        sortedMap.putAll(map);

        return sortedMap.keySet();
    }

    private ITopicPart findTopicByValue(Map<ITopicPart, Integer> map,
            Integer value) {
        for (Entry<ITopicPart, Integer> entry : map.entrySet()) {
            if (value == entry.getValue()
                    || (value != null && value.equals(entry.getValue())))
                return entry.getKey();
        }
        return null;
    }

}
