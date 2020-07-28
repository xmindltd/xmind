package org.xmind.ui.internal.mindmap;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.ui.internal.decorators.InfoItemIconDecorator;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGImageFigure;
import org.xmind.ui.internal.svgsupport.SVGReference;
import org.xmind.ui.mindmap.IInfoItemPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.resources.ImageReference;

public class InfoItemIconPart extends MindMapPartBase
        implements IInfoItemPart, IPropertyChangeListener {

    private IAction action;

    private IMenuManager menu;

    private String actionId;

    private ImageReference imageRef = null;

    private SVGReference svgRef = null;

    private ResourceManager resources;

    public InfoItemIconPart() {
        setDecorator(InfoItemIconDecorator.getInstance());
    }

    @Override
    protected IFigure createFigure() {
        if (svgRef != null) {
            SVGImageFigure figure = new SVGImageFigure();
            figure.setManager(resources);
            return figure;
        } else
            return new SizeableImageFigure();
    }

    public IAction getAction() {
        return action;
    }

    public Image getImage() {
        if (imageRef != null && !imageRef.isDisposed())
            return imageRef.getImage();
        return null;
    }

    public SVGImageData getSVGData() {
        return svgRef == null ? null : svgRef.getSVGData();
    }

    public IMenuManager getPopupMenu() {
        InfoPart infoPart = (InfoPart) getParent();
        ITopicPart topicPart = infoPart.getTopicPart();
        ITopic topic = infoPart.getTopic();
        InfoItemIcon infoItem = (InfoItemIcon) InfoItemIconPart.this.getModel();
        final List<IAction> actions = infoItem.getContributor()
                .getPopupMenuActions(topicPart, topic);
        if (actions == null) {
            return null;
        } else {
            if (menu == null) {
                menu = new MenuManager();
                menu.setRemoveAllWhenShown(true);
                menu.addMenuListener(new IMenuListener() {
                    public void menuAboutToShow(IMenuManager manager) {
                        for (IAction action : actions) {
                            menu.add(action);
                        }
                    }
                });
            }
        }
        return menu;
    }

    public ITopic getTopic() {
        return (ITopic) super.getRealModel();
    }

    public IInfoPart getInforPart() {
        if (getParent() instanceof IInfoPart)
            return (IInfoPart) getParent();
        return null;
    }

    public ITopicPart getTopicPart() {
        if (getInforPart() != null)
            return getInforPart().getTopicPart();
        return null;
    }

    @Override
    public void setParent(IPart parent) {
        if (getParent() instanceof InfoPart)
            ((InfoPart) getParent()).removeInfoItemIcon(this);
        super.setParent(parent);
        if (getParent() instanceof InfoPart)
            ((InfoPart) getParent()).addInfoItemIcon(this);
    }

    @Override
    public void setModel(Object model) {
        super.setModel(model);

        if (svgRef == null) {
            if (model instanceof InfoItemIcon) {
                String svgFilePath = ((InfoItemIcon) model).getContributor()
                        .getSVGFilePath(getTopic(), action);
                if (svgFilePath != null && !"".equals(svgFilePath)) //$NON-NLS-1$
                    svgRef = createSVGReference(svgFilePath);
            }
        }

    }

    @Override
    protected void register() {
        super.register();
        if (getModel() instanceof InfoItemIcon)
            setAction(((InfoItemIcon) getModel()).getAction());
    }

    @Override
    protected void unregister() {
        setAction(null);
        super.unregister();
    }

    private void setAction(IAction action) {
        if (action == this.action)
            return;

        if (this.action != null)
            this.action.removePropertyChangeListener(this);
        if (actionId != null)
            unregisterAction(actionId, this.action);

        this.action = action;

        actionId = action == null ? null : action.getId();
        if (actionId != null)
            registerAction(action);
        if (action != null)
            action.addPropertyChangeListener(this);
        updateImage();
    }

    private void unregisterAction(String actionId, IAction action) {
        IInfoPart branchPart = getInforPart();
        if (branchPart == null)
            return;

        IActionRegistry actionRegistry = (IActionRegistry) branchPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry == null)
            return;

        if (actionRegistry.getAction(actionId) == action) {
            actionRegistry.removeAction(actionId);
        }
    }

    private void registerAction(IAction action) {
        IInfoPart infoPart = getInforPart();
        if (infoPart == null)
            return;

        IActionRegistry actionRegistry = (IActionRegistry) infoPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry == null)
            return;

        actionRegistry.addAction(action);
    }

    private void updateImage() {
        if (svgRef == null) {
            ImageDescriptor oldImageDescriptor = imageRef == null ? null
                    : imageRef.getImageDescriptor();
            ImageDescriptor newImageDescriptor = null;
            if (action != null)
                newImageDescriptor = action.isEnabled()
                        ? action.getImageDescriptor()
                        : action.getDisabledImageDescriptor();
            if (oldImageDescriptor != newImageDescriptor
                    && (oldImageDescriptor == null || !oldImageDescriptor
                            .equals(newImageDescriptor))) {
                if (imageRef != null) {
                    imageRef.dispose();
                }
                imageRef = newImageDescriptor == null ? null
                        : new ImageReference(newImageDescriptor, false);
            }
        } else {
            Object model = getModel();
            if (model != null && model instanceof InfoItemIcon) {
                String filePath = ((InfoItemIcon) model).getContributor()
                        .getSVGFilePath(getTopic(), action);
                if (filePath == null || "".equals(filePath)) { //$NON-NLS-1$
                    svgRef = null;
                    getParent().refresh();
                }
            }
        }
    }

    @Override
    protected void onActivated() {
        resources = new LocalResourceManager(JFaceResources.getResources());
        super.onActivated();
    }

    @Override
    protected void onDeactivated() {
        if (imageRef != null) {
            imageRef.dispose();
            imageRef = null;
        }
        if (svgRef != null) {
            svgRef = null;
            resources.dispose();
        }
        super.onDeactivated();
    }

    @Override
    protected void updateView() {
        super.updateView();
        updateToolTip();
        updateImage();
    }

    protected IFigure createToolTip() {
        if (action != null) {
            String text = action.getText();
            String tooltip = action.getToolTipText();
            if (text != null || tooltip != null) {
                IFigure fig = new Figure();

                fig.setBorder(new MarginBorder(1, 3, 1, 3));

                ToolbarLayout layout = new ToolbarLayout(false);
                layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
                layout.setSpacing(7);
                fig.setLayoutManager(layout);

                if (text != null) {
                    text = Action.removeAcceleratorText(text);
                    text = Action.removeMnemonics(text);
                    Label title = new Label(text);
                    title.setFont(
                            FontUtils.getBold(JFaceResources.DEFAULT_FONT));
                    fig.add(title);
                }

                if (tooltip != null) {
                    RotatableWrapLabel description = new RotatableWrapLabel(
                            tooltip, RotatableWrapLabel.NORMAL);
                    description.setTextAlignment(PositionConstants.LEFT);
                    description.setPrefWidth(Math.min(
                            Display.getCurrent().getClientArea().width / 3,
                            128));
                    description.setFont(FontUtils.getRelativeHeight(
                            JFaceResources.DEFAULT_FONT, -1));
                    description.setForegroundColor(ColorConstants.gray);
                    description.setShowTabToSpace(true);
                    fig.add(description);
                }

                return fig;
            }
        }
        return super.createToolTip();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == Image.class)
            return getImage();
        if (adapter == IAction.class)
            return getAction();
        if (adapter == IMenuManager.class)
            return getPopupMenu();
        return super.getAdapter(adapter);
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
    }

    protected IFeedback createFeedback() {
        return new SimpleSelectionFeedback(this);
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new SelectionFeedbackHelper();
    }

    private SVGReference createSVGReference(String svgFilePath) {
        SVGReference ref = new SVGReference(svgFilePath);

        if (this.resources == null)
            resources = new LocalResourceManager(JFaceResources.getResources());

        return ref;
    }

    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (IAction.TEXT.equals(property)
                || IAction.TOOL_TIP_TEXT.equals(property)) {
            updateToolTip();
        } else if (IAction.IMAGE.equals(property)) {
            updateImage();
            update();
        }
    }

}
