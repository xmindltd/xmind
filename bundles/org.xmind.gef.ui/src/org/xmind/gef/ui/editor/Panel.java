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
package org.xmind.gef.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class Panel implements IPanel {

    private static final List<IPanelContribution> NO_CONTRIBUTIONS = Collections
            .emptyList();

    private Composite container;

    private Control content;

    private Map<Integer, List<IPanelContribution>> contributions = null;

    public void addContribution(int orientation, IPanelContribution contribution) {
        if (contribution == null || orientation < TOP || orientation > RIGHT)
            return;

        removeContribution(contribution);
        if (contributions == null)
            contributions = new HashMap<Integer, List<IPanelContribution>>();
        List<IPanelContribution> list = contributions.get(orientation);
        if (list == null) {
            list = new ArrayList<IPanelContribution>();
            contributions.put(orientation, list);
        }
        list.add(contribution);
        contributionAdded(contribution);
    }

    public List<IPanelContribution> getContributions(int orientation) {
        if (contributions != null) {
            List<IPanelContribution> list = contributions.get(orientation);
            if (list != null)
                return list;
        }
        return NO_CONTRIBUTIONS;
    }

    public void removeContribution(IPanelContribution contribution) {
        if (contribution == null || contributions == null)
            return;
        for (Integer orientation : contributions.keySet()) {
            List<IPanelContribution> list = contributions.get(orientation);
            if (list.contains(contribution)) {
                list.remove(contribution);
                if (list.isEmpty()) {
                    contributions.remove(orientation);
                }
                contributionRemoved(contribution);
                return;
            }
        }
    }

    private void contributionAdded(IPanelContribution contribution) {
        contribution.setPanel(this);
        createContributionControl(contribution);
    }

    private void createContributionControl(IPanelContribution contribution) {
        if (containerExists()) {
            contribution.createControl(container);
        }
    }

    private void contributionRemoved(IPanelContribution contribution) {
        disposeContributionControl(contribution);
        contribution.setPanel(null);
    }

    private void disposeContributionControl(IPanelContribution contribution) {
        Control c = contribution.getControl();
        if (c != null) {
            c.dispose();
        }
    }

    protected boolean isEmpty() {
        return contributions == null || contributions.isEmpty();
    }

    public void update() {
        if (containerExists()) {
            GridLayout layout = getContainerLayout();
            int numColumns = calcNumColumns();
            layout.numColumns = numColumns;

            Control last = null;
            last = adaptContributions(TOP, numColumns, last);
            last = adaptContributions(LEFT, numColumns, last);

            if (content != null) {
//                moveAfter(content, last);
                last = content;
                GridData data = getControlLayoutData(content);
                defaultLayoutData(data);
                data.exclude = false;
                data.grabExcessHorizontalSpace = true;
                data.grabExcessVerticalSpace = true;
                data.horizontalAlignment = SWT.FILL;
                data.verticalAlignment = SWT.FILL;
                data.horizontalSpan = 1;
                data.verticalSpan = 1;
                last = content;
            }

            last = adaptContributions(RIGHT, numColumns, last);
            last = adaptContributions(BOTTOM, numColumns, last);

            container.layout();
        }
    }

    private Control adaptContributions(int orientation, int numColumns,
            Control last) {
        if (contributions == null)
            return last;

        List<IPanelContribution> list = contributions.get(orientation);
        if (list != null) {
            for (IPanelContribution contribution : list) {
                Control c = contribution.getControl();
                if (c != null && !c.isDisposed()) {
                    moveAfter(c, last);
                    last = c;
                    boolean visible = contribution.isVisible();
                    c.setVisible(visible);
                    GridData data = getControlLayoutData(c);
                    contributionLayoutData(data, orientation == LEFT
                            || orientation == RIGHT, orientation == TOP
                            || orientation == LEFT, visible, numColumns);
                }
            }
        }
        return last;
    }

    private void contributionLayoutData(GridData data, boolean horizontal,
            boolean beginning, boolean visible, int numColumns) {
        defaultLayoutData(data);
        data.exclude = !visible;
        data.grabExcessHorizontalSpace = !horizontal;
        data.grabExcessVerticalSpace = horizontal;
        data.horizontalAlignment = horizontal ? (beginning ? SWT.LEFT
                : SWT.RIGHT) : SWT.FILL;
        data.horizontalSpan = horizontal ? 1 : numColumns;
        data.verticalAlignment = horizontal ? SWT.FILL : (beginning ? SWT.TOP
                : SWT.BOTTOM);
        data.verticalSpan = horizontal ? numColumns : 1;
    }

    private static void defaultLayoutData(GridData data) {
        data.heightHint = SWT.DEFAULT;
        data.horizontalIndent = 0;
        data.minimumHeight = 0;
        data.minimumWidth = 0;
        data.verticalIndent = 0;
        data.widthHint = SWT.DEFAULT;
    }

    private GridData getControlLayoutData(Control c) {
        Object data = c.getLayoutData();
        if (data == null || !(data instanceof GridData)) {
            data = new GridData();
            c.setLayoutData(data);
        }
        return (GridData) data;
    }

    private static void moveAfter(Control current, Control last) {
        if (last == null)
            current.moveAbove(null);
        else
            current.moveBelow(last);
    }

    private int calcNumColumns() {
        int num = 0;
        if (contributions != null) {
            List<IPanelContribution> left = contributions.get(LEFT);
            if (left != null) {
                for (IPanelContribution contribution : left) {
                    Control c = contribution.getControl();
                    if (c != null && !c.isDisposed()) {
                        num++;
                    }
                }
            }
            List<IPanelContribution> right = contributions.get(RIGHT);
            if (right != null) {
                for (IPanelContribution contribution : right) {
                    Control c = contribution.getControl();
                    if (c != null && !c.isDisposed()) {
                        num++;
                    }
                }
            }
        }
        if (content != null) {
            num++;
        }
        return num;
    }

    private GridLayout getContainerLayout() {
        Layout layout = container.getLayout();
        if (layout == null || !(layout instanceof GridLayout)) {
            GridLayout gridLayout = new GridLayout();
            gridLayout.horizontalSpacing = 0;
            gridLayout.verticalSpacing = 0;
            gridLayout.marginHeight = 0;
            gridLayout.marginWidth = 0;
            gridLayout.makeColumnsEqualWidth = false;
            container.setLayout(gridLayout);
            layout = gridLayout;
        }
        return (GridLayout) layout;
    }

    protected void createControls(Composite parent) {
        if (container == null || !containerExists()) {
            container = new Composite(parent, SWT.NONE);

            if (contributions != null) {
                for (List<IPanelContribution> list : contributions.values()) {
                    for (IPanelContribution contribution : list) {
                        createContributionControl(contribution);
                    }
                }
            }
        }
    }

    protected Composite getContainer() {
        return container;
    }

    protected void setContent(Control content) {
        this.content = content;
    }

    protected boolean containerExists() {
        return container != null && !container.isDisposed();
    }

}