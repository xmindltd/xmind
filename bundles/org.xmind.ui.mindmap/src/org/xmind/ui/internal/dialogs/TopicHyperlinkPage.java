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
package org.xmind.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.dialogs.HyperlinkPage;

/**
 * @author Frank Shaka
 */
public class TopicHyperlinkPage extends HyperlinkPage {

    private class TopicPageContentProvider implements ITreeContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IWorkbook) {
                List<ISheet> sheets = ((IWorkbook) inputElement).getSheets();
                List<ITopic> rootTopics = new ArrayList<ITopic>(sheets.size());
                for (ISheet sheet : sheets) {
                    rootTopics.add(sheet.getRootTopic());
                }
                return rootTopics.toArray();
            }
            return new Object[0];
        }

        public boolean hasChildren(Object element) {
            if (element instanceof ITopic) {
                return !((ITopic) element).getAllChildren().isEmpty();
            }
            return false;
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ITopic) {
                return ((ITopic) parentElement).getAllChildren().toArray();
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof ITopic) {
                ITopic topic = (ITopic) element;
                if (topic.isRoot())
                    return topic.getOwnedWorkbook();
                return topic.getParent();
            }
            return null;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }
    }

    private class TopicPageLabelProvider extends LabelProvider {

        public String getText(Object element) {
            if (element instanceof ITopic) {
                ITopic topic = (ITopic) element;
                if (topic.isRoot()) {
                    return topic.getTitleText() + " (" //$NON-NLS-1$
                            + topic.getOwnedSheet().getTitleText() + ")"; //$NON-NLS-1$
                }
                String titleText = topic.getTitleText();
                return titleText.replaceAll("\\r\\n|\\n|\\r", " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return super.getText(element);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.ui.viewers.ImageCachedLabelProvider#createImage(java.lang
         * .Object)
         */
//        protected Image createImage(Object element) {
//            ImageDescriptor icon = MindMapUI.getImages().getElementIcon(element,
//                    true);
//            if (icon == null)
//                return null;
//            return icon.createImage(false);
//        }

    }

    private class TopicSelectionListener implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) selection;
                Object element = ss.getFirstElement();
                if (element instanceof ITopic) {
                    isModifyValue = true;
                    setValue(HyperlinkUtils.toInternalURL((ITopic) element));
                    isModifyValue = false;
                    setCanFinish(true);
                }
            }

        }
    }

    public Composite composite;

    public IWorkbook workbook;

    public boolean isModifyValue = false;

    public String str;

    private TreeViewer topicViewer;

    public TopicHyperlinkPage() {
    }

    public void init(IEditorPart editor, IStructuredSelection selection) {
        this.workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        createLabel(composite);
        createTopicViewer(composite);
    }

    /**
     * @param parent
     */
    private void createLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        label.setText(DialogMessages.TopicHyperlinkPage_label);
    }

    /**
     * @param parent
     */
    private void createTopicViewer(Composite parent) {
//        topicViewer = new TreeViewer(parent, SWT.SINGLE | SWT.BORDER);
        PatternFilter filter = new PatternFilter();
        FilteredTree tree = new FilteredTree(parent,
                SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, filter,
                true);
        topicViewer = tree.getViewer();
        topicViewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        topicViewer.setAutoExpandLevel(2);

        topicViewer.setContentProvider(new TopicPageContentProvider());
        topicViewer.setLabelProvider(new TopicPageLabelProvider());
        if (workbook != null)
            topicViewer.setInput(workbook);

        topicViewer.addSelectionChangedListener(new TopicSelectionListener());
    }

    public void setValue(String value) {
        super.setValue(value);
        if (!isModifyValue) {
            if (topicViewer != null && topicViewer.getControl() != null
                    && !topicViewer.getControl().isDisposed()) {
                Object element = getElement(value);
                if (element != null) {
                    topicViewer.setSelection(new StructuredSelection(element),
                            true);
                } else {
                    topicViewer.setSelection(StructuredSelection.EMPTY);
                }
            }
        }
    }

    /**
     * @param value
     */
    private Object getElement(String value) {
        if (value == null)
            return null;
        if (workbook == null)
            return null;
        return HyperlinkUtils.findElement(value, workbook);
    }

    public void dispose() {
    }

    public Control getControl() {
        return composite;
    }

    public void setFocus() {
        if (topicViewer != null) {
            Control control = topicViewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.setFocus();
            }
        }
    }

}
