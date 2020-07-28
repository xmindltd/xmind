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
package org.xmind.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.UITimer;
import org.xmind.ui.viewers.ImageButton;

/**
 * @author Frank Shaka
 */
public class SmoothPopupDialog extends Window {

    private static Map<String, PopupGroup> groups = new HashMap<String, PopupGroup>();

    private static class PopupGroup {

        private Point initBottomRight = null;

        private Point bottomRight = null;

        private int width = 0;

        private List<SmoothPopupDialog> dialogs = new ArrayList<SmoothPopupDialog>();

        public Point getBottomRight() {
            return bottomRight;
        }

        public void setBottomRight(int right, int bottom) {
            this.initBottomRight = new Point(right, bottom);
            this.bottomRight = new Point(right, bottom);
        }

        public void add(SmoothPopupDialog dialog, int height, int width) {
            if (bottomRight == null)
                throw new IllegalStateException();

            dialogs.add(dialog);
            int top = bottomRight.y - height;
            if (top < Display.getCurrent().getClientArea().y) {
                this.bottomRight.x -= this.width;
                this.bottomRight.y = this.initBottomRight.y;
            } else {
                this.width = Math.max(this.width, width);
                this.bottomRight.y -= height;
            }
        }

        public void remove(SmoothPopupDialog dialog) {
            dialogs.remove(dialog);
            if (dialogs.isEmpty()) {
                initBottomRight = null;
                bottomRight = null;
            }
        }
    }

    private final class BorderFillLayout extends Layout {

        private int borderWidth;

        private int margin;

        public BorderFillLayout(int borderWidth) {
            this.borderWidth = borderWidth;
            this.margin = borderWidth + borderWidth;
        }

        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            Point size = computeContentSize();
            return new Point(size.x + margin, size.y + margin);
        }

        private Point computeContentSize() {
            if (targetSize != null) {
                return targetSize;
            } else if (getContents() != null) {
                return getContents().computeSize(DEFAULT_TARGET_SIZE.x,
                        DEFAULT_TARGET_SIZE.y);
            }
            return DEFAULT_TARGET_SIZE;
        }

