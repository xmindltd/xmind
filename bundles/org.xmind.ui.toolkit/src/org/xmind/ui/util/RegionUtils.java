package org.xmind.ui.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

public class RegionUtils {

    public static Region getRoundedRectangle(Rectangle bounds, int corner) {
        Assert.isTrue(bounds.width > 0 && bounds.height > 0 && corner >= 0);
        Assert.isTrue(
                bounds.width >= corner * 2 && bounds.height >= corner * 2);

        Rectangle r = new Rectangle(bounds.x, bounds.y, bounds.width,
                bounds.height);

        Region region = new Region();
        region.add(new Rectangle(r.x, r.y, r.width, r.height));

        //4 corners
        Region corner1 = new Region();
        corner1.add(new Rectangle(r.x, r.y, corner, corner));
        corner1.subtract(circle(corner, r.x + corner, r.y + corner));
        region.subtract(corner1);

        Region corner2 = new Region();
        corner2.add(new Rectangle(r.x + r.width - corner, r.y, corner, corner));
        corner2.subtract(circle(corner, r.x + r.width - corner, r.y + corner));
        region.subtract(corner2);

        Region corner3 = new Region();
        corner3.add(
                new Rectangle(r.x, r.y + r.height - corner, corner, corner));
        corner3.subtract(circle(corner, r.x + corner, r.y + r.height - corner));
        region.subtract(corner3);

        Region corner4 = new Region();
        corner4.add(new Rectangle(r.x + r.width - corner,
                r.y + r.height - corner, corner, corner));
        corner4.subtract(circle(corner, r.x + r.width - corner,
                r.y + r.height - corner));
        region.subtract(corner4);

        return region;
    }

    private static int[] circle(int r, int offsetX, int offsetY) {
        int[] polygon = new int[8 * r + 4];
        //x^2 + y^2 = r^2
        for (int i = 0; i < 2 * r + 1; i++) {
            int x = i - r;
            int y = (int) Math.sqrt(r * r - x * x);
            polygon[2 * i] = offsetX + x;
            polygon[2 * i + 1] = offsetY + y;
            polygon[8 * r - 2 * i - 2] = offsetX + x;
            polygon[8 * r - 2 * i - 1] = offsetY - y;
        }
        return polygon;
    }

    public static Region getPolygon(Point... polygon) {
        Region region = new Region();

        int[] pointArray = new int[polygon.length * 2];
        for (int i = 0; i < polygon.length; i++) {
            pointArray[2 * i] = polygon[i].x;
            pointArray[2 * i + 1] = polygon[i].y;
        }
        region.add(pointArray);

        return region;
    }

}
