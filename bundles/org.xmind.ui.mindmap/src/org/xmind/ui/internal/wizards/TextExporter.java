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
package org.xmind.ui.internal.wizards;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.ui.wizards.ExportPart;
import org.xmind.ui.wizards.ExportUtils;
import org.xmind.ui.wizards.Exporter;
import org.xmind.ui.wizards.IExportPart;
import org.xmind.ui.wizards.IExporter;
import org.xmind.ui.wizards.RelationshipDescription;

class TextExporter extends Exporter {

    private static class EmptyLinePart extends ExportPart implements
            ITextExportPart {

        public EmptyLinePart(IExporter exporter) {
            this(exporter, 1);
        }

        public EmptyLinePart(IExporter exporter, int lineNumber) {
            super(exporter, lineNumber);
        }

        public void write(PrintStream ps) {
            int lineNumber = ((Integer) getElement()).intValue();
            for (int i = 0; i < lineNumber; i++) {
                ps.println();
            }
        }

    }

    private static class TitlePart extends ExportPart implements
            ITextExportPart {

        public TitlePart(IExporter exporter, ITopic element) {
            super(exporter, element);
        }

        public void write(PrintStream ps) {
            ITopic topic = (ITopic) getElement();
            String numberingText = ExportUtils.getNumberingText(topic,
                    getExporter().getCentralTopic());
            if (numberingText != null && !"".equals(numberingText)) { //$NON-NLS-1$
                ps.print(numberingText);
                ps.print(' ');
            }
            ps.println(topic.getTitleText());
        }

    }

    private static class TagsPart extends ExportPart implements ITextExportPart {

        private Set<IMarkerRef> markers;

        private Set<String> labels;

        public TagsPart(IExporter exporter, ITopic element,
                Set<IMarkerRef> markers, Set<String> labels) {
            super(exporter, element);
            this.markers = markers;
            this.labels = labels;
        }

        public void write(PrintStream ps) {
            ps.print(WizardMessages.Export_Tags);
            ps.print(' ');

            boolean hasMarkers = false;
            boolean hasLabels = !labels.isEmpty();

            Iterator<IMarkerRef> markerIt = markers.iterator();
            while (markerIt.hasNext()) {
                IMarker marker = markerIt.next().getMarker();
                if (marker != null) {
                    hasMarkers = true;
                    ps.print(marker.getName());
                    if (markerIt.hasNext()) {
                        ps.print(COMMA);
                    }
                }
            }

            if (hasMarkers && hasLabels) {
                ps.print(COMMA);
            }

            if (hasLabels) {
                Iterator<String> labelIt = labels.iterator();
                while (labelIt.hasNext()) {
                    ps.print(labelIt.next());
                    if (labelIt.hasNext()) {
                        ps.print(COMMA);
                    }
                }
            }
            ps.println();
        }

    }

    private static class NotesPart extends ExportPart implements
            ITextExportPart {

        public NotesPart(IExporter exporter, IPlainNotesContent element) {
            super(exporter, element);
        }

        public void write(PrintStream ps) {
            IPlainNotesContent content = (IPlainNotesContent) getElement();
            String textContent = content.getTextContent();
            ps.println(textContent);
        }

    }

    private static class RelationshipsPart extends ExportPart implements
            ITextExportPart {

        private List<RelationshipDescription> relationships;

        public RelationshipsPart(IExporter exporter, ITopic element,
                List<RelationshipDescription> relationships) {
            super(exporter, element);
            this.relationships = relationships;
        }

        public void write(PrintStream ps) {
            ps.print(WizardMessages.Export_SeeAlso);
            ps.print(' ');
            Iterator<RelationshipDescription> relIt = relationships.iterator();
            while (relIt.hasNext()) {
                ps.print(relIt.next().description);
                if (relIt.hasNext()) {
                    ps.print(COMMA);
                }
            }
            ps.println();
        }

    }

    private static class SummaryPart extends ExportPart implements
            ITextExportPart {

        public SummaryPart(IExporter exporter, ISummary summary) {
            super(exporter, summary);
        }

        public void write(PrintStream ps) {
            ISummary summary = (ISummary) getElement();
            ps.print('(');
            List<ITopic> topics = summary.getEnclosingTopics();
            Iterator<ITopic> topicIt = topics.iterator();
            while (topicIt.hasNext()) {
                ITopic topic = topicIt.next();
                ps.print(topic.getTitleText());
                if (topicIt.hasNext())
                    ps.print(COMMA);
            }
            ps.println(')');
        }
    }

    public TextExporter(ISheet sheet, ITopic centralTopic) {
        super(sheet, centralTopic);
    }

    private PrintStream ps;

    public void setPrintStream(PrintStream ps) {
        this.ps = ps;
    }

    public boolean canStart() {
        return super.canStart() && ps != null;
    }

    protected void write(IProgressMonitor monitor, IExportPart part)
            throws InvocationTargetException, InterruptedException {
        if (ps == null)
            return;

        if (part instanceof ITextExportPart) {
            ((ITextExportPart) part).write(ps);
        }
    }

    public void init() {
        appendTopic(getCentralTopic());
    }

    private void appendTopic(ITopic topic) {
        if (!topic.equals(getCentralTopic()))
            append(new EmptyLinePart(this));

        append(new TitlePart(this, topic));
        collectTopicContent(topic);
    }

    private void collectTopicContent(ITopic topic) {
        Set<IMarkerRef> markers = topic.getMarkerRefs();
        Set<String> labels = topic.getLabels();
        if (!markers.isEmpty() || !labels.isEmpty()) {
            append(new TagsPart(this, topic, markers, labels));
        }

        INotesContent content = topic.getNotes().getContent(INotes.PLAIN);
        if (content instanceof IPlainNotesContent) {
            append(new EmptyLinePart(this));
            append(new NotesPart(this, (IPlainNotesContent) content));
        }

        List<RelationshipDescription> relationships = ExportUtils.getRelationships(topic,
                getRelationships());
        if (!relationships.isEmpty()) {
            append(new EmptyLinePart(this));
            append(new RelationshipsPart(this, topic, relationships));
        }

        for (ITopic child : topic.getChildren(ITopic.ATTACHED)) {
            appendTopic(child);
        }
        for (ISummary summary : topic.getSummaries()) {
            ITopic summaryTopic = summary.getTopic();
            if (summaryTopic != null) {
                appendSummaryTopic(topic, summary, summaryTopic);
            }
        }
        for (ITopic child : topic.getChildren(ITopic.DETACHED)) {
            appendTopic(child);
        }
    }

    private void appendSummaryTopic(ITopic parent, ISummary summary,
            ITopic summaryTopic) {
        append(new EmptyLinePart(this));
        append(new TitlePart(this, summaryTopic));
        append(new SummaryPart(this, summary));
        collectTopicContent(summaryTopic);
    }
}