package org.xmind.ui.internal.e4handlers;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;

public class ShowPullDownMenuToolItem extends DirectToolItem {

    @Override
    protected void showPullDownMenuAt(Rectangle itemBoundsToDisplay) {
        Menu menu = getMenu();
        if (menu == null || menu.isDisposed())
            return;

        Point location = new Point(itemBoundsToDisplay.x,
                itemBoundsToDisplay.y + itemBoundsToDisplay.height);
        menu.setLocation(location);
        menu.setVisible(true);
    }

}
