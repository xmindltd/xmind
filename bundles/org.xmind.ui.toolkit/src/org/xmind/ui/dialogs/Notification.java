package org.xmind.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.xmind.ui.internal.ToolkitImages;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.RegionUtils;
import org.xmind.ui.util.UITimer;

public class Notification extends Dialog {

    private static int STAY_DURATION = 5000;

    private static List<Window> group = new ArrayList<Window>();

    private String infoText;

    private IAction action;

    private Rectangle bounds;

    private boolean isCenter;

    private boolean isLong;

    private boolean isInfoHyperlink;

    private String infoTooltip;

    private UITimer timer;

    private int duration = STAY_DURATION;

    private Label close;

    private ResourceManager resources;

    private boolean noButton;

    public Notification(Shell parent, String infoText, IAction action,
            Rectangle bounds, boolean isCenter, boolean isLong,
            boolean isInfoHyperlink, String infoTooltip) {
        this(parent, infoText, action, bounds, isCenter, isLong,
                isInfoHyperlink, infoTooltip, false);
    }

    public Notification(Shell parent, String infoText, IAction action,
            Rectangle bounds, boolean isCenter, boolean isLong,
            boolean isInfoHyperlink, String infoTooltip, boolean noButton) {
        super(parent);
        this.infoText = infoText;
        this.action = action;
        this.isCenter = isCenter;
        this.isLong = isLong;
        this.isInfoHyperlink = isInfoHyperlink;
        this.infoTooltip = infoTooltip;
        this.noButton = noButton;

        //initial bounds
        if (bounds == null) {
            bounds = parent.getBounds();
        }
        this.bounds = new Rectangle(bounds.x, bounds.y, bounds.width,
                bounds.height);

        setShellStyle(SWT.NO_TRIM | SWT.TOOL);
        setBlockOnOpen(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                newShell);

        newShell.setAlpha(0x95);
        newShell.setBackgroundMode(SWT.INHERIT_FORCE);
    }

