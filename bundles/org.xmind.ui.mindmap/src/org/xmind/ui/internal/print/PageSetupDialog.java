/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.print;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.mindmap.MindMapViewer;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.UnitConvertor;
import org.xmind.ui.viewers.SWTUtils;

public class PageSetupDialog extends TitleAreaDialog {

    public static enum Entries {
        PlusMinusVisible(PrintConstants.PLUS_MINUS_VISIBLE,
                IMindMapViewer.PLUS_MINUS_VISIBLE), //
                PlusMinusHidden(PrintConstants.PLUS_MINUS_HIDDEN,
                        IMindMapViewer.PLUS_MINUS_HIDDEN), //
                        PlusVisibleMinusHidden(
                                PrintConstants.PLUS_VISIBLE_MINUS_HIDDEN,
                                IMindMapViewer.PLUS_VISIBLE_MINUS_HIDDEN);

        private String settingsValue;

        private String propertiesValue;

        private Entries(String settingsValue, String propertiesValue) {
            this.settingsValue = settingsValue;
            this.propertiesValue = propertiesValue;
        }

        public static String getPropertiesValue(String settingsValue) {
            for (Entries entry : values()) {
                if (entry.settingsValue.equals(settingsValue)) {
                    return entry.propertiesValue;
                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    private static final String SECTION_ID = "org.xmind.ui.PageSetupDialog"; //$NON-NLS-1$

    private static final int VIEWER_WIDTH = 480;

    private static final int VIEWER_HEIGHT = 360;

    private static final int PORTRAIT_VIEWER_WIDTH = 270;

    private static final int PORTRAIT_VIEWER_HEIGHT = 360;

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

            setProperty(key, FontUtils.toString(dialog.getFontList()));
        }
    }

    private class PreviewLayout extends AbstractLayout {

        protected Dimension calculatePreferredSize(IFigure container, int wHint,
                int hHint) {
            return container.getSize();
        }

        public void layout(IFigure container) {
            Rectangle area = container.getClientArea();
            if (borderFigure != null) {
                borderFigure.setBounds(area);
            }
            if (headerFigure != null) {
                Dimension size = headerFigure.getPreferredSize(area.width, -1);
                headerFigure.setBounds(new Rectangle(area.x, area.y + 3,
                        area.width, size.height));
            }
            if (footerFigure != null) {
                Dimension size = footerFigure.getPreferredSize(area.width, -1);
                footerFigure.setBounds(new Rectangle(area.x,
                        area.y + area.height - size.height - 1, area.width,
                        size.height));
            }
        }

    }

//    private IGraphicalEditor sourceEditor;

//    private IGraphicalEditorPage sourcePge;

//    private IMindMapViewer sourceViewer;

    private IMindMap sourceMindMap;

    private Button currentMapRadio;

    private Button wholeWorkbookRadio;

    private Button showBothRadio;

    private Button hideBothRadio;

    private Button hideMinusShowPlusRadio;

    private Button backgroundCheck;

    private Button borderCheck;

    private Button landscapeRadio;

    private Button portraitRadio;

    private Map<String, Text> inputControls;

    private Combo unitChooser;

    private Map<String, IAction[]> actions;

    private MindMapViewer previewViewer;

    private Composite previewPage;

    private GridLayout pageLayout;

    private IFigure previewFigure;

    private IFigure borderFigure;

    private ITextFigure headerFigure;

    private ITextFigure footerFigure;

    private IDialogSettings settings;

    private boolean updating = false;

    private boolean modifyingText = false;

    private Listener eventHandler = new Listener() {
        public void handleEvent(Event event) {
            handleWidgetEvent(event);
        }
    };

//    private IMindMap temp = getSourceMindMap();

//    public PageSetupDialog(Shell parentShell, IGraphicalEditor sourceEditor,
//            IGraphicalEditorPage sourcePge, IMindMapViewer sourceViewer,
//            IMindMap sourceMindMap) {
//        super(parentShell);
//        this.sourceEditor = sourceEditor;
//        this.sourcePge = sourcePge;
//        this.sourceViewer = sourceViewer;
//        this.sourceMindMap = sourceMindMap;
//        this.settings = MindMapUIPlugin.getDefault().getDialogSettings(
//                SECTION_ID);
//    }

    public PageSetupDialog(Shell parentShell, IMindMap sourceMindMap) {
        super(parentShell);
        this.sourceMindMap = sourceMindMap;
        this.settings = MindMapUIPlugin.getDefault()
                .getDialogSettings(SECTION_ID);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.PageSetupDialog_windowTitle);
    }

