package no.kantega.pdf.jersey;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.kantega.pdf.TestResource;
import no.kantega.pdf.mime.MSMediaType;
import org.testng.annotations.Test;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class SessionResourceTest extends AbstractJerseyTest {

    @Override
    protected Class<?> getComponent() {
        return SessionResource.class;
    }

    @Test(expectedExceptions = NotAllowedException.class)
    public void testGetOnSessionCreate() throws Exception {
        target("session").request().get(SessionDescription.class);
    }

    @Test
    public void testSessionCreateAndDelete() throws Exception {

        SessionDescription sessionDescription = target("session").request().post(null, SessionDescription.class);
        assertNotNull(SessionResource.getSessionFactory().findSession(sessionDescription.getId()));
        assertTrue(sessionDescription.getTimeout() > System.currentTimeMillis());
        assertTrue(sessionDescription.isValid());
        assertNotNull(sessionDescription.getId());

        assertEquals(target(String.format("session/%s", sessionDescription.getId())).request().get(SessionDescription.class), sessionDescription);
        assertNotNull(SessionResource.getSessionFactory().findSession(sessionDescription.getId()));

        SessionDescription deletedSessionDescription = target(String.format("session/%s", sessionDescription.getId())).request().delete(SessionDescription.class);
        assertNotEquals(deletedSessionDescription, sessionDescription);
        assertFalse(deletedSessionDescription.isValid());
        assertNull(SessionResource.getSessionFactory().findSession(sessionDescription.getId()));

        assertEquals(target(String.format("session/%s", sessionDescription.getId())).request().get().getStatusInfo(), Response.Status.NOT_FOUND);
    }

    @Test(dependsOnMethods = "testSessionCreateAndDelete")
    public void testSessionUpload() throws Exception {

        File wordFile = TestResource.DOCX.materializeIn(Files.createTempDir());
        SessionDescription sessionDescription = target("session").request().post(null, SessionDescription.class);
        JobDescription jobDescription = target(String.format("session/%s/job", sessionDescription.getId())).request()
                .post(Entity.entity(new FileInputStream(wordFile), MSMediaType.WORD_DOCX), JobDescription.class);

        assertFalse(jobDescription.isDone());
        assertFalse(jobDescription.isCancelled());
        assertNull(jobDescription.getSuccessful());

        String jobName = jobDescription.getName();
        assertNotNull(jobDescription.getName());

        // If the conversion does not complete within this time limit, there is most likely something wrong.
        // It might however also mean that the testing system is running too many processes outside of the JVM.
        Thread.sleep(2000L);

        jobDescription = target(String.format("session/%s/job/%s", sessionDescription.getId(), jobDescription.getName()))
                .request().get(JobDescription.class);

        assertTrue(jobDescription.isDone());
        assertFalse(jobDescription.isCancelled());
        assertTrue(jobDescription.getSuccessful());
        assertEquals(jobDescription.getName(), jobName);

        Response response = target(String.format("session/%s/job", sessionDescription.getId())).request().get();
        assertTrue(response.getMediaType().equals(OutputMediaType.APPLICATION_ZIP_TYPE));

        File folder = Files.createTempDir();
        InputStream inputStream = (InputStream) response.getEntity();
        OutputStream outputStream = new FileOutputStream(TestResource.ZIP.absoluteTo(folder));
        ByteStreams.copy(inputStream, outputStream);

        inputStream.close();
        outputStream.close();

        assertEquals(unpackZip(TestResource.ZIP.absoluteTo(folder), folder), 1);

    }

    private int unpackZip(File zipFile, File targetFolder) throws Exception {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        int fileCount = 0;
        for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
            String fileName = zipEntry.getName();
            File newFile = new File(targetFolder, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            ByteStreams.copy(zipInputStream, fileOutputStream);
            fileOutputStream.close();
            fileCount++;
            assertTrue(newFile.exists());
            assertTrue(newFile.length() > 0L);
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
        return fileCount;
    }

}
