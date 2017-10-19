package org.xmind.core.tests;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;
import org.xmind.core.*;
import org.xmind.core.io.*;
import org.xmind.core.util.*;

public class WorkbookTestCase {

    @Test
    public void testWorkbookCreation() throws Exception {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
        assertNotNull(workbook);

        ISheet sheet = workbook.getPrimarySheet();
        assertNotNull(sheet);
        assertEquals("", sheet.getTitleText());

        ITopic rootTopic = sheet.getRootTopic();
        assertNotNull(rootTopic);
        assertEquals("", rootTopic.getTitleText());
    }

    @Test
    public void testWorkbookSerializing() throws Exception {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();

        /// 1. Save to an output target
        IOutputTarget target = new ByteArrayStorage().getOutputTarget();
        ISerializer serializer1 = Core.getWorkbookBuilder().newSerializer();
        serializer1.setWorkbook(workbook);
        serializer1.setOutputTarget(target);
        serializer1.serialize(null);

        /// 2. Save to an output stream
        OutputStream targetStream = new ByteArrayOutputStream();
        ISerializer serializer2 = Core.getWorkbookBuilder().newSerializer();
        serializer2.setWorkbook(workbook);
        serializer2.setOutputStream(targetStream);
        serializer2.serialize(null);

        /// 3. Save to workbook's own temp storage
        ISerializer serializer3 = Core.getWorkbookBuilder().newSerializer();
        serializer3.setWorkbook(workbook);
        serializer3.setWorkbookStorageAsOutputTarget();
        serializer3.serialize(null);
    }

    @Test
    public void testWorkbookDeserializing() throws Exception {
        IWorkbook sourceWorkbook = Core.getWorkbookBuilder().createWorkbook();
        sourceWorkbook.getPrimarySheet().setTitleText(UUID.randomUUID().toString());
        sourceWorkbook.getPrimarySheet().getRootTopic().setTitleText(UUID.randomUUID().toString());

        IComment sourceComment1 = sourceWorkbook.getCommentManager().createComment(UUID.randomUUID().toString(),
                System.currentTimeMillis(), sourceWorkbook.getPrimarySheet().getRootTopic().getId());
        sourceComment1.setContent(UUID.randomUUID().toString());
        sourceWorkbook.getCommentManager().addComment(sourceComment1);

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
        serializer.setWorkbook(sourceWorkbook);
        serializer.setOutputStream(byteOutput);
        serializer.serialize(null);

        byte[] fileData = byteOutput.toByteArray();

        IDeserializer deserializer = Core.getWorkbookBuilder().newDeserializer();
        deserializer.setInputStream(new ByteArrayInputStream(fileData));
        deserializer.deserialize(null);

        IWorkbook workbook = deserializer.getWorkbook();

        assertNotNull(workbook);

        ISheet sheet = workbook.getPrimarySheet();
        assertNotNull(sheet);
        assertEquals(sourceWorkbook.getPrimarySheet().getId(), sheet.getId());
        assertEquals(sourceWorkbook.getPrimarySheet().getTitleText(), sheet.getTitleText());

        ITopic rootTopic = sheet.getRootTopic();
        assertNotNull(rootTopic);
        assertEquals(sourceWorkbook.getPrimarySheet().getRootTopic().getId(), rootTopic.getId());
        assertEquals(sourceWorkbook.getPrimarySheet().getRootTopic().getTitleText(), rootTopic.getTitleText());

        Set<IComment> comments = workbook.getCommentManager().getComments(rootTopic.getId());
        assertEquals(1, comments.size());
        IComment comment1 = comments.iterator().next();
        assertEquals(sourceComment1.getContent(), comment1.getContent());
        assertEquals(sourceComment1.getAuthor(), comment1.getAuthor());
        assertEquals(sourceComment1.getTime(), comment1.getTime());
    }

    private static class TestEncodedOutputStream extends FilterOutputStream {

