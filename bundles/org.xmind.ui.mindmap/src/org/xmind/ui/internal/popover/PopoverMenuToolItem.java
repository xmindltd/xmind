package org.xmind.ui.internal.popover;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.internal.e4handlers.DirectToolItem;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.RegionUtils;

public class PopoverMenuToolItem extends DirectToolItem {

    private Shell shell;

    private Control contents;

    private ShellAdapter shellDeactivedListener = new ShellAdapter() {

        @Override
        public void shellDeactivated(ShellEvent e) {
            handleShellDeactived();
        }
    };

    private LocalResourceManager localResourceManager;

    @Override
    protected void showExtensionControl(Rectangle itemBoundsToDisplay) {
        if (shell != null && !shell.isDisposed()) {
            if (shellDeactivedListener != null) {
                shell.removeShellListener(shellDeactivedListener);
            }
            shell.dispose();
        }
        shell = createShell();
        localResourceManager = new LocalResourceManager(
                JFaceResources.getResources(), shell);

        configureShell(shell);
        contents = createContents(shell);
        initializeBounds(itemBoundsToDisplay);

        shell.setActive();
        shell.setVisible(true);
        shell.forceFocus();
    }

    protected Shell createShell() {
        return new Shell(Display.getCurrent().getActiveShell(), SWT.NO_TRIM);
    }

    protected void configureShell(Shell newShell) {
        newShell.addShellListener(shellDeactivedListener);
        Layout layout = getLayout();
        if (layout != null) {
            newShell.setLayout(layout);
        }
        newShell.setBackground(
                localResourceManager.createColor(ColorUtils.toRGB("#c2c2c2"))); //$NON-NLS-1$
    }

    protected Layout getLayout() {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        return layout;
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                localResourceManager.createColor(ColorUtils.toRGB("#ffffff"))); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        return composite;
    }

    protected void initializeBounds(Rectangle itemBoundsToDisplay) {
        Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Region region = RegionUtils
                .getRoundedRectangle(new Rectangle(0, 0, size.x, size.y), 6);
        shell.setRegion(region);

        Region region2 = RegionUtils.getRoundedRectangle(
                new Rectangle(0, 0, size.x - 2, size.y - 2), 6);
        contents.setRegion(region2);

        Point location = getLocation(itemBoundsToDisplay, size);
        shell.setLocation(location);
    }

    private Point getLocation(Rectangle itemBoundsToDisplay, Point size) {
        return new Point(
                itemBoundsToDisplay.x + itemBoundsToDisplay.width / 2
                        - size.x / 2,
                itemBoundsToDisplay.y + itemBoundsToDisplay.height);
    }

    protected void handleShellDeactived() {
        if (shell == null || shell.isDisposed())
            return;

        shell.removeShellListener(shellDeactivedListener);
        Region region = shell.getRegion();
        if (region != null) {
            region.dispose();
            region = null;
        }

        Region region2 = contents.getRegion();
        if (region2 != null) {
            region2.dispose();
            region2 = null;
        }

        shell.dispose();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (LocalResourceManager.class.equals(adapter)) {
            return adapter.cast(localResourceManager);
        }
        return super.getAdapter(adapter);
    }

}