    @Override
    protected void initializeBounds() {
        Point size = getInitialSize();
        Region region = RegionUtils
                .getRoundedRectangle(new Rectangle(0, 0, size.x, size.y), 2);
        getShell().setRegion(region);

        Point location = getInitialLocation();
        getShell().setLocation(location);

        //dispose region
        getShell().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                getShell().getRegion().dispose();
            }
        });
    }

    private Point getInitialLocation() {
        Point size = getInitialSize();
        int x = isCenter ? bounds.x + (bounds.width - size.x) / 2
                : bounds.x + bounds.width - size.x;
        int y = bounds.y + bounds.height - size.y;
        //if other notifications are being shown.
        if (group.size() != 0) {
            Window topDialog = group.get(group.size() - 1);
            if (!topDialog.getShell().isDisposed()) {
                y = topDialog.getShell().getBounds().y - size.y - 1;
            }
        }

        return new Point(x, y);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#000000"))); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = isLong ? 500 : 380;
        gridData.heightHint = 64;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(noButton ? 2 : 3, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createImageSection(composite);
        createInfoSection(composite);
        if (!noButton) {
            createButtonsSection(composite);
        }

        return composite;
    }

    private void createImageSection(Composite parent) {
        Image image = null;
        if (action != null) {
            ImageDescriptor icon = action.getImageDescriptor();
            if (icon != null) {
                image = (Image) resources.get(icon);
            }
        }

        //get default image
        if (image == null) {
            image = (Image) resources
                    .get(ToolkitImages.get("notification-default-small.png")); //$NON-NLS-1$
        }

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, false);
        gridData.widthHint = 76;
        composite.setLayoutData(gridData);
        composite.setLayout(new GridLayout(1, false));

        Label iconLabel = new Label(composite, SWT.CENTER);
        iconLabel.setBackground(composite.getBackground());
        GridData gridData2 = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        iconLabel.setLayoutData(gridData2);
        iconLabel.setImage(image);
    }

    private void createInfoSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginRight = 20;
        composite.setLayout(gridLayout);

        if (isInfoHyperlink) {
            StyledLink link;
            String content = infoText;
            if (content.indexOf("<form>") >= 0) { //$NON-NLS-1$
                link = new StyledLink(composite, SWT.NONE);
            } else {
                link = new StyledLink(composite, SWT.SIMPLE);
            }
            link.setBackground(composite.getBackground());
            link.setForeground(
                    (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
            link.setFont(
                    (Font) resources.get(FontDescriptor.createFrom(FontUtils
                            .relativeHeight(link.getFont().getFontData(), 1))));
            link.setText(content);
            if (infoTooltip != null) {
                link.setToolTipText(infoTooltip);
            }

            GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true,
                    true);
            link.setLayoutData(layoutData);
            if (action == null)
                link.setEnabled(false);
            final IAction theAction = this.action;
            link.addHyperlinkListener(new IHyperlinkListener() {
                public void linkExited(HyperlinkEvent e) {
                }

                public void linkEntered(HyperlinkEvent e) {
                }

                public void linkActivated(HyperlinkEvent e) {
                    Display.getCurrent().asyncExec(new Runnable() {
                        public void run() {
                            if (theAction != null)
                                theAction.run();
                        }
                    });
                }
            });

        } else {
            Label infoLabel = null;
            String content = infoText;
            if (content.indexOf("<form>") >= 0) { //$NON-NLS-1$
                infoLabel = new Label(composite, SWT.MULTI | SWT.WRAP);
            } else {
                infoLabel = new Label(composite, SWT.SIMPLE);
            }

            infoLabel.setBackground(composite.getBackground());
            infoLabel.setForeground(
                    (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
            infoLabel.setFont((Font) resources
                    .get(FontDescriptor.createFrom(FontUtils.relativeHeight(
                            infoLabel.getFont().getFontData(), 1))));
            infoLabel.setText(content);
            GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true,
                    true);
            infoLabel.setLayoutData(layoutData);
            if (infoTooltip != null) {
                infoLabel.setToolTipText(infoTooltip);
            }
        }
    }

    private void createButtonsSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData gridData = new GridData(SWT.RIGHT, SWT.FILL, false, true);
        gridData.widthHint = 74;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);

        createCloseButton(composite);
        createActionButton(composite);
    }

    private void createCloseButton(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#9b9b9b"))); //$NON-NLS-1$
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 1;
        layout.marginBottom = 1;
        composite.setLayout(layout);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setBackground(parent.getBackground());
        GridData layoutData2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite2.setLayoutData(layoutData2);

        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        composite2.setLayout(layout2);

        close = new Label(composite2, SWT.RIGHT);
        close.setBackground(close.getParent().getBackground());
        close.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        close.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

        close.setFont(
                (Font) resources
                        .get(FontDescriptor.createFrom(FontUtils.bold(
                                FontUtils.relativeHeight(
                                        close.getFont().getFontData(), -1),
                                true))));
        close.setText(Messages.Notification_closeButton_text);

        composite2.setCursor(
                parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        close.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        //add select feedback
        final Color original = close.getForeground();
        MouseTrackListener mouseTrackListener = new MouseTrackListener() {

            public void mouseHover(MouseEvent e) {
            }

            public void mouseExit(MouseEvent e) {
                close.setForeground(original);
            }

            public void mouseEnter(MouseEvent e) {
                close.setForeground((Color) resources
                        .get(ColorUtils.toDescriptor("#3399ff"))); //$NON-NLS-1$
            }
        };

        composite.addMouseTrackListener(mouseTrackListener);
        close.addMouseTrackListener(mouseTrackListener);

        MouseListener mouseListener = new MouseListener() {

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                close();
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
        };

        composite2.addMouseListener(mouseListener);
        close.addMouseListener(mouseListener);
    }

    private void createActionButton(Composite parent) {
        boolean hasAction = action != null;
        if (hasAction) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setBackground(
                    (Color) resources.get(ColorUtils.toDescriptor("#9b9b9b"))); //$NON-NLS-1$
            GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
            composite.setLayoutData(layoutData);

            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.marginLeft = 1;
            layout.marginTop = 0;
            composite.setLayout(layout);

            Composite composite2 = new Composite(composite, SWT.NONE);
            composite2.setBackground(parent.getBackground());
            GridData layoutData2 = new GridData(SWT.FILL, SWT.FILL, true, true);
            composite2.setLayoutData(layoutData2);

            GridLayout layout2 = new GridLayout(1, false);
            layout2.marginWidth = 0;
            layout2.marginHeight = 0;
            composite2.setLayout(layout2);

            final Label actionButton = new Label(composite2, SWT.RIGHT);
            actionButton.setBackground(parent.getBackground());
            actionButton.setForeground(
                    (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
            actionButton.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, true, true));

            actionButton.setFont((Font) resources
                    .get(FontDescriptor.createFrom(FontUtils.bold(
                            FontUtils.relativeHeight(
                                    actionButton.getFont().getFontData(), -1),
                            true))));
            actionButton.setText(action.getText());

            composite2.setCursor(
                    parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            actionButton.setCursor(
                    parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

            //add select feedback
            final Color original = actionButton.getForeground();
            MouseTrackListener mouseTrackListener = new MouseTrackListener() {

                public void mouseHover(MouseEvent e) {
                }

                public void mouseExit(MouseEvent e) {
                    actionButton.setForeground(original);
                }

                public void mouseEnter(MouseEvent e) {
                    actionButton.setForeground((Color) resources
                            .get(ColorUtils.toDescriptor("#3399ff"))); //$NON-NLS-1$
                }
            };

            composite.addMouseTrackListener(mouseTrackListener);
            actionButton.addMouseTrackListener(mouseTrackListener);

            Listener mouseDownListener = new Listener() {

                public void handleEvent(Event event) {
                    close();
                    action.run();
                }
            };

            composite2.addListener(SWT.MouseDown, mouseDownListener);
            actionButton.addListener(SWT.MouseDown, mouseDownListener);
        }
    }

    /**
     * @param stayDuration
     *            the duration this dialog will stay on the screen, in
     *            milliseconds
     */
    public void setDuration(int stayDuration) {
        this.duration = stayDuration;
    }

    @Override
    public int open() {
        int code = super.open();

        if (timer == null) {
            timer = new UITimer(duration, 0, 0, new SafeRunnable() {

                public void run() {
                    close();
                }

            });
        }
        timer.run();

        group.add(this);
        return code;
    }

    @Override
    public boolean close() {
        boolean closed = super.close();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        group.remove(this);
        return closed;
    }

    public void setCloseButtonListener(int eventType, Listener listener) {
        if (close == null || close.isDisposed())
            return;

        close.addListener(eventType, listener);
        close.getParent().addListener(eventType, listener);
    }

}
