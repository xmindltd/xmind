/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.internal.ShareOption;
import org.xmind.ui.internal.ShareOptionRegistry;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

/**
 * @author Frank Shaka
 * @author Shawn Liu
 * @since 3.6.50
 */
public class ShareDialog extends Dialog {

    private static final String ID_KEY = "org.xmind.share.option.id"; //$NON-NLS-1$

    private static final String COLOR_BACKGROUND = "#FFFFFF"; //$NON-NLS-1$
    private static final String COLOR_SEPARATOR = "#D9D9D9"; //$NON-NLS-1$
    private static final String COLOR_TEXT = "#555555"; //$NON-NLS-1$

    private ShareOptionRegistry optionRegistry;

    private ShareOption selectedOption;

    private ResourceManager resources;

    private Listener eventHandler = new Listener() {
        public void handleEvent(Event event) {
            handleWidgetEvent(event);
        }
    };

    public ShareDialog(Shell parentShell, ShareOptionRegistry optionRegistry) {
        super(parentShell);
        this.optionRegistry = optionRegistry;
        this.selectedOption = null;
        setBlockOnOpen(true);
    }

    /**
     * @return the selectedOption
     */
    public ShareOption getSelectedOption() {
        return selectedOption;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#create()
     */
    @Override
    public void create() {
        super.create();
        getShell().setText(DialogMessages.ShareDialog_dialog_title);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.
     * Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                newShell);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        return null;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setBackground((Color) resources
                .get(ColorUtils.toDescriptor(COLOR_BACKGROUND)));

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        applyDialogFont(composite);

        boolean hasTopItem = hasEnabledItem(
                RegistryConstants.VAL_CATEGORY_POPULAR);
        boolean hasBottomItem = hasEnabledItem(
                RegistryConstants.VAL_CATEGORY_NORMAL);

        if (hasTopItem) {
            createTopSection(composite);
        }
        if (hasTopItem && hasBottomItem) {
            createSeparator(composite);
        }
        if (hasBottomItem) {
            createBottomSection(composite);
        }

        return composite;
    }

    private void createTopSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());

        GridLayout layout = new GridLayout(3, true);
        layout.marginWidth = 31;
        layout.marginHeight = 26;
        layout.marginTop = 10;
        layout.horizontalSpacing = 20;
        composite.setLayout(layout);

        List<ShareOption> options = optionRegistry
                .getOptionsByCategory(RegistryConstants.VAL_CATEGORY_POPULAR);
        for (ShareOption option : options) {
            boolean disabled = (isCnUser()
                    && "cn".equals(option.getDisabledSite())); //$NON-NLS-1$
            if (!disabled) {
                createShareItem(composite, option.getLabel(),
                        (Image) resources.get(option.getImage()),
                        option.getId(), 3, 8);
            }
        }
    }

    private void createSeparator(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(layoutData);
        composite.setBackground(parent.getBackground());

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite separator = new Composite(composite, SWT.NONE);
        GridData layoutData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        separator.setLayoutData(layoutData2);
        layoutData2.heightHint = 1;
        separator.setBackground((Color) resources
                .get(ColorUtils.toDescriptor(COLOR_SEPARATOR)));
        separator.setLayout(new GridLayout());
    }

    private void createBottomSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite
                .setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());

        GridLayout layout = new GridLayout(4, true);
        layout.marginWidth = 22;
        layout.marginHeight = 28;
        layout.marginBottom = 16;
        layout.horizontalSpacing = 38;
        layout.verticalSpacing = 24;
        composite.setLayout(layout);

        List<ShareOption> options = optionRegistry
                .getOptionsByCategory(RegistryConstants.VAL_CATEGORY_NORMAL);
        for (ShareOption option : options) {
            boolean disabled = (isCnUser()
                    && "cn".equals(option.getDisabledSite())); //$NON-NLS-1$
            if (!disabled) {
                createShareItem(composite, option.getLabel(),
                        (Image) resources.get(option.getImage()),
                        option.getId(), 0, 5);
            }
        }
    }

    private Control createShareItem(Composite parent, String text, Image image,
            String id, int relativeHeight, int verticalSpacing) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
        composite.setBackground(parent.getBackground());
        composite.setForeground(parent.getForeground());

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = verticalSpacing;
        composite.setLayout(layout);

        Label imageLabel = new Label(composite, SWT.NONE);
        imageLabel.setBackground(composite.getBackground());
        imageLabel
                .setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
        imageLabel.setImage(image);

        Label textLabel = new Label(composite, SWT.WRAP);
        textLabel.setBackground(composite.getBackground());

        GridData layoutData = new GridData(SWT.CENTER, SWT.TOP, false, false);
        layoutData.widthHint = image.getBounds().width + 20;
        textLabel.setLayoutData(layoutData);
        textLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor(COLOR_TEXT)));
        textLabel.setAlignment(SWT.CENTER);
        textLabel.setFont((Font) resources
                .get(FontDescriptor.createFrom(FontUtils.relativeHeight(
                        textLabel.getFont().getFontData(), relativeHeight))));
        textLabel.setText(text);

        composite.setCursor(
                parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        imageLabel.setCursor(
                parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        textLabel.setCursor(
                parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        //add mouse down event
        composite.setData(ID_KEY, id);
        imageLabel.setData(ID_KEY, id);
        textLabel.setData(ID_KEY, id);

        hookWidget(composite, SWT.MouseDown);
        hookWidget(imageLabel, SWT.MouseDown);
        hookWidget(textLabel, SWT.MouseDown);

        return composite;
    }

    private void hookWidget(Widget widget, int eventType) {
        widget.addListener(eventType, eventHandler);
    }

    private void handleWidgetEvent(Event event) {
        String id = (String) event.widget.getData(ID_KEY);
        if (id == null)
            return;

        selectedOption = optionRegistry.getOptionById(id);
        if (selectedOption == null)
            return;

        okPressed();
    }

    private boolean hasEnabledItem(String category) {
        List<ShareOption> options = optionRegistry
                .getOptionsByCategory(category);

        for (ShareOption option : options) {
            boolean disabled = (isCnUser()
                    && "cn".equals(option.getDisabledSite())); //$NON-NLS-1$
            if (!disabled) {
                return true;
            }
        }

        return false;
    }

    private boolean isCnUser() {
        String cnUser = System.getProperty("account.cnUser"); //$NON-NLS-1$
        boolean isCnUser = "true".equals(cnUser); //$NON-NLS-1$
        return isCnUser;
    }

}
