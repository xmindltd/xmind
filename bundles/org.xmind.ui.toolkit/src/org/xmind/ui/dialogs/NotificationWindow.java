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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class NotificationWindow extends Window {

    private static final int DEFAULT_WIDTH = 260;

    private static final int MARGIN = 15;

    private static final int TWO_MARGINS = MARGIN + MARGIN;

    private static final int CORNER = 19;

    private static final int SPACING = 15;

    private static final int DEFAULT_DURATION = 30000;

    private static final int FINAL_ALPHA = 200;

//    private static final int FADE_IN_DURATION = 1200;

//    private static final int FADE_OUT_DURATION = 260;

    private static final int AUTO_FADE_OUT_DURATION = 4000;

    private static final int HOVER_FADE_IN_DURATION = 600;

    private static final Map<Monitor, List<NotificationWindow>> TRAILING_DIALOGS = new HashMap<Monitor, List<NotificationWindow>>(
            2);

    private Shell sourceShell;

    private String title;

    private IAction action;

    private IAction moreAction;

    private int duration;

    private Region shape = null;

    private long startTime = 0;

    private boolean showing = false;

    private boolean closing = false;

    private boolean hovered = false;

    private Control closeButton = null;

    private boolean closeOnMoreLink = false;

    public NotificationWindow(Shell sourceShell, String title, IAction action,
            IAction moreAction, int duration) {
        super((Shell) null);
        this.title = title;
        Assert.isNotNull(action);
        this.sourceShell = sourceShell;
        this.action = action;
        this.moreAction = moreAction;
        if (duration < 0) {
            this.duration = DEFAULT_DURATION;
        } else {
            this.duration = duration;
        }
        setBlockOnOpen(false);
        setShellStyle(SWT.NO_TRIM | SWT.ON_TOP);
    }

    public NotificationWindow setCloseOnMoreLink(boolean closeOnMoreLink) {
        this.closeOnMoreLink = closeOnMoreLink;
        return this;
    }

    @Override
    protected void constrainShellSize() {
        Monitor monitor;
        if (sourceShell == null || sourceShell.isDisposed()) {
            monitor = Display.getCurrent().getPrimaryMonitor();
        } else {
            monitor = sourceShell.getMonitor();
        }
        Rectangle clientArea = monitor.getClientArea();
        final Shell shell = getShell();
        Point contentsSize = getContents().computeSize(DEFAULT_WIDTH,
                SWT.DEFAULT, true);
        Rectangle result = shell.computeTrim(0, 0,
                contentsSize.x + TWO_MARGINS, contentsSize.y + TWO_MARGINS);
        List<NotificationWindow> dialogs = TRAILING_DIALOGS.get(monitor);
        if (dialogs == null) {
            dialogs = new ArrayList<NotificationWindow>(10);
            TRAILING_DIALOGS.put(monitor, dialogs);
        }

        Shell trailingShell = findLastShell(dialogs);
        if (trailingShell != null && !trailingShell.isDisposed()) {
            result = computeNewLocation(clientArea, result,
                    trailingShell.getBounds());
        } else {
            result = computeNewLocation(clientArea, result);
        }
        shell.setBounds(result);
        dialogs.remove(this);
        dialogs.add(this);

        // Set shell shape:
        if (shape == null || shape.isDisposed()) {
            shape = createShape(shell, result);
        }
        shell.setRegion(shape);
    }

    private Shell findLastShell(List<NotificationWindow> dialogs) {
        for (int i = dialogs.size() - 1; i >= 0; i--) {
            NotificationWindow dialog = dialogs.get(i);
            Shell lastShell = dialog.getShell();
            if (lastShell == null || lastShell.isDisposed()) {
                dialogs.remove(i);
            } else {
                return lastShell;
            }
        }
        return null;
    }

    private Rectangle computeNewLocation(Rectangle clientArea, Rectangle size,
            Rectangle lastBounds) {
        int newY = lastBounds.y + lastBounds.height + SPACING;
        if (newY + size.height < clientArea.y + clientArea.height) {
            return new Rectangle(lastBounds.x, newY, size.width, size.height);
        }
        int newX = lastBounds.x - SPACING - size.width;
        if (newX > clientArea.x) {
            return new Rectangle(newX, clientArea.y + SPACING, size.width,
                    size.height);
        }
        return computeNewLocation(clientArea, size);
    }

    private Rectangle computeNewLocation(Rectangle clientArea, Rectangle size) {
        return new Rectangle(clientArea.x + clientArea.width - SPACING
                - size.width, clientArea.y + SPACING, size.width, size.height);
    }

    private Region createShape(Shell shell, Rectangle r) {
        int left = 0;
        int top = 0;
        int right = left + r.width;
        int bottom = top + r.height;
        Polygon polygon = new Polygon(250);
        polygon.lineTo(left + CORNER, top);
        polygon.lineTo(right, top);
        polygon.lineTo(right, bottom - CORNER);
        polygon.roundCornerTo(right, bottom, right - CORNER, bottom);
        polygon.lineTo(left, bottom);
        polygon.lineTo(left, top + CORNER);
        polygon.roundCornerTo(left, top, left + CORNER, top);

        final Region s = new Region(shell.getDisplay());
        s.add(polygon.toPointList());
        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                s.dispose();
            }
        });
        return s;
    }

    @Override
    protected Layout getLayout() {
        GridLayout layout = new GridLayout();
        layout.marginWidth = MARGIN;
        layout.marginHeight = MARGIN;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        return layout;
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 9;
        layout.horizontalSpacing = 10;
        composite.setLayout(layout);

        createIconAndActions(composite);
        createCloseButton(parent);

        return composite;
    }

    private void createIconAndActions(Composite parent) {
        Image image = null;
        final Image imageToDispose;
        ImageDescriptor icon = action.getImageDescriptor();
        if (icon != null) {
            image = icon.createImage(false);
            imageToDispose = image;
        } else {
            imageToDispose = null;
        }
        if (image == null && sourceShell != null && !sourceShell.isDisposed()) {
            image = findBrandingImage(sourceShell.getImage(),
                    sourceShell.getImages());
        }
        if (image != null) {
            ((GridLayout) parent.getLayout()).numColumns = 2;
            Label iconLabel = new Label(parent, SWT.NONE);
            iconLabel.setBackground(parent.getBackground());
            iconLabel.setForeground(parent.getForeground());
            iconLabel.setImage(image);

            iconLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
                    false, true));

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setBackground(parent.getBackground());
            composite.setForeground(parent.getForeground());
            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.verticalSpacing = 9;
            layout.horizontalSpacing = 0;
            composite.setLayout(layout);
            composite
                    .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            if (imageToDispose != null) {
                iconLabel.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        imageToDispose.dispose();
                    }
                });
            }
            parent = composite;
        }
        createActions(parent);
    }

    private Image findBrandingImage(Image mainImage, Image[] images) {
        Image best = null;
        int scale = -1;
        Rectangle r;
        int s;
        if (mainImage != null) {
            r = mainImage.getBounds();
            s = Math.abs(r.width - 48) * Math.abs(r.height - 48);
            if (scale < 0 || s < scale) {
                best = mainImage;
                scale = s;
            }
        }
        for (Image img : images) {
            r = img.getBounds();
            s = Math.abs(r.width - 48) * Math.abs(r.height - 48);
            if (scale < 0 || s < scale) {
                best = img;
                scale = s;
            }
        }
        return best;
    }

    private void createActions(Composite parent) {
        createTitle(parent);
        createActionLink(parent);
        if (moreAction != null) {
            createMoreLink(parent);
        }
    }

    private void createTitle(Composite parent) {
        String text = Display.getAppName();
        if (text == null && title == null)
            return;
        if (text == null) {
            text = title;
        } else if (title != null) {
            text = text + " - " + title; //$NON-NLS-1$
        }
        Label label = new Label(parent, SWT.WRAP);
        label.setText(text);
        label.setBackground(parent.getBackground());
        label.setForeground(parent.getForeground());
        label.setFont(FontUtils.getBoldRelative(JFaceResources.DEFAULT_FONT, 1));
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    private void createActionLink(Composite parent) {
        StyledLink link;
        String content = action.getText();
        if (content.indexOf("<form>") >= 0) { //$NON-NLS-1$
            link = new StyledLink(parent, SWT.NONE);
        } else {
            link = new StyledLink(parent, SWT.SIMPLE);
        }
        link.setText(content);
        link.setBackground(parent.getBackground());
        link.setForeground(parent.getForeground());
        link.setEnabled(action.isEnabled());
        link.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                Util.isMac() ? -2 : -1));
        link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final IAction theAction = this.action;
        link.addHyperlinkListener(new IHyperlinkListener() {
            public void linkExited(HyperlinkEvent e) {
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        close();
                        theAction.run();
                    }
                });
            }
        });
    }

    private void createMoreLink(Composite parent) {
        String moreText = moreAction.getText();
        if (moreText == null) {
            moreText = Messages.NotificationDialog_MoreLink_defaultText;
        }
        StyledLink link = new StyledLink(parent, SWT.SIMPLE);
        link.setText(moreText);
        link.setFont(FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                -2));
        link.setBackground(parent.getBackground());
        link.setForeground(ColorUtils.getColor(80, 100, 250));
        link.setEnabled(moreAction.isEnabled());
        link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final IAction theMoreAction = this.moreAction;
        link.addHyperlinkListener(new IHyperlinkListener() {
            public void linkExited(HyperlinkEvent e) {
            }

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        if (closeOnMoreLink) {
                            close();
                        }
                        theMoreAction.run();
                    }
                });
            }
        });
    }

    private void createCloseButton(final Composite parent) {
        final Label label = new Label(parent, SWT.NONE);
        closeButton = label;
        label.setBackground(ColorUtils.getColor(8, 8, 8));
        label.setForeground(parent.getForeground());
        GridData layoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false,
                false);
        layoutData.exclude = true;
        label.setLayoutData(layoutData);
        label.setSize(32, 32);
        label.setLocation(0, 0);
        label.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        label.moveAbove(null);
        label.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;
                gc.setAntialias(SWT.ON);
                gc.setLineStyle(SWT.LINE_SOLID);
                gc.setLineJoin(SWT.JOIN_ROUND);

                gc.setLineWidth(1);
                gc.fillRectangle(0, 0, 32, 32);

                gc.setLineWidth(3);
                gc.drawArc(5, 5, 21, 21, 0, 360);
                int a = 11, b = 20;
                gc.drawLine(a, a, b, b);
                gc.drawLine(a, b, b, a);
            }
        });

        Polygon polygon = new Polygon(250);
        int x1, x2, x3, y1, y2, y3;
        x1 = y1 = 4;
        x2 = y2 = 16;
        x3 = y3 = 28;
        polygon.lineTo(x2, y1);
        polygon.roundCornerTo(x3, y1, x3, y2);
        polygon.roundCornerTo(x3, y3, x2, y3);
        polygon.roundCornerTo(x1, y3, x1, y2);
        polygon.roundCornerTo(x1, y1, x2, y1);
        final Region region = new Region(parent.getDisplay());
        region.add(polygon.toPointList());
        label.setRegion(region);
        label.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                region.dispose();
            }
        });
        label.setVisible(false);
        label.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                event.display.asyncExec(new Runnable() {
                    public void run() {
                        close();
                    }
                });
            }
        });
    }

    private void toggleCloseButton(boolean visible) {
        if (closeButton != null && !closeButton.isDisposed()) {
            closeButton.setVisible(visible);
        }
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        final Display display = newShell.getDisplay();
        newShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        newShell.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        newShell.setAlpha(FINAL_ALPHA);
    }

    @Override
    public void create() {
        super.create();
        final Shell shell = getShell();
        final Listener mouseTracker = new Listener() {
            public void handleEvent(Event event) {
                if (shell.isDisposed())
                    return;
                Point p = ((Control) event.widget).toDisplay(event.x, event.y);
                if (shell.getBounds().contains(p)) {
                    if (closing)
                        return;
                    hovered = true;
                    toggleCloseButton(true);
                    if (!showing && shell.getAlpha() < FINAL_ALPHA) {
                        showing = true;
                        fadeTo(FINAL_ALPHA, HOVER_FADE_IN_DURATION,
                                new Runnable() {
                                    public void run() {
                                        showing = false;
                                        countDown(5000);
                                    }
                                });
                    }
                } else {
                    hovered = false;
                    toggleCloseButton(false);
                }
            }
        };
        trackMouseMove(shell, mouseTracker);
    }

    private void trackMouseMove(Control c, Listener mouseTracker) {
        c.addListener(SWT.MouseExit, mouseTracker);
        c.addListener(SWT.MouseEnter, mouseTracker);
        if (c instanceof Composite) {
            for (Control child : ((Composite) c).getChildren()) {
                trackMouseMove(child, mouseTracker);
            }
        }
    }

    private void fadeTo(final int finalAlpha, int defaultDuration,
            final Runnable callback) {
        final Shell shell = getShell();
        if (shell == null || shell.isDisposed())
            return;

        final int startAlpha = shell.getAlpha();
        final int t = defaultDuration * 10 / FINAL_ALPHA;
        final int minAlpha = Math.min(startAlpha, finalAlpha);
        final int maxAlpha = Math.max(startAlpha, finalAlpha);
        final int[] step = new int[1];
        step[0] = 0;
        final long start = System.currentTimeMillis();
        startTime = start;
        Display.getCurrent().timerExec(1, new Runnable() {
            public void run() {
                if (startTime != start)
                    return;
                if (shell == null || shell.isDisposed())
                    return;
                if (finalAlpha > startAlpha)
                    step[0] += 10;
                else
                    step[0] -= 10;
                int newAlpha = startAlpha + (int) (step[0] / t);
                if (newAlpha > maxAlpha || newAlpha < minAlpha) {
                    if (callback != null)
                        callback.run();
                } else {
                    if (newAlpha != shell.getAlpha()) {
                        shell.setAlpha(newAlpha);
                    }
                    Display.getCurrent().timerExec(1, this);
                }
            }
        });
    }

    public int open() {
        if (getShell() == null || getShell().isDisposed()) {
            // create the window
            create();
        }

        // limit the shell size to the display size
        constrainShellSize();

        // open the window
        getShell().setVisible(true);

        showing = true;
        closing = false;
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                countDown(duration);
            }
        });
