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
package org.xmind.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;
import org.xmind.core.Core;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.net.util.LinkUtils;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.AdvancedToolbarLayout;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.GraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.FramePart;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.mindmap.ViewerModel;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IViewerModel;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.resources.ImageUtils;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.MarkerImageDescriptor;
import org.xmind.ui.util.MindMapUtils;

/**
 * Export wizard page for exporting documents with mixed text and graphics.
 * 
 * @author MANGOSOFT
 */
public abstract class DocumentExportPageBase extends AbstractMindMapExportPage {

    private class SheetPreviewPart extends GraphicalEditPart {

        public SheetPreviewPart(ISheet sheet) {
            setModel(sheet);
        }

        protected IFigure createFigure() {
            return new Figure();
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            figure.setBorder(new MarginBorder(10));
            figure.setPreferredSize(new Dimension(240, 310));
        }

        protected LayoutManager createLayoutManager() {
            AdvancedToolbarLayout layout = new AdvancedToolbarLayout(false);
            layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_TOPLEFT);
            layout.setStretchMinorAxis(true);
            layout.setSpacing(2);
            return layout;
        }

        public ISheet getSheet() {
            return (ISheet) super.getModel();
        }

        protected void installModelListeners() {
            super.installModelListeners();
            addPropertyListener(ExportContants.INCLUDE_OVERVIEW, this);
            addPropertyListener(ExportContants.SEPARATE_OVERVIEW, this);
            addPropertyListener(ExportContants.INCLUDE_MARKERS, this);
            addPropertyListener(ExportContants.INCLUDE_LABELS, this);
            addPropertyListener(ExportContants.INCLUDE_IMAGE, this);
            addPropertyListener(ExportContants.INCLUDE_NOTES, this);
            addPropertyListener(ExportContants.INCLUDE_RELATIONSHIPS, this);
            addPropertyListener(ExportContants.INCLUDE_FLOATING_TOPICS, this);
            addPropertyListener(ExportContants.INCLUDE_SUMMARIES, this);
        }

        protected void uninstallModelListeners() {
            removePropertyListener(ExportContants.INCLUDE_MARKERS, this);
            removePropertyListener(ExportContants.INCLUDE_LABELS, this);
            removePropertyListener(ExportContants.INCLUDE_OVERVIEW, this);
            removePropertyListener(ExportContants.SEPARATE_OVERVIEW, this);
            removePropertyListener(ExportContants.INCLUDE_IMAGE, this);
            removePropertyListener(ExportContants.INCLUDE_NOTES, this);
            removePropertyListener(ExportContants.INCLUDE_RELATIONSHIPS, this);
            removePropertyListener(ExportContants.INCLUDE_FLOATING_TOPICS,
                    this);
            removePropertyListener(ExportContants.INCLUDE_SUMMARIES, this);
            super.uninstallModelListeners();
        }

