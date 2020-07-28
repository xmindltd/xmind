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
package org.xmind.ui.internal.print.multipage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.print.PrintConstants;
import org.xmind.ui.internal.print.PrintUtils;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.UnitConvertor;
import org.xmind.ui.viewers.SWTUtils;

public class MultipageSetupDialog extends TrayDialog {

    private static final String SECTION_ID = "org.xmind.ui.print.multiPageSetupDialog"; //$NON-NLS-1$

    private static final int PREVIEW_DELAY = 1000;

    private static final int HIDE_DETAIL_ID = 2;

    private class GeneratePreviewJob extends Job {

        public GeneratePreviewJob() {
            super(MindMapMessages.MultipageSetupDialog_GeneratePreview);
        }

        protected IStatus run(IProgressMonitor monitor) {
            if (settings instanceof DialogSettingsDecorator
                    && ((DialogSettingsDecorator) settings).isDirty()) {
                return new Status(IStatus.CANCEL, MindMapUI.PLUGIN_ID,
                        MindMapMessages.MultipageSetupDialog_GeneratingPreview);
            }

            monitor.beginTask(null, 100);
            monitor.worked(50);
            display.asyncExec(new Runnable() {

                public void run() {
                    asyncUpdateViewer(display, null, PreviewState.Showing,
                            false, null);
                    boolean multiPages = getBoolean(PrintConstants.MULTI_PAGES);
                    createPreviewImage(multiPages);

                    if (multiPages) {
                        viewer.setImageIndex(0);
                    } else {
                        viewer.setImageIndex(1);
                        viewer.disableImageButtons();
                        viewer.setPageNumberVisible(false);
                    }
                    viewer.initPreviewImageRatio();
                    if (!getImageCreator().checkImage()) {
                        asyncUpdateViewer(display, null, PreviewState.Error,
                                false, null);
                    }
                }
            });

            monitor.worked(50);
            if (generatePreviewJob == this)
                generatePreviewJob = null;
            monitor.done();
            return new Status(IStatus.OK, MindMapUI.PLUGIN_ID,
                    MindMapMessages.MultipageSetupDialog_GeneratedPreview);
        }
    }

    private static enum PreviewState {
        Showing(null, SWT.COLOR_DARK_GRAY, SWT.BOTTOM | SWT.RIGHT) {
            public String getTitle(Image image, boolean largeImage) {
                if (image == null || image.isDisposed())
                    return super.getTitle(image, largeImage);
                org.eclipse.swt.graphics.Rectangle r = image.getBounds();
                return String.format("%d x %d", r.width, r.height); //$NON-NLS-1$
            }
        }, //
        Generating(MindMapMessages.MultipageSetupDialog_GeneratingPreview,
                SWT.COLOR_DARK_GRAY, SWT.NONE), //
        Error(MindMapMessages.MultipageSetupDialog_FaildGenerate
                + MindMapMessages.MultipageSetupDialog_PrintDirectly,
                SWT.COLOR_DARK_RED, SWT.NONE) {
            public String getTitle(Image image, boolean largeImage) {
                return makeErrorMessage(super.getTitle(image, largeImage),
                        largeImage);
            }
        };

        private static String makeErrorMessage(String originalMessage,
                boolean largeImage) {
            if (largeImage) {
                return originalMessage + " " //$NON-NLS-1$
                        + MindMapMessages.MultipageSetupDialog_ImageTooLarge;
            }
            return originalMessage;
        }

        private int colorId;

        private String title;

        private int titlePlacement;

        private PreviewState(String title, int colorId, int titlePlacement) {
            this.title = title;
            this.colorId = colorId;
            this.titlePlacement = titlePlacement;
        }

        public String getTitle(Image image, boolean largeImage) {
            return title;
        }

        public void setColor(Control control) {
            control.setForeground(control.getDisplay().getSystemColor(colorId));
        }

        public int getTitlePlacement() {
            return titlePlacement;
        }
    }

    private class AlignAction extends Action {

        private String key;

        private String value;

        public AlignAction(String key, String value) {
            super(null, AS_CHECK_BOX);
            this.key = key;
            this.value = value;
            if (PrintConstants.LEFT.equals(value)) {
                setText(DialogMessages.PageSetupDialog_AlignLeft_text);
                setToolTipText(
                        DialogMessages.PageSetupDialog_AlignLeft_toolTip);
                setImageDescriptor(MindMapUI.getImages()
                        .get(IMindMapImages.ALIGN_LEFT, true));
            } else if (PrintConstants.CENTER.equals(value)) {
                setText(DialogMessages.PageSetupDialog_AlignCenter_text);
                setToolTipText(
                        DialogMessages.PageSetupDialog_AlignCenter_toolTip);
                setImageDescriptor(MindMapUI.getImages()
                        .get(IMindMapImages.ALIGN_CENTER, true));
            } else /* if (PrintConstants.RIGHT.equals(value)) */ {
                setText(DialogMessages.PageSetupDialog_AlignRight_text);
                setToolTipText(
                        DialogMessages.PageSetupDialog_AlignRight_toolTip);
                setImageDescriptor(MindMapUI.getImages()
                        .get(IMindMapImages.ALIGN_RIGHT, true));
            }
        }

        public void run() {
            setProperty(key, value);
        }
    }

    private class FontAction extends Action {
        private String key;

