package no.kantega.pdf.util;

import com.google.common.io.Files;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class ExportAidTest {

    private static final String ENUM_DATA_PROVIDER_NAME = "EnumProvider";

    private File temporaryFolder;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        temporaryFolder = Files.createTempDir();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        temporaryFolder.delete();
    }

    @DataProvider(name = ENUM_DATA_PROVIDER_NAME)
    public Object[][] enumProvider() {
        Object[][] data = new Object[ShellScript.values().length][1];
        int i = 0;
        for (ShellScript shellScript : ShellScript.values()) {
            data[i++][0] = shellScript;
        }
        return data;
    }

    @Test(dataProvider = ENUM_DATA_PROVIDER_NAME)
    public void testMaterialize(ShellScript shellScript) throws Exception {
        File target = new File(temporaryFolder, shellScript.getScriptName());
        assertFalse(target.exists());
        ExportAid exportAid = new ExportAid(temporaryFolder);
        exportAid.materialize(shellScript);
        assertTrue(target.exists());
        assertTrue(target.canExecute());
    }
}
