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
package org.xmind.ui.internal.notes;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Font;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.findreplace.AbstractFindReplaceOperationProvider;
import org.xmind.ui.mindmap.ITopicPart;

/**
 * @author Karelun huang
 */
public class NotesFindReplaceOperationProvider
        extends AbstractFindReplaceOperationProvider {

    private IAdaptable view;

//    private boolean findingInEditor = false;

    public NotesFindReplaceOperationProvider(IAdaptable view) {
        this.view = view;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider#
     * getContextName()
     */
    public String getContextName() {
        ITopicPart topicPart = (ITopicPart) view.getAdapter(ITopicPart.class);
        if (topicPart != null) {
            return NLS.bind(MindMapMessages.Notes_FindReplaceContextPattern,
                    topicPart.getTopic().getTitleText());
        }
        return MindMapMessages.EmptyNotes_FindReplaceContextName;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.findreplace.IFindReplaceOperationProvider#
     * getContextName(int, org.eclipse.swt.graphics.Font)
     */
    public String getContextName(int maxWidth, Font font) {
        return constrainText(getContextName(), maxWidth, font);
    }

    protected IFindReplaceTarget getFindReplaceTarget() {
        return (IFindReplaceTarget) view.getAdapter(IFindReplaceTarget.class);
    }

    protected boolean findAll(String toFind) {
        return false;
    }

//    @Override
//    public boolean find(String toFind) {
//        if (findingInEditor)
//            return false;
//        return super.find(toFind);
//    }

    @Override
    protected boolean findNext(String toFind) {
        return findInNotes(toFind);
//        return findInNotes(toFind) || findInEditor(toFind);
    }

    private boolean findInNotes(String toFind) {
        return findInNotes(getFindReplaceTarget(), toFind);
    }

    private boolean findInNotes(IFindReplaceTarget target, String toFind) {
        if (target != null && target.canPerformFind()) {
            int offset = target
                    .findAndSelect(
                            isForward()
                                    ? target.getSelection().x
                                            + target.getSelection().y
                                    : target.getSelection().x - 1,
                            toFind, isForward(), isCaseSensitive(),
                            isWholeWord());
            return offset >= 0;
        }
        return false;
    }

//    private boolean findInEditor(String toFind) {
//        IContributedContentsView contributed = (IContributedContentsView) view
//                .getAdapter(IContributedContentsView.class);
//        if (contributed != null) {
//            IWorkbenchPart contributing = contributed.getContributingPart();
//            if (contributing != null) {
//                IFindReplaceOperationProvider frProvider = (IFindReplaceOperationProvider) contributing
//                        .getAdapter(IFindReplaceOperationProvider.class);
//                if (frProvider != null) {
//                    findingInEditor = true;
//                    try {
//                        boolean found = frProvider.find(toFind);
//                        if (found) {
//                            view.getSite().getPage().activate(contributing);
//                        }
//                        return found;
//                    } finally {
//                        findingInEditor = false;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    @Override
    protected boolean replaceAll(String toFind, String toReplaceWith) {
        IFindReplaceTarget target = getFindReplaceTarget();
        boolean found = target.findAndSelect(0, toFind, true, isCaseSensitive(),
                isWholeWord()) >= 0;
        if (found) {
            while (replaceInNotes(target, toFind, toReplaceWith)) {
            }
        }
        return found;
    }

    @Override
    protected boolean replaceNext(String toFind, String toReplaceWith) {
        return replaceInNotes(toFind, toReplaceWith);
//        return replaceInNotes(toFind, toReplaceWith) || findInEditor(toFind);
    }

    private boolean replaceInNotes(String toFind, String toReplaceWith) {
        return replaceInNotes(getFindReplaceTarget(), toFind, toReplaceWith);
    }

    private boolean replaceInNotes(IFindReplaceTarget target, String toFind,
            String toReplaceWith) {
        if (target != null && target.canPerformFind() && target.isEditable()) {
            target.replaceSelection(toReplaceWith);
        }
        return findInNotes(target, toFind);
    }

    public boolean canFind(String toFind) {
        return toFind != null;
    }

    public boolean canFindAll(String toFind) {
        return false;
    }

    public boolean canReplace(String toFind, String toReplaceWith) {
        return canFind(toFind);
    }

    @Override
    public boolean canReplaceAll(String toFind, String toReplaceWith) {
        return super.canReplaceAll(toFind, toReplaceWith);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.findreplace.AbstractFindReplaceOperationProvider
     * #understandsPatameter(int)
     */
    @Override
    public boolean understandsPatameter(int parameter) {
        return super.understandsPatameter(parameter)
                && parameter != PARAM_WORKBOOK
                && parameter != PARAM_CURRENT_MAP;
    }

}
