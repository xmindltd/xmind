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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.ui.internal.ToolkitImages;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.UITimer;

/**
 * @author Shawn Liu
 * @since 3.6.50
 */
public class Promotion extends Dialog {

    private static final int MARGIN = 5;

    private static int STAY_DURATION = 5000;

    private static List<Promotion> promotionList = new ArrayList<Promotion>();

    private String infoText;

    private IAction action;

    private boolean isCenter;

    private UITimer timer;

    private int duration = STAY_DURATION;

    private ResourceManager resources;

    private boolean isClosed = false;

    private boolean isClickCloseButton = false;

    public Promotion(Shell parent, String infoText, IAction action,
            boolean isCenter) {
        super(parent);
        this.infoText = infoText;
        this.action = action;
        this.isCenter = isCenter;

        setShellStyle(SWT.NO_TRIM | SWT.TOOL);
        setBlockOnOpen(false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                newShell);
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        Point size = getInitialSize();

        Rectangle bounds = getParentShell().getBounds();
        int x = isCenter ? bounds.x + (bounds.width - size.x) / 2
                : bounds.x + bounds.width - size.x - MARGIN;
        int y = bounds.y + bounds.height - size.y - MARGIN;

        if (promotionList.size() != 0) {
            Dialog topDialog = promotionList.get(promotionList.size() - 1);
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
                (Color) resources.get(ColorUtils.toDescriptor("#e1e1e1"))); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 446;
        gridData.heightHint = 170;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 1;
        gridLayout.marginHeight = 1;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createImageSection(composite);
        createContentSection(composite);

        return composite;
    }

    private void createImageSection(Composite parent) {
        Image image = null;
        if (action != null) {
            ImageDescriptor imageDesc = action.getImageDescriptor();
            if (imageDesc != null) {
                image = (Image) resources.get(imageDesc);
            }
        }

        //get default image
        Shell sourceShell = getParentShell();
        if (image == null && sourceShell != null && !sourceShell.isDisposed()) {
            image = findBrandingImage(sourceShell.getImage(),
                    sourceShell.getImages());
        }

        if (image != null) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setBackground(
                    (Color) resources.get(ColorUtils.toDescriptor("#f7f7f7"))); //$NON-NLS-1$
            GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
            composite.setLayoutData(gridData);

            GridLayout layout = new GridLayout(2, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.horizontalSpacing = 0;
            composite.setLayout(layout);

            Composite composite2 = new Composite(composite, SWT.NONE);
            composite2.setBackground(composite.getBackground());
            GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData2.widthHint = 174;
            composite2.setLayoutData(gridData2);

            GridLayout layout2 = new GridLayout(1, false);
            layout2.marginWidth = 0;
            layout2.marginHeight = 0;
            composite2.setLayout(layout2);

            Label iconLabel = new Label(composite2, SWT.CENTER);
            iconLabel.setBackground(composite2.getBackground());
            GridData gridData3 = new GridData(SWT.CENTER, SWT.CENTER, true,
                    true);
            iconLabel.setLayoutData(gridData3);
            iconLabel.setImage(image);

            //create separator
            createSeperator(composite);
        } else {
            ((GridLayout) parent.getLayout()).numColumns = 1;
        }
    }

    private void createSeperator(Composite parent) {
        Composite seperator = new Composite(parent, SWT.NONE);
        seperator.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#e1e1e1"))); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.CENTER, SWT.FILL, false, true);
        gridData.widthHint = 1;
        seperator.setLayoutData(gridData);
        seperator.setLayout(new GridLayout());
    }

    private void createContentSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        composite.setLayout(gridLayout);

