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

package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.IBoundary;
import org.xmind.core.INotes;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.IWordContext;
import org.xmind.ui.IWordContextProvider;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author Frank Shaka
 */
public class MindMapWordContextProvider implements IWordContextProvider {

    private class TopicWordContext implements IWordContext {

        private ITopic topic;

        private static final String TYPE = "topic"; //$NON-NLS-1$

        public TopicWordContext(ITopic topic) {
            this.topic = topic;
        }

        public String getContent() {
            return topic.getTitleText();
        }

        public ImageDescriptor getIcon() {
            return MindMapUI.getImages().getTopicIcon(topic, true);
        }

        public String getName() {
            return topic.getTitleText();
        }

        public boolean replaceWord(int start, int length, String replacement) {
            return replaceText(topic, replaceString(topic.getTitleText(), start,
                    length, replacement));
        }

        public void reveal() {
            revealElement(topic, false);
        }

        public void revealWord(int start, int length) {
            revealInvalidWord(topic, start, length, GEF.REQ_EDIT);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TopicWordContext)
                if (this.topic != null
                        && this.topic.equals(((TopicWordContext) obj).topic))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result += result * topic.hashCode();
            result += result * TYPE.hashCode();
            return result;
        }

    }

    private class LabelWordContext implements IWordContext {

        private ITopic topic;

        private static final String TYPE = "label"; //$NON-NLS-1$

        public LabelWordContext(ITopic topic) {
            super();
            this.topic = topic;
        }

        public String getContent() {
            return MindMapUtils.getLabelText(topic.getLabels());
        }

        public ImageDescriptor getIcon() {
            return MindMapUI.getImages().get(IMindMapImages.LABEL, true);
        }

        public String getName() {
            return NLS.bind(MindMapMessages.WordContext_Label_pattern,
                    topic.getTitleText());
        }

        public boolean replaceWord(int start, int length, String replacement) {
            return replaceText(topic,
                    replaceString(MindMapUtils.getLabelText(topic.getLabels()),
                            start, length, replacement),
                    MindMapUI.REQ_MODIFY_LABEL);
        }

        public void reveal() {
            editor.getSite().getSelectionProvider()
                    .setSelection(new StructuredSelection(topic));
        }

        public void revealWord(int start, int length) {
            revealInvalidWord(topic, start, length, MindMapUI.REQ_EDIT_LABEL);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LabelWordContext)
                if (this.topic != null
                        && this.topic.equals(((LabelWordContext) obj).topic))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result += result * topic.hashCode();
            result += result * TYPE.hashCode();
            return result;
        }
    }

    private class NotesWordContext implements IWordContext {

        private ITopic topic;

        private static final String TYPE = "notes"; //$NON-NLS-1$

        public NotesWordContext(ITopic topic) {
            super();
            this.topic = topic;
        }

        public String getContent() {
            IPlainNotesContent plainNotesContent = (IPlainNotesContent) topic
                    .getNotes().getContent(INotes.PLAIN);
            if (plainNotesContent == null)
                return ""; //$NON-NLS-1$
            return plainNotesContent.getTextContent();
        }

        public ImageDescriptor getIcon() {
            return MindMapUI.getImages().get(IMindMapImages.NOTES, true);
        }

        public String getName() {
            return NLS.bind(MindMapMessages.WordContext_Notes_pattern,
                    topic.getTitleText());
        }

        public boolean replaceWord(int start, int length, String replacement) {
            revealWord(start, length);

            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            MPart part = E4Utils.findPart(window,
                    IModelConstants.PART_ID_NOTES);

            if (part != null) {
                Object obj = part.getObject();
                if (obj instanceof IAdaptable) {
                    ITextViewer viewer = (ITextViewer) ((IAdaptable) obj)
                            .getAdapter(ITextViewer.class);
                    if (viewer != null) {
                        String toFind = viewer.getTextWidget().getText(start,
                                start + length - 1);

                        IFindReplaceTarget target = (IFindReplaceTarget) ((IAdaptable) obj)
                                .getAdapter(IFindReplaceTarget.class);
                        if (target != null) {
                            int r = target.findAndSelect(start, toFind, true,
                                    true, false);
                            if (r > -1) {
                                target.replaceSelection(replacement);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        public void reveal() {
            revealElement(topic, false);
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    IWorkbenchWindow window = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow();

                    if (window != null)
                        E4Utils.showPart(
                                IModelConstants.COMMAND_SHOW_MODEL_PART, window,
                                IModelConstants.PART_ID_NOTES, null,
                                IModelConstants.PART_STACK_ID_RIGHT);
                }
            });
        }

        public void revealWord(int start, int length) {
            reveal();

            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            MPart part = E4Utils.findPart(window,
                    IModelConstants.PART_ID_NOTES);

            if (part != null) {
                Object obj = part.getObject();
                if (obj instanceof IAdaptable) {
                    ITextViewer viewer = (ITextViewer) ((IAdaptable) obj)
                            .getAdapter(ITextViewer.class);
                    if (viewer != null) {
                        viewer.setSelectedRange(start, length);
                    }
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NotesWordContext)
                if (this.topic != null
                        && this.topic.equals(((NotesWordContext) obj).topic))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result += result * topic.hashCode();
            result += result * TYPE.hashCode();
            return result;
        }
    }

    private class BoundaryWordContext implements IWordContext {

        private IBoundary boundary;

        public BoundaryWordContext(IBoundary boundary) {
            this.boundary = boundary;
        }

        public String getContent() {
            return boundary.getTitleText();
        }

        public ImageDescriptor getIcon() {
            return MindMapUI.getImages().getElementIcon(boundary, true);
        }

        public String getName() {
            return boundary.getTitleText();
        }

        public boolean replaceWord(int start, int length, String replacement) {
            return replaceText(boundary, replaceString(boundary.getTitleText(),
                    start, length, replacement));
        }

        public void reveal() {
            revealElement(boundary, false);
        }

        public void revealWord(int start, int length) {
            revealInvalidWord(boundary, start, length, GEF.REQ_EDIT);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BoundaryWordContext)
                if (this.boundary != null && this.boundary
                        .equals(((BoundaryWordContext) obj).boundary))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            return 17 * boundary.hashCode();
        }

    }

    private class RelationshipWordContext implements IWordContext {

        private IRelationship relationship;

        public RelationshipWordContext(IRelationship relationship) {
            this.relationship = relationship;
        }

        public String getContent() {
            return relationship.getTitleText();
        }

        public ImageDescriptor getIcon() {
            return MindMapUI.getImages().getElementIcon(relationship, true);
        }

        public String getName() {
            return relationship.getTitleText();
        }

        public boolean replaceWord(int start, int length, String replacement) {
            return replaceText(relationship, replaceString(
                    relationship.getTitleText(), start, length, replacement));
        }

        public void reveal() {
            revealElement(relationship, false);
        }

        public void revealWord(int start, int length) {
            revealInvalidWord(relationship, start, length, GEF.REQ_EDIT);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RelationshipWordContext)
                if (this.relationship != null && this.relationship
                        .equals(((RelationshipWordContext) obj).relationship))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            return 17 * relationship.hashCode();
        }

    }

    private IGraphicalEditor editor;

    /**
     * 
     */
    public MindMapWordContextProvider(IGraphicalEditor editor) {
        this.editor = editor;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.IWordContextProvider#getWordContexts()
     */
    public List<IWordContext> getWordContexts() {
        List<IWordContext> contexts = new ArrayList<IWordContext>();
        IWorkbook workbook = (IWorkbook) editor.getAdapter(IWorkbook.class);
        if (workbook != null) {
            for (ISheet sheet : workbook.getSheets()) {
                getContexts(sheet.getRootTopic(), contexts);
                for (IRelationship relationship : sheet.getRelationships()) {
                    contexts.add(new RelationshipWordContext(relationship));
                }
            }
        }
        return contexts;
    }

    private void getContexts(ITopic topic, List<IWordContext> contexts) {
        contexts.add(new TopicWordContext(topic));
        if (!topic.getLabels().isEmpty()) {
            contexts.add(new LabelWordContext(topic));
        }
        if (!topic.getNotes().isEmpty()) {
            contexts.add(new NotesWordContext(topic));
        }
        for (IBoundary boundary : topic.getBoundaries()) {
            contexts.add(new BoundaryWordContext(boundary));
        }
        for (ITopic child : topic.getAllChildren()) {
            getContexts(child, contexts);
        }
    }

    private void revealElement(Object element, boolean makeActive) {
        if (makeActive) {
            editor.getSite().getPage().activate(editor);
        }
        editor.getSite().getSelectionProvider()
                .setSelection(new StructuredSelection(element));
    }

    private void revealInvalidWord(ISheetComponent element, int start,
            int length, String reqType) {
        revealElement(element, true);
        IGraphicalEditorPage page = editor.findPage(element.getOwnedSheet());
        if (page != null) {
            IGraphicalViewer viewer = page.getViewer();
            EditDomain domain = page.getEditDomain();
            IPart part = viewer.findPart(element);
            if (part != null) {
                Request request = new Request(reqType).setPrimaryTarget(part)
                        .setDomain(domain).setViewer(viewer)
                        .setParameter(GEF.PARAM_FOCUS, Boolean.FALSE)
                        .setParameter(GEF.PARAM_TEXT_SELECTION,
                                new TextSelection(start, length));
                domain.handleRequest(request);
            }
        }
    }

    private boolean replaceText(ISheetComponent element, String newText) {
        return replaceText(element, newText, GEF.REQ_MODIFY);
    }

    private boolean replaceText(ISheetComponent element, String newText,
            String reqType) {
        revealElement(element, false);
        IGraphicalEditorPage page = editor.findPage(element.getOwnedSheet());
        if (page != null) {
            IGraphicalViewer viewer = page.getViewer();
            EditDomain domain = page.getEditDomain();
            IPart part = viewer.findPart(element);
            if (part != null) {
                Request request = new Request(reqType).setPrimaryTarget(part)
                        .setDomain(domain).setViewer(viewer)
                        .setParameter(GEF.PARAM_TEXT, newText);
                domain.handleRequest(request);
                return true;
            }
        }
        return false;
    }

    private static String replaceString(String text, int start, int length,
            String replacement) {
        return text.substring(0, start) + replacement
                + text.substring(start + length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MindMapWordContextProvider) {
            MindMapWordContextProvider provider = (MindMapWordContextProvider) obj;
            if (editor != null && editor.equals(provider.editor))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 17 * editor.hashCode();
    }
}
