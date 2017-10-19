package org.xmind.ui.tabfolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.xmind.ui.util.IStyleProvider;

/**
 * <dl>
 * <dt>Styles</dt>
 * <dd>RADIO, PUSH, SIMPLE, SEPARATOR</dd>
 * <dt>Style Keys:</dt>
 * <dd>TEXT, IMAGE, FILL, MARGIN, null</dd>
 * </dl>
 * 
 * @author Frank Shaka
 * @since 3.6.0
 */
public class MTabBarItem extends Item {

    public static final String TEXT = IStyleProvider.TEXT;
    public static final String TEXT_ALIGN = IStyleProvider.TEXT_ALIGN;
    public static final String IMAGE = IStyleProvider.IMAGE;
    public static final String FILL = IStyleProvider.FILL;
    public static final String MARGIN = IStyleProvider.MARGIN;
    public static final String SEPARATOR = IStyleProvider.SEPARATOR;

    public static final int DEFAULT_SEPARATOR_WIDTH = 24;

    private MTabBar parent;

    private int width = DEFAULT_SEPARATOR_WIDTH;
    private boolean visible = true;

    private boolean selected = false;
    private boolean preselected = false;

    private String color;

    ///////////////////////////////////////////////////////////////////
    // Layout Caches
    // (validated by layout, used by paint)
    //
    // including tab bar's border and padding
    protected Rectangle bounds = new Rectangle(0, 0, 0, 0);
    // excluding tab bar's border and padding
    protected Rectangle contentBounds = new Rectangle(0, 0, 0, 0);
    // other layout caches
    protected Rectangle imageBounds = new Rectangle(0, 0, 0, 0);
    protected Rectangle textBounds = new Rectangle(0, 0, 0, 0);
    protected int marginWidth = 0;
    protected int marginHeight = 0;
    protected int hSpacing = 0;
    protected int vSpacing = 0;
    protected int textPosition = SWT.BOTTOM;
    protected boolean imageVisible = true;
    protected boolean textVisible = true;
    protected Font font = null;
    ///////////////////////////////////////////////////////////////////

    public MTabBarItem(MTabBar parent, int style) {
        super(parent, checkStyle(style));
        this.parent = parent;
        parent.createItem(this, parent.getItemCount());
    }

    private static int checkStyle(int style) {
        int primaryStyle = style
                & (SWT.RADIO | SWT.PUSH | SWT.SIMPLE | SWT.SEPARATOR);
        if (primaryStyle == 0) {
            primaryStyle = SWT.RADIO;
        } else if ((primaryStyle & SWT.PUSH) != 0) {
            primaryStyle = SWT.PUSH;
        } else if ((primaryStyle & SWT.SIMPLE) != 0) {
            primaryStyle = SWT.SIMPLE;
        } else if ((primaryStyle & SWT.SEPARATOR) != 0) {
            primaryStyle = SWT.SEPARATOR;
        } else {
            primaryStyle = SWT.RADIO;
        }
        return primaryStyle;
    }

    public MTabBarItem(MTabBar parent, int style, int index) {
        super(parent, style, index);
        this.parent = parent;
        parent.createItem(this, index);
    }

    public MTabBar getParent() {
        return parent;
    }

    public int getWidth() {
        checkWidget();
        return width;
    }

    public void setWidth(int width) {
        checkWidget();
//        if (!isSeparator())
//            return;
        if (width == SWT.DEFAULT)
            width = DEFAULT_SEPARATOR_WIDTH;
        if (width < SWT.SEPARATOR_FILL || width == this.width)
            return;
        this.width = width;
        parent.updateItem(this);
    }

    public boolean getVisible() {
        checkWidget();
        return visible;
    }

    public void setVisible(boolean visible) {
        checkWidget();
        if (visible == this.visible)
            return;
        this.visible = visible;
        parent.updateItem(this);
    }

    @Override
    public void setImage(Image image) {
        if (isSeparator())
            return;
        super.setImage(image);
        parent.updateItem(this);
    }

    @Override
    public void setText(String string) {
        if (isSeparator())
            return;
        super.setText(string);
        parent.updateItem(this);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public boolean isSelected() {
        checkWidget();
        return selected;
    }

    public boolean isPreselected() {
        checkWidget();
        return preselected;
    }

    protected void setSelected(boolean selected) {
        if (isSeparator() || isSimple())
            return;
        if (selected == this.selected)
            return;
        this.selected = selected;
        parent.updateItem(this);
    }

    protected void setPreselected(boolean preselected) {
        if (isSeparator() || isSimple())
            return;
        if (preselected == this.preselected)
            return;
        this.preselected = preselected;
        parent.updateItem(this);
    }

    public Rectangle getBounds() {
        checkWidget();
        return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    protected boolean setBounds(int x, int y, int width, int height) {
        boolean changed = bounds.x != x || bounds.y != y
                || bounds.width != width || bounds.height != height;
        bounds.x = x;
        bounds.y = y;
        bounds.width = width;
        bounds.height = height;
        return changed;
    }

    protected boolean isRadioButton() {
        return (getStyle() & SWT.RADIO) != 0;
    }

    protected boolean isSeparator() {
        return (getStyle() & SWT.SEPARATOR) != 0;
    }

    protected boolean isPushButton() {
        return (getStyle() & SWT.PUSH) != 0;
    }

    protected boolean isSimple() {
        return (getStyle() & SWT.SIMPLE) != 0;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (isDisposed())
            return;
        parent.destroyItem(this);
        parent = null;
    }

}
