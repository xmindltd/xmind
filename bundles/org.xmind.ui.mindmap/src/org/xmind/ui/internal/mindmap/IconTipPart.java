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
import org.xmind.ui.internal.decorators.IconTipDecorator;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGImageFigure;
import org.xmind.ui.internal.svgsupport.SVGReference;
import org.xmind.ui.mindmap.IIconTipPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.resources.ImageReference;

public class IconTipPart extends MindMapPartBase
        implements IIconTipPart, IPropertyChangeListener {

    public static final String TEXT_COLOR = "#2e2e2e"; //$NON-NLS-1$

    private IAction action;

    private IMenuManager menu;

    private String actionId;

    private ImageReference imageRef = null;

    private SVGReference svgRef = null;

    private ResourceManager resources;

    public IconTipPart() {
        setDecorator(IconTipDecorator.getInstance());
    }

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

    public SizeableImageFigure getImageFigure() {
        return (SizeableImageFigure) super.getFigure();
    }

    public IMenuManager getPopupMenu() {
        TopicPart topicPart = (TopicPart) getParent();
        ITopic topic = topicPart.getTopic();
        IconTip iconTip = (IconTip) IconTipPart.this.getModel();
        final List<IAction> actions = iconTip.getInfoItemContributor()
                .getPopupMenuActions(topicPart, topic);
        if (actions == null || actions.isEmpty()) {
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
            return menu;
        }
    }

    public ITopic getTopic() {
        return (ITopic) super.getRealModel();
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).removeIconTip(this);
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).addIconTip(this);
        }
    }

    public void setModel(Object model) {
        super.setModel(model);

        if (svgRef == null) {
            if (model instanceof IconTip) {
                String svgFilePath = ((IconTip) model).getInfoItemContributor()
                        .getSVGFilePath(getTopic(), action);
                if (svgFilePath != null && !"".equals(svgFilePath)) //$NON-NLS-1$
                    svgRef = createSVGReference(svgFilePath);
            }
        }
    }

    protected void register() {
        super.register();
        if (getModel() instanceof IconTip) {
            setAction(((IconTip) getModel()).getAction());
        }
    }

    protected void unregister() {
        setAction(null);
        super.unregister();
    }

    public void setAction(IAction action) {
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
        ITopicPart topicPart = getTopicPart();
        if (topicPart == null)
            return;

        IActionRegistry actionRegistry = (IActionRegistry) topicPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry == null)
            return;

        if (actionRegistry.getAction(actionId) == action) {
            actionRegistry.removeAction(actionId);
        }
    }

    private void registerAction(IAction action) {
        ITopicPart topicPart = getTopicPart();
        if (topicPart == null)
            return;

        IActionRegistry actionRegistry = (IActionRegistry) topicPart
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
                newImageDescriptor = action.getImageDescriptor();
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
            if (model != null && model instanceof IconTip) {
                String svgFilePath = ((IconTip) model).getInfoItemContributor()
                        .getSVGFilePath(getTopic(), action);
                if (svgFilePath == null || "".equals(svgFilePath)) { //$NON-NLS-1$
                    /// for favicon
                    svgRef = null;
                    getParent().refresh();
                }
            }
        }
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        resources = new LocalResourceManager(JFaceResources.getResources());
    }

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

//                if (actionId != null
//                        && actionId.equals(MindMapActionFactory.EDIT_NOTES
//                                .getId())) {
//                    text = null;
//                }

                if (text != null) {
                    text = Action.removeAcceleratorText(text);
                    text = Action.removeMnemonics(text);
                    Label title = new Label(text);
                    title.setFont(
                            FontUtils.getBold(JFaceResources.DEFAULT_FONT));
                    title.setForegroundColor(ColorConstants.darkGray);
                    fig.add(title);
                }

                if (tooltip != null) {
                    RotatableWrapLabel description = new RotatableWrapLabel(
                            tooltip, RotatableWrapLabel.NORMAL);
                    description.setTextAlignment(PositionConstants.LEFT);
                    description.setPrefWidth(Math.min(
                            Display.getCurrent().getClientArea().width / 3,
                            350));
                    description.setFont(FontUtils.getRelativeHeight(
                            JFaceResources.DEFAULT_FONT, -1));
//                    description.setForegroundColor(ColorConstants.gray);
                    description.setForegroundColor(
                            ColorUtils.getColor(TEXT_COLOR));
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

    public String getActionId() {
        return actionId;
    }
}
