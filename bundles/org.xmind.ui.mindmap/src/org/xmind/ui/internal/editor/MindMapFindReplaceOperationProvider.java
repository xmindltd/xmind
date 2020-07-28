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
package org.xmind.ui.internal.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.ITitled;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.AbstractTool;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.findreplace.AbstractFindReplaceOperationProvider;
import org.xmind.ui.internal.tools.LabelEditTool;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.FloatingTextEditTool;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.tools.TitleEditTool;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author Frank Shaka
 */
public class MindMapFindReplaceOperationProvider extends
        AbstractFindReplaceOperationProvider {

    private static final int PROP_TITLE = 1;

    private static final int PROP_LABEL = 2;

//    private static final int PROP_NOTES = 3;

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider#
     * getContextName()
     */
    public String getContextName() {
        String title = editor.getTitle();
        IGraphicalEditorPage page = editor.getActivePageInstance();
        String pageTitle = page == null ? null : page.getPageTitle();
        if (pageTitle == null || "".equals(pageTitle)) { //$NON-NLS-1$
            return title;
        }
        return NLS.bind("{0} - {1}", title, pageTitle); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider#
     * getContextName(int, org.eclipse.swt.graphics.Font)
     */
    public String getContextName(int maxWidth, Font font) {
        String title = editor.getTitle();
        IGraphicalEditorPage page = editor.getActivePageInstance();
        String pageTitle = page == null ? null : page.getPageTitle();
        if (pageTitle == null || "".equals(pageTitle)) { //$NON-NLS-1$
            return constrainText(title, maxWidth, font);
        }
        String name = NLS.bind("{0} - {1}", title, pageTitle); //$NON-NLS-1$
        if (computeTextWidth(name, font) > maxWidth) {
            int halfMaxWidth = (maxWidth - computeTextWidth(" - ", font)) / 2 - 1; //$NON-NLS-1$
            int w1 = computeTextWidth(title, font);
            int w2 = computeTextWidth(pageTitle, font);
            if (w1 > halfMaxWidth) {
                title = constrainText(title,
                        Math.max(halfMaxWidth, halfMaxWidth * 2 - w2), font);
                w1 = computeTextWidth(title, font);
            }
            name = NLS.bind("{0} - {1}", title, pageTitle); //$NON-NLS-1$
            if (computeTextWidth(name, font) > maxWidth) {
                pageTitle = constrainText(pageTitle,
                        Math.max(halfMaxWidth, halfMaxWidth * 2 - w1), font);
            }
            name = NLS.bind("{0} - {1}", title, pageTitle); //$NON-NLS-1$
        }
        return name;
    }

    private class SearchResult {

        final String toFind;

        IPart part = null;

        int offset = -1;

        int propertyId = 0;

        /**
         * @param host
         * @param offset
         * @param isInNotes
         */
        public SearchResult(String toFind) {
            super();
            this.toFind = toFind;
        }

        public boolean found() {
            if (part == null)
                return false;
//            if (part instanceof ITopicPart) {
//                if (propertyId == PROP_NOTES)
//                    return true;
//            }
            return offset >= 0;
        }

        public boolean sameLocation(SearchResult that) {
            if (that == null)
                return false;
            if (this.part != that.part)
                return false;
//            if (this.propertyId == PROP_NOTES)
//                return that.propertyId == PROP_NOTES;
            return this.propertyId == that.propertyId
                    && this.offset == that.offset;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof SearchResult))
                return false;
            SearchResult that = (SearchResult) obj;
            return (this.toFind == that.toFind || (this.toFind != null && this.toFind
                    .equals(that.toFind))) && sameLocation(that);
        }

    }

    private IGraphicalEditor editor;

    private SearchResult result = null;

