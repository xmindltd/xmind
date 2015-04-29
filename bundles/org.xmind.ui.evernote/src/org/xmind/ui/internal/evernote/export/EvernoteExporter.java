package org.xmind.ui.internal.evernote.export;

import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.INCLUDE_FILE;
import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.INCLUDE_IMAGE;
import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.INCLUDE_TEXT;
import static org.xmind.ui.internal.evernote.export.EvernoteExportConstants.NOTEBOOK;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IMeta;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.util.Properties;
import org.xmind.ui.evernote.EvernotePlugin;
import org.xmind.ui.internal.evernote.EvernoteMessages;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapExtractor;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.MindMapUtils;

import com.evernote.clients.NoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.LazyMap;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.thrift.TException;

/**
 * @author Jason Wong
 */
public class EvernoteExporter {

    private abstract class EvernoteExportPart {

        protected final String AND = "&amp;"; //$NON-NLS-1$

        protected final String SPACE = "&nbsp;"; //$NON-NLS-1$

        protected final String LESS_THAN = "&lt;"; //$NON-NLS-1$

        protected final String GREATER_THAN = "&gt;"; //$NON-NLS-1$

        protected final String ALIGN_LEFT = "left"; //$NON-NLS-1$

        protected final String ALIGN_CENTER = "center"; //$NON-NLS-1$

        @SuppressWarnings("unused")
        protected final String ALIGN_RIGHT = "right"; //$NON-NLS-1$

        protected Note note;

        protected ITopic topic;

        protected StringBuffer content;

        private File tempDir;

        private final String TEMP_PATH = "export/evernote"; //$NON-NLS-1$

        private final String IMAGE_PATH = "image"; //$NON-NLS-1$

        public EvernoteExportPart(ITopic topic) {
            this.topic = topic;
        }

        public void write(Note note, StringBuffer content) {
            this.note = note;
            this.content = content;

            write();
        }

        public void update(Note note, StringBuffer content) {
            this.note = note;
            this.content = content;

            update();
        }

        protected abstract void write();

        protected abstract void update();

        protected File getTempDir() {
            if (tempDir == null)
                tempDir = createTempDir();
            return tempDir;
        }

        private File createTempDir() {
            String id = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", //$NON-NLS-1$
                    System.currentTimeMillis());

            File dir = new File(Core.getWorkspace().getTempDir(TEMP_PATH), id);
            return FileUtils.ensureDirectory(dir);
        }

        protected File getImageDir() {
            return new File(getTempDir(), IMAGE_PATH);
        }

        protected Data readFileAsData(File file) throws Exception {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byte[] block = new byte[10240];
            int len;
            while ((len = in.read(block)) >= 0)
                byteOut.write(block, 0, len);
            in.close();

            byte[] body = byteOut.toByteArray();
            Data data = new Data();
            data.setSize(body.length);
            data.setBodyHash(MessageDigest.getInstance("MD5").digest(body)); //$NON-NLS-1$
            data.setBody(body);

            return data;
        }