        public FontAction(String key) {
            this.key = key;
            setText(DialogMessages.PageSetupDialog_Font_text);
            setToolTipText(DialogMessages.PageSetupDialog_Font_toolTip);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.FONT, true));
        }

        public void run() {
            FontDialog dialog = new FontDialog(getShell());
            dialog.setEffectsVisible(false);
            String string = getString(key, null);
            if (string == null) {
                dialog.setFontList(JFaceResources.getDefaultFontDescriptor()
                        .getFontData());
            } else {
                dialog.setFontList(FontUtils.toFontData(string));
            }
            FontData open = dialog.open();
            if (open == null)
                return;

            getSettings().put(key, FontUtils.toString(dialog.getFontList()));
            refreshWidthHeightPages();
            update(key);
            generatePreview(true);
        }
    }

    private Display display;

    private IGraphicalEditorPage page;

    private IMindMap sourceMindMap;

    private IDialogSettings settings;

    private Composite leftButtonBar;

    private Button hideDetailsButton;

    private Label contentSectionLabel;

    private Composite contentSectionComposite;

    private Button currentMapRadio;

    private Button wholeWorkbookRadio;

    private Button backgroundCheck;

    private Button borderCheck;

    private Button showPlusCheck;

    private Button showMinusCheck;

    private Button landscapeRadio;

    private Button portraitRadio;

    private Map<String, Text> inputControls;

    private Combo unitChooser;

    private Map<String, IAction[]> actions;

    private Combo pagesChooser;

    private Composite multipageComposite;

    private Spinner widthPages;

    private Spinner heightPages;

    private Button pageLockCheck;

    private List<Control> hideControls = new ArrayList<Control>();

    private PrintMultipagePreviewImageCreator previewImageCreator;

    private GeneratePreviewJob generatePreviewJob;

    private MultipageImagePreviewViewer viewer;

    private boolean updating = false;

    private boolean modifyingText = false;

    private boolean isRefreshingPages = false;

    private ResourceManager resources;

    private Listener eventHandler = new Listener() {
        public void handleEvent(Event event) {
            handleWidgetEvent(event);
        }
    };

    public MultipageSetupDialog(Shell parentShell, IGraphicalEditorPage page,
            IMindMap sourceMindMap) {
        super(parentShell);
        this.display = Display.getCurrent();
        this.page = page;
        this.sourceMindMap = sourceMindMap;
        this.settings = new DialogSettingsDecorator(
                MindMapUIPlugin.getDefault().getDialogSettings(SECTION_ID));
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                newShell);
        newShell.setText(DialogMessages.PageSetupDialog_windowTitle);
    }

    public void create() {
        super.create();
        update(null);
    }

    protected IDialogSettings getDialogBoundsSettings() {
        return getSettings();
    }

    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION;
    }

    public IDialogSettings getSettings() {
        return settings;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite1 = new Composite(parent, SWT.NONE);
        composite1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout2 = new GridLayout(2, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 0;
        composite1.setLayout(layout2);

        leftButtonBar = new Composite(composite1, SWT.NONE);
        leftButtonBar
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout layout3 = new GridLayout(0, false);
        layout3.marginWidth = 12;
        layout3.marginHeight = 0;
        leftButtonBar.setLayout(layout3);

        Composite composite = new Composite(composite1, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        layout.marginWidth = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_MARGIN);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);
        GridData data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite);
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        hideDetailsButton = createButton(leftButtonBar, HIDE_DETAIL_ID,
                MindMapMessages.MultipageSetupDialog_HideDetails, false);
        hookWidget(hideDetailsButton, SWT.Selection);
        super.createButtonsForButtonBar(parent);
    }

    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        if (id == IDialogConstants.OK_ID)
            label = IDialogConstants.NEXT_LABEL;
        return super.createButton(parent, id, label, defaultButton);
    }

    @Override
    protected void setButtonLayoutData(Button button) {
        if (((Integer) button.getData()) == HIDE_DETAIL_ID) {
            GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, false);
            int widthHint = convertHorizontalDLUsToPixels(
                    IDialogConstants.BUTTON_WIDTH);
            org.eclipse.swt.graphics.Point minSize = button
                    .computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            data.widthHint = Math.max(widthHint, minSize.x);
            button.setLayoutData(data);
        } else {
            super.setButtonLayoutData(button);
        }
    }

    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        PrintDialogLayout layout = new PrintDialogLayout(settings);
        layout.marginWidth = 20;
        layout.marginHeight = 20;
        layout.horizontalSpacing = 20;
        layout.verticalSpacing = 20;
        container.setLayout(layout);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.minimumWidth = 510;
        container.setLayoutData(layoutData);

        createSettingsPart(container);
        createPreviewPart(container);

        return container;
    }

    private void createSettingsPart(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 15;
        layout.verticalSpacing = 20;
        composite.setLayout(layout);

        creatContentSection(composite);
        createPageSetupSection(composite);
        createShowPlusMinusIconsSection(composite);
        createOrientationSection(composite);
        createMarginsSection(composite);
        createHeaderFooterSection(composite);
        createPagesSection(composite);
    }

    private void creatContentSection(Composite parent) {
        contentSectionLabel = new Label(parent, SWT.NONE);
        contentSectionLabel
                .setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        contentSectionLabel
                .setFont(FontUtils.getBold(contentSectionLabel.getFont()));
        contentSectionLabel
                .setText(MindMapMessages.MultipageSetupDialog_Content);

        contentSectionComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 15;
        contentSectionComposite.setLayout(layout);
        contentSectionComposite
                .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

        currentMapRadio = new Button(contentSectionComposite, SWT.RADIO);
        currentMapRadio.setText(DialogMessages.PageSetupDialog_CurrentMap);
        currentMapRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(currentMapRadio, SWT.Selection);

        wholeWorkbookRadio = new Button(contentSectionComposite, SWT.RADIO);
        wholeWorkbookRadio
                .setText(DialogMessages.PageSetupDialog_WholeWorkbook);
        wholeWorkbookRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(wholeWorkbookRadio, SWT.Selection);
    }

    private void createPageSetupSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        label.setFont(FontUtils.getBold(label.getFont()));
        label.setText(MindMapMessages.MultipageSetupDialog_PageSetup);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 20;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

        backgroundCheck = new Button(composite, SWT.CHECK);
        backgroundCheck.setText(DialogMessages.PageSetupDialog_Background);
        backgroundCheck
                .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        hookWidget(backgroundCheck, SWT.Selection);

        borderCheck = new Button(composite, SWT.CHECK);
        borderCheck.setText(DialogMessages.PageSetupDialog_Border);
        borderCheck
                .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        hookWidget(borderCheck, SWT.Selection);
    }

    private void createShowPlusMinusIconsSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        label.setFont(FontUtils.getBold(label.getFont()));
        label.setText(MindMapMessages.MultipageSetupDialog_Collapse_Expand);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

        createShowPlusCheck(composite);
        createShowMinusCheck(composite);

        initPlusMinusCheckState();

        addHideControl(label);
        addHideControl(composite);
    }

    private void createShowPlusCheck(Composite parent) {
        showPlusCheck = createPlusMinusCheck(parent,
                MindMapMessages.MultipageSetupDialog_showPlusCheck_text,
                (Image) resources
                        .get(MindMapUI.getImages().get("plus.png", true))); //$NON-NLS-1$
    }

    private void createShowMinusCheck(Composite parent) {
        showMinusCheck = createPlusMinusCheck(parent,
                MindMapMessages.MultipageSetupDialog_showMinusCheck_text,
                (Image) resources
                        .get(MindMapUI.getImages().get("minus.png", true))); //$NON-NLS-1$
    }

    private Button createPlusMinusCheck(Composite parent, String text,
            Image image) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Button check = new Button(composite, SWT.CHECK);
        check.setBackground(composite.getBackground());
        check.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        check.setText(text);

        Label imageLabel = new Label(composite, SWT.NONE);
        imageLabel.setBackground(composite.getBackground());
        imageLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        imageLabel.setImage(image);

        hookWidget(check, SWT.Selection);

        return check;
    }

    private void initPlusMinusCheckState() {
        boolean plusVisible = getBoolean(getSettings(),
                PrintConstants.PLUS_VISIBLE,
                PrintConstants.DEFAULT_PLUS_VISIBLE);
        boolean minusVisible = getBoolean(getSettings(),
                PrintConstants.MINUS_VISIBLE,
                PrintConstants.DEFAULT_MINUS_VISIBLE);

        showPlusCheck.setSelection(plusVisible);
        showMinusCheck.setSelection(minusVisible);
    }

    private boolean getBoolean(IDialogSettings settings, String key,
            boolean defaultValue) {
        boolean value = defaultValue;
        if (settings.get(key) != null) {
            value = settings.getBoolean(key);
        }

        return value;
    }

    private void createOrientationSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        label.setFont(FontUtils.getBold(label.getFont()));
        label.setText(MindMapMessages.MultipageSetupDialog_Orientation);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 20;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

        landscapeRadio = new Button(composite, SWT.RADIO);
        landscapeRadio.setData(Integer.valueOf(PrinterData.LANDSCAPE));
        landscapeRadio.setText(DialogMessages.PageSetupDialog_Landscape);
        landscapeRadio
                .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        hookWidget(landscapeRadio, SWT.Selection);

        portraitRadio = new Button(composite, SWT.RADIO);
        portraitRadio.setData(Integer.valueOf(PrinterData.PORTRAIT));
        portraitRadio.setText(DialogMessages.PageSetupDialog_Portrait);
        portraitRadio
                .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
    }

    private void createMarginsSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
        gridData.verticalIndent = 3;
        label.setLayoutData(gridData);
        label.setFont(FontUtils.getBold(label.getFont()));
        label.setText(MindMapMessages.MultipageSetupDialog_Margin);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        Composite leftComposite = new Composite(composite, SWT.NONE);
        GridLayout layout1 = new GridLayout(2, false);
        layout1.marginHeight = 0;
        layout1.marginWidth = 0;
        leftComposite.setLayout(layout1);
        leftComposite
                .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        Composite rightComposite = new Composite(composite, SWT.NONE);
        GridLayout layout2 = new GridLayout(2, false);
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        rightComposite.setLayout(layout2);
        rightComposite
                .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        createMarginInput(leftComposite, PrintConstants.LEFT_MARGIN,
                DialogMessages.PageSetupDialog_Left);
        createMarginInput(rightComposite, PrintConstants.RIGHT_MARGIN,
                DialogMessages.PageSetupDialog_Right);
        createMarginInput(leftComposite, PrintConstants.TOP_MARGIN,
                DialogMessages.PageSetupDialog_Top);
        createMarginInput(rightComposite, PrintConstants.BOTTOM_MARGIN,
                DialogMessages.PageSetupDialog_Bottom);

        unitChooser = new Combo(composite,
                SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
        unitChooser.add(DialogMessages.PageSetupDialog_Inch);
        unitChooser.add(DialogMessages.PageSetupDialog_Millimeter);
        GridData unitLayoutData = new GridData(SWT.END, SWT.FILL, true, false);
        unitLayoutData.horizontalSpan = 2;
        unitChooser.setLayoutData(unitLayoutData);
        hookWidget(unitChooser, SWT.Selection);

        addHideControl(label);
        addHideControl(composite);
    }

    private void createHeaderFooterSection(Composite parent) {
        Composite leftComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        leftComposite.setLayout(layout);
        leftComposite
                .setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

        Label label = new Label(leftComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, true);
        gridData.verticalIndent = 2;
        label.setLayoutData(gridData);
        label.setFont(FontUtils.getBold(label.getFont()));
        label.setText(MindMapMessages.MultipageSetupDialog_Header);

        Label label2 = new Label(leftComposite, SWT.NONE);
        GridData gridData2 = new GridData(SWT.RIGHT, SWT.TOP, false, true);
        gridData2.verticalIndent = 2;
        label2.setLayoutData(gridData2);
        label2.setFont(FontUtils.getBold(label.getFont()));
        label2.setText(MindMapMessages.MultipageSetupDialog_Footer);

        Composite rightComposite = new Composite(parent, SWT.NONE);
        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.verticalSpacing = 5;
        rightComposite.setLayout(layout2);
        rightComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createHeaderSection(rightComposite);
        createFooterSection(rightComposite);

        addHideControl(leftComposite);
        addHideControl(rightComposite);
    }

    private void createPagesSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
        gridData.verticalIndent = 3;
        label.setLayoutData(gridData);
        label.setFont(FontUtils.getBold(label.getFont()));
        label.setText(MindMapMessages.MultipageSetupDialog_Pages);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 15;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        pagesChooser = new Combo(composite,
                SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
        pagesChooser.add(MindMapMessages.MultipageSetupDialog_SinglePage);
        pagesChooser.add(MindMapMessages.MultipageSetupDialog_MultiplePages);
        GridData unitLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        unitLayoutData.horizontalSpan = 2;
        pagesChooser.setLayoutData(unitLayoutData);
        pagesChooser.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        hookWidget(pagesChooser, SWT.Selection);

        multipageComposite = new Composite(composite, SWT.NONE);
        GridLayout layout2 = new GridLayout(2, false);
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.horizontalSpacing = 10;
        layout2.verticalSpacing = 15;
        multipageComposite.setLayout(layout2);
        multipageComposite
                .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        pageLockCheck = new Button(multipageComposite, SWT.CHECK);
        pageLockCheck.setText(
                MindMapMessages.MultipageSetupDialog_OptimalPagingEffect);
        GridData layoutData = new GridData(SWT.LEFT, SWT.FILL, false, false);
        layoutData.horizontalSpan = 2;
        pageLockCheck.setLayoutData(layoutData);
        hookWidget(pageLockCheck, SWT.Selection);

        boolean isLinux = Util.isLinux();
        if (isLinux) {
            layout2.numColumns = 1;
            layoutData.horizontalSpan = 1;
        }

        widthPages = createChangePageSection(multipageComposite,
                MindMapMessages.MultipageSetupDialog_Horizontal);
        heightPages = createChangePageSection(multipageComposite,
                MindMapMessages.MultipageSetupDialog_Vertical);
    }

    private void createMarginInput(Composite parent, final String key,
            String name) {
        Label nameLabel = new Label(parent, SWT.NONE);
        nameLabel.setText(name);
        nameLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Text input = createInputControl(parent, key, true);
        GridData gd = new GridData(SWT.END, SWT.FILL, false, false);
        gd.widthHint = 45;
        input.setLayoutData(gd);
    }

    private Text createInputControl(Composite parent, final String key,
            boolean numeric) {
        Text input = new Text(parent, SWT.BORDER | SWT.SINGLE);
        input.setData(key);
        if (numeric)
            SWTUtils.makeNumeralInput(input, false, true);
        hookWidget(input, SWT.Modify);
        hookWidget(input, SWT.DefaultSelection);
        hookWidget(input, SWT.FocusIn);
        if (numeric)
            hookWidget(input, SWT.KeyDown);
        if (inputControls == null)
            inputControls = new HashMap<String, Text>();
        inputControls.put(key, input);
        input.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (inputControls != null) {
                    inputControls.remove(key);
                }
            }
        });
        return input;
    }

    private void createHeaderSection(Composite parent) {
        createHFSection(parent, DialogMessages.PageSetupDialog_Header,
                PrintConstants.HEADER_ALIGN, PrintConstants.HEADER_FONT,
                PrintConstants.HEADER_TEXT);
    }

    private void createFooterSection(Composite parent) {
        createHFSection(parent, DialogMessages.PageSetupDialog_Footer,
                PrintConstants.FOOTER_ALIGN, PrintConstants.FOOTER_FONT,
                PrintConstants.FOOTER_TEXT);
    }

    private void createHFSection(Composite parent, String name,
            final String alignKey, final String fontKey, String textKey) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 1;
        container.setLayout(layout);

        Text input = createInputControl(container, textKey, false);
        GridData inputLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                false);
        inputLayoutData.horizontalSpan = 3;
        input.setLayoutData(inputLayoutData);

        AlignAction leftAction = new AlignAction(alignKey, PrintConstants.LEFT);
        AlignAction centerAction = new AlignAction(alignKey,
                PrintConstants.CENTER);
        AlignAction rightAction = new AlignAction(alignKey,
                PrintConstants.RIGHT);

        ToolBarManager alignBar = new ToolBarManager(SWT.FLAT);
        alignBar.add(leftAction);
        alignBar.add(centerAction);
        alignBar.add(rightAction);

        alignBar.createControl(container);
        alignBar.getControl()
                .setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));

        addActions(alignKey, leftAction, centerAction, rightAction);
        alignBar.getControl().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                removeActions(alignKey);
            }
        });

        ToolBarManager fontBar = new ToolBarManager(SWT.FLAT);
        FontAction fontAction = new FontAction(fontKey);
        fontBar.add(fontAction);
        fontBar.createControl(container);
        fontBar.getControl()
                .setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
    }

    private void addActions(String key, IAction... actions) {
        if (this.actions == null) {
            this.actions = new HashMap<String, IAction[]>();
        }
        this.actions.put(key, actions);
    }

    private void removeActions(String key) {
        if (this.actions != null) {
            this.actions.remove(key);
        }
    }

    private IAction[] getActions(String key) {
        return this.actions == null ? null : this.actions.get(key);
    }

    private Spinner createChangePageSection(Composite parent, String text) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label.setText(text);

        Spinner spinner = new Spinner(composite, SWT.BORDER);
        spinner.setDigits(0);
        spinner.setMinimum(1);
        spinner.setMaximum(100);
        spinner.setSelection(1);
        spinner.setIncrement(1);
        spinner.setPageIncrement(2);
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
//        gridData.widthHint = 20;
        spinner.setLayoutData(gridData);

        hookWidget(spinner, SWT.Modify);

        return spinner;
    }

    private void addHideControl(Control hideControl) {
        hideControls.add(hideControl);
    }

    private void createPreviewPart(Composite parent) {
        viewer = new MultipageImagePreviewViewer(true);
        viewer.createControl(parent);
        viewer.getControl().setFont(
                FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, 8));
    }

    private void hookWidget(Widget widget, int eventType) {
        widget.addListener(eventType, eventHandler);
    }

    private void handleWidgetEvent(Event event) {
        if (event.widget == hideDetailsButton) {
            boolean hideDetail = !getBoolean(PrintConstants.HIDE_DETAILS);
            setProperty(PrintConstants.HIDE_DETAILS, hideDetail);

        } else if (event.widget == currentMapRadio) {
            setProperty(PrintConstants.CONTENTWHOLE,
                    !currentMapRadio.getSelection());

        } else if (event.widget == wholeWorkbookRadio) {
            setProperty(PrintConstants.CONTENTWHOLE,
                    wholeWorkbookRadio.getSelection());

        } else if (event.widget == backgroundCheck) {
            setProperty(PrintConstants.NO_BACKGROUND,
                    !backgroundCheck.getSelection());

        } else if (event.widget == borderCheck) {
            setProperty(PrintConstants.BORDER, borderCheck.getSelection());

        } else if (event.widget == showPlusCheck) {
            setProperty(PrintConstants.PLUS_VISIBLE,
                    showPlusCheck.getSelection());

        } else if (event.widget == showMinusCheck) {
            setProperty(PrintConstants.MINUS_VISIBLE,
                    showMinusCheck.getSelection());

        } else if (event.widget == landscapeRadio
                || event.widget == portraitRadio) {
            Button selectedButton = (landscapeRadio.getSelection())
                    ? landscapeRadio : portraitRadio;
            getSettings().put(PrintConstants.ORIENTATION,
                    ((Integer) selectedButton.getData()).intValue());
            refreshWidthHeightPages();
            update(PrintConstants.ORIENTATION);

        } else if (event.widget instanceof Text && inputControls != null
                && inputControls.containsValue(event.widget)) {
            Text input = (Text) event.widget;
            if (event.type == SWT.FocusIn) {
                input.selectAll();
            } else if (event.type == SWT.KeyDown) {
                if (SWTUtils.matchKey(event.stateMask, event.keyCode, 0,
                        SWT.ARROW_UP)) {
                    stepValue(input, 1);
                } else if (SWTUtils.matchKey(event.stateMask, event.keyCode, 0,
                        SWT.ARROW_DOWN)) {
                    stepValue(input, -1);
                }
            } else if (event.type == SWT.DefaultSelection
                    || event.type == SWT.Modify) {
                if (updating)
                    return;

                int caretPosition = input.getCaretPosition();
                modifyingText = true;
                String key = (String) event.widget.getData();
                if (key.equals(PrintConstants.HEADER_TEXT)
                        || key.equals(PrintConstants.FOOTER_TEXT)) {
                    String oldValue = getString(key,
                            key.equals(PrintConstants.HEADER_TEXT)
                                    ? PrintConstants.DEFAULT_HEADER_TEXT
                                    : PrintConstants.DEFAULT_FOOTER_TEXT);
                    String newValue = input.getText();
                    if (("".equals(oldValue) && !"".equals(newValue)) //$NON-NLS-1$ //$NON-NLS-2$
                            || (!"".equals(oldValue) && "".equals(newValue))) { //$NON-NLS-1$ //$NON-NLS-2$
                        getSettings().put(key, input.getText());
                        refreshWidthHeightPages();
                        update(key);
                        generatePreview(false);
                    } else {
                        setProperty(key, input.getText());
                    }
                } else {
                    try {
                        double value = Double.parseDouble(input.getText());
                        setMargin(key, value);
                    } catch (NumberFormatException e) {
                    }
                }
                modifyingText = false;
                caretPosition = Math.min(caretPosition,
                        input.getText().length());
                input.setSelection(caretPosition);
            }

        } else if (event.widget == unitChooser) {
            int index = unitChooser.getSelectionIndex();
            if (index < 0 || index >= PrintConstants.UNITS.size())
                index = 0;
            setProperty(PrintConstants.MARGIN_UNIT,
                    PrintConstants.UNITS.get(index));

        } else if (event.widget == pagesChooser) {
            int index = pagesChooser.getSelectionIndex();
            setProperty(PrintConstants.MULTI_PAGES, index == 1);

        } else if (event.widget == widthPages) {
            boolean locked = getBoolean(PrintConstants.ASPECT_RATIO_LOCKED);
            if (!isRefreshingPages) {
                if (locked) {
                    isRefreshingPages = true;
                    getSettings().put(PrintConstants.FILL_HEIGHT, false);
                    int heightPageNumber = getHeightPageByWidth(page,
                            sourceMindMap, getSettings(),
                            widthPages.getSelection());
                    heightPages.setSelection(heightPageNumber);
                    setProperty(PrintConstants.WIDTH_PAGES,
                            widthPages.getSelection());
                    isRefreshingPages = false;
                } else {
                    getSettings().put(PrintConstants.FILL_HEIGHT, false);
                    setProperty(PrintConstants.WIDTH_PAGES,
                            widthPages.getSelection());
                }
            } else {
                getSettings().put(PrintConstants.WIDTH_PAGES,
                        widthPages.getSelection());
            }

        } else if (event.widget == heightPages) {
            boolean locked = getBoolean(PrintConstants.ASPECT_RATIO_LOCKED);
            if (!isRefreshingPages) {
                if (locked) {
                    isRefreshingPages = true;
                    getSettings().put(PrintConstants.FILL_HEIGHT, true);
                    widthPages.setSelection(
                            getWidthPageByHeight(page, sourceMindMap,
                                    getSettings(), heightPages.getSelection()));
                    setProperty(PrintConstants.HEIGHT_PAGES,
                            heightPages.getSelection());
                    isRefreshingPages = false;
                } else {
                    getSettings().put(PrintConstants.FILL_HEIGHT, true);
                    setProperty(PrintConstants.HEIGHT_PAGES,
                            heightPages.getSelection());
                }
            } else {
                getSettings().put(PrintConstants.HEIGHT_PAGES,
                        heightPages.getSelection());
            }

        } else if (event.widget == pageLockCheck) {
            boolean locked = pageLockCheck.getSelection();
            getSettings().put(PrintConstants.ASPECT_RATIO_LOCKED, locked);
            refreshWidthHeightPages();
            update(PrintConstants.ASPECT_RATIO_LOCKED);
        }
    }

    private void stepValue(Text input, int stepFactor) {
        double value;
        try {
            value = Double.parseDouble(input.getText());
        } catch (NumberFormatException e) {
            return;
        }

        String[] parts = split1000(value);
        int integer = Integer.parseInt(parts[0], 10);
        integer += getStep() * stepFactor;
        if (integer < 100) {
            integer = 100;
        }
        value = join1000(String.valueOf(integer), parts[1]);
        setMargin((String) input.getData(), value);
    }

    private int getStep() {
        if (PrintConstants.MILLIMETER.equals(
                getString(PrintConstants.MARGIN_UNIT, PrintConstants.INCH)))
            return 500;
        return 100;
    }

    private void refreshWidthHeightPages() {
        boolean locked = getBoolean(PrintConstants.ASPECT_RATIO_LOCKED);
        if (locked) {
            boolean fillWidth = !getSettings()
                    .getBoolean(PrintConstants.FILL_HEIGHT);
            isRefreshingPages = true;
            if (fillWidth) {
                heightPages
                        .setSelection(getHeightPageByWidth(page, sourceMindMap,
                                getSettings(), widthPages.getSelection()));
            } else {
                widthPages
                        .setSelection(getWidthPageByHeight(page, sourceMindMap,
                                getSettings(), heightPages.getSelection()));
            }
            isRefreshingPages = false;
        }
    }

    private void setMargin(String key, double value) {
        if (PrintConstants.MILLIMETER.equals(
                getString(PrintConstants.MARGIN_UNIT, PrintConstants.INCH))) {
            value = UnitConvertor.mm2inch(value);
        }
        getSettings().put(key, value);
        refreshWidthHeightPages();
        update(key);
    }

    private void update(String key) {
        if (updating)
            return;

        boolean hideDetailChanged = key == null
                || PrintConstants.HIDE_DETAILS.equals(key);
        boolean contentChanged = key == null
                || PrintConstants.CONTENTWHOLE.equals(key);
        boolean backgroundChanged = key == null
                || PrintConstants.NO_BACKGROUND.equals(key);
        boolean borderChanged = key == null
                || PrintConstants.BORDER.equals(key);
        boolean showPlusMinusIconsChanged = key == null
                || PrintConstants.PLUS_VISIBLE.equals(key)
                || PrintConstants.MINUS_VISIBLE.equals(key);
        boolean orientationChanged = key == null
                || PrintConstants.ORIENTATION.equals(key);
        boolean unitChanged = key == null
                || PrintConstants.MARGIN_UNIT.equals(key);
        boolean marginChanged = key == null || unitChanged
                || PrintConstants.LEFT_MARGIN.equals(key)
                || PrintConstants.RIGHT_MARGIN.equals(key)
                || PrintConstants.TOP_MARGIN.endsWith(key)
                || PrintConstants.BOTTOM_MARGIN.equals(key);
        boolean headerChanged = key == null
                || PrintConstants.HEADER_ALIGN.equals(key)
                || PrintConstants.HEADER_FONT.equals(key)
                || PrintConstants.HEADER_TEXT.equals(key);
        boolean footerChanged = key == null
                || PrintConstants.FOOTER_ALIGN.equals(key)
                || PrintConstants.FOOTER_FONT.equals(key)
                || PrintConstants.FOOTER_TEXT.equals(key);
        boolean pagesChanged = (key == null
                || PrintConstants.MULTI_PAGES.equals(key));
        boolean widthPagesChanged = (key == null
                || PrintConstants.WIDTH_PAGES.equals(key));
        boolean heightPagesChanged = (key == null
                || PrintConstants.HEIGHT_PAGES.equals(key));
        boolean buttonLockChanged = (key == null
                || PrintConstants.ASPECT_RATIO_LOCKED.equals(key));

        updating = true;

        if (hideDetailChanged) {
            boolean hideDetail = getBoolean(PrintConstants.HIDE_DETAILS);
            hideDetailsButton.setText(hideDetail
                    ? MindMapMessages.MultipageSetupDialog_ShowDetails
                    : MindMapMessages.MultipageSetupDialog_HideDetails);

            for (Control control : hideControls) {
                control.setVisible(!hideDetail);
                ((GridData) control.getLayoutData()).exclude = hideDetail;
            }
        }

        if (contentChanged) {
            currentMapRadio
                    .setSelection(!getBoolean(PrintConstants.CONTENTWHOLE));
            wholeWorkbookRadio
                    .setSelection(getBoolean(PrintConstants.CONTENTWHOLE));
        }

        if (backgroundChanged) {
            boolean showBackground = !getBoolean(PrintConstants.NO_BACKGROUND);
            getImageCreator().setSourceImageValid(false);
            if (backgroundCheck != null && !backgroundCheck.isDisposed()) {
                backgroundCheck.setSelection(showBackground);
            }
        }

        if (borderChanged) {
            boolean showBorder = getBoolean(PrintConstants.BORDER);
            borderCheck.setSelection(showBorder);
            viewer.setBorderVisible(showBorder);
        }

        if (showPlusMinusIconsChanged) {
            boolean plusVisible = getBoolean(settings,
                    PrintConstants.PLUS_VISIBLE,
                    PrintConstants.DEFAULT_PLUS_VISIBLE);
            boolean minusVisible = getBoolean(settings,
                    PrintConstants.MINUS_VISIBLE,
                    PrintConstants.DEFAULT_MINUS_VISIBLE);

            getImageCreator().setPlusVisible(plusVisible);
            getImageCreator().setMinusVisible(minusVisible);
            getImageCreator().setSourceImageValid(false);
        }

        if (orientationChanged) {
            updateOrientation();
        }

        if (unitChanged) {
            if (unitChooser != null && !unitChooser.isDisposed()) {
                int index = PrintConstants.UNITS.indexOf(getString(
                        PrintConstants.MARGIN_UNIT, PrintConstants.INCH));
                if (index < 0 || index >= unitChooser.getItemCount())
                    index = 0;
                unitChooser.select(index);
            }
        }

        if (marginChanged) {
            if (key == null || unitChanged) {
                updateMargins(PrintConstants.LEFT_MARGIN,
                        PrintConstants.RIGHT_MARGIN, PrintConstants.TOP_MARGIN,
                        PrintConstants.BOTTOM_MARGIN);
            } else {
                updateMargins(key);
            }
        }

        if (headerChanged) {
            updateHFSectionAndPreview(PrintConstants.HEADER_TEXT,
                    PrintConstants.DEFAULT_HEADER_TEXT,
                    PrintConstants.HEADER_ALIGN,
                    PrintConstants.DEFAULT_HEADER_ALIGN,
                    PositionConstants.CENTER, PrintConstants.HEADER_FONT);
        }

        if (footerChanged) {
            updateHFSectionAndPreview(PrintConstants.FOOTER_TEXT,
                    PrintConstants.DEFAULT_FOOTER_TEXT,
                    PrintConstants.FOOTER_ALIGN,
                    PrintConstants.DEFAULT_FOOTER_ALIGN,
                    PositionConstants.RIGHT, PrintConstants.FOOTER_FONT);
        }

        if (pagesChanged) {
            boolean multiPages = getBoolean(PrintConstants.MULTI_PAGES);
            pagesChooser.select(multiPages ? 1 : 0);

            if (viewer != null && !multiPages) {
                viewer.setPageNumberVisible(false);
            }

            //set multiPages settings area visible.
            multipageComposite.setVisible(multiPages);
            ((GridData) multipageComposite
                    .getLayoutData()).exclude = !multiPages;

            //enabled/disabled content settings area.
            contentSectionLabel.setEnabled(!multiPages);
            for (Control child : contentSectionComposite.getChildren()) {
                child.setEnabled(!multiPages);
            }

            //update content section's selection
            if (multiPages) {
                currentMapRadio.setSelection(true);
                wholeWorkbookRadio.setSelection(false);
            } else {
                currentMapRadio
                        .setSelection(!getBoolean(PrintConstants.CONTENTWHOLE));
                wholeWorkbookRadio
                        .setSelection(getBoolean(PrintConstants.CONTENTWHOLE));
            }

            getShell().pack();
            getShell().layout(true, true);
        }

        if (buttonLockChanged) {
            boolean isButtonLocked = getBoolean(
                    PrintConstants.ASPECT_RATIO_LOCKED);
            pageLockCheck.setSelection(isButtonLocked);
        }

        if (hideDetailChanged || orientationChanged) {
            boolean hideDetail = getBoolean(PrintConstants.HIDE_DETAILS);
            int orientation = getInteger(PrintConstants.ORIENTATION,
                    PrinterData.LANDSCAPE);
            boolean landscape = (orientation != PrinterData.PORTRAIT);

            int prefWidth = hideDetail ? (landscape ? 299 : 211)
                    : (landscape ? 289 : 204);
            int prefHeight = hideDetail ? (landscape ? 211 : 299)
                    : (landscape ? 204 : 289);

            viewer.updateBackgroundImageComposite(!hideDetail, landscape);
            viewer.updateBackgroundImage();

            viewer.setPrefSize(
                    new org.eclipse.swt.graphics.Point(prefWidth, prefHeight));
        }

        if (key == null) {
            initWidthHeightPages();
        }

        if (key == null) {
            generatePreview(false);
        } else if (backgroundChanged || showPlusMinusIconsChanged
                || orientationChanged || marginChanged || pagesChanged
                || widthPagesChanged || heightPagesChanged
                || buttonLockChanged) {
            generatePreview(true);
        }

        updating = false;
    }

    private void initWidthHeightPages() {
        boolean locked = getBoolean(PrintConstants.ASPECT_RATIO_LOCKED);
        boolean fillWidth = !getBoolean(PrintConstants.FILL_HEIGHT);
        int widthPageNumber = getInteger(PrintConstants.WIDTH_PAGES,
                PrintConstants.DEFAULT_WIDTH_PAGES);
        int heightPageNumber = getInteger(PrintConstants.HEIGHT_PAGES,
                PrintConstants.DEFAULT_HEIGHT_PAGES);

        if (locked) {
            if (fillWidth) {
                widthPages.setSelection(widthPageNumber);
            } else {
                heightPages.setSelection(heightPageNumber);
            }
        } else {
            widthPages.setSelection(widthPageNumber);
            heightPages.setSelection(heightPageNumber);
        }
    }

    private void updateHFSectionAndPreview(String textKey, String defaultText,
            String alignKey, String defaultAlign, int defaultDraw2DAlign,
            String fontKey) {
        String text = getString(textKey, defaultText);
        String alignValue = getString(alignKey, defaultAlign);

        IAction[] alignActions = getActions(alignKey);
        if (alignActions != null) {
            for (IAction action : alignActions) {
                action.setChecked(
                        ((AlignAction) action).value.equals(alignValue));
            }
        }

        if (!modifyingText && inputControls != null) {
            Text input = inputControls.get(textKey);
            if (input != null && !input.isDisposed()) {
                input.setText(text);
            }
        }

        String fontValue = getString(fontKey, null);
        Font font = null;
        if (fontValue != null) {
            font = FontUtils.getFont(fontValue);
        }
        if (font == null) {
            font = Display.getCurrent().getSystemFont();
        }
        if (viewer != null) {
            if (PrintConstants.HEADER_TEXT.equals(textKey)) {
                viewer.updateHeaderPreview(text, alignValue, defaultDraw2DAlign,
                        fontValue);
            } else if (PrintConstants.FOOTER_TEXT.equals(textKey)) {
                viewer.updateFooterPreview(text, alignValue, defaultDraw2DAlign,
                        fontValue);
            }
        }
    }

    private void updateMargins(String... keys) {
        if (!modifyingText && inputControls != null) {
            for (String key : keys) {
                Text text = inputControls.get(key);
                if (text != null && !text.isDisposed()) {
                    text.setText(getMarginText(key));
                }
            }
        }
    }

    private String getMarginText(String key) {
        double value = getDouble(key, PrintConstants.DEFAULT_MARGIN);
        if (PrintConstants.MILLIMETER.equals(
                getString(PrintConstants.MARGIN_UNIT, PrintConstants.INCH))) {
            value = UnitConvertor.inch2mm(value);
        }
        return String.valueOf(value);
    }

    private void updateOrientation() {
        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrinterData.LANDSCAPE);
        boolean landscape = orientation != PrinterData.PORTRAIT;
        landscapeRadio.setSelection(landscape);
        portraitRadio.setSelection(!landscape);
    }

    private void setProperty(String key, String value) {
        getSettings().put(key, value);
        update(key);
    }

    private void setProperty(String key, int value) {
        getSettings().put(key, value);
        update(key);
    }

    private void setProperty(String key, boolean value) {
        getSettings().put(key, value);
        update(key);
    }

    private String getString(String key, String defaultValue) {
        String value = getSettings().get(key);
        return value == null ? defaultValue : value;
    }

    private boolean getBoolean(String key) {
        return getSettings().getBoolean(key);
    }

    private double getDouble(String key, double defaultValue) {
        try {
            return getSettings().getDouble(key);
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    private int getInteger(String key, int defaultValue) {
        try {
            return getSettings().getInt(key);
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    private void generatePreview(boolean hasDelay) {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            updateViewer(null, PreviewState.Generating, false, null);
            viewer.setFeedbackVisible(false);
        }
        disposePreview();

        generatePreviewJob = new GeneratePreviewJob();
        if (getSettings() instanceof DialogSettingsDecorator) {
            ((DialogSettingsDecorator) getSettings()).setDirty(false);
        }
        if (hasDelay) {
            generatePreviewJob.schedule(PREVIEW_DELAY);
        } else {
            generatePreviewJob.schedule();
        }
    }

    private void disposePreview() {
        cancel();
    }

    private void cancel() {
        if (generatePreviewJob != null) {
            generatePreviewJob.cancel();
            generatePreviewJob = null;
        }
    }

    private void asyncUpdateViewer(Display display, final Image image,
            final PreviewState state, final boolean largeImage,
            final Point origin) {
        if (Thread.currentThread() != display.getThread()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    updateViewer(image, state, largeImage, origin);
                }
            });
        } else {
            updateViewer(image, state, largeImage, origin);
        }
    }

    private void updateViewer(final Image image, PreviewState state,
            boolean largeImage, Point origin) {
        if (viewer == null || viewer.getControl().isDisposed())
            return;

        if (image != null && origin != null) {
            viewer.setImage(image, origin.x, origin.y);
        } else {
            viewer.setImage(image);
        }
        viewer.setTitle(state.getTitle(image, largeImage));
        viewer.setTitlePlacement(state.getTitlePlacement());
        state.setColor(viewer.getControl());
        viewer.initPreviewImageRatio();
    }

    private void createPreviewImage(boolean multiPages) {
        previewImageCreator = getImageCreator();
        viewer.setImageBorderBounds(
                getImageBorderBounds(page, sourceMindMap, getSettings()));
        previewImageCreator.releaseResource();

        Image wholeImage = null;
        Image[] singleImages = null;
        if (multiPages) {
            wholeImage = previewImageCreator.createPrintPreviewRoughImage(
                    viewer.getPrefWidth(), viewer.getPrefHeight());
            singleImages = previewImageCreator.getSingleImages();
        } else {
            Image singleImage = previewImageCreator
                    .createPrintPreviewSingleImage();
            singleImages = new Image[] { singleImage };
        }
        viewer.setWholeImage(wholeImage);
        viewer.setSingleImages(singleImages);
    }

    private PrintMultipagePreviewImageCreator getImageCreator() {
        if (previewImageCreator == null) {
            previewImageCreator = createImageCreator(display);
        }
        return previewImageCreator;
    }

    private PrintMultipagePreviewImageCreator createImageCreator(
            Display display) {
        PrintMultipagePreviewImageCreator exporter = new PrintMultipagePreviewImageCreator(
                display, page, sourceMindMap, getSettings());
        return exporter;
    }

    @Override
    public boolean close() {
        if (previewImageCreator != null) {
            previewImageCreator.dispose();
            previewImageCreator = null;
        }
        return super.close();
    }

    //fill width
    private int getHeightPageByWidth(IGraphicalEditorPage page,
            IMindMap mindmap, IDialogSettings settings, int widthPages) {
        org.eclipse.draw2d.geometry.Rectangle sheetFigureBounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);

        int sourceWidth = sheetFigureBounds.width;
        int sourceHeight = sheetFigureBounds.height;

        int leftMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.LEFT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int rightMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.RIGHT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int topMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.TOP_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int bottomMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.BOTTOM_MARGIN, PrintConstants.DEFAULT_MARGIN));

        int headerHeight = PrintUtils.getHeaderHeight(settings,
                PrintConstants.DEFAULT_DPI);
        int footerHeight = PrintUtils.getBottomHeight(settings,
                PrintConstants.DEFAULT_DPI);

        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrintConstants.DEFAULT_ORIENTATION);
        int perPageWidth = orientation == PrinterData.LANDSCAPE
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageWidth = perPageWidth - leftMarginPixel
                - rightMarginPixel;

        int perPageHeight = orientation == PrinterData.PORTRAIT
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageHeight = perPageHeight - topMarginPixel
                - bottomMarginPixel - headerHeight - footerHeight;

        double ratio = (double) usefulPerPageWidth * widthPages / sourceWidth;

        int usefulPerPageHeightByRatio = (int) (usefulPerPageHeight / ratio);
        int heightPages = sourceHeight / usefulPerPageHeightByRatio;
        heightPages = (sourceHeight % usefulPerPageHeightByRatio == 0)
                ? heightPages : heightPages + 1;

        return heightPages;
    }

    //fill height
    private int getWidthPageByHeight(IGraphicalEditorPage page,
            IMindMap mindmap, IDialogSettings settings, int heightPages) {
        org.eclipse.draw2d.geometry.Rectangle sheetFigureBounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);

        int sourceWidth = sheetFigureBounds.width;
        int sourceHeight = sheetFigureBounds.height;

        int leftMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.LEFT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int rightMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.RIGHT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int topMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.TOP_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int bottomMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.BOTTOM_MARGIN, PrintConstants.DEFAULT_MARGIN));

        int headerHeight = PrintUtils.getHeaderHeight(settings,
                PrintConstants.DEFAULT_DPI);
        int footerHeight = PrintUtils.getBottomHeight(settings,
                PrintConstants.DEFAULT_DPI);

        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrintConstants.DEFAULT_ORIENTATION);
        int perPageWidth = orientation == PrinterData.LANDSCAPE
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageWidth = perPageWidth - leftMarginPixel
                - rightMarginPixel;

        int perPageHeight = orientation == PrinterData.PORTRAIT
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageHeight = perPageHeight - topMarginPixel
                - bottomMarginPixel - headerHeight - footerHeight;

        double ratio = (double) usefulPerPageHeight * heightPages
                / sourceHeight;

        int usefulPerPageWidthByRatio = (int) (usefulPerPageWidth / ratio);
        int widthPages = sourceWidth / usefulPerPageWidthByRatio;
        widthPages = (sourceWidth % usefulPerPageWidthByRatio == 0) ? widthPages
                : widthPages + 1;

        return widthPages;
    }

    private Rectangle getImageBorderBounds(IGraphicalEditorPage page,
            IMindMap mindmap, IDialogSettings settings) {
        org.eclipse.draw2d.geometry.Rectangle sheetFigureBounds = PrintMultipageUtils
                .getSheetFigureBounds(page, mindmap);

        int sourceWidth = sheetFigureBounds.width;
        int sourceHeight = sheetFigureBounds.height;

        int leftMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.LEFT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int rightMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.RIGHT_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int topMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.TOP_MARGIN, PrintConstants.DEFAULT_MARGIN));
        int bottomMarginPixel = PrintConstants.toPixel(getDouble(
                PrintConstants.BOTTOM_MARGIN, PrintConstants.DEFAULT_MARGIN));

        int headerHeight = PrintUtils.getHeaderHeight(settings,
                PrintConstants.DEFAULT_DPI);
        int footerHeight = PrintUtils.getBottomHeight(settings,
                PrintConstants.DEFAULT_DPI);

        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrintConstants.DEFAULT_ORIENTATION);
        int perPageWidth = orientation == PrinterData.LANDSCAPE
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageWidth = perPageWidth - leftMarginPixel
                - rightMarginPixel;

        int perPageHeight = orientation == PrinterData.PORTRAIT
                ? PrintConstants.PAGE_LENGTH : PrintConstants.PAGE_SHORT;
        int usefulPerPageHeight = perPageHeight - topMarginPixel
                - bottomMarginPixel - headerHeight - footerHeight;

        double ratio;
        boolean multiPages = getBoolean(PrintConstants.MULTI_PAGES);
        if (multiPages) {
            int widthPages = getInteger(PrintConstants.WIDTH_PAGES, 1);
            int heightPages = getInteger(PrintConstants.HEIGHT_PAGES, 1);
            boolean isAspectRatio = settings
                    .getBoolean(PrintConstants.ASPECT_RATIO_LOCKED);
            boolean fullWidth = !settings
                    .getBoolean(PrintConstants.FILL_HEIGHT);

            if (!isAspectRatio) {
                double fillWidthRatio = (double) usefulPerPageWidth * widthPages
                        / sourceWidth;
                double fillHeightRatio = (double) usefulPerPageHeight
                        * heightPages / sourceHeight;

                fullWidth = (fillWidthRatio <= fillHeightRatio) ? true : false;
            }

            ratio = fullWidth
                    ? ((double) usefulPerPageWidth * widthPages / sourceWidth)
                    : ((double) usefulPerPageHeight * heightPages
                            / sourceHeight);
        } else {
            double widthRatio = (double) usefulPerPageWidth / sourceWidth;
            double heightRatio = (double) usefulPerPageHeight / sourceHeight;
            ratio = Math.min(widthRatio, heightRatio);
        }

        int leftMarginByRatio = (int) (leftMarginPixel / ratio);
        int topMarginByRation = (int) ((topMarginPixel + headerHeight) / ratio);
        int usefulPerPageWidthByRatio = (int) (usefulPerPageWidth / ratio);
        int usefulPerPageHeightByRatio = (int) (usefulPerPageHeight / ratio);

        return new Rectangle(leftMarginByRatio, topMarginByRation + 1,
                usefulPerPageWidthByRatio, usefulPerPageHeightByRatio);
    }

    /**
     * Multiply the given number by 1000, and then split the result into integer
     * part and decimal part.
     * <p>
     * Sample:<br>
     * 
     * <pre>
     * String[] parts = split1000(34.56);
     * assert parts[0] == &quot;34560&quot;;
     * assert parts[1] == &quot;00&quot;;
     * 
     * Srting[] parts2 = split1000(0.034524);
     * assert parts2[0] == &quot;0034&quot;;
     * assert parts2[1] == &quot;524000&quot;;
     * </pre>
     * </p>
     * 
     * @param value
     * @return
     */
    private static String[] split1000(double value) {
        String repr = String.valueOf(value) + "000"; //$NON-NLS-1$
        int dotIndex = repr.indexOf("."); //$NON-NLS-1$
        if (dotIndex < 0) {
            return new String[] { repr, "" }; //$NON-NLS-1$
        } else {
            return new String[] {
                    repr.substring(0, dotIndex)
                            + repr.substring(dotIndex + 1, dotIndex + 4),
                    repr.substring(dotIndex + 4) };
        }
    }

    /**
     * Merge prefix(integer part) and suffix(decimal part) into a number and
     * return result of the number devided by 1000.
     * <p>
     * Sample:<br>
     * 
     * <pre>
     * double value = join1000("34560", "00")
     * assert value == 34.56
     * 
     * value = join1000("34", "524000")
     * assert value == 0.034524
     * </pre>
     * </p>
     * 
     * @param prefix
     * @param suffix
     * @return
     */
    private static double join1000(String prefix, String suffix) {
        prefix = "000" + prefix; //$NON-NLS-1$
        String mid = prefix.substring(prefix.length() - 3);
        prefix = prefix.substring(0, prefix.length() - 3);
        return Double.parseDouble(prefix + "." + mid + suffix); //$NON-NLS-1$
    }

}