        createCloseButton(composite);
        createInfoSection(composite);
        createActionButton(composite);
    }

    private void createCloseButton(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData layoutData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 14;
        layout.marginRight = 14;
        composite.setLayout(layout);

        final Label close = new Label(composite, SWT.RIGHT);
        close.setBackground(composite.getBackground());
        close.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        final Image focusedImage = (Image) resources
                .get(ToolkitImages.get("close.png", true)); //$NON-NLS-1$
        final Image noFocusedImage = (Image) resources
                .get(ToolkitImages.get("close.png", false)); //$NON-NLS-1$

        close.setImage(noFocusedImage);

        composite.setCursor(
                parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        close.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        MouseTrackListener mouseTrackListener = new MouseTrackListener() {

            public void mouseHover(MouseEvent e) {
            }

            public void mouseExit(MouseEvent e) {
                close.setImage(noFocusedImage);
            }

            public void mouseEnter(MouseEvent e) {
                close.setImage(focusedImage);
            }
        };

        composite.addMouseTrackListener(mouseTrackListener);
        close.addMouseTrackListener(mouseTrackListener);

        MouseListener mouseListener = new MouseListener() {

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                isClickCloseButton = true;
                close();
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
        };

        composite.addMouseListener(mouseListener);
        close.addMouseListener(mouseListener);
    }

    private void createInfoSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 20;
        gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);

        StyledLink link;
        String content = infoText;
        if (content.indexOf("<form>") >= 0) { //$NON-NLS-1$
            link = new StyledLink(composite, SWT.NONE);
        } else {
            link = new StyledLink(composite, SWT.SIMPLE);
        }
        link.setBackground(composite.getBackground());
        link.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#4a4a4a"))); //$NON-NLS-1$
        link.setFont((Font) resources.get(FontDescriptor.createFrom(
                FontUtils.relativeHeight(link.getFont().getFontData(), 3))));
        link.setText(content);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
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
    }

    private void createActionButton(Composite parent) {
        boolean hasAction = action != null;
        if (hasAction) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setBackground(parent.getBackground());
            GridData layoutData = new GridData(SWT.CENTER, SWT.BOTTOM, false,
                    false);
            composite.setLayoutData(layoutData);

            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.marginBottom = 13;
            composite.setLayout(layout);

            Hyperlink hyperlink = new Hyperlink(composite, SWT.NONE);
            hyperlink.setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, false, false));
            hyperlink.setBackground(composite.getBackground());
            hyperlink.setForeground(
                    (Color) resources.get(ColorUtils.toDescriptor("#4a90e2"))); //$NON-NLS-1$
            hyperlink
                    .setFont(
                            (Font) resources
                                    .get(FontDescriptor.createFrom(
                                            FontUtils.bold(
                                                    FontUtils.relativeHeight(
                                                            hyperlink.getFont()
                                                                    .getFontData(),
                                                            3),
                                                    true))));
            hyperlink.setUnderlined(true);
            hyperlink.setText(action.getText());

            hyperlink.addHyperlinkListener(new IHyperlinkListener() {

                public void linkExited(HyperlinkEvent e) {
                }

                public void linkEntered(HyperlinkEvent e) {
                }

                public void linkActivated(HyperlinkEvent e) {
                    close();
                    action.run();
                }
            });
        }
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
                    timer.cancel();
                    timer = null;
                }

            });
        }
        timer.run();

        promotionList.add(this);
        return code;
    }

    @Override
    public boolean close() {
        return closePromotion();
    }

    private synchronized boolean closePromotion() {
        if (isClosed)
            return false;
        isClosed = true;

        movePromotionLocation(promotionList.indexOf(this));
        promotionList.remove(this);
        return super.close();
    }

    private void movePromotionLocation(int index) {

        if (promotionList.size() - 1 > index && -1 < index) {
            for (int i = index + 1; i < promotionList.size(); i++) {
                Promotion prom = promotionList.get(i);
                prom.getShell()
                        .setLocation(new Point(prom.getShell().getBounds().x,
                                prom.getShell().getBounds().y
                                        + prom.getInitialSize().y));
            }
        }
    }

    public boolean isClickCloseButton() {
        return isClickCloseButton;
    }
}