        protected void layout(Composite composite, boolean flushCache) {
            int x = borderWidth;
            int y = borderWidth;
            Rectangle clientArea = composite.getClientArea();
            if (targetSize != null) {
                clientArea.width = targetSize.x;
                clientArea.height = targetSize.y;
            }
            int width = clientArea.width;
            int height = clientArea.height;
            Control[] children = composite.getChildren();
            for (Control c : children) {
                c.setBounds(x, y, width - 2 * borderWidth,
                        height - 2 * borderWidth);
            }
        }

    }

    protected class PullDownTask implements Runnable {

        Display display;

        boolean canceled = false;

        public PullDownTask(Display display) {
            this.display = display;
        }

        public void cancel() {
            this.canceled = true;
        }

        public boolean isCanceled() {
            return this.canceled;
        }

        public void run() {
            if (canceled || display.isDisposed())
                return;
            pullDown();
        }
    }

    protected static final String DEFAULT_BACKGROUDCOLOR_VALUE = "#e9e8e9"; //$NON-NLS-1$

    private static final int VERTICAL_SPEED = 3;

    private static final int ANIM_INTERVALS = 8;

    private static final GridDataFactory LAYOUTDATA_GRAB_BOTH = GridDataFactory
            .fillDefaults().grab(true, true);

    private static final GridDataFactory LAYOUTDATA_GRAB_HORIZONTAL = GridDataFactory
            .fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

    private static final GridDataFactory LAYOUTDATA_ALIGN_RIGHT = GridDataFactory
            .fillDefaults().align(SWT.END, SWT.CENTER);

    private static final GridLayoutFactory LAYOUT_CONTENTS = GridLayoutFactory
            .fillDefaults().numColumns(1).margins(1, 1)
            .extendedMargins(0, 0, 0, 0).spacing(1, 1);

    /**
     * Margin width (in pixels) to be used in layouts inside popup dialogs
     * (value is 0).
     */
    public final static int POPUP_MARGINWIDTH = 0;

    /**
     * Margin height (in pixels) to be used in layouts inside popup dialogs
     * (value is 0).
     */
    public final static int POPUP_MARGINHEIGHT = 0;

    /**
     * Vertical spacing (in pixels) between cells in the layouts inside popup
     * dialogs (value is 1).
     */
    public final static int POPUP_VERTICALSPACING = 1;

    /**
     * Vertical spacing (in pixels) between cells in the layouts inside popup
     * dialogs (value is 1).
     */
    public final static int POPUP_HORIZONTALSPACING = 1;

    /**
     * 
     */
    private static final GridLayoutFactory POPUP_LAYOUT_FACTORY = GridLayoutFactory
            .fillDefaults().margins(POPUP_MARGINWIDTH, POPUP_MARGINHEIGHT)
            .spacing(POPUP_HORIZONTALSPACING, POPUP_VERTICALSPACING);

    private static final int POPUP_GAP = 3;

    /**
     * Border thickness in pixels.
     */
    private static final int BORDER_THICKNESS = 1;

    private static int STAY_DURATION = 5000;

    protected static ImageDescriptor IMG_CLOSE_NORMAL = null;

    private Point DEFAULT_TARGET_SIZE;

    private boolean showCloseButton = false;

    private Point targetSize = null;

    private String titleText = null;

    private Control dialogArea = null;

    private Point startingBottomRight = null;

    private boolean centerPopUp = false;

    private boolean popup = false;

    private UITimer timer = null;

    private int targetWidth = 0;

    private int targetHeight = 0;

    private int currentHeight = 0;

    private PullDownTask pullDownTask = null;

    private Control sourceControl = null;

    private Listener sourceControlMoveListener = null;

    private PopupGroup group = null;

    private int duration = STAY_DURATION;

    public SmoothPopupDialog(Shell parent, boolean showCloseButton,
            String titleText) {
        super(parent);
        setShellStyle(SWT.TOOL);
        setBlockOnOpen(false);
        this.showCloseButton = showCloseButton;
        this.titleText = titleText;

        Point size = parent.getSize();
        DEFAULT_TARGET_SIZE = new Point(size.x * 70 / 100, -1);
    }

    /**
     * 
     * @param stayDuration
     *            the duration this dialog will stay on the screen, in
     *            milliseconds
     */
    public void setDuration(int stayDuration) {
        this.duration = stayDuration;
    }

    public int getDuration() {
        return this.duration;
    }

    protected void configureShell(Shell shell) {
        Display display = shell.getDisplay();

        int border = getBorderThickness();
        shell.setLayout(new BorderFillLayout(border));

        if (border > 0) {
            Color borderColor = getBorderColor(display);
            if (borderColor == null)
                borderColor = display.getSystemColor(SWT.COLOR_GRAY);
            shell.setBackground(borderColor);
        }

        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                handleDispose();
            }
        });
    }

    private int getBorderThickness() {
        return ((getShellStyle() & SWT.NO_TRIM) == 0) ? 0 : BORDER_THICKNESS;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        LAYOUT_CONTENTS.applyTo(composite);

        if (hasTitleArea()) {
            Control titleArea = createTitleArea(composite);
            LAYOUTDATA_GRAB_HORIZONTAL.applyTo(titleArea);
        }

        dialogArea = createDialogArea(composite);
        if (dialogArea.getLayoutData() == null) {
            LAYOUTDATA_GRAB_BOTH.applyTo(dialogArea);
        }

        applyColors(composite);
        return composite;
    }

    /**
     * Creates and returns the contents of the dialog (the area below the title
     * area and above the info text area.
     * <p>
     * The <code>PopupDialog</code> implementation of this framework method
     * creates and returns a new <code>Composite</code> with standard margins
     * and spacing.
     * <p>
     * The returned control's layout data must be an instance of
     * <code>GridData</code>. This method must not modify the parent's layout.
     * <p>
     * Subclasses must override this method but may call <code>super</code> as
     * in the following example:
     * 
     * <pre>
     * Composite composite = (Composite) super.createDialogArea(parent);
     * //add controls to composite as necessary
     * return composite;
     * </pre>
     * 
     * @param parent
     *            the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        POPUP_LAYOUT_FACTORY.applyTo(composite);
        return composite;
    }

    protected boolean hasTitleArea() {
        return titleText != null || showCloseButton;
    }

    protected Control createTitleArea(Composite parent) {
        Composite titleAreaComposite = new Composite(parent, SWT.NONE);

        boolean hasTitle = titleText != null;
        GridLayoutFactory.fillDefaults()
                .numColumns(hasTitle && showCloseButton ? 2 : 1)
                .applyTo(titleAreaComposite);

        if (hasTitle) {
            Control title = createTitleControl(titleAreaComposite);
            LAYOUTDATA_GRAB_HORIZONTAL.applyTo(title);
        }

        if (showCloseButton) {
            Control closeButton = createCloseButton(titleAreaComposite);
            LAYOUTDATA_ALIGN_RIGHT.grab(!hasTitle, false).applyTo(closeButton);
        }

        return titleAreaComposite;
    }

    protected Control createCloseButton(Composite parent) {
        ImageButton closeButton = new ImageButton(parent, SWT.NONE);
        closeButton.setNormalImageDescriptor(getCloseButtonNormalImage());
        closeButton.setDisabledImageDescriptor(getDisabledCloseButtonImage());
        closeButton.setHoveredImageDescriptor(getHoverCloseButtonImage());
        closeButton.setPressedImageDescriptor(getPressedCloseButtonImage());
        closeButton.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                close();
            }
        });
        return closeButton.getControl();
    }

    public static void setDefaultCloseButtonNormalImage(ImageDescriptor img) {
        IMG_CLOSE_NORMAL = img;
    }

    protected ImageDescriptor getCloseButtonNormalImage() {
        if (IMG_CLOSE_NORMAL == null) {
            IMG_CLOSE_NORMAL = createDefaultCloseButtonImage();
        }
        return IMG_CLOSE_NORMAL;
    }

    private static ImageDescriptor createDefaultCloseButtonImage() {
        Display display = Display.getCurrent();
        Image img = new Image(display, 16, 16);
        GC gc = new GC(img);
        gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
        gc.setBackground(ColorUtils.getColor(DEFAULT_BACKGROUDCOLOR_VALUE));
        gc.fillRectangle(0, 0, 16, 16);
        gc.setLineWidth(2);
        gc.drawLine(4, 4, 11, 11);
        gc.drawLine(4, 11, 11, 4);
        gc.dispose();
        ImageData data = img.getImageData();
        img.dispose();
        return ImageDescriptor.createFromImageData(data);
    }

    protected ImageDescriptor getHoverCloseButtonImage() {
        return null;
    }

    protected ImageDescriptor getDisabledCloseButtonImage() {
        return null;
    }

    protected ImageDescriptor getPressedCloseButtonImage() {
        return null;
    }

    protected Control createTitleControl(Composite parent) {
        Composite titleContainer = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(5, 2).spacing(0, 0)
                .numColumns(1).applyTo(titleContainer);

        Label title = new Label(titleContainer, SWT.NONE);
        if (titleText != null)
            title.setText(titleText);
        LAYOUTDATA_GRAB_HORIZONTAL.applyTo(title);

        return titleContainer;
    }

    /**
     * Apply any desired color to the specified composite and its children.
     * 
     * @param composite
     *            the contents composite
     * @param display
     */
    private void applyColors(Composite composite) {
        Display display = getShell().getDisplay();
        applyForegroundColor(display.getSystemColor(SWT.COLOR_DARK_GRAY),
                composite, getForegroundColorExclusions());
        applyBackgroundColor(ColorUtils.getColor(DEFAULT_BACKGROUDCOLOR_VALUE),
                composite, getBackgroundColorExclusions());
    }

    /**
     * Set the specified foreground color for the specified control and all of
     * its children, except for those specified in the list of exclusions.
     * 
     * @param color
     *            the color to use as the foreground color
     * @param control
     *            the control whose color is to be changed
     * @param exclusions
     *            a list of controls who are to be excluded from getting their
     *            color assigned
     */
    private void applyForegroundColor(Color color, Control control,
            List exclusions) {
        if (exclusions.contains(control))
            return;
        control.setForeground(color);
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (int i = 0; i < children.length; i++) {
                applyForegroundColor(color, children[i], exclusions);
            }
        }
    }

    /**
     * Set the specified background color for the specified control and all of
     * its children.
     * 
     * @param color
     *            the color to use as the background color
     * @param control
     *            the control whose color is to be changed
     * @param exclusions
     *            a list of controls who are to be excluded from getting their
     *            color assigned
     */
    private void applyBackgroundColor(Color color, Control control,
            List exclusions) {
        if (exclusions.contains(control))
            return;
        control.setBackground(color);
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (int i = 0; i < children.length; i++) {
                applyBackgroundColor(color, children[i], exclusions);
            }
        }
    }

    /**
     * Set the specified foreground color for the specified control and all of
     * its children. Subclasses may override this method, but typically do not.
     * If a subclass wishes to exclude a particular control in its contents from
     * getting the specified foreground color, it may instead override
     * <code>PopupDialog.getForegroundColorExclusions</code>.
     * 
     * @param color
     *            the color to use as the background color
     * @param control
     *            the control whose color is to be changed
     * @see PopupDialog#getBackgroundColorExclusions()
     */
    protected void applyForegroundColor(Color color, Control control) {
        applyForegroundColor(color, control, getForegroundColorExclusions());
    }

    /**
     * Set the specified background color for the specified control and all of
     * its children. Subclasses may override this method, but typically do not.
     * If a subclass wishes to exclude a particular control in its contents from
     * getting the specified background color, it may instead override
     * <code>PopupDialog.getBackgroundColorExclusions</code>.
     * 
     * @param color
     *            the color to use as the background color
     * @param control
     *            the control whose color is to be changed
     * @see PopupDialog#getBackgroundColorExclusions()
     */
    protected void applyBackgroundColor(Color color, Control control) {
        applyBackgroundColor(color, control, getBackgroundColorExclusions());
    }

    /**
     * Return a list of controls which should never have their foreground color
     * reset. Subclasses may extend this method (should always call
     * <code>super.getForegroundColorExclusions</code> to aggregate the list.
     * 
     * @return the List of controls
     */
    protected List getForegroundColorExclusions() {
        List list = new ArrayList(3);
        return list;
    }

    /**
     * Return a list of controls which should never have their background color
     * reset. Subclasses may extend this method (should always call
     * <code>super.getBackgroundColorExclusions</code> to aggregate the list.
     * 
     * @return the List of controls
     */
    protected List getBackgroundColorExclusions() {
        List list = new ArrayList(2);
        return list;
    }

    protected Color getBorderColor(Display display) {
        return null;
    }

    protected Point getTargetSize() {
        return targetSize;
    }

    protected void setTargetSize(Point targetSize) {
        this.targetSize = targetSize;
    }

    protected void setCenterPopUp(boolean centerPopUp) {
        this.centerPopUp = centerPopUp;
    }

    public void setGroupId(String groupId) {
        if (groupId == null) {
            if (group != null) {
                group.remove(this);
                group = null;
            }
        } else {
            group = groups.get(groupId);
            if (group == null) {
                group = new PopupGroup();
                groups.put(groupId, group);
            }
        }
    }

    public void popUp() {
        Display display = Display.getCurrent();

        Shell shell = null;
        if (PlatformUI.isWorkbenchRunning()) {
            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window != null) {
                shell = window.getShell();
            }
        }
        if (shell == null)
            shell = display.getActiveShell();
        if (shell != null && !shell.isDisposed()) {
            popUp(shell);
        } else {
            Rectangle area = display.getClientArea();
            popUp(area.x + area.width - 50, area.y + area.height - 50);
        }
    }

    public void popUp(Control sourceControl) {
        open(sourceControl, true);
    }

    public void popUp(int right, int bottom) {
        open(right, bottom, true);
    }

    public void open(Control sourceControl) {
        open(sourceControl, false);
    }

    public void open(int right, int bottom) {
        open(right, bottom, false);
    }

    protected void open(Control sourceControl, boolean popup) {
        if (sourceControlMoveListener != null) {
            sourceControl.removeListener(SWT.Move, sourceControlMoveListener);
            if (this.sourceControl != null) {
                this.sourceControl.removeListener(SWT.Move,
                        sourceControlMoveListener);
            }
        }
        this.sourceControl = sourceControl;
        if (sourceControlMoveListener == null) {
            sourceControlMoveListener = new Listener() {
                public void handleEvent(Event event) {
                    group = null;
                    updateShellBounds(currentHeight);
                }
            };
        }
        this.sourceControl.addListener(SWT.Move, sourceControlMoveListener);

        Point bottomRight = getBottomRight(sourceControl);
        open(bottomRight.x, bottomRight.y, popup);
    }

    private Point getBottomRight(Control sourceControl) {
        Rectangle bounds = getSourceArea(sourceControl);
        Point loc = sourceControl.toDisplay(bounds.x, bounds.y);
        int pointX = loc.x + bounds.width - POPUP_GAP;
        if (centerPopUp) {
            pointX = pointX - bounds.width / 2 + POPUP_GAP;
            if (targetSize != null)
                pointX += targetSize.x / 2;
            else
                pointX += DEFAULT_TARGET_SIZE.x / 2;
        }
        return new Point(pointX, loc.y + bounds.height - POPUP_GAP);
    }

    private Rectangle getSourceArea(Control sourceControl) {

        Rectangle bounds = sourceControl.getBounds();
        if (sourceControl instanceof Composite) {
            Rectangle computedSize = ((Composite) sourceControl)
                    .getClientArea();
            return computedSize;
        }
        return bounds;
    }

    protected void open(int right, int bottom, boolean popup) {
        this.startingBottomRight = getStartingBottomRight(right, bottom);
        this.popup = popup;
        open();
    }

    private Point getStartingBottomRight(int right, int bottom) {
        if (group != null) {
            Point bottomRight = group.getBottomRight();
            if (bottomRight == null) {
                group.setBottomRight(right, bottom);
                bottomRight = group.getBottomRight();
            }
            return new Point(bottomRight.x, bottomRight.y);
        }
        return new Point(right, bottom);
    }

    public int open() {
        stop();

        Shell shell = showShell();
        if (group != null) {
            group.add(this, targetHeight, targetWidth);
        }

        if (shell != null && !shell.isDisposed()) {
            if (popup && startingBottomRight != null) {
                doPopUp(shell);
            } else {
                postOpen();
            }
            popup = false;
        }
        return OK;
    }

    private Shell showShell() {
        Shell shell = getShell();
        if (shell == null || shell.isDisposed()) {
            shell = null;
            // create the window
            create();
            shell = getShell();
        }

        initializeBounds();

        // limit the shell size to the display size
        constrainShellSize();

        shell.setVisible(true);
        return shell;
    }

    private void doPopUp(Shell shell) {
        currentHeight = shell.getSize().y;
        timer = new UITimer(0, ANIM_INTERVALS, new SafeRunnable() {
            public void run() {
                currentHeight += VERTICAL_SPEED;
                if (currentHeight > targetHeight) {
                    stop();
                    postOpen();
                } else {
                    updateShellBounds(currentHeight);
                }
            }

        });
        timer.run();
    }

    protected void postOpen() {
        updateShellBounds(targetHeight);
        if (getDuration() > 0) {
            Display display = getShell().getDisplay();
            pullDownTask = new PullDownTask(display);
            display.timerExec(getDuration(), pullDownTask);
        }
        currentHeight = targetHeight;
    }

    protected void updateShellBounds(int height) {
        Shell shell = getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.setRedraw(false);
            shell.setBounds(getCurrentBounds(height, shell));
            shell.setRedraw(true);
        }
    }

    protected Rectangle getCurrentBounds(int height, Shell shell) {
        Point bottomRight;
        Point start;
        if (sourceControl != null && group == null) {
            start = getBottomRight(sourceControl);
        } else {
            start = startingBottomRight;
        }
        if (start != null) {
            bottomRight = new Point(start.x, start.y);
        } else {
            Rectangle bounds = shell.getBounds();
            bottomRight = new Point(bounds.x + bounds.width,
                    bounds.y + bounds.height);
        }
        int x = bottomRight.x - targetWidth;
        int y = bottomRight.y - height;

        return new Rectangle(x, y, targetWidth, height);
    }

    private void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (pullDownTask != null) {
            pullDownTask.cancel();
            pullDownTask = null;
        }
    }

    protected Point getInitialSize() {
        Point size = super.getInitialSize();

        targetWidth = size.x;
        targetHeight = size.y;

        if (!popup)
            return size;

        return new Point(size.x, 1);
    }

    protected Point getInitialLocation(Point initialSize) {
        if (startingBottomRight == null)
            return super.getInitialLocation(initialSize);
        return new Point(startingBottomRight.x - initialSize.x,
                startingBottomRight.y - initialSize.y);
    }

    public static int getStayDuration() {
        return STAY_DURATION;
    }

    public static void setStayDuration(int duration) {
        STAY_DURATION = duration;
    }

    public boolean isShowing() {
        return getShell() != null && !getShell().isDisposed();
    }

    public boolean close() {
        stop();
        currentHeight = 0;
        if (group != null) {
            group.remove(this);
        }
        return super.close();
    }

    public void pullDown() {
        stop();
        Shell shell = getShell();
        if (shell != null && !shell.isDisposed()) {
            targetHeight = 0;
            doPullDown(shell);
        }
    }

    private void doPullDown(Shell shell) {
        Rectangle bounds = shell.getBounds();
        currentHeight = bounds.height;
        timer = new UITimer(0, ANIM_INTERVALS, new SafeRunnable() {
            public void run() {
                currentHeight -= VERTICAL_SPEED;
                if (currentHeight <= 0) {
                    close();
                } else {
                    updateShellBounds(currentHeight);
                }
            }
        });
        timer.run();
    }

    protected void handleDispose() {
        stop();
        if (sourceControl != null && !sourceControl.isDisposed()) {
            if (sourceControlMoveListener != null) {
                sourceControl.removeListener(SWT.Move,
                        sourceControlMoveListener);
            }
            sourceControl = null;
        }
    }

}