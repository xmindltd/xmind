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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.Core;
import org.xmind.core.ILegend;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.util.IMarkerRefCounter;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.ui.internal.decorators.LegendDecorator;
import org.xmind.ui.internal.figures.LegendFigure;
import org.xmind.ui.internal.layouts.LegendLayout;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class LegendPart extends MindMapPartBase implements ILegendPart {

    private ITitleTextPart title = null;

    private List<ILegendItemPart> items = null;

    private LegendSeparatorPart separator = null;

    private ChildSorter sorter = new ChildSorter(this);

    private Point prefPosition = null;

    public LegendPart() {
        setDecorator(LegendDecorator.getInstance());
    }

    protected IFigure createFigure() {
        return new LegendFigure();
    }

    public ILegend getLegend() {
        return (ILegend) super.getRealModel();
    }

    public ISheetPart getOwnedSheet() {
        if (getParent() instanceof ISheetPart)
            return (ISheetPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof SheetPart) {
            SheetPart sheet = (SheetPart) getParent();
            if (sheet.getLegend() == this) {
                sheet.setLegend(null);
            }
        }
        super.setParent(parent);
        if (getParent() instanceof SheetPart) {
            SheetPart sheet = (SheetPart) getParent();
            sheet.setLegend(this);
        }
    }

    public ITitleTextPart getTitle() {
        return title;
    }

    public void setTitle(ITitleTextPart title) {
        this.title = title;
    }

    public List<ILegendItemPart> getItems() {
        if (items == null)
            items = new ArrayList<ILegendItemPart>();
        return items;
    }

    public void addItem(ILegendItemPart item) {
        getItems().add(item);
        sorter.sort(getItems());
    }

    public void removeItem(ILegendItemPart item) {
        getItems().remove(item);
        sorter.sort(getItems());
    }

    public LegendSeparatorPart getSeparator() {
        return separator;
    }

    public void setSeparator(LegendSeparatorPart separator) {
        this.separator = separator;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ILegend.class))
            return getLegend();
        if (adapter == LegendSeparatorPart.class)
            return separator;
        if (adapter == ITitleTextPart.class
                || adapter == LegendTitleTextPart.class)
            return title;
        return super.getAdapter(adapter);
    }

    protected LayoutManager createLayoutManager() {
        return new LegendLayout(this);
    }

    protected Object[] getModelChildren(Object model) {
        List<Object> list = new ArrayList<Object>();
        ILegend legend = getLegend();
        list.add(new ViewerModel(LegendTitleTextPart.class, legend));
        list.add(new ViewerModel(LegendSeparatorPart.class, legend));
        Collection<String> markerIds = getAllMarkers(legend);
        if (markerIds != null && !markerIds.isEmpty()) {
            Set<LegendItem> items = new TreeSet<LegendItem>();
            for (String markerId : markerIds) {
                items.add(new LegendItem(legend, markerId));
            }
            list.addAll(items);
        }
        return list.toArray();
    }

    private Collection<String> getAllMarkers(ILegend legend) {
        IMarkerRefCounter counter = (IMarkerRefCounter) legend.getOwnedSheet()
                .getAdapter(IMarkerRefCounter.class);
        if (counter != null) {
            return counter.getCountedRefs();
        }
        return null;
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_LEGEND_MOVABLE);
    }

    protected void registerCoreEvents(Object source, ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.Position);
        register.register(Core.MarkerDescription);

        Object counter = getLegend().getOwnedSheet().getAdapter(
                IMarkerRefCounter.class);
        if (counter != null) {
            register.setNextSourceFrom(counter);
            register.register(Core.ResourceRefs);
        }
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.Position.equals(type)) {
            setPreferredPosition(MindMapUtils.toGraphicalPosition(getLegend()
                    .getPosition()));
        } else if (Core.MarkerDescription.equals(type)) {
            String markerId = (String) event.getTarget();
            if (markerId != null) {
                ILegendItemPart item = findItem(markerId);
                if (item != null) {
                    item.refresh();
                }
            }
        } else if (Core.ResourceRefs.equals(type)) {
            refresh();
        } else {
            super.handleCoreEvent(event);
        }
    }

    private ILegendItemPart findItem(String markerId) {
        for (ILegendItemPart item : getItems()) {
            if (markerId.equals(item.getMarkerId()))
                return item;
        }
        return null;
    }

    protected void updateChildren() {
        super.updateChildren();
        for (ILegendItemPart item : getItems()) {
            item.update();
        }
    }

    public Point getPreferredPosition() {
        return prefPosition;
    }

    public void setPreferredPosition(Point point) {
        this.prefPosition = point;
        getFigure().revalidate();
    }

    public IPart findAt(Point position) {
        IPart ret = super.findAt(position);
        if (ret != null) {
            if (ret == title || ret == separator)
                return this;
        }
        return ret;
    }

    public Cursor getCursor(Point pos) {
        return Cursors.HAND;
    }

}