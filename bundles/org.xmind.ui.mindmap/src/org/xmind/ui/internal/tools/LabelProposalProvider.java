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
package org.xmind.ui.internal.tools;

import static org.xmind.ui.mindmap.MindMapUI.LABEL_SEPARATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IFilter;
import org.xmind.core.ILabeled;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.core.util.ILabelRefCounter;
import org.xmind.gef.part.IPart;
import org.xmind.ui.util.MindMapUtils;

public class LabelProposalProvider implements IContentProposalProvider {

    private static final String[] EMPTY = new String[0];

    private String[] usedLabels;

    public LabelProposalProvider(IPart part) {
        this.usedLabels = findUsedLabels(part);
    }

    private String[] findUsedLabels(IPart part) {
        Object m = MindMapUtils.getRealModel(part);
        if (m instanceof ILabeled && m instanceof ISheetComponent) {
            ISheet sheet = ((ISheetComponent) m).getOwnedSheet();
            if (sheet != null) {
                ILabelRefCounter counter = (ILabelRefCounter) sheet
                        .getAdapter(ILabelRefCounter.class);
                if (counter != null) {
                    String[] array = counter.getCountedRefs().toArray(EMPTY);
                    Arrays.sort(array);
                    return array;
                }
            }
        }
        return EMPTY;
    }

    public IContentProposal[] getProposals(String contents, int position) {
        Collection<String> labelsBefore = new ArrayList<String>();
        int prevSepIndex = contents.lastIndexOf(LABEL_SEPARATOR, position - 1);
        if (prevSepIndex > 0) {
            fillLabelList(labelsBefore,
                    prevSepIndex < 0 ? "" : contents.substring(0, prevSepIndex)); //$NON-NLS-1$
        }

        Collection<String> labelsAfter = new ArrayList<String>();
        int nextSepIndex = contents.indexOf(LABEL_SEPARATOR, position);
        if (nextSepIndex > 0) {
            fillLabelList(labelsAfter, contents.substring(nextSepIndex + 1,
                    contents.length()));
        }

        List<String> propTexts = new ArrayList<String>();
        for (int i = 0; i < usedLabels.length; i++) {
            String prop = usedLabels[i];
            if (!labelsBefore.contains(prop) && !labelsAfter.contains(prop)) {
                propTexts.add(prop);
            }
        }

        String prefix = makeLabelText(labelsBefore);
        String suffix = makeLabelText(labelsAfter);//contents.substring( position );

        IFilter filter = makeFilter(contents.substring(prevSepIndex + 1,
                position).trim());
        List<IContentProposal> props = new ArrayList<IContentProposal>(
                propTexts.size());
        for (String propText : propTexts) {
            if (filter.select(propText)) {
                props.add(makeContentProposal(prefix, suffix, propText));
            }
        }
        if (!props.isEmpty()) {
            props.add(0, makeContentProposal(prefix, suffix, getCurrentLabel(
                    contents, prevSepIndex, nextSepIndex)));
        }
        return props.toArray(new IContentProposal[props.size()]);
    }

    private String getCurrentLabel(String contents, int prevSepIndex,
            int nextSepIndex) {
        if (nextSepIndex < 0)
            nextSepIndex = contents.length();
        return contents.substring(prevSepIndex + 1, nextSepIndex).trim();
    }

    private IFilter makeFilter(final String filterText) {
        return new IFilter() {
            int filterLength = filterText.length();

            public boolean select(Object toTest) {
                if (toTest instanceof String) {
                    String propText = (String) toTest;
                    return propText.length() > filterLength
                            && propText.substring(0, filterLength)
                                    .equalsIgnoreCase(filterText);
                }
                return false;
            }
        };
    }

    private String makeLabelText(Collection<String> labels) {
        StringBuffer sb = new StringBuffer(labels.size() * 10);
        for (String label : labels) {
            if (sb.length() > 0) {
                sb.append(LABEL_SEPARATOR);
                sb.append(' ');
            }
            sb.append(label);
        }
        return sb.toString();
    }

    private void fillLabelList(Collection<String> labelList, String contents) {
        String[] labels = contents.split(LABEL_SEPARATOR);
        for (int i = 0; i < labels.length; i++) {
            labelList.add(labels[i].trim());
        }
    }

    private IContentProposal makeContentProposal(final String prefix,
            final String suffix, final String proposal) {
        return new IContentProposal() {
            String content = null;
            int preLength = prefix.length();
            int sufLength = suffix.length();
            int propLength = proposal.length();
            int cursorPosition = -1;

            public String getContent() {
                if (content == null) {
                    StringBuffer sb = new StringBuffer(preLength + sufLength
                            + propLength + 4);
                    if (preLength > 0) {
                        sb.append(prefix);
                        sb.append(LABEL_SEPARATOR);
                        sb.append(' ');
                    }
                    sb.append(proposal);
                    if (sufLength > 0) {
                        sb.append(LABEL_SEPARATOR);
                        sb.append(' ');
                        sb.append(suffix);
                    }
                    if (propLength > 0 || sufLength > 0) {
                        sb.append(LABEL_SEPARATOR);
                        sb.append(' ');
                    }
                    content = sb.toString();
                }
                return content;
            }

            public String getDescription() {
                return null;
            }

            public String getLabel() {
                return getContent();
            }

            public int getCursorPosition() {
                if (cursorPosition < 0) {
                    cursorPosition = propLength;
                    if (preLength > 0) {
                        cursorPosition += preLength + 2;
                    }
                    if (propLength > 0 || sufLength > 0) {
                        cursorPosition += 2;
                    }
                }
                return cursorPosition;
            }

            @Override
            public String toString() {
                return getContent();
            }
        };
    }

}