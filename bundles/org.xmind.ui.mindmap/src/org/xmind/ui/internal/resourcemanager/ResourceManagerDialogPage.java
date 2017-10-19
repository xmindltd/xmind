package org.xmind.ui.internal.resourcemanager;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.ui.internal.e4models.IModelPartContext;
import org.xmind.ui.internal.e4models.ModelPageContainer;

public abstract class ResourceManagerDialogPage extends ModelPageContainer
        implements IResourceManagerDialogPage {

    private final static int BUTTON_MIN_WIDTH = Util.isMac() ? 150 : 120;
    private Image image;
    private ImageDescriptor imageDescriptor;
    private String pageId;
    private String title;

    @Inject
    protected IModelPartContext context;

    private ResourceManagerViewer viewer;

    @Override
    protected Control createMainPage(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        GridLayoutFactory.swtDefaults().spacing(0, 0).margins(0, 0)
                .applyTo(composite);
        Composite viewerComposite = new Composite(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0)
                .applyTo(viewerComposite);
        viewerComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = createViewer();

        context.setSelectionProvider(viewer);
        context.registerContextMenu(viewerComposite, getContextMenuId());
        viewer.createControl(viewerComposite);

        createButtonBar(composite);

        registerRunnable(context.getAdapter(MPart.class).getContext());

        return composite;
    }

    protected void registerRunnable(IEclipseContext eclipseContext) {
    }

    protected abstract ResourceManagerViewer createViewer();

    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0)
                .applyTo(composite);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.heightHint = 40;
        composite.setLayoutData(gridData);

        Composite buttonBar = new Composite(composite, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        layout.marginWidth = 15;
        layout.marginHeight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        buttonBar.setLayout(layout);
        buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));
        buttonBar.setFont(parent.getFont());

        // Add buttons to the button bar.
        createButtonsForButtonBar(buttonBar);
        return buttonBar;
    }

    protected void createButtonsForButtonBar(Composite composite) {
        // create OK and Cancel buttons by default
        createButton(composite, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(composite, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);

    }

    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        // increment the number of columns in the button bar\
        ((GridLayout) parent.getLayout()).numColumns++;
        Button button = new Button(parent, SWT.PUSH | SWT.NONE);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(new Integer(id));

        GridData gridData = new GridData(SWT.FILL, SWT.NONE, false, false);
        button.setLayoutData(gridData);
        gridData.widthHint = Math.max(gridData.widthHint, BUTTON_MIN_WIDTH);
        return button;
    }

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (ISelectionProvider.class.equals(adapter)) {
            return adapter.cast(viewer);
        } else if (Viewer.class.equals(adapter)) {
            return adapter.cast(viewer);
        }
        return null;
    }

    public String getId() {
        return pageId == null ? getModelPageId() : pageId;
    }

    public void setId(String id) {
        this.pageId = id;
    }

    @Override
    public String getTitle() {
        return title == null ? getModelPageTitle() : title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setImageDescriptor(ImageDescriptor imageDes) {
        this.imageDescriptor = imageDes;
        if (image != null) {
            image.dispose();
            image = null;
        }
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    @Override
    public Image getImage() {
        if (image == null) {
            if (imageDescriptor != null) {
                image = imageDescriptor.createImage();
            }
        }
        return image;
    }

    @Override
    public void refresh() {
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            viewer.refresh();
        }
    }

    protected abstract String getContextMenuId();
}
