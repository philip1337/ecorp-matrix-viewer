package veloxio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Provider {
    /**
     * Archives
     */
    private HashMap<String, Archive> archives;

    /**
     * Archive loader
     */
    private ArchiveLoader loader;

    /**
     * Disk support
     */
    private boolean disk_ = false;

    /**
     * Disk path
     */
    private String diskPath_ = "";

    /**
     * Mutex
     */
    private Semaphore mutex_ = new Semaphore(1);

    /**
     * Constructor
     */
    public Provider(boolean disk, String diskPath) {
        archives = new HashMap<>();
        loader = new ArchiveLoader();
        disk_ = disk;
        diskPath_ = diskPath;
    }

    /**
     * Path sanitize (sandbox directory)
     * @param uri previous path
     * @return new path (absolute)
     */
    public String Sanitize(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Remove root slash
        uri = uri.substring(1);
        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Check path traversal
        File file = new File(uri);
        if (file.isAbsolute()) {
            // Absolute paths are not allowed
            return null;
        }
        try
        {
            String canonical = file.getCanonicalPath();
            String absolute = file.getAbsolutePath();

            if (!canonical.equals(absolute))
            {
                // Prevent traversing up
                return null;
            }

            // Convert to absolute path.
            return diskPath_ + File.separator + uri;
        }
        catch (IOException e)
        {
            return null;
        }
    }


    /**
     * Register archive
     *
     * @param path to the archive
     * @return true if it was successfully
     * @throws FileNotFoundException
     */
    public boolean RegisterArchive(String path) throws FileNotFoundException {
        // Archive already registered
        if (archives.containsKey(path))
            return false;

        Archive a = new Archive(path);
        try {
            if (loader.Load(path, a)) {
                archives.put(path, a);
                return true;
            }
        } catch (IOException e) {}
        return false;
    }

    /**
     * Get file
     *
     * @param path to the file in the vfs
     * @return File
     * @throws IOException File not found or path traversal
     */
    public byte[] Get(String path) throws IOException {
        // Try to get file from disk if we support disk paths
        if (disk_) {
            // Sanitize cause we don't wanna give access to the whole hard drive :).
            String sanitized = Sanitize(path);
            if (sanitized != null) {
                File f = new File(sanitized);
                if (f.exists())
                    return Files.readAllBytes(f.toPath());
            } else {
                throw new IOException("Attempted path traversal: " + path);
            }
        }

        // Lock
        try {
            mutex_.acquire();
            long hashedPath = loader.GetHasher().GetPath(path);
            for (HashMap.Entry<String, Archive> entry : archives.entrySet()) {
                Archive archive = entry.getValue();
                if (archive.HasFile(hashedPath)) {
                    ArchiveEntry e = archive.GetEntry(hashedPath);
                    byte[] ret = new ArchiveFile(archive, e, path).Get();
                    mutex_.release();    // Unlock mutex
                    return ret;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Release base
        mutex_.release();

        // If our vfs provider do not own the file we just return an exception
        throw new IOException("File not found: " + path);
    }
}