    public void create() {
        super.create();
        update(null);
        setTitle(NLS.bind(DialogMessages.PageSetupDialog_title,
                sourceMindMap.getCentralTopic().getTitleText()));
        setMessage(DialogMessages.PageSetupDialog_description);
    }

    protected IDialogSettings getDialogBoundsSettings() {
        return getSettings();
    }

    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION;
    }

//    public IGraphicalEditor getSourceEditor() {
//        return sourceEditor;
//    }

    public IMindMap getSourceMindMap() {
        return sourceMindMap;
    }

    public IMindMap setSourceMindMap(IMindMap sourceMindMap) {
        return this.sourceMindMap = sourceMindMap;
    }

//    public IGraphicalEditorPage getSourcePge() {
//        return sourcePge;
//    }

//    public IMindMapViewer getSourceViewer() {
//        return sourceViewer;
//    }

    public IDialogSettings getSettings() {
        return settings;
    }

    protected Button createButton(Composite parent, int id, String label,
            boolean defaultButton) {
        if (id == IDialogConstants.OK_ID)
            label = IDialogConstants.NEXT_LABEL;
        return super.createButton(parent, id, label, defaultButton);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 7;
        layout.marginWidth = 7;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSettingsPart(container);
        createPreviewPart(container);

        return composite;
    }

    private void createSettingsPart(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.widthHint = 210;
        layoutData.minimumWidth = 180;
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        creatContentSection(composite);
        createPageSetupSection(composite);
        createShowPlusMinusIconsSection(composite);
        createOrientationSection(composite);
        createMarginsSection(composite);
        createHeaderFooterSection(composite);
    }

    private void createShowPlusMinusIconsSection(Composite parent) {
        Composite section = createSection(parent,
                DialogMessages.PageSetupDialog_PlusAndMinusIcons);

        hideBothRadio = new Button(section, SWT.RADIO);
        hideBothRadio.setText(DialogMessages.PageSetupDialog_HideBoth);
        hideBothRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(hideBothRadio, SWT.Selection);

        showBothRadio = new Button(section, SWT.RADIO);
        showBothRadio.setText(DialogMessages.PageSetupDialog_ShowBoth);
        showBothRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(showBothRadio, SWT.Selection);

        hideMinusShowPlusRadio = new Button(section, SWT.RADIO);
        hideMinusShowPlusRadio
                .setText(DialogMessages.PageSetupDialog_HideMinusShowPlus);
        hideMinusShowPlusRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(hideMinusShowPlusRadio, SWT.Selection);
    }

    private void creatContentSection(Composite parent) {
        Composite section = createSection(parent,
                DialogMessages.PageSetupDialog_Content);

        currentMapRadio = new Button(section, SWT.RADIO);
        currentMapRadio.setText(DialogMessages.PageSetupDialog_CurrentMap);
        currentMapRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(currentMapRadio, SWT.Selection);

        wholeWorkbookRadio = new Button(section, SWT.RADIO);
        wholeWorkbookRadio
                .setText(DialogMessages.PageSetupDialog_WholeWorkbook);
        wholeWorkbookRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(wholeWorkbookRadio, SWT.Selection);
    }

    private void createPageSetupSection(Composite parent) {
        Composite section = createSection(parent,
                DialogMessages.PageSetupDialog_PageSetup);

        backgroundCheck = new Button(section, SWT.CHECK);
        backgroundCheck.setText(DialogMessages.PageSetupDialog_Background);
        backgroundCheck
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(backgroundCheck, SWT.Selection);

        borderCheck = new Button(section, SWT.CHECK);
        borderCheck.setText(DialogMessages.PageSetupDialog_Border);
        borderCheck
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(borderCheck, SWT.Selection);
    }

