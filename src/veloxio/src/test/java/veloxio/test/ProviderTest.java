package veloxio.test;


import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import veloxio.Provider;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class ProviderTest {
    @Test
    public void SanitizeValidFile() {
        Provider provider = new Provider(false, "");
        String sanitized = provider.Sanitize("/file");
        assertEquals(File.separator + "file", sanitized);
    }

    @Test
    public void SanitizeSubdirectoryFile() {
        Provider provider = new Provider(false, "");
        String sanitized = provider.Sanitize("/directory/file");
        assertEquals(File.separator + "directory" + File.separator + "file", sanitized);
    }

    @Test
    public void SanitizeAbsoluteWindows() {
        Assumptions.assumeTrue(SystemUtils.IS_OS_WINDOWS);

        Provider provider = new Provider(false, "");
        String sanitized = provider.Sanitize("/C:/file");
        assertNull(sanitized);
    }

    @Test
    public void SanitizeAbsoluteLinux() {
        Assumptions.assumeTrue(SystemUtils.IS_OS_LINUX);

        Provider provider = new Provider(false, "");
        String sanitized = provider.Sanitize("//file");
        assertNull(sanitized);
    }

    @Test
    public void SanitizeRelativeDirectory() {
        Provider provider = new Provider(false, "");
        String sanitized = provider.Sanitize("/../");
        assertNull(sanitized);
    }

    @Test
    public void SanitizeRelativeFile() {
        Provider provider = new Provider(false, "");
        String sanitized = provider.Sanitize("/../file");
        assertNull(sanitized);
    }
}