//    private boolean findingInNotes = false;

    /**
     * 
     */
    public MindMapFindReplaceOperationProvider(IGraphicalEditor editor) {
        this.editor = editor;
    }

    protected IPart getCurrentPart() {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null) {
            IPart part = viewer.getFocusedPart();
            if (part != null)
                return part;
        }
        return getCurrentCentralTopicPart();
    }

    /**
     * @return
     */
    private IPart getCurrentCentralTopicPart() {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null)
            return (ITopicPart) viewer.getAdapter(ITopicPart.class);
        return null;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#canFind(java.lang.String)
     */
    public boolean canFind(String toFind) {
        return getCurrentPart() != null;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.IFindReplaceOperationProvider#canReplace(java.lang.String,
     *      java.lang.String)
     */
    public boolean canReplace(String toFind, String toReplaceWith) {
        return canFind(toFind);
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#findAll(java.lang.String)
     */
    @Override
    protected boolean findAll(String toFind) {
        List<IPart> result = findAllParts(toFind);
        select(result);
        return !result.isEmpty();
    }

//    @Override
//    public boolean find(String toFind) {
//        if (findingInNotes)
//            return false;
//        return super.find(toFind);
//    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#findNext(java.lang.String)
     */
    @Override
    protected boolean findNext(String toFind) {
        SearchResult start = getStartingLocation();
        SearchResult newResult = new SearchResult(toFind);
        findNext(newResult, start);
        result = newResult;
        return result.found() && select(newResult);
    }

    private void findNext(SearchResult result, SearchResult start) {
        findNextInProperty(result, start);
        SearchResult next = start;
        while (!result.found()) {
            next = getNextProperty(next);
            if (next == null
                    || (next.part == start.part && next.propertyId == start.propertyId))
                break;
            findNextInProperty(result, next);
        }
    }

    private SearchResult getNextProperty(SearchResult start) {
        SearchResult result = new SearchResult(start.toFind);
        if (start.part instanceof ITopicPart) {
            if (start.propertyId == PROP_TITLE) {
                result.part = start.part;
                result.propertyId = PROP_LABEL;
            } else if (start.propertyId == PROP_LABEL) {
//                result.part = start.part;
//                result.propertyId = PROP_NOTES;
//            } else {
                result.part = getNextPart(start.part);
                result.propertyId = PROP_TITLE;
            }
        } else {
            result.part = getNextPart(start.part);
            result.propertyId = PROP_TITLE;
        }
        result.offset = getStartingOffset(result);
        return result;
    }

    private void findNextInProperty(SearchResult result, SearchResult start) {
        String text = getPropertyText(start.part, start.propertyId);
        if (text != null) {
            int offset = indexOf(text, result.toFind, start.offset);
            if (offset >= 0) {
                result.part = start.part;
                result.propertyId = start.propertyId;
//                if (start.propertyId != PROP_NOTES) {
                result.offset = offset;
//                }
            }
        }
    }

    private boolean select(SearchResult result) {
        if (result.found()) {
            int pageIndex = getPageIndex(result.part);
            if (pageIndex >= 0) {
                if (pageIndex != editor.getActivePage())
                    editor.setActivePage(pageIndex);
                IGraphicalEditorPage page = editor.getActivePageInstance();
                if (page != null) {
                    EditDomain domain = page.getEditDomain();
                    IGraphicalViewer viewer = page.getViewer();
                    if (result.propertyId == PROP_TITLE) {
                        editAndSelect(result, domain, viewer, GEF.REQ_EDIT);
                    } else if (result.propertyId == PROP_LABEL) {
                        editAndSelect(result, domain, viewer,
                                MindMapUI.REQ_EDIT_LABEL);
//                    } else if (result.propertyId == PROP_NOTES) {
//                        return findInNotes(result, page);
                    }
                    return true;
                }
            }
        }
        return false;
    }

//    private boolean findInNotes(final SearchResult result,
//            IGraphicalEditorPage page) {
//        page.getEditDomain().handleRequest(GEF.REQ_CANCEL, page.getViewer());
//        page.getViewer()
//                .setSelection(
//                        new StructuredSelection(MindMapUtils
//                                .getRealModel(result.part)), true);
//        final IViewPart[] notesView = new IViewPart[1];
//        SafeRunner.run(new SafeRunnable() {
//            public void run() throws Exception {
//                notesView[0] = editor.getSite().getPage().showView(
//                        MindMapUI.VIEW_NOTES);
//            }
//        });
//        if (notesView[0] != null) {
//            ITextViewer textViewer = (ITextViewer) notesView[0]
//                    .getAdapter(ITextViewer.class);
//            if (textViewer != null) {
//                textViewer.setSelectedRange(isForward() ? 0 : textViewer
//                        .getDocument().getLength(), 0);
//            }
//            IFindReplaceOperationProvider frProvider = (IFindReplaceOperationProvider) notesView[0]
//                    .getAdapter(IFindReplaceOperationProvider.class);
//            if (frProvider != null) {
//                findingInNotes = true;
//                try {
//                    return frProvider.find(result.toFind);
//                } finally {
//                    findingInNotes = false;
//                }
//            }
//        }
//        return false;
//    }

    private void editAndSelect(final SearchResult result,
            final EditDomain domain, final IGraphicalViewer viewer,
            final String requestType) {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                Request request = new Request(requestType)
                        .setPrimaryTarget(result.part)
                        .setDomain(domain)
                        .setViewer(getActiveViewer())
                        .setParameter(GEF.PARAM_FOCUS, Boolean.FALSE)
                        .setParameter(
                                GEF.PARAM_TEXT_SELECTION,
                                new TextSelection(result.offset, result.toFind
                                        .length()));
                domain.handleRequest(request);
            }
        });
    }

    private SearchResult getStartingLocation() {
        SearchResult start = new SearchResult(result == null ? null
                : result.toFind);
        start.part = getCurrentPart();
        ITool tool = editor.getActivePageInstance().getEditDomain()
                .getActiveTool();
        if (tool instanceof LabelEditTool) {
            start.propertyId = PROP_LABEL;
            start.offset = getStartingOffset((LabelEditTool) tool);
        } else if (tool instanceof TitleEditTool) {
            start.propertyId = PROP_TITLE;
            start.offset = getStartingOffset((TitleEditTool) tool);
//        } else if (result != null && result.part == start.part
//                && result.propertyId == PROP_NOTES) {
//            start.part = getNextPart(start.part);
//            start.propertyId = PROP_TITLE;
//            start.offset = getStartingOffset(start);
        } else {
            start.propertyId = PROP_TITLE;
            start.offset = getStartingOffset(start);
        }
        return start;
    }

    private int getStartingOffset(FloatingTextEditTool tool) {
        ITextSelection selection = tool.getTextSelection();
        if (isForward()) {
            return selection.getOffset() + selection.getLength();
        } else {
            return selection.getOffset();
        }
    }

    private int getStartingOffset(SearchResult result) {
        String text = getPropertyText(result.part, result.propertyId);
        return text == null ? -1 : getNewOffset(text);
    }

    private String getPropertyText(IPart part, int propertyId) {
//        if (propertyId == PROP_NOTES) {
//            if (part instanceof ITopicPart) {
//                INotes notes = ((ITopicPart) part).getTopic().getNotes();
//                INotesContent content = notes.getContent(INotes.PLAIN);
//                if (content instanceof IPlainNotesContent) {
//                    return ((IPlainNotesContent) content).getTextContent();
//                }
//            }
//            return null;
//        }
        if (propertyId == PROP_LABEL) {
            ILabelPart label = ((ITopicPart) part).getOwnerBranch().getLabel();
            return label == null ? null : label.getLabelText();
        }

        Object model = MindMapUtils.getRealModel(part);
        if (model instanceof ITitled && ((ITitled) model).hasTitle()) {
            return ((ITitled) model).getTitleText();
        }

        ITitled titled = (ITitled) part.getAdapter(ITitled.class);
        if (titled != null && titled.hasTitle()) {
            return titled.getTitleText();
        }

        return (String) part.getAdapter(String.class);
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#replaceAll(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected boolean replaceAll(String toFind, String toReplaceWith) {
        boolean found = findAll(toFind);
        if (found) {
            EditDomain domain = getCurrentDomain();
            if (domain == null) {
                found = false;
            } else {
                Boolean ignoreCase = Boolean
                        .valueOf((getParameter() & PARAM_CASE_SENSITIVE) == 0);
                domain.handleRequest(new Request(MindMapUI.REQ_REPLACE_ALL)
                        .setParameter(GEF.PARAM_TEXT, toFind)
                        .setParameter(MindMapUI.PARAM_REPLACEMENT,
                                toReplaceWith)
                        .setParameter(MindMapUI.PARAM_IGNORE_CASE, ignoreCase)
                        .setDomain(domain).setViewer(getActiveViewer()));
            }
        }
        return found;
    }

    /**
     * @see cn.brainy.ui.mindmap.dialogs.AbstractFindReplaceOperationProvider#replaceNext(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected boolean replaceNext(String toFind, String toReplaceWith) {
        EditDomain domain = getCurrentDomain();
        if (domain != null) {
            ITool tool = domain.getActiveTool();
            if (tool != null && tool instanceof FloatingTextEditTool) {
                FloatingTextEditor textEditor = ((FloatingTextEditTool) tool)
                        .getEditor();
                if (textEditor != null && !textEditor.isClosed()) {
                    textEditor.replaceText(toReplaceWith, true);
                    textEditor.close(true);
                }
            }
        }
        return findNext(toFind);
    }

    /**
     * @param toFind
     * @return
     */
    protected List<IPart> findAllParts(String toFind) {
        return findAllParts(toFind, getCurrentPart(), null,
                new ArrayList<IPart>());
    }

    protected List<IPart> findAllParts(String toFind, IPart current,
            IPart start, List<IPart> result) {
        if (start == null)
            start = current;
        String text = getText(current);
        int index = indexOf(text, toFind, getNewOffset(text));
        if (index >= 0) {
            result.add(current);
        }
        IPart next = getNextPart(current);
        if (next != start) {
            result = findAllParts(toFind, next, start, result);
        }
        return result;
    }

    /**
     * @param result
     */
    protected boolean select(List<IPart> result) {
        if (isEditing(null)) {
            getEditTool(null).handleRequest(
                    new Request(GEF.REQ_FINISH).setViewer(getActiveViewer()));
        }
        getActiveViewer().setSelection(new StructuredSelection(result), true);
//        ITool selectTool = getDefaultTool();
//        if (selectTool == null)
//            return false;
//        selectTool
//                .handleRequest(new Request(GEF.REQ_SELECT).setTargets(result));
        return true;
    }

    protected String getText(IPart p) {
        if (p == null)
            return null;

        if (p instanceof ILabelPart) {
            ILabelPart labelPart = (ILabelPart) p;
            String text = labelPart.getLabelText();
            return text;
        }

        ITitled titled = (ITitled) p.getAdapter(ITitled.class);
        if (titled != null && titled.hasTitle()) {
            return titled.getTitleText();
        }
        return (String) p.getAdapter(String.class);
    }

    private int getPageIndex(IPart part) {
        Object input = part.getSite().getViewer().getInput();
        IGraphicalEditorPage page = editor.findPage(input);
        return page == null ? editor.getActivePage() : page.getIndex();
    }

    private IPart getNextPart(IPart current) {
        return isForward() ? getForwardPart(current) : getBackwardPart(current);
    }

    /**
     * @param current
     * @return
     */
    private IPart getBackwardPart(IPart current) {
        if (!isCentral(current) || !isWorkbook()) {
            IPart prev = findPrecedingPart(current);
            if (prev != null && prev != current)
                return prev;
            if (!isWorkbook())
                return current;
        }
        int pageIndex = getPageIndex(current);
        int pageCount = editor.getPageCount();
        if (pageIndex == 0)
            pageIndex = pageCount - 1;
        else
            pageIndex--;
        IGraphicalEditorPage page = editor.getPage(pageIndex);
        IGraphicalViewer viewer = page.getViewer();
        IPart central = (IPart) viewer.getAdapter(ITopicPart.class);
        if (central != null) {
            IPart prev = findPrecedingPart(central);
            if (prev != null && prev != central)
                return prev;
        }
        return central;
    }

    /**
     * @param current
     * @return
     */
    private IPart getForwardPart(IPart current) {
        IPart next = findSucceedingPart(current);
        if (next != null && next != current
                && (!isCentral(next) || !isWorkbook()))
            return next;
        if (!isWorkbook())
            return current;
        int pageIndex = getPageIndex(current);
        int pageCount = editor.getPageCount();
        if (pageIndex == pageCount - 1)
            pageIndex = 0;
        else
            pageIndex++;
        IGraphicalEditorPage page = editor.getPage(pageIndex);
        IGraphicalViewer viewer = page.getViewer();
        return (IPart) viewer.getAdapter(ITopicPart.class);
    }

    private IPart findSucceedingPart(IPart current) {
        if (current instanceof ITopicPart) {
            IPart next = firstBoundary(((ITopicPart) current).getOwnerBranch());
            if (next != null)
                return next;
        } else if (current instanceof IBoundaryPart) {
            IPart next = nextBoundary((IBoundaryPart) current);
            if (next != null)
                return next;
            return findNavNext(((IBoundaryPart) current).getOwnedBranch()
                    .getTopicPart());
        } else if (current instanceof IRelationshipPart) {
            IPart next = nextRelationship((IRelationshipPart) current);
            if (next != null)
                return next;
            return ((IRelationshipPart) current).getOwnerSheet()
                    .getCentralBranch().getTopicPart();
        }
        IPart next = findNavNext(current);
        if (current instanceof ITopicPart && isCentral(next)) {
            IPart rel = firstRelationship((ISheetPart) current.getSite()
                    .getViewer().getAdapter(ISheetPart.class));
            if (rel != null)
                return rel;
        }
        return next;
    }

    private IPart findPrecedingPart(IPart current) {
        if (current instanceof ITopicPart) {
            if (isCentral(current)) {
                IPart rel = lastRelationship((ISheetPart) current.getSite()
                        .getViewer().getAdapter(ISheetPart.class));
                if (rel != null)
                    return rel;
            }
            IPart prev = findNavPrev(current);
            if (prev != current && prev instanceof ITopicPart) {
                if (((ITopicPart) current).getOwnerBranch().getParentBranch() == ((ITopicPart) prev)
                        .getOwnerBranch()) {
                    IPart prevBoundary = lastBoundary(((ITopicPart) prev)
                            .getOwnerBranch());
                    if (prevBoundary != null)
                        return prevBoundary;
                }
            }
            return prev;
        } else if (current instanceof IBoundaryPart) {
            IPart prev = prevBoundary((IBoundaryPart) current);
            if (prev != null)
                return prev;
            return ((IBoundaryPart) current).getOwnedBranch().getTopicPart();
        } else if (current instanceof IRelationshipPart) {
            IPart prev = prevRelationship((IRelationshipPart) current);
            if (prev != null)
                return prev;
            return findNavPrev(((IRelationshipPart) current).getOwnerSheet()
                    .getCentralBranch().getTopicPart());
        }
        return null;
    }

    private IPart findNavNext(IPart current) {
        if (current.hasRole(GEF.ROLE_NAVIGABLE)) {
            Request navRequest = new Request(GEF.REQ_NAV_NEXT);
            navRequest.setPrimaryTarget(current);
            IViewer viewer = current.getSite().getViewer();
            navRequest.setViewer(viewer);
            current.handleRequest(navRequest, GEF.ROLE_NAVIGABLE);
            Object result = navRequest.getResult(GEF.RESULT_NAVIGATION);
            if (result instanceof IPart[]) {
                IPart[] parts = (IPart[]) result;
                if (parts.length > 0) {
                    IPart part = parts[0];
                    return part;
                }
            }
        }
        return null;
    }

    private IPart findNavPrev(IPart current) {
        if (current.hasRole(GEF.ROLE_NAVIGABLE)) {
            Request navRequest = new Request(GEF.REQ_NAV_PREV)
                    .setPrimaryTarget(current).setViewer(
                            current.getSite().getViewer());
            current.handleRequest(navRequest, GEF.ROLE_NAVIGABLE);
            Object result = navRequest.getResult(GEF.RESULT_NAVIGATION);
            if (result instanceof IPart[]) {
                IPart[] parts = (IPart[]) result;
                if (parts.length > 0)
                    return parts[0];
            }
        }
        return null;
    }

    private IPart firstBoundary(IBranchPart branch) {
        List<IBoundaryPart> boundaries = branch.getBoundaries();
        if (boundaries.isEmpty())
            return null;
        int start = -1, end = -1;
        IBoundaryPart first = null;
        for (IBoundaryPart b : boundaries) {
            int s = b.getBoundary().getStartIndex();
            int e = b.getBoundary().getEndIndex();
            if (start < 0 || s < start || (s == start && (end < 0 || e > end))) {
                start = s;
                end = e;
                first = b;
            }
        }
        return first;
    }

    private IPart lastBoundary(IBranchPart branch) {
        List<IBoundaryPart> boundaries = branch.getBoundaries();
        if (boundaries.isEmpty())
            return null;
        int start = -1, end = -1;
        IBoundaryPart last = null;
        for (IBoundaryPart b : boundaries) {
            int s = b.getBoundary().getStartIndex();
            int e = b.getBoundary().getEndIndex();
            if (start < 0 || s > start || (s == start && (end < 0 || e < end))) {
                start = s;
                end = e;
                last = b;
            }
        }
        return last;
    }

    private IPart nextBoundary(IBoundaryPart boundary) {
        List<IBoundaryPart> boundaries = boundary.getOwnedBranch()
                .getBoundaries();
        int start0 = boundary.getBoundary().getStartIndex();
        int end0 = boundary.getBoundary().getEndIndex();
        int start = -1, end = -1;
        IBoundaryPart next = null;
        for (IBoundaryPart b : boundaries) {
            if (b != boundary) {
                int s = b.getBoundary().getStartIndex();
                int e = b.getBoundary().getEndIndex();
                if (s > start0 || (s == start0 && e < end0)) {
                    if (start < 0 || s < start
                            || (s == start && (end < 0 || e > end))) {
                        start = s;
                        end = e;
                        next = b;
                    }
                }
            }
        }
        return next;
    }

    private IPart prevBoundary(IBoundaryPart boundary) {
        List<IBoundaryPart> boundaries = boundary.getOwnedBranch()
                .getBoundaries();
        int start0 = boundary.getBoundary().getStartIndex();
        int end0 = boundary.getBoundary().getEndIndex();
        int start = -1, end = -1;
        IBoundaryPart next = null;
        for (IBoundaryPart b : boundaries) {
            if (b != boundary) {
                int s = b.getBoundary().getStartIndex();
                int e = b.getBoundary().getEndIndex();
                if (s < start0 || (s == start0 && e > end0)) {
                    if (start < 0 || s > start
                            || (s == start && (end < 0 || e < end))) {
                        start = s;
                        end = e;
                        next = b;
                    }
                }
            }
        }
        return next;
    }

    private List<IRelationshipPart> getRelationships(ISheetPart sheet) {
        if (sheet != null) {
            return sheet.getRelationships();
        }
        return null;
    }

    private IPart firstRelationship(ISheetPart sheet) {
        List<IRelationshipPart> rels = getRelationships(sheet);
        if (rels != null && !rels.isEmpty())
            return rels.get(0);
        return null;
    }

    private IPart lastRelationship(ISheetPart sheet) {
        List<IRelationshipPart> rels = getRelationships(sheet);
        if (rels != null && !rels.isEmpty())
            return rels.get(rels.size() - 1);
        return null;
    }

    private IPart nextRelationship(IRelationshipPart rel) {
        List<IRelationshipPart> rels = getRelationships(rel.getOwnerSheet());
        if (rels != null && !rels.isEmpty()) {
            int index = rels.indexOf(rel);
            if (index >= 0 && index < rels.size() - 1)
                return rels.get(index + 1);
        }
        return null;
    }

    private IPart prevRelationship(IRelationshipPart rel) {
        List<IRelationshipPart> rels = getRelationships(rel.getOwnerSheet());
        if (rels != null && !rels.isEmpty()) {
            int index = rels.indexOf(rel);
            if (index > 0)
                return rels.get(index - 1);
        }
        return null;
    }

    private boolean isCentral(IPart part) {
        return part instanceof ITopicPart
                && ((ITopicPart) part).getOwnerBranch().isCentral();
    }

    protected FloatingTextEditor getTextEditor(IPart part) {
        ITool editTool = getEditTool(part);
        if (editTool instanceof FloatingTextEditTool) {
            return ((FloatingTextEditTool) editTool).getEditor();
        }
        return null;
    }

    protected boolean isEditing(IPart part) {
        ITool editTool = getEditTool(part);
        return editTool != null
                && ((AbstractTool) editTool).getStatus()
                        .isStatus(GEF.ST_ACTIVE);
    }

    protected ITool getEditTool(IPart part) {
        EditDomain domain = getCurrentDomain();
        if (part instanceof ILabelPart) {
            return domain == null ? null : domain
                    .getTool(MindMapUI.TOOL_EDIT_LABEL);
        } else if (part instanceof IBoundaryPart
                || part instanceof IRelationshipPart) {
            return domain == null ? null : domain.getTool(GEF.TOOL_EDIT);
        } else
            return domain == null ? null : domain
                    .getTool(MindMapUI.TOOL_EDIT_TOPIC_TITLE);

    }

    /**
     * @return
     */
    protected ITool getDefaultTool() {
        EditDomain domain = getCurrentDomain();
        return domain == null ? null : domain.getDefaultTool();
    }

    /**
     * @return
     */
    protected EditDomain getCurrentDomain() {
        IGraphicalEditorPage page = getActivePage();
        return page == null ? null : page.getEditDomain();
    }

    /**
     * @return
     */
    protected IGraphicalViewer getActiveViewer() {
        IGraphicalEditorPage page = getActivePage();
        return page == null ? null : page.getViewer();
    }

    /**
     * @return
     */
    private IGraphicalEditorPage getActivePage() {
        return editor.getActivePageInstance();
    }

}