    private void createOrientationSection(Composite parent) {
        Composite section = createSection(parent,
                DialogMessages.PageSetupDialog_Orientation);
        Composite container = new Composite(section, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        landscapeRadio = new Button(container, SWT.RADIO);
        landscapeRadio.setData(Integer.valueOf(PrinterData.LANDSCAPE));
        landscapeRadio.setText(DialogMessages.PageSetupDialog_Landscape);
        landscapeRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(landscapeRadio, SWT.Selection);

        portraitRadio = new Button(container, SWT.RADIO);
        portraitRadio.setData(Integer.valueOf(PrinterData.PORTRAIT));
        portraitRadio.setText(DialogMessages.PageSetupDialog_Portrait);
        portraitRadio
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        hookWidget(portraitRadio, SWT.Selection);
    }

    private void createMarginsSection(Composite parent) {
        Composite section = createSection(parent,
                DialogMessages.PageSetupDialog_Margins);

        Composite container = new Composite(section, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginLeft = 5;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createMarginInput(container, PrintConstants.LEFT_MARGIN,
                DialogMessages.PageSetupDialog_Left);
        createMarginInput(container, PrintConstants.RIGHT_MARGIN,
                DialogMessages.PageSetupDialog_Right);
        createMarginInput(container, PrintConstants.TOP_MARGIN,
                DialogMessages.PageSetupDialog_Top);
        createMarginInput(container, PrintConstants.BOTTOM_MARGIN,
                DialogMessages.PageSetupDialog_Bottom);

        unitChooser = new Combo(section,
                SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
        unitChooser.add(DialogMessages.PageSetupDialog_Inch);
        unitChooser.add(DialogMessages.PageSetupDialog_Millimeter);
        GridData unitLayoutData = new GridData(SWT.END, SWT.FILL, true, false);
        unitChooser.setLayoutData(unitLayoutData);
        hookWidget(unitChooser, SWT.Selection);
    }

    private void createMarginInput(Composite parent, final String key,
            String name) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        container.setLayout(layout);

        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText(name);
        nameLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Text input = createInputControl(container, key, true);
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

    private void createHeaderFooterSection(Composite parent) {
        Composite section = createSection(parent,
                DialogMessages.PageSetupDialog_HeaderFooter);

        createHeaderSection(section);
        createFooterSection(section);
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

        Label label = new Label(container, SWT.NONE);
        label.setText(name);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

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
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

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
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Text input = createInputControl(container, textKey, false);
        GridData inputLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                false);
        inputLayoutData.horizontalSpan = 3;
        input.setLayoutData(inputLayoutData);
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

    private void hookWidget(Widget widget, int eventType) {
        widget.addListener(eventType, eventHandler);
    }

    private Composite createSection(Composite parent, String title) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createSectionTitle(composite, title);

        Composite container = new Composite(composite, SWT.NONE);
        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 5;
        layout2.marginLeft = 5;
        container.setLayout(layout2);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        return container;
    }

    private void createSectionTitle(Composite parent, String title) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label titleLabel = new Label(composite, SWT.NONE);
        titleLabel.setText(title);
        titleLabel.setFont(FontUtils.getBold(JFaceResources.DEFAULT_FONT));
        titleLabel.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    private void createPreviewPart(Composite parent) {
        Composite container = createPreviewContainer(parent);
        Composite blackContainer = createBlackContainer(container);
        Composite page = createPage(blackContainer);

        previewViewer = new MindMapViewer();
        Properties properties = previewViewer.getProperties();
        properties.set(IMindMapViewer.VIEWER_CONSTRAINED, Boolean.TRUE);
        properties.set(IMindMapViewer.VIEWER_MARGIN, Integer.valueOf(20));
        properties.set(IMindMapViewer.VIEWER_GRADIENT, Boolean.FALSE);
        previewViewer.createControl(page);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        previewViewer.getControl().setLayoutData(layoutData);
        previewViewer.getZoomManager().setConstraints(0, Double.MAX_VALUE);

        previewViewer.getCanvas().setScrollBarVisibility(FigureCanvas.NEVER);
        previewViewer.setInput(getSourceMindMap());

//        CenterPresercationService centerPresercationService = new CenterPresercationService(
//                previewViewer);
//        previewViewer.installService(CenterPresercationService.class,
//                centerPresercationService);
//        centerPresercationService.setActive(true);

        Layer feedback = previewViewer.getLayer(GEF.LAYER_FEEDBACK);
        if (feedback != null) {
            previewFigure = new Figure();
            previewFigure.setLayoutManager(new PreviewLayout());
            feedback.add(previewFigure);

            borderFigure = new Figure();
            borderFigure.setBorder(new LineBorder(
                    parent.getDisplay().getSystemColor(SWT.COLOR_BLACK), 1));
            previewFigure.add(borderFigure);

            headerFigure = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
            previewFigure.add(headerFigure);

            footerFigure = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
            previewFigure.add(footerFigure);

            ((IGraphicalPart) previewViewer.getRootPart()).getFigure()
                    .addLayoutListener(new LayoutListener.Stub() {
                        public boolean layout(IFigure container) {
                            layoutPreviewFigure();
                            return super.layout(container);
                        }
                    });
        }

        Label forReferenceLabel = new Label(container, SWT.LEAD);
        forReferenceLabel
                .setText(DialogMessages.PageSetupDialog_JustForReference);
        forReferenceLabel.setFont(FontUtils.getNewHeight(
                JFaceResources.DEFAULT_FONT, Util.isMac() ? 9 : 7));
        forReferenceLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    private void layoutPreviewFigure() {
        if (previewViewer == null || previewViewer.getControl().isDisposed()
                || previewFigure == null)
            return;

        FigureCanvas canvas = previewViewer.getCanvas();
        org.eclipse.swt.graphics.Rectangle maxSize = canvas.getClientArea();
        previewFigure.setBounds(new Rectangle(maxSize));
    }

    private Composite createPage(Composite parent) {
        previewPage = new Composite(parent, SWT.NO_BACKGROUND);
        pageLayout = new GridLayout(1, false);
        pageLayout.marginWidth = 0;
        pageLayout.marginHeight = 0;
        previewPage.setLayout(pageLayout);
        GridData layoutData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        layoutData.widthHint = VIEWER_WIDTH;
        layoutData.heightHint = VIEWER_HEIGHT;
        previewPage.setLayoutData(layoutData);
        previewPage.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
                org.eclipse.swt.graphics.Rectangle r = ((Control) e.widget)
                        .getBounds();
                e.gc.fillRectangle(0, 0, r.width, r.height);
            }
        });
        return previewPage;
    }