        public TestEncodedOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            if (b == 0xff) {
                b = 0;
            } else {
                b = b + 1;
            }
            super.write(b);
        }
    }

    private static class TestDecodedInputStream extends FilterInputStream {

        public TestDecodedInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b < 0)
                return -1;
            if (b == 0)
                return 0xff;
            return b - 1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int i;
            for (i = 0; i < len; i++) {
                int r = read();
                if (r < 0) {
                    return i == 0 ? -1 : i;
                }
                b[off + i] = (byte) r;
            }
            return i;
        }

    }

    @Test
    public void testNormalization() throws Exception {
        IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
        workbook.getPrimarySheet().setTitleText(UUID.randomUUID().toString());
        workbook.getPrimarySheet().getRootTopic().setTitleText(UUID.randomUUID().toString());
        byte[] attachment1 = "foo bar is ok".getBytes();
        IFileEntry attEntry1 = workbook.getManifest().createAttachmentFromStream(new ByteArrayInputStream(attachment1),
                "a.txt");
        String attPath1 = attEntry1.getPath();
        workbook.getPrimarySheet().getRootTopic().setHyperlink(HyperlinkUtils.toAttachmentURL(attPath1));

        assertArrayEquals(attachment1, toBytes(workbook.getManifest().getFileEntry(attPath1).openInputStream()));
        assertEquals(IEntryStreamNormalizer.NULL, workbook.getAdapter(IEntryStreamNormalizer.class));

        IEntryStreamNormalizer encoder = new IEntryStreamNormalizer() {

            @Override
            public OutputStream normalizeOutputStream(OutputStream stream, IFileEntry fileEntry)
                    throws IOException, CoreException {
                return new TestEncodedOutputStream(stream);
            }

            @Override
            public InputStream normalizeInputStream(InputStream stream, IFileEntry fileEntry)
                    throws IOException, CoreException {
                return new TestDecodedInputStream(stream);
            }
        };

        /// 1. Save to external target with this encoding

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ISerializer serializer1 = Core.getWorkbookBuilder().newSerializer();
        serializer1.setWorkbook(workbook);
        serializer1.setOutputStream(outputStream);
        serializer1.setEntryStreamNormalizer(encoder);
        serializer1.serialize(null);

        assertArrayEquals(attachment1, toBytes(workbook.getManifest().getFileEntry(attPath1).openInputStream()));
        assertEquals(IEntryStreamNormalizer.NULL, workbook.getAdapter(IEntryStreamNormalizer.class));

//        IDeserializer deserializer1 = Core.getSerializationProvider().newDeserializer();
//        deserializer1.setEntryStreamNormalizer(encoder);
//        deserializer1.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
//        deserializer1.setWorkbookStorage(new ByteArrayStorage());
//        deserializer1.deserialize(null);
//
//        IWorkbook workbook2 = deserializer1.getWorkbook();
//        assertEquals(workbook.getPrimarySheet().getId(), workbook2.getPrimarySheet().getId());
//        assertEquals(workbook.getPrimarySheet().getTitleText(), workbook2.getPrimarySheet().getTitleText());
//        assertEquals(workbook.getPrimarySheet().getRootTopic().getId(),
//                workbook2.getPrimarySheet().getRootTopic().getId());
//        assertEquals(workbook.getPrimarySheet().getRootTopic().getTitleText(),
//                workbook2.getPrimarySheet().getRootTopic().getTitleText());
//        assertArrayEquals(attachment1, toBytes(workbook2.getManifest().getFileEntry(attPath1).openInputStream()));

        /// 2. Save to internal storage with new encoding/encryption

        ISerializer serializer2 = Core.getWorkbookBuilder().newSerializer();
        serializer2.setWorkbook(workbook);
        serializer2.setWorkbookStorageAsOutputTarget();
        serializer2.setEntryStreamNormalizer(encoder);

        assertEquals(IEntryStreamNormalizer.NULL, workbook.getAdapter(IEntryStreamNormalizer.class));

        serializer2.serialize(null);

        assertEquals(encoder, workbook.getAdapter(IEntryStreamNormalizer.class));

        assertArrayEquals(attachment1, toBytes(workbook.getManifest().getFileEntry(attPath1).openInputStream()));

    }

    private static byte[] toBytes(InputStream source) throws IOException {
        try {
            ByteArrayOutputStream target = new ByteArrayOutputStream();
            try {
                FileUtils.transfer(source, target, false);
            } finally {
                target.close();
            }
            return target.toByteArray();
        } finally {
            source.close();
        }
    }

}