//        fadeTo(FINAL_ALPHA, FADE_IN_DURATION, new Runnable() {
//            public void run() {
//                showing = false;
//                countDown(duration);
//            }
//        });
        return OK;
    }

    public boolean close() {
        closing = true;
        hovered = false;
        showing = false;
        hardClose();
//        fadeTo(0, FADE_OUT_DURATION, new Runnable() {
//            public void run() {
//                hardClose();
//            }
//        });
        return true;
    }

    private void hardClose() {
        super.close();
        Shell shell = getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.dispose();
        }
    }

    private void countDown(int duration) {
        if (duration == 0)
            return;
        final long countDownTime = System.currentTimeMillis();
        startTime = countDownTime;
        Display.getCurrent().timerExec(duration, new Runnable() {
            public void run() {
                if (startTime != countDownTime)
                    return;

                if (hovered) {
                    Display.getCurrent().timerExec(1000, this);
                } else {
                    fadeTo(0, AUTO_FADE_OUT_DURATION, new Runnable() {
                        public void run() {
                            hardClose();
                        }
                    });
                }
            }
        });
    }

    public static void main(String[] args) {
        Display.setAppName("Main"); //$NON-NLS-1$
        Display display = new Display();
        try {
            final IAction action = new Action() {
                @Override
                public void run() {
                    super.run();
                    System.out.println("Action clicked."); //$NON-NLS-1$
                }
            };
            action.setText("<form><p>Test <b>test</b>  test test\n test test test test waf awoijf oa&amp;wfoaw <b><i>foaawefwfewfawj</i></b> foaw fo awof aw.</p><p>&quot;WOfjsiowf&quot; s fowe &#x6587;&#x5B57; ofwjfosj</p></form>"); //$NON-NLS-1$
            action.setImageDescriptor(ImageDescriptor.createFromImage(display
                    .getSystemImage(SWT.ICON_INFORMATION)));
            action.setEnabled(true);

            final IAction action2 = new Action() {
                public void run() {
                    System.out.println("Simple action clicked."); //$NON-NLS-1$
                };
            };
            action2.setText("File has been downloaded:\nC:\\Users\\USER\\Download\\test.xmind\r\nXMind test test test aowif waof awiof jawiofaj wiofaowfjawoifjaw ofawoifjaowf aowe jfoaw."); //$NON-NLS-1$

            final IAction moreAction = new Action() {
                @Override
                public void run() {
                    super.run();
                    System.out.println("More clicked."); //$NON-NLS-1$
                }
            };
            moreAction.setText("Details..."); //$NON-NLS-1$

            new NotificationWindow(null, "XMind", action, null, SWT.DEFAULT) //$NON-NLS-1$
                    .open();
            new NotificationWindow(null, "Test Notification", action, //$NON-NLS-1$
                    moreAction, SWT.DEFAULT).open();
            new NotificationWindow(null, null, action, null, 4000).open();
            new NotificationWindow(null, null, action2, moreAction, SWT.DEFAULT)
                    .open();
            new NotificationWindow(null, null, action2, null, 10000).open();

            Shell shell = new Shell(display);
            shell.setSize(400, 300);

            Button addButton = new Button(shell, SWT.PUSH);
            addButton.setText("Add"); //$NON-NLS-1$
            addButton.setSize(100,
                    addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
            addButton.setLocation(10, 10);
            addButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    new NotificationWindow(null, null, action, moreAction,
                            SWT.DEFAULT).open();
                }
            });

            shell.open();
            while (!shell.isDisposed() && !display.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            shell.dispose();

        } finally {
            display.dispose();
        }
    }

}