    private Composite createBlackContainer(Composite parent) {
        Composite container = new Composite(parent, SWT.NO_BACKGROUND);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 7;
        layout.marginHeight = 7;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLACK));
                org.eclipse.swt.graphics.Rectangle r = ((Control) e.widget)
                        .getBounds();
                e.gc.fillRectangle(0, 0, r.width, r.height);
            }
        });
        return container;
    }

    private Composite createPreviewContainer(Composite parent) {
        Composite container = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return container;
    }

    private void setProperty(IViewer viewer, String key, String value) {
        getSettings().put(key, value);
        update(key);
        if (viewer != null) {
            Properties properties = viewer.getProperties();
            properties.set(key, Entries.getPropertiesValue(value));
        }
    }

    protected void setProperty(String key, String value) {
        getSettings().put(key, value);
        update(key);
    }

    protected void setProperty(String key, int value) {
        getSettings().put(key, value);
        update(key);
    }

    protected void setProperty(String key, double value) {
        getSettings().put(key, value);
        update(key);
    }

    protected void setProperty(String key, boolean value) {
        getSettings().put(key, value);
        update(key);
    }

    private void update(String key) {
        if (updating)
            return;

        boolean backgroundChanged = key == null
                || PrintConstants.NO_BACKGROUND.equals(key);
        boolean borderChanged = key == null
                || PrintConstants.NO_BORDER.equals(key);
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
        boolean orientationChanged = key == null
                || PrintConstants.ORIENTATION.equals(key);
        boolean contentChanged = key == null
                || PrintConstants.CONTENTWHOLE.equals(key);
        boolean showPlusMinusIconsChanged = key == null
                || PrintConstants.PLUS_MINUS_VISIBILITY.equals(key);

        updating = true;

        if (showPlusMinusIconsChanged) {
            if (key == null) {
                String plusMinusVisibility = getSettings()
                        .get(PrintConstants.PLUS_MINUS_VISIBILITY);
                if (plusMinusVisibility == null
                        || "".equals(plusMinusVisibility)) { //$NON-NLS-1$
                    getSettings().put(PrintConstants.PLUS_MINUS_VISIBILITY,
                            PrintConstants.DEFAULT_PLUS_MINUS_VISIBILITY_VALUE);
                }
            }
            String plusMinusVisibility = getSettings()
                    .get(PrintConstants.PLUS_MINUS_VISIBILITY);
            hideBothRadio.setSelection(PrintConstants.PLUS_MINUS_HIDDEN
                    .equals(plusMinusVisibility));
            showBothRadio.setSelection(PrintConstants.PLUS_MINUS_VISIBLE
                    .equals(plusMinusVisibility));
            hideMinusShowPlusRadio
                    .setSelection(PrintConstants.PLUS_VISIBLE_MINUS_HIDDEN
                            .equals(plusMinusVisibility));
        }

        if (contentChanged) {
            currentMapRadio
                    .setSelection(!getBoolean(PrintConstants.CONTENTWHOLE));
            wholeWorkbookRadio
                    .setSelection(getBoolean(PrintConstants.CONTENTWHOLE));
        }

        if (backgroundChanged) {
            boolean showBackground = !getBoolean(PrintConstants.NO_BACKGROUND);
            if (previewViewer != null
                    && !previewViewer.getControl().isDisposed()) {
                Layer layer = previewViewer.getLayer(GEF.LAYER_BACKGROUND);
                if (layer != null) {
                    layer.setOpaque(showBackground);
                }
            }
            if (backgroundCheck != null && !backgroundCheck.isDisposed()) {
                backgroundCheck.setSelection(showBackground);
            }
        }

        if (borderChanged) {
            boolean showBorder = !getBoolean(PrintConstants.NO_BORDER);
            if (borderFigure != null) {
                borderFigure.setVisible(showBorder);
            }
            if (borderCheck != null && !borderCheck.isDisposed()) {
                borderCheck.setSelection(showBorder);
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

        if (unitChanged) {
            if (unitChooser != null && !unitChooser.isDisposed()) {
                int index = PrintConstants.UNITS.indexOf(getString(
                        PrintConstants.MARGIN_UNIT, PrintConstants.INCH));
                if (index < 0 || index >= unitChooser.getItemCount())
                    index = 0;
                unitChooser.select(index);
            }
        }

        if (headerChanged) {
            updateHFSectionAndPreview(PrintConstants.HEADER_TEXT,
                    PrintConstants.DEFAULT_HEADER_TEXT,
                    PrintConstants.HEADER_ALIGN,
                    PrintConstants.DEFAULT_HEADER_ALIGN,
                    PositionConstants.CENTER, PrintConstants.HEADER_FONT,
                    headerFigure);
        }

        if (footerChanged) {
            updateHFSectionAndPreview(PrintConstants.FOOTER_TEXT,
                    PrintConstants.DEFAULT_FOOTER_TEXT,
                    PrintConstants.FOOTER_ALIGN,
                    PrintConstants.DEFAULT_FOOTER_ALIGN,
                    PositionConstants.RIGHT, PrintConstants.FOOTER_FONT,
                    footerFigure);
        }

        if (orientationChanged) {
            updateOrientation();
        }

        updating = false;
    }

    private void updateHFSectionAndPreview(String textKey, String defaultText,
            String alignKey, String defaultAlign, int defaultDraw2DAlign,
            String fontKey, ITextFigure textFigure) {
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

        if (textFigure != null) {
            textFigure.setText(text);
            textFigure.setTextAlignment(PrintConstants
                    .toDraw2DAlignment(alignValue, defaultDraw2DAlign));
            String fontValue = getString(fontKey, null);
            Font font = null;
            if (fontValue != null) {
                font = FontUtils.getFont(fontValue);
            }
            if (font == null) {
                font = Display.getCurrent().getSystemFont();
            }
            textFigure.setFont(font);
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
        if (pageLayout != null && previewPage != null
                && !previewPage.isDisposed()) {
            for (String key : keys) {
                int margin = getControlMargin(key);
                if (PrintConstants.LEFT_MARGIN.equals(key)) {
                    pageLayout.marginLeft = margin;
                } else if (PrintConstants.RIGHT_MARGIN.equals(key)) {
                    pageLayout.marginRight = margin;
                } else if (PrintConstants.TOP_MARGIN.equals(key)) {
                    pageLayout.marginTop = margin;
                } else /* if (PrintConstants.BOTTOM_MARKER.equals(key) */ {
                    pageLayout.marginBottom = margin;
                }
            }
            previewPage.layout();
        }
    }

    private void updateOrientation() {
        int orientation = getInteger(PrintConstants.ORIENTATION,
                PrinterData.LANDSCAPE);
        boolean landscape = orientation != PrinterData.PORTRAIT;
        landscapeRadio.setSelection(landscape);
        portraitRadio.setSelection(!landscape);

        GridData layoutData = (GridData) previewPage.getLayoutData();
        if (orientation == PrinterData.LANDSCAPE) {
            layoutData.widthHint = VIEWER_WIDTH;
            layoutData.heightHint = VIEWER_HEIGHT;
        } else {
            layoutData.widthHint = PORTRAIT_VIEWER_WIDTH;
            layoutData.heightHint = PORTRAIT_VIEWER_HEIGHT;
        }
        previewPage.getParent().layout();
//        System.out.println("Landscape: " + landscape);
    }

    private int getControlMargin(String key) {
        double value = getDouble(key, PrintConstants.DEFAULT_MARGIN);
        if (PrintConstants.LEFT_MARGIN.equals(key)
                || PrintConstants.RIGHT_MARGIN.equals(key)) {
            value *= UnitConvertor.getScreenDpi().x;
        } else {
            value *= UnitConvertor.getScreenDpi().y;
        }
        return (int) (value / 2);
    }

    private String getMarginText(String key) {
        double value = getDouble(key, PrintConstants.DEFAULT_MARGIN);
        if (PrintConstants.MILLIMETER.equals(
                getString(PrintConstants.MARGIN_UNIT, PrintConstants.INCH))) {
            value = UnitConvertor.inch2mm(value);
        }
        return String.valueOf(value);
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

    private void handleWidgetEvent(Event event) {
        if (event.widget == currentMapRadio) {
            setProperty(PrintConstants.CONTENTWHOLE,
                    !currentMapRadio.getSelection());
        } else if (event.widget == wholeWorkbookRadio) {
            setProperty(PrintConstants.CONTENTWHOLE,
                    wholeWorkbookRadio.getSelection());
        } else if (event.widget == backgroundCheck) {
            setProperty(PrintConstants.NO_BACKGROUND,
                    !backgroundCheck.getSelection());
        } else if (event.widget == borderCheck) {
            setProperty(PrintConstants.NO_BORDER, !borderCheck.getSelection());
        } else if (event.widget == hideBothRadio) {
            setProperty(previewViewer, PrintConstants.PLUS_MINUS_VISIBILITY,
                    PrintConstants.PLUS_MINUS_HIDDEN);
        } else if (event.widget == showBothRadio) {
            setProperty(previewViewer, PrintConstants.PLUS_MINUS_VISIBILITY,
                    PrintConstants.PLUS_MINUS_VISIBLE);
        } else if (event.widget == hideMinusShowPlusRadio) {
            setProperty(previewViewer, PrintConstants.PLUS_MINUS_VISIBILITY,
                    PrintConstants.PLUS_VISIBLE_MINUS_HIDDEN);
        } else if (event.widget == unitChooser) {
            int index = unitChooser.getSelectionIndex();
            if (index < 0 || index >= PrintConstants.UNITS.size())
                index = 0;
            setProperty(PrintConstants.MARGIN_UNIT,
                    PrintConstants.UNITS.get(index));
        } else if (event.widget == landscapeRadio
                || event.widget == portraitRadio) {
            setProperty(PrintConstants.ORIENTATION,
                    ((Integer) event.widget.getData()).intValue());
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
                    setProperty(key, input.getText());
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

    public int getStep() {
        if (PrintConstants.MILLIMETER.equals(
                getString(PrintConstants.MARGIN_UNIT, PrintConstants.INCH)))
            return 500;
        return 100;
    }

    /**
     * Multiply the given number by 1000, and then split the result into integer
     * part and decimal part.
     * 
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
     * 
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
     * 
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
     * 
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

    private void setMargin(String key, double value) {
        if (PrintConstants.MILLIMETER.equals(
                getString(PrintConstants.MARGIN_UNIT, PrintConstants.INCH))) {
            value = UnitConvertor.mm2inch(value);
        }
        setProperty(key, value);
    }

}