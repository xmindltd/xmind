package org.xmind.cathy.internal.renderer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class XSashLayout extends Layout {

    int suggestedSizeForViewStack = 310;
    int suggestedDownDistanceForViewStack = 22;

    int marginLeft = 0;
    int marginRight = 0;
    int marginTop = 0;
    int marginBottom = 0;
    int sashWidth = 0;

    MUIElement root;

    class SashRect {
        Rectangle rect;
        MGenericTile<?> container;
        MUIElement left;
        MUIElement right;

        public SashRect(Rectangle rect, MGenericTile<?> container,
                MUIElement left, MUIElement right) {
            this.container = container;
            this.rect = rect;
            this.left = left;
            this.right = right;
        }
    }

    public XSashLayout(MUIElement root) {
        this.root = root;
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        if (root == null)
            return;

        Rectangle bounds = composite.getBounds();
        if (composite instanceof Shell)
            bounds = ((Shell) composite).getClientArea();
        else {
            bounds.x = 0;
            bounds.y = 0;
        }

        bounds.width -= (marginLeft + marginRight);
        bounds.height -= (marginTop + marginBottom);
        bounds.x += marginLeft;
        bounds.y += marginTop;

        tileSubNodes(bounds, root);
    }

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        return new Point(600, 400);
    }

    private int totalScalableSectionWeight(MGenericTile<?> node) {
        int total = 0;
        for (MUIElement subNode : node.getChildren()) {
            if (subNode.isToBeRendered() && subNode.isVisible()) {
                Object renderer = subNode.getRenderer();
                if (renderer != null
                        && renderer.getClass() != XStackRenderer.class) {
                    total += getWeight(subNode);
                }
            }
        }
        return total;
    }

    private void tileSubNodes(Rectangle bounds, MUIElement node) {
        if (node != root)
            setRectangle(node, bounds);

        if (!(node instanceof MGenericTile<?>))
            return;

        MGenericTile<?> sashContainer = (MGenericTile<?>) node;
        List<MUIElement> visibleChildren = getVisibleChildren(sashContainer);
        int childCount = visibleChildren.size();

        // How many pixels do we have?
        int availableWidth = sashContainer.isHorizontal() ? bounds.width
                : bounds.height;

        // Subtract off the room for the sashes
        availableWidth -= ((childCount - 1) * sashWidth);

        int availableScalableSectionWidth = availableWidth;

        // Get the total of the weights
        double totalScalableSectionWeight = totalScalableSectionWeight(
                sashContainer);
        for (MUIElement subNode : visibleChildren) {
            Object renderer = subNode.getRenderer();
            if (renderer != null
                    && renderer.getClass() == XStackRenderer.class) {
                availableScalableSectionWidth -= suggestedSizeForViewStack;
            }
        }
        int tilePos = sashContainer.isHorizontal() ? bounds.x : bounds.y;

        MUIElement prev = null;
        for (MUIElement subNode : visibleChildren) {
            // Add a 'sash' between this node and the 'prev'
            if (prev != null) {
                tilePos += sashWidth;
            }

            // Calc the new size as a %'age of the total
            int weight = getWeight(subNode);
            double ratio = weight / totalScalableSectionWeight;
            int newSize = (int) ((availableScalableSectionWidth * ratio) + 0.5);
            Object renderer = subNode.getRenderer();

            int y = bounds.y;
            int height = bounds.height;
            if (renderer != null
                    && renderer.getClass() == XStackRenderer.class) {
                newSize = suggestedSizeForViewStack;
                y = y + suggestedDownDistanceForViewStack;
                height = height - suggestedDownDistanceForViewStack - 1;
            }

            Rectangle subBounds = sashContainer.isHorizontal()
                    ? new Rectangle(tilePos, y, newSize, height)
                    : new Rectangle(bounds.x, tilePos, bounds.width, newSize);
            tilePos += newSize;

            tileSubNodes(subBounds, subNode);
            prev = subNode;
        }
    }

    /**
     * @param node
     * @param bounds
     */
    private void setRectangle(MUIElement node, Rectangle bounds) {
        if (node.getWidget() instanceof Control) {
            Control ctrl = (Control) node.getWidget();
            ctrl.setBounds(bounds);
        } else if (node.getWidget() instanceof Rectangle) {
            Rectangle theRect = (Rectangle) node.getWidget();
            theRect.x = bounds.x;
            theRect.y = bounds.y;
            theRect.width = bounds.width;
            theRect.height = bounds.height;
        }
    }

    private List<MUIElement> getVisibleChildren(MGenericTile<?> sashContainer) {
        List<MUIElement> visKids = new ArrayList<MUIElement>();
        for (MUIElement child : sashContainer.getChildren()) {
            if (child.isToBeRendered() && child.isVisible())
                visKids.add(child);
        }
        return visKids;
    }

    private static int getWeight(MUIElement element) {
        String info = element.getContainerData();
        if (info == null || info.length() == 0) {
            return 0;
        }

        try {
            int value = Integer.parseInt(info);
            return value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