        protected Object[] getModelChildren(Object model) {
            List<Object> list = new ArrayList<Object>();
            ISheet sheet = getSheet();
            ITopic rootTopic = sheet.getRootTopic();

            list.add(new ViewerModel(TopicTitlePreviewPart.class, rootTopic));

            boolean includeOverview = getBoolean(
                    ExportContants.INCLUDE_OVERVIEW);
            if (includeOverview) {
                list.add(new ViewerModel(OverviewPreviewPart.class, rootTopic));
            }

            List<ITopic> children = rootTopic.getChildren(ITopic.ATTACHED);
            ITopic mainTopic = children.get(0);
            list.add(new ViewerModel(HyperlinkPreviewPart.class, mainTopic));
            if (getBoolean(ExportContants.INCLUDE_MARKERS)
                    || getBoolean(ExportContants.INCLUDE_LABELS)) {
                list.add(new ViewerModel(TagsPreviewPart.class, mainTopic));
            }

            if (includeOverview
                    && getBoolean(ExportContants.SEPARATE_OVERVIEW)) {
                list.add(new ViewerModel(OverviewPreviewPart.class, mainTopic));
            }

            if (getBoolean(ExportContants.INCLUDE_IMAGE)) {
                list.add(new ViewerModel(ImagePreviewPart.class, mainTopic));
            }

            if (getBoolean(ExportContants.INCLUDE_NOTES)) {
                list.add(new ViewerModel(NotesPreviewPart.class, mainTopic));
            }

            if (getBoolean(ExportContants.INCLUDE_RELATIONSHIPS)) {
                list.add(new ViewerModel(RelationshipsPreviewPart.class,
                        mainTopic));
            }

            for (ITopic sub : mainTopic.getChildren(ITopic.ATTACHED)) {
                if (sub.getHyperlink() != null)
                    list.add(new ViewerModel(AttachmentPreviewPart.class, sub));
                else
                    list.add(new ViewerModel(TopicTitlePreviewPart.class, sub));
            }

            if (getBoolean(ExportContants.INCLUDE_SUMMARIES)) {
                for (ITopic t : mainTopic.getChildren(ITopic.SUMMARY)) {
                    list.add(new ViewerModel(TopicTitlePreviewPart.class, t));
                }
            }

            if (getBoolean(ExportContants.INCLUDE_FLOATING_TOPICS)) {
                for (ITopic t : rootTopic.getChildren(ITopic.DETACHED)) {
                    list.add(new ViewerModel(TopicTitlePreviewPart.class, t));
                }
            }
            return list.toArray();
        }
    }

    protected class TopicTitlePreviewPart extends GraphicalEditPart {

        private RotatableWrapLabel number;

        private RotatableWrapLabel title;

        public TopicTitlePreviewPart() {
        }

        public ITopic getTopic() {
            return (ITopic) MindMapUtils.getRealModel(this);
        }

        protected IFigure createFigure() {
            return new Figure();
        }

        protected LayoutManager createLayoutManager() {
            AdvancedToolbarLayout layout = new AdvancedToolbarLayout(true);
            if (getTopic().isRoot()) {
                layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
            } else {
                layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_TOPLEFT);
            }
            layout.setMinorAlignment(AdvancedToolbarLayout.ALIGN_BOTTOMRIGHT);
            layout.setInnerMinorAlignment(
                    AdvancedToolbarLayout.ALIGN_BOTTOMRIGHT);
            layout.setSpacing(5);
            return layout;
        }

        public RotatableWrapLabel getNumber() {
            return number;
        }

        public RotatableWrapLabel getTitle() {
            return title;
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            ITopic topic = getTopic();
            int level = MindMapUtils.getLevel(getTopic(), null);
            level = Math.min(2, level);
            Font font;
            if (level <= 1) {
                font = FontUtils.getBoldRelative(JFaceResources.DEFAULT_FONT,
                        -level);
            } else {
                font = FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT,
                        -level);
            }

            String num = ExportUtils.getNumberingText(topic, null);
            if (num != null) {
                number = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
                number.setFont(font);
                number.setForegroundColor(ColorConstants.black);
                number.setText(num);
                figure.add(number);
            }

            title = new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
            title.setFont(font);
            title.setForegroundColor(ColorConstants.black);
            title.setText(topic.getTitleText());
            figure.add(title);
        }
    }

    protected class HyperlinkPreviewPart extends TopicTitlePreviewPart {

        public HyperlinkPreviewPart() {
        }

        protected void installModelListeners() {
            super.installModelListeners();
            addPropertyListener(ExportContants.INCLUDE_HYPERLINK, this);
        }

        protected void uninstallModelListeners() {
            removePropertyListener(ExportContants.INCLUDE_HYPERLINK, this);
            super.uninstallModelListeners();
        }

        protected void updateView() {
            super.updateView();
            RotatableWrapLabel label = getTitle();
            TextStyle style = new TextStyle();
            style.font = label.getFont();
            if (getBoolean(ExportContants.INCLUDE_HYPERLINK)) {
                style.foreground = ColorConstants.blue;
                style.underline = true;
            } else {
                style.foreground = ColorConstants.black;
                style.underline = false;
            }
            label.setStyle(style);
        }
    }

    protected class AttachmentPreviewPart extends TopicTitlePreviewPart {

        public AttachmentPreviewPart() {
        }

        protected void installModelListeners() {
            super.installModelListeners();
            addPropertyListener(ExportContants.INCLUDE_ATTACHMENT, this);
        }

        protected void uninstallModelListeners() {
            removePropertyListener(ExportContants.INCLUDE_ATTACHMENT, this);
            super.uninstallModelListeners();
        }

        protected void updateView() {
            super.updateView();
            RotatableWrapLabel label = getTitle();
            TextStyle style = new TextStyle();
            style.font = label.getFont();
            if (getBoolean(ExportContants.INCLUDE_ATTACHMENT)) {
                style.foreground = ColorConstants.blue;
                style.underline = true;
            } else {
                style.foreground = ColorConstants.black;
                style.underline = false;
            }
            label.setStyle(style);
        }
    }

    protected class OverviewPreviewPart extends GraphicalEditPart {

        private SizeableImageFigure imgFigure;

        public OverviewPreviewPart() {
        }

        public ITopic getTopic() {
            return (ITopic) MindMapUtils.getRealModel(this);
        }

        protected IFigure createFigure() {
            return new Figure();
        }

        protected LayoutManager createLayoutManager() {
            AdvancedToolbarLayout layout = new AdvancedToolbarLayout(true);
            return layout;
        }

        protected void installModelListeners() {
            super.installModelListeners();
            if (getTopic().isRoot()) {
                addPropertyListener(ExportContants.SEPARATE_OVERVIEW, this);
            }
        }

        protected void uninstallModelListeners() {
            if (getTopic().isRoot()) {
                removePropertyListener(ExportContants.SEPARATE_OVERVIEW, this);
            }
            super.uninstallModelListeners();
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            imgFigure = new SizeableImageFigure();
            figure.add(imgFigure);
        }

        protected void updateView() {
            super.updateView();
            if (getTopic().isRoot()) {
                if (getBoolean(ExportContants.SEPARATE_OVERVIEW)) {
                    imgFigure.setImage(getOverviewImage(OVERVIEW2));
                } else {
                    imgFigure.setImage(getOverviewImage(OVERVIEW1));
                }
            } else {
                imgFigure.setImage(getOverviewImage(OVERVIEW3));
            }
            imgFigure.setPreferredSize(imgFigure.getImageSize());
        }
    }

    protected class TagsPreviewPart extends GraphicalEditPart {

        public TagsPreviewPart() {
        }

        public ITopic getTopic() {
            return (ITopic) MindMapUtils.getRealModel(this);
        }

        protected IFigure createFigure() {
            return new Figure();
        }

        protected LayoutManager createLayoutManager() {
            AdvancedToolbarLayout layout = new AdvancedToolbarLayout(true);
            layout.setMinorAlignment(AdvancedToolbarLayout.ALIGN_BOTTOMRIGHT);
            layout.setInnerMinorAlignment(
                    AdvancedToolbarLayout.ALIGN_BOTTOMRIGHT);
            layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_TOPLEFT);
            layout.setSpacing(2);
            return layout;
        }

        protected void installModelListeners() {
            super.installModelListeners();
            addPropertyListener(ExportContants.INCLUDE_MARKERS, this);
            addPropertyListener(ExportContants.INCLUDE_LABELS, this);
        }

        protected void uninstallModelListeners() {
            removePropertyListener(ExportContants.INCLUDE_LABELS, this);
            removePropertyListener(ExportContants.INCLUDE_MARKERS, this);
            super.uninstallModelListeners();
        }

        protected Object[] getModelChildren(Object model) {
            ArrayList<Object> list = new ArrayList<Object>();
            ITopic topic = getTopic();

            if (getBoolean(ExportContants.INCLUDE_MARKERS)) {
                for (IMarkerRef mr : topic.getMarkerRefs()) {
                    list.add(new ViewerModel(MarkerPreviewPart.class, mr));
                }
            }

            if (getBoolean(ExportContants.INCLUDE_LABELS)) {
                if (!topic.getLabels().isEmpty()) {
                    list.add(new ViewerModel(LabelsPreviewPart.class, topic));
                }
            }
            return list.toArray();
        }
    }

    protected class MarkerPreviewPart extends GraphicalEditPart {

        public MarkerPreviewPart() {
        }

        protected IFigure createFigure() {
            return new SizeableImageFigure();
        }

        public IMarkerRef getMarkerRef() {
            return (IMarkerRef) MindMapUtils.getRealModel(this);
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            SizeableImageFigure imgFigure = (SizeableImageFigure) figure;
            IMarkerRef mr = getMarkerRef();
            imgFigure.setImage(getMarkerImage(mr.getMarkerId()));
            Dimension size = new Dimension(12, 12);
            imgFigure.setStretched(true);
            imgFigure.setPreferredSize(size);
            imgFigure.setSize(size);
        }

    }

    protected class LabelsPreviewPart extends GraphicalEditPart {

        public LabelsPreviewPart() {
        }

        protected IFigure createFigure() {
            return new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
        }

        public ITopic getTopic() {
            return (ITopic) MindMapUtils.getRealModel(this);
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            RotatableWrapLabel label = (RotatableWrapLabel) figure;
            label.setFont(
                    FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, 6));
            label.setForegroundColor(ColorConstants.darkGray);

            String text = MindMapUtils.getLabelText(getTopic().getLabels());
            label.setText(text);
        }

    }

    protected class ImagePreviewPart extends GraphicalEditPart {

        public ImagePreviewPart() {
        }

        protected IFigure createFigure() {
            return new Figure();
        }

        public ITopic getTopic() {
            return (ITopic) MindMapUtils.getRealModel(this);
        }

        protected LayoutManager createLayoutManager() {
            AdvancedToolbarLayout layout = new AdvancedToolbarLayout(true);
            layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_TOPLEFT);
            return layout;
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            SizeableImageFigure imgFigure = new SizeableImageFigure();
            figure.add(imgFigure);
            Image image = ImageUtils.getImage(MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_IMAGE, true));
            imgFigure.setImage(image);
            imgFigure.setPreferredSize(imgFigure.getImageSize());
        }

    }

    protected class NotesPreviewPart extends GraphicalEditPart {

        public NotesPreviewPart() {
        }

        protected IFigure createFigure() {
            return new RotatableWrapLabel(RotatableWrapLabel.NORMAL);
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            RotatableWrapLabel label = (RotatableWrapLabel) figure;
            label.setTextAlignment(PositionConstants.LEFT);
            label.setFont(
                    FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, 4));
            label.setForegroundColor(ColorConstants.darkGray);
            label.setText(getSampleNotes());
        }

    }

    protected class RelationshipsPreviewPart extends GraphicalEditPart {

        public RelationshipsPreviewPart() {
        }

        protected IFigure createFigure() {
            return new Figure();
        }

        protected LayoutManager createLayoutManager() {
            AdvancedToolbarLayout layout = new AdvancedToolbarLayout(true);
            layout.setMinorAlignment(AdvancedToolbarLayout.ALIGN_BOTTOMRIGHT);
            layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_TOPLEFT);
            layout.setSpacing(3);
            return layout;
        }

        protected void initFigure(IFigure figure) {
            super.initFigure(figure);
            RotatableWrapLabel seeAlsoLabel = new RotatableWrapLabel(
                    RotatableWrapLabel.NORMAL);
            seeAlsoLabel.setPrefWidth(300);
            seeAlsoLabel
                    .setFont(FontUtils.getBold(JFaceResources.DEFAULT_FONT, 6));
            seeAlsoLabel.setForegroundColor(ColorConstants.darkGray);
            seeAlsoLabel.setText(WizardMessages.Export_SeeAlso);

            RotatableWrapLabel relationshipsLabel = new RotatableWrapLabel(
                    RotatableWrapLabel.NORMAL);
            relationshipsLabel.setPrefWidth(250);
            relationshipsLabel.setFont(
                    FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, 6));
            relationshipsLabel.setForegroundColor(ColorConstants.darkGray);
            relationshipsLabel.setText(
                    WizardMessages.DocumentExportPage_Sample_Relationships);

            figure.add(seeAlsoLabel);
            figure.add(relationshipsLabel);
        }

    }

    protected class PreviewPartFactory implements IPartFactory {

        private IPartFactory factory;

        public PreviewPartFactory(IPartFactory factory) {
            this.factory = factory;
        }

        public IPart createPart(IPart context, Object model) {
            if (model instanceof ISheet && context instanceof FramePart) {
                return new SheetPreviewPart((ISheet) model);
            } else if (model instanceof IViewerModel) {
                IViewerModel m = (IViewerModel) model;
                try {
                    IPart part = (IPart) m.getPartType().getConstructors()[0]
                            .newInstance(DocumentExportPageBase.this);
                    part.setModel(model);
                    return part;
                } catch (Throwable e) {
                    Logger.log(e, "Failed to create export preview part: " //$NON-NLS-1$
                            + m.getPartType());
                }
            }
            return factory.createPart(context, model);
        }

    }

    private static final String PROPERTY_NAME = "PROPERTY_NAME"; //$NON-NLS-1$

    protected static final String OVERVIEW1 = "icons/misc/overview_preview.png"; //$NON-NLS-1$

    protected static final String OVERVIEW2 = "icons/misc/overview_preview2.png"; //$NON-NLS-1$

    protected static final String OVERVIEW3 = "icons/misc/overview_preview3.png"; //$NON-NLS-1$

    private Map<String, Widget> widgets = new HashMap<String, Widget>();

    private boolean handlingEvent = false;

    private Map<String, Image> images = null;

    private String sampleNotes = null;

    private Map<String, List<IPart>> propertyListeners = null;

    private GraphicalViewer previewViewer = null;

    private Button allCheck;

    private Set<String> propertyNames;

    private boolean handlingAllCheck = false;

    private boolean allCheckCreationEnded = false;

    public DocumentExportPageBase(String pageName, String title) {
        super(pageName, title);
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 15;
        composite.setLayout(layout);
        setControl(composite);

        Control settingsGroup = createSettingsGroup(composite);
        settingsGroup
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control fileGroup = createFileControls(composite);
        fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    protected Control createSettingsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(WizardMessages.DocumentExportPage_SettingsGroup_title);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 20;
        layout.marginWidth = 7;
        layout.marginHeight = 7;
        group.setLayout(layout);

        Composite widgetContainer = new Composite(group, SWT.NONE);
        widgetContainer
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout containerLayout = new GridLayout(1, false);
        containerLayout.marginHeight = 0;
        containerLayout.marginWidth = 0;
        containerLayout.verticalSpacing = 7;
        widgetContainer.setLayout(containerLayout);
        createPropertyWidgets(widgetContainer);

        Composite previewContainer = new Composite(group, SWT.NONE);
        GridData previewLayoutData = new GridData(SWT.END, SWT.FILL, false,
                true);
        previewLayoutData.widthHint = 260;
        previewLayoutData.heightHint = 330;

        previewContainer.setLayoutData(previewLayoutData);
        GridLayout previewLayout = new GridLayout(1, false);
        previewLayout.marginHeight = 0;
        previewLayout.marginWidth = 0;

        previewContainer.setLayout(previewLayout);
        createPreviewViewer(previewContainer);

        return group;
    }

    protected void createPreviewViewer(Composite parent) {
        previewViewer = new GalleryViewer();
        initPreviewViewer(previewViewer);
        previewViewer.createControl(parent);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        previewViewer.getControl().setLayoutData(gridData);
        previewViewer.getControl().setBackground(null);
        if (!"win32".equals(SWT.getPlatform())) //$NON-NLS-1$
            previewViewer.getCanvas().getLightweightSystem().getRootFigure()
                    .setOpaque(false);

        previewViewer.setInput(getPreviewViewerInput());
    }

    public GraphicalViewer getPreviewViewer() {
        return previewViewer;
    }

    protected Object getPreviewViewerInput() {
        return createSampleSheet();
    }

    protected void initPreviewViewer(IGraphicalViewer previewViewer) {
        previewViewer.setPartFactory(
                new PreviewPartFactory(previewViewer.getPartFactory()));
        Properties properties = previewViewer.getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, Boolean.TRUE);
        properties.set(GalleryViewer.HideTitle, Boolean.TRUE);
        properties.set(GalleryViewer.Layout, new GalleryLayout().margins(0));
    }

    protected void createPropertyWidgets(Composite parent) {
        createBooleanWidget(parent, ExportContants.INCLUDE_OVERVIEW,
                WizardMessages.DocumentExportPage_IncludeOverview);

        Widget widget = createBooleanWidget(parent,
                ExportContants.SEPARATE_OVERVIEW,
                WizardMessages.DocumentExportPage_SeparateOverviews);
        indentWidget(widget, 16);
        setWidgetEnabled(widget, getBoolean(ExportContants.INCLUDE_OVERVIEW));

        createAdditionalWidgets(parent);
    }

    protected void createAdditionalWidgets(Composite parent) {
        createAllCheck(parent);
        createBooleanWidget(parent, ExportContants.INCLUDE_HYPERLINK);
        createBooleanWidget(parent, ExportContants.INCLUDE_ATTACHMENT);
        createBooleanWidget(parent, ExportContants.INCLUDE_MARKERS);
        createBooleanWidget(parent, ExportContants.INCLUDE_LABELS);
        createBooleanWidget(parent, ExportContants.INCLUDE_IMAGE);
        createBooleanWidget(parent, ExportContants.INCLUDE_NOTES);
        createBooleanWidget(parent, ExportContants.INCLUDE_RELATIONSHIPS);
        createBooleanWidget(parent, ExportContants.INCLUDE_SUMMARIES);
        createBooleanWidget(parent, ExportContants.INCLUDE_FLOATING_TOPICS);
        endAllCheckCreation();
    }

    protected Widget createBooleanWidget(Composite parent,
            String propertyName) {
        return createBooleanWidget(parent, propertyName,
                getDefaultLabel(propertyName));
    }

    protected Widget createBooleanWidget(Composite parent, String propertyName,
            String text) {
        Button widget = new Button(parent, SWT.CHECK);
        widget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        widget.setText(text);
        registerPropertyWidget(propertyName, widget);
        widget.setSelection(getBoolean(propertyName));
        hookWidget(widget, SWT.Selection);
        return widget;
    }

    protected void registerPropertyWidget(String propertyName, Widget widget) {
        widget.setData(PROPERTY_NAME, propertyName);
        widgets.put(propertyName, widget);
        if (propertyNames != null && !allCheckCreationEnded) {
            propertyNames.add(propertyName);
            indentWidget(widget, 16);
            updateAllCheck();
        }
    }

    protected void indentWidget(Widget widget, int indent) {
        if (widget instanceof Control) {
            Object layoutData = ((Control) widget).getLayoutData();
            if (layoutData instanceof GridData) {
                ((GridData) layoutData).horizontalIndent = indent;
            }
        }
    }

    protected void setWidgetEnabled(Widget widget, boolean enabled) {
        if (widget instanceof Control) {
            ((Control) widget).setEnabled(enabled);
        }
    }

    protected void handleWidgetEvent(Event event) {
        if (event.widget == allCheck) {
            handlingAllCheck = true;
            boolean selection = allCheck.getSelection();
            setAllBooleanProperties(selection);
            handlingAllCheck = false;
            allCheck.setGrayed(!selection);
        } else {
            Object propertyName = event.widget.getData(PROPERTY_NAME);
            if (propertyName instanceof String) {
                if (event.widget instanceof Button) {
                    handlingEvent = true;
                    setValue((String) propertyName,
                            ((Button) event.widget).getSelection());
                    handlingEvent = false;
                }
            } else {
                super.handleWidgetEvent(event);
            }
        }
    }

    protected Widget getWidget(String propertyName) {
        return widgets.get(propertyName);
    }

    protected void setValue(String propertyName, boolean value) {
        IDialogSettings dialogSettings = getWizard().getDialogSettings();
        if (dialogSettings != null) {
            dialogSettings.put(propertyName, value);
            firePropertyChanged(propertyName);
        }
        if (!handlingEvent) {
            setValueToWidget(propertyName, value);
        }
        if (ExportContants.INCLUDE_OVERVIEW.equals(propertyName)) {
            Widget widget2 = getWidget(ExportContants.SEPARATE_OVERVIEW);
            if (widget2 instanceof Control) {
                ((Control) widget2).setEnabled(value);
            }
        }
        if (!handlingAllCheck) {
            if (propertyNames != null && propertyNames.contains(propertyName)) {
                updateAllCheck();
            }
        }
    }

    protected void setValueToWidget(String propertyName, boolean value) {
        Widget widget = getWidget(propertyName);
        if (widget instanceof Button) {
            ((Button) widget).setSelection(value);
        }
    }

    protected boolean getBoolean(String propertyName) {
        IDialogSettings dialogSettings = getWizard().getDialogSettings();
        if (dialogSettings != null)
            return dialogSettings.getBoolean(propertyName);
        return false;
    }

    protected void createAllCheck(Composite parent) {
        allCheck = new Button(parent, SWT.CHECK);
        allCheck.setText(WizardMessages.DocumentExportPage_AllContents_text);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        layoutData.verticalIndent = 8;
        allCheck.setLayoutData(layoutData);
        hookWidget(allCheck, SWT.Selection);
        propertyNames = new HashSet<String>();
    }

    private Set<String> getCurrentSelection() {
        if (propertyNames == null)
            return null;
        Set<String> selected = null;
        for (String propertyName : propertyNames) {
            if (getBoolean(propertyName)) {
                if (selected == null)
                    selected = new HashSet<String>();
                selected.add(propertyName);
            }
        }
        return selected;
    }

    private void setAllBooleanProperties(boolean value) {
        if (propertyNames == null)
            return;

        for (String propertyName : propertyNames) {
            setValue(propertyName, value);
        }
    }

    protected void updateAllCheck() {
        if (allCheck == null || allCheck.isDisposed())
            return;

        Set<String> selection = getCurrentSelection();
        allCheck.setSelection(selection != null && !selection.isEmpty());
        allCheck.setGrayed(!isAllSelected(selection));
    }

    private boolean isAllSelected(Set<String> selection) {
        return selection == propertyNames
                || (selection != null && selection.equals(propertyNames));
    }

    protected void endAllCheckCreation() {
        allCheckCreationEnded = true;
    }

    public void dispose() {
        widgets.clear();
        super.dispose();
        if (images != null) {
            for (Image image : images.values()) {
                image.dispose();
            }
            images = null;
        }
        sampleNotes = null;
        propertyListeners = null;
        propertyNames = null;
        allCheck = null;
        allCheckCreationEnded = false;

    }

    protected Image getOverviewImage(String path) {
        if (images == null)
            images = new HashMap<String, Image>();
        Image image = images.get(path);
        if (image == null && !images.containsKey(path)) {
            ImageDescriptor id = MindMapUI.getImages().get(path);
            if (id != null) {
                image = id.createImage(false);
                images.put(path, image);
            }
        }
        return image;
    }

    protected Image getMarkerImage(String markerId) {
        if (images == null)
            images = new HashMap<String, Image>();
        Image image = images.get(markerId);
        if (image == null && !images.containsKey(markerId)) {
            IMarker marker = MindMapUI.getResourceManager()
                    .getSystemMarkerSheet().findMarker(markerId);
            if (marker != null) {
                ImageDescriptor id = MarkerImageDescriptor
                        .createFromMarker(marker);
                image = id.createImage(false);
                images.put(markerId, image);
            }
        }
        return image;
    }

    protected String getSampleNotes() {
        if (sampleNotes == null) {
            sampleNotes = createSampleNotes();
        }
        return sampleNotes;
    }

    protected String createSampleNotes() {
        StringBuilder sb = new StringBuilder(200);
        for (int i = 0; i < 190; i++) {
            sb.append('x');
            sb.append(' ');
        }
        return sb.toString();
    }

    protected void addPropertyListener(String propertyName, IPart listener) {
        if (propertyListeners == null)
            propertyListeners = new HashMap<String, List<IPart>>();
        List<IPart> list = propertyListeners.get(propertyName);
        if (list == null) {
            list = new ArrayList<IPart>();
            propertyListeners.put(propertyName, list);
        }
        list.add(listener);
    }

    protected void removePropertyListener(String propertyName, IPart listener) {
        if (propertyListeners == null)
            return;
        List<IPart> list = propertyListeners.get(propertyName);
        if (list == null)
            return;
        list.remove(listener);
        if (list.isEmpty()) {
            propertyListeners.remove(propertyName);
            if (propertyListeners.isEmpty())
                propertyListeners = null;
        }
    }

    private void firePropertyChanged(String propertyName) {
        if (propertyListeners == null)
            return;

        List<IPart> list = propertyListeners.get(propertyName);
        if (list == null)
            return;

        for (Object o : list.toArray()) {
            ((IPart) o).refresh();
        }
    }

    protected ISheet createSampleSheet() {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
        ISheet sheet = workbook.getPrimarySheet();
        ITopic rootTopic = sheet.getRootTopic();
        rootTopic.setTitleText(
                WizardMessages.DocumentExportPage_Sample_CentralTopic);

        ITopic mainTopic = workbook.createTopic();
        rootTopic.add(mainTopic);
        mainTopic.setTitleText(
                WizardMessages.DocumentExportPage_Sample_MainTopic1);
        mainTopic.addMarker("priority-1"); //$NON-NLS-1$
        mainTopic.addMarker("smiley-smile"); //$NON-NLS-1$
        mainTopic.setLabels(
                Arrays.asList(WizardMessages.DocumentExportPage_Sample_Label1,
                        WizardMessages.DocumentExportPage_Sample_Label2));
        mainTopic.setHyperlink(LinkUtils.getHostByLanguage(true, true));
        mainTopic.getImage().setSource("temp.png"); //$NON-NLS-1$
        INotesContent notesContent = workbook.createNotesContent(INotes.PLAIN);
        ((IPlainNotesContent) notesContent).setTextContent(getSampleNotes());
        mainTopic.getNotes().setContent(INotes.PLAIN, notesContent);

        ITopic sub1 = workbook.createTopic();
        sub1.setTitleText(WizardMessages.DocumentExportPage_Sample_Subtopic1);
        mainTopic.add(sub1);

        ITopic sub2 = workbook.createTopic();
        sub2.setTitleText(WizardMessages.DocumentExportPage_Sample_Attachment);
        sub2.setHyperlink("xap:attachments/foo.bar"); //$NON-NLS-1$
        mainTopic.add(sub2);

        ITopic summary1 = workbook.createTopic();
        summary1.setTitleText(
                WizardMessages.DocumentExportPage_Sample_Summary1);
        mainTopic.add(summary1, ITopic.SUMMARY);

        ITopic summary2 = workbook.createTopic();
        summary2.setTitleText(
                WizardMessages.DocumentExportPage_Sample_Summary2);
        mainTopic.add(summary2, ITopic.SUMMARY);

        ITopic floating1 = workbook.createTopic();
        floating1.setTitleText(
                WizardMessages.DocumentExportPage_Sample_FloatingTopic1);
        rootTopic.add(floating1, ITopic.DETACHED);

        ITopic floating2 = workbook.createTopic();
        floating2.setTitleText(
                WizardMessages.DocumentExportPage_Sample_FloatingTopic2);
        rootTopic.add(floating2, ITopic.DETACHED);
        return sheet;
    }

    private static final String getDefaultLabel(String propertyName) {
        if (ExportContants.INCLUDE_MARKERS.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeMarkers;
        if (ExportContants.INCLUDE_IMAGE.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeImages;
        if (ExportContants.INCLUDE_NOTES.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeNotes;
        if (ExportContants.INCLUDE_LABELS.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeLabels;
        if (ExportContants.INCLUDE_RELATIONSHIPS.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeRelationships;
        if (ExportContants.INCLUDE_SUMMARIES.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeSummaries;
        if (ExportContants.INCLUDE_FLOATING_TOPICS.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeFloatingTopics;
        if (ExportContants.INCLUDE_HYPERLINK.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeHyperlinks;
        if (ExportContants.INCLUDE_ATTACHMENT.equals(propertyName))
            return WizardMessages.DocumentExportPage_IncludeAttachments;
        return ""; //$NON-NLS-1$
    }
}