        protected String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte hashByte : bytes) {
                int intVal = 0xff & hashByte;
                if (intVal < 0x10) {
                    sb.append('0');
                }
                sb.append(Integer.toHexString(intVal));
            }
            return sb.toString();
        }

        protected Resource insertFileToResource(File file, String fileName,
                String mimeType) {
            Data data = null;
            try {
                data = readFileAsData(file);
            } catch (Exception e) {
                return null;
            }

            Resource resource = new Resource();
            resource.setData(data);
            resource.setMime(mimeType);

            ResourceAttributes attributes = new ResourceAttributes();
            String name = fileName.replaceAll("\\r\\n|\\r|\\n", " "); //$NON-NLS-1$//$NON-NLS-2$
            attributes.setFileName(name);
            resource.setAttributes(attributes);
            return resource;
        }

        @SuppressWarnings("nls")
        protected String getResourceContent(String mimeType, String hashHex) {
            return "<div><en-media type=\"" + mimeType + "\" hash=\"" + hashHex
                    + "\"/></div>";
        }

        @SuppressWarnings("nls")
        protected String getOverviewContent(String mimeType, String hashHex) {
            return "<div style=\"text-align:center\"><en-media type=\""
                    + mimeType + "\" hash=\"" + hashHex + "\"/></div>";
        }

        protected String findResourceHash(String resourceKey) {
            if (resourceKey == null)
                return null;

            List<Resource> resources = note.getResources();
            if (resources == null)
                return null;

            for (Resource r : resources) {
                ResourceAttributes ra = r.getAttributes();
                if (ra == null)
                    continue;

                LazyMap map = ra.getApplicationData();
                if (map == null)
                    continue;

                for (String key : map.getKeysOnly()) {
                    if (resourceKey.equals(key))
                        return bytesToHex(r.getData().getBodyHash());
                }
            }

            return null;
        }

        protected String getPrefix(int level) {
            if (level == 1)
                return ""; //$NON-NLS-1$

            String str = SPACE;
            int maxPrefix = level <= 5 ? level : 5;
            for (int i = 1; i < maxPrefix; i++) {
                str += str;
            }
            return str;
        }

        @SuppressWarnings("nls")
        protected void writeText(String text, String align, double lineHeight,
                int fontSize) {
            content.append("<div style=\"");
            content.append("text-align:" + align + ";\">");
            content.append("<span style=\"");
            content.append("line-height:" + lineHeight + ";");
            content.append("font-size:" + fontSize + "px;\">");
            content.append(text);
            content.append("</span></div>");
        }

        protected String formatText(String text) {
            return formatString(text.replaceAll("&", AND) //$NON-NLS-1$
                    .replaceAll(">", GREATER_THAN) //$NON-NLS-1$
                    .replaceAll("<", LESS_THAN)); //$NON-NLS-1$
        }

        public StringBuffer getContent() {
            return this.content;
        }
    }

    private class TitlePart extends EvernoteExportPart {

        private final int level;

        public TitlePart(ITopic topic, int level) {
            super(topic);
            this.level = level;
        }

        @Override
        protected void write() {
            if (level == TITLE_LEVEL) {
                writeTitle();
                return;
            }

            if (level == ROOT_TOPIC_LEVEL) {
                writeRootTopic();
                return;
            }

            writeTopic();
        }

        @Override
        protected void update() {
            // do nothing.
        }

        private void writeTitle() {
            String title = formatText(topic.getTitleText());
            writeText(title, ALIGN_CENTER, 3, 25);
        }

        private void writeRootTopic() {
            content.append("<h4 style=\"line-height:1.5;font-size=14px\">" //$NON-NLS-1$
                    + formatText(topic.getTitleText()) + "</h4>"); //$NON-NLS-1$
        }

        private void writeTopic() {
            StringBuffer topicText = new StringBuffer();
            topicText.append(getPrefix(level));
            topicText.append(getNumbering());
            topicText.append(formatText(topic.getTitleText()));
            writeText(topicText.toString(), ALIGN_LEFT, 2, 14);
        }

        private String getNumbering() {
            String numbering = MindMapUtils.getFullNumberingText(topic,
                    MindMapUI.PREVIEW_NUMBER_FORMAT);

            if (numbering != null) {
                numbering += " "; //$NON-NLS-1$
                return numbering;
            }
            return ""; //$NON-NLS-1$
        }

    }

    private class OverviewPart extends EvernoteExportPart {

        private static final String MIME_TYPE = "image/png"; //$NON-NLS-1$

        private static final String OVERVIEW_NAME = "overview.png"; //$NON-NLS-1$

        private GhostShellProvider overviewExportShellProvider;

        public OverviewPart(ITopic topic) {
            super(topic);
        }

        @Override
        protected void write() {
            Resource resource = getResource();
            if (resource == null)
                return;

            String hashHex = bytesToHex(resource.getData().getBodyHash());
            writeOverview(resource, hashHex);

            resourceHashs.put(RESOURCE_OVERVIEW, hashHex);
            note.addToResources(resource);
        }

        @Override
        protected void update() {
            Resource resource = getResource();
            if (resource == null)
                return;

            String newHash = bytesToHex(resource.getData().getBodyHash());
            String oldHash = findResourceHash(RESOURCE_OVERVIEW);
            if (newHash == null || oldHash == null)
                return;

            updateOverview(oldHash, newHash);
            updateData(resource, newHash);
        }

        private void writeOverview(Resource resource, String hashHex) {
            content.append(getOverviewContent(resource.getMime(), hashHex));
        }

        private void updateOverview(String oldHash, String newHash) {
            String newContent = content.toString().replace(oldHash, newHash);
            content = new StringBuffer(newContent);
        }

        private void updateData(Resource resource, String newHash) {
            resourceHashs.put(RESOURCE_OVERVIEW, newHash);
            note.addToResources(resource);
        }

        private File createOverviewDir() {
            File file = new File(getImageDir(), topic.getId()
                    + ImageFormat.PNG.getExtensions().get(0));
            FileUtils.ensureFileParent(file);

            final MindMapImageExporter imgExporter = createOverviewExporter();
            imgExporter.setTargetFile(file);
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    imgExporter.export();
                }
            });
            return file;
        }

        private MindMapImageExporter createOverviewExporter() {
            if (overviewExportShellProvider == null)
                overviewExportShellProvider = new GhostShellProvider(
                        getDisplay());
            Properties properties = new Properties();
            MindMapImageExporter imgExporter = new MindMapImageExporter(
                    getDisplay());
            imgExporter.setSource(new MindMap(topic.getOwnedSheet(), topic),
                    overviewExportShellProvider, properties, null);
            return imgExporter;
        }

        private Display getDisplay() {
            return Display.getDefault();
        }

        private Resource getResource() {
            File overview = createOverviewDir();
            Resource resource = insertFileToResource(overview, OVERVIEW_NAME,
                    MIME_TYPE);
            return resource;
        }

    }

    private class AttachmentPart extends EvernoteExportPart {

        private static final String MIME_TYPE = "application/octet-stream"; //$NON-NLS-1$

        private final IMindMapViewer viewer;

        private MindMapExtractor extractor;

        private IWorkbook workbook;

        public AttachmentPart(ITopic topic, IMindMapViewer viewer) {
            super(topic);
            this.viewer = viewer;
        }

        @Override
        protected void write() {
            Resource resource = getResource();
            if (resource == null)
                return;

            String hashHex = bytesToHex(resource.getData().getBodyHash());
            writeResource(resource, hashHex);

            resourceHashs.put(RESOURCE_XMIND_FILE, hashHex);
            note.addToResources(resource);
        }

        @Override
        protected void update() {
            Resource resource = getResource();
            if (resource == null)
                return;

            String newHash = bytesToHex(resource.getData().getBodyHash());
            String oldHash = findResourceHash(RESOURCE_XMIND_FILE);
            if (newHash == null || oldHash == null)
                return;

            updateResource(oldHash, newHash);
            updateData(resource, newHash);
        }

        private void writeResource(Resource resource, String hashHex) {
            content.append(getResourceContent(resource.getMime(), hashHex));
        }

        private void updateResource(String oldHash, String newHash) {
            String newContentStr = content.toString().replace(oldHash, newHash);
            content = new StringBuffer(newContentStr);
        }

        private void updateData(Resource resource, String newHash) {
            resourceHashs.put(RESOURCE_XMIND_FILE, newHash);
            note.addToResources(resource);
        }

        private File getXMindFile() {
            extractor = new MindMapExtractor(viewer);

            workbook = extractor.extract();
            trimWorkbook();
            generatePreview();
            saveInfoToMetaData(workbook);

            File tempFile = new File(getTempDir(), Core.getIdFactory()
                    .createId() + MindMapUI.FILE_EXT_XMIND);

            try {
                workbook.save(tempFile.getAbsolutePath());
            } catch (IOException e) {
                return null;
            } catch (CoreException e) {
                return null;
            } finally {
                clearTemp();
            }

            return tempFile;
        }

        private void trimWorkbook() {
            for (ISheet sheet : workbook.getSheets())
                trimWorkbook(sheet.getRootTopic());
        }

        private void trimWorkbook(ITopic topic) {
            String hyperlink = topic.getHyperlink();
            if (hyperlink != null) {
                if (HyperlinkUtils.isAttachmentURL(hyperlink))
                    topic.setHyperlink(null);

                if (HyperlinkUtils.getProtocolName(hyperlink) != null
                        && HyperlinkUtils.getProtocolName(hyperlink).equals(
                                "file")) //$NON-NLS-1$
                    topic.setHyperlink(null);

                if (HyperlinkUtils.isInternalURL(hyperlink)) {
                    if (workbook.findTopic(hyperlink.substring(hyperlink
                            .indexOf('#') + 1)) == null)
                        topic.setHyperlink(null);
                }
            }

            if (topic.getExtension("org.xmind.ui.audionotes") != null) //$NON-NLS-1$
                topic.deleteExtension("org.xmind.ui.audionotes"); //$NON-NLS-1$

            for (ITopic subTopic : topic.getAllChildren())
                trimWorkbook(subTopic);
        }

        private void generatePreview() {
            final Display display = Display.getDefault();
            display.syncExec(new Runnable() {
                public void run() {
                    MindMapImageExporter exporter = new MindMapImageExporter(
                            display);
                    exporter.setSource(new MindMap(workbook.getPrimarySheet()),
                            null, null);
                    exporter.setTargetWorkbook(workbook);
                    exporter.export();
                }
            });

        }

        private Resource getResource() {
            File xmind = getXMindFile();
            if (xmind != null && xmind.exists()) {
                return insertFileToResource(xmind, getResourceName(), MIME_TYPE);
            }
            return null;
        }

        private String getResourceName() {
            String format = rootTopic.getTitleText().replaceAll(
                    "\\r\\n|\\r|\\n", " "); //$NON-NLS-1$ //$NON-NLS-2$

            char[] value = format.toCharArray();
            StringBuffer title = new StringBuffer();
            for (char c : value) {
                if (c >= ' ')
                    title.append(c);
            }
            title.append(MindMapUI.FILE_EXT_XMIND);
            return title.toString();
        }

        private void clearTemp() {
            if (extractor != null) {
                extractor.delete();
                extractor = null;
            }
        }

        private void saveInfoToMetaData(IWorkbook workbook) {
            ISheet sheet = workbook.getSheets().get(0);

            if (note != null && sheet != null) {
                IMeta meta = workbook.getMeta();
                meta.setValue(NOTE_GUID_TAG, note.getGuid());
                meta.setValue(SHEET_ID_TAG, sheet.getId());
            }
        }
    }

    private class NotesPart extends EvernoteExportPart {

        private final int level;

        public NotesPart(ITopic topic, int level) {
            super(topic);
            this.level = level;
        }

        @Override
        protected void write() {
            String notes = getPlainText();
            if (notes == null)
                return;

            StringBuffer topicText = new StringBuffer();
            topicText.append(getPrefix(level));
            topicText.append(formatText(notes));
            writeText(topicText.toString(), ALIGN_LEFT, 1, 13);
        }

        @Override
        protected void update() {
            // do nothing.
        }

        private String getPlainText() {
            INotesContent content = topic.getNotes().getContent(INotes.PLAIN);
            if (content != null && content instanceof IPlainNotesContent)
                return ((IPlainNotesContent) content).getTextContent();
            return null;
        }
    }

    private static final String SECTION_NAME = "org.xmind.ui.evernote.export"; //$NON-NLS-1$

    private static final String EVERNOTE_TAG = "Evernote"; //$NON-NLS-1$

    private static final String NOTE_GUID_TAG = EVERNOTE_TAG + "/NoteGuid"; //$NON-NLS-1$

    private static final String SHEET_ID_TAG = EVERNOTE_TAG + "/SheetGuid"; //$NON-NLS-1$

    private static final String RESOURCE_OVERVIEW = "Overview"; //$NON-NLS-1$

    private static final String RESOURCE_XMIND_FILE = "XMindFile"; //$NON-NLS-1$

    private static final int TITLE_LEVEL = -1;

    private static final int ROOT_TOPIC_LEVEL = 0;

    private static Map<String, String> resourceHashs = new HashMap<String, String>();

    private IMindMapViewer viewer;

    private ITopic rootTopic;

    private NoteStoreClient noteStore;

    private List<EvernoteExportPart> parts;

    private IDialogSettings settings;

    private Note note;

    public EvernoteExporter(IMindMapViewer viewer, NoteStoreClient noteStore) {
        this.viewer = viewer;
        this.rootTopic = viewer.getCentralTopic();
        this.noteStore = noteStore;
        this.parts = new ArrayList<EvernoteExportPart>();
    }

    public void export() throws EDAMUserException, EDAMSystemException,
            EDAMNotFoundException, TException {
        init();

        String localNoteGuid = getMetaData(NOTE_GUID_TAG);
        this.note = getNote(localNoteGuid);

        if (localNoteGuid != null && localNoteGuid.equals(note.getGuid())) {
            update(note);
        } else {
            write(note);
        }
        note = noteStore.updateNote(note);

        end();
    }

    private void init() {
        if (getBoolean(INCLUDE_FILE))
            append(new AttachmentPart(rootTopic, viewer));

        append(new TitlePart(rootTopic, TITLE_LEVEL));

        if (getBoolean(INCLUDE_IMAGE))
            append(new OverviewPart(rootTopic));

        if (getBoolean(INCLUDE_TEXT))
            appendTopic(rootTopic, ROOT_TOPIC_LEVEL);
    }

    private void write(Note note) throws EDAMUserException,
            EDAMSystemException, TException, EDAMNotFoundException {
        StringBuffer content = new StringBuffer();

        writeHeader(content);
        writeContent(note, content);
        writeWaterMark(content);
        writeFooter(content);

        note.setContent(content.toString());
    }

    private void writeContent(Note note, StringBuffer content) {
        for (EvernoteExportPart part : parts)
            part.write(note, content);
    }

    private void update(Note note) throws EDAMUserException,
            EDAMSystemException, EDAMNotFoundException, TException {
        String content = updateContent(note);
        note.setContent(content);
    }

    private String updateContent(Note note) {
        StringBuffer content = new StringBuffer(note.getContent());

        for (EvernoteExportPart part : parts) {
            part.update(note, content);
            content = part.getContent();
        }

        return content.toString();
    }

    private void end() throws EDAMUserException, EDAMSystemException,
            EDAMNotFoundException, TException {
        recordResourceMarkers();
        saveInfoToMetaData();
        clear();
    }

    private void clear() {
        parts = null;
    }

    private void appendTopic(ITopic topic, int level) {
        append(new TitlePart(topic, level));
        appendTopicContent(topic, level);
    }

    private void appendTopicContent(ITopic topic, int level) {
        append(new NotesPart(topic, level));

        int nextLevel = level + 1;
        for (ITopic subTopic : topic.getChildren(ITopic.ATTACHED)) {
            appendTopic(subTopic, nextLevel);
        }

        for (ITopic subTopic : topic.getChildren(ITopic.DETACHED)) {
            appendTopic(subTopic, nextLevel);
        }
    }

    private void append(EvernoteExportPart part) {
        if (parts == null)
            parts = new ArrayList<EvernoteExportPart>();
        parts.add(part);
    }

    private boolean getBoolean(String key) {
        if (settings == null)
            settings = getSettings();
        return settings.getBoolean(key);
    }

    private IDialogSettings getSettings() {
        return EvernotePlugin.getDialogSettings(SECTION_NAME);
    }

    private Note getNote(String noteGuid) throws EDAMUserException,
            EDAMSystemException, TException, EDAMNotFoundException {
        String notebookName = getSettings().get(NOTEBOOK);
        boolean isSameSheet = rootTopic.getOwnedSheet().getId()
                .equals(getMetaData(SHEET_ID_TAG));

        if (noteGuid != null && isSameSheet) {
            try {
                Note note = noteStore.getNote(noteGuid, true, true, true, true);
                if (notebookName != null && !"".equals(notebookName)) {//$NON-NLS-1$
                    Notebook notebook = noteStore.getNotebook(note
                            .getNotebookGuid());
                    if (!notebookName.equals(notebook.getName())) {
                        note = createNote(notebookName);
                    } else {
                        if (!note.isActive())
                            note = createNote(notebookName);
                    }
                }
                return note;
            } catch (Exception e) {
            }
        }

        return createNote(notebookName);
    }

    private Note createNote(String notebookName) throws EDAMUserException,
            EDAMSystemException, TException, EDAMNotFoundException {
        Notebook notebook = getNotebook(notebookName);
        Note note = new Note();
        note.setNotebookGuid(notebook.getGuid());
        note.setTitle(getNoteTitle());
        note.setContent(getSimpleNoteContent());
        return noteStore.createNote(note);
    }

    private Notebook getNotebook(String notebookName) throws EDAMUserException,
            EDAMSystemException, TException {
        if (notebookName == null || "".equals(notebookName.trim())) //$NON-NLS-1$
            return noteStore.getDefaultNotebook();

        for (Notebook notebook : noteStore.listNotebooks()) {
            if (notebookName.equals(notebook.getName()))
                return notebook;
        }

        return createNotebook(notebookName);
    }

    private Notebook createNotebook(String notebookName)
            throws EDAMUserException, EDAMSystemException, TException {
        Notebook notebook = new Notebook();
        notebook.setName(notebookName);
        return noteStore.createNotebook(notebook);
    }

    private String getNoteTitle() {
        String str = rootTopic.getTitleText();
        if (str == null || "".equals(str)) //$NON-NLS-1$
            str = EvernoteMessages.EvernoteExporter_Note_untitled;

        String format = str.replaceAll("\\r\\n|\\r|\\n", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$

        char[] value = format.toCharArray();
        StringBuffer title = new StringBuffer();
        for (char c : value) {
            if (c >= ' ' && c != (char) 127)
                title.append(c);
        }

        if (title.length() > 255)
            return title.substring(0, 255);
        return title.toString();
    }

    private void recordResourceMarkers() throws EDAMUserException,
            EDAMSystemException, EDAMNotFoundException, TException {
        if (getBoolean(INCLUDE_IMAGE))
            recordResourceMarker(RESOURCE_OVERVIEW);

        if (getBoolean(INCLUDE_FILE))
            recordResourceMarker(RESOURCE_XMIND_FILE);
    }

    private void recordResourceMarker(String resourceName)
            throws EDAMUserException, EDAMSystemException,
            EDAMNotFoundException, TException {
        String resourceGuid = findResourceGuid(note,
                resourceHashs.get(resourceName));
        if (resourceGuid != null)
            setApplicationData(resourceGuid, resourceName);
    }

    private void saveInfoToMetaData() {
        ISheet sheet = rootTopic.getOwnedSheet();
        if (note != null && sheet != null) {
            IMeta meta = rootTopic.getOwnedWorkbook().getMeta();
            meta.setValue(NOTE_GUID_TAG, note.getGuid());
            meta.setValue(SHEET_ID_TAG, sheet.getId());
        }
    }

    private String getMetaData(String key) {
        IMeta meta = rootTopic.getOwnedWorkbook().getMeta();
        return meta.getValue(key);
    }

    private String findResourceGuid(Note note, String resourceDataHash) {
        if (note.isSetResources()) {
            for (Resource r : note.getResources()) {
                if (r.isSetData() && r.getData().isSetBodyHash()) {
                    String noteResourceHash = bytesToHex(r.getData()
                            .getBodyHash());
                    if (noteResourceHash.equals(resourceDataHash))
                        return r.getGuid();
                }
            }
        }
        return null;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : bytes) {
            int intVal = 0xff & hashByte;
            if (intVal < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(intVal));
        }
        return sb.toString();
    }

    private void setApplicationData(String guid, String key)
            throws EDAMUserException, EDAMSystemException,
            EDAMNotFoundException, TException {
        setApplicationData(guid, key, key);
    }

    private void setApplicationData(String guid, String key, String value)
            throws EDAMUserException, EDAMSystemException,
            EDAMNotFoundException, TException {
        noteStore.setResourceApplicationDataEntry(guid, key, value);
    }

    @SuppressWarnings("nls")
    private void writeHeader(StringBuffer content) {
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append("<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">");
        content.append("<en-note>");
    }

    @SuppressWarnings("nls")
    private void writeWaterMark(StringBuffer content) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
        String date = format.format(Calendar.getInstance().getTime());

        content.append("<div style=\"text-align:right;font-size:12px;color:rgb(153,153,153)\">");
        content.append("<span>" + date + ". Created by XMind" + "</span>");
        content.append("</div>");
    }

    private void writeFooter(StringBuffer content) {
        content.append("</en-note>"); //$NON-NLS-1$
    }

    private String getSimpleNoteContent() {
        StringBuffer content = new StringBuffer();
        writeHeader(content);
        writeWaterMark(content);
        writeFooter(content);
        return content.toString();
    }

    private static String formatString(String text) {
        char[] value = text.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (char c : value) {
            if (c >= ' ' && c != (char) 127) {
                sb.append(c);
            } else if (c == 13 || c == 10) {
                sb.append(c);
            } else {
                sb.append(" "); //$NON-NLS-1$
            }
        }
        return sb.toString();
    }

}
