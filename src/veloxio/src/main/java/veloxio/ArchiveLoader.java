package veloxio;

import veloxio.utils.Stream;
import veloxio.utils.XXHash;

import java.io.IOException;

class ArchiveLoader {
    /**
     * Hasher
     */
    private XXHash hasher;

    /**
     * Loader constructor
     */
    public ArchiveLoader() {
        hasher = new XXHash();
    }

    /**
     * Hasher used to hash paths
     *
     * @return XXHash
     */
    public XXHash GetHasher() {
        return hasher;
    }

    /**
     * Load archive
     *
     * @param path String
     * @param a    Archive
     * @return boolean true if success
     * @throws IOException if archive stream is invalid
     */
    public boolean Load(String path, Archive a) throws IOException {
        a.SetPath(path);

        // Validate header header
        if (!ValidateHeader(a))
            return false;

        // Failed to read entries
        return ReadEntries((a));
    }

    /**
     * Validate header
     *
     * @param a Archive container
     * @return boolean true if everything went well
     */
    private boolean ValidateHeader(Archive a) throws IOException {
        Stream stream = a.GetHandle();

        ArchiveHeader header = new ArchiveHeader();
        header.count = stream.ReadInt();
        header.offset = stream.ReadInt();
        header.magic = stream.ReadString(VeloxConfig.magic.getBytes().length);

        // Invalid magic
        if (!header.magic.equals(VeloxConfig.magic)) {
            System.out.printf("Invalid magic %s != %s. \n", header.magic, VeloxConfig.magic);
            return false;
        }

        a.SetHeader(header);
        return true;
    }

    /**
     * Read entries from index
     *
     * @param a Archive container
     * @return boolean if success true
     * @throws IOException if stream is invalid
     */
    private boolean ReadEntries(Archive a) throws IOException {
        Stream stream = a.GetHandle();
        ArchiveHeader header = a.GetHeader();

        // Seek to offset where the header starts
        stream.getChannel().position(header.offset);

        // Read index
        for (int i = 0; i < header.count; i++) {
            // Archive entry
            ArchiveEntry entry = new ArchiveEntry();

            // This has to be in the exact order to work (we read bytes by bytes)
            entry.path = stream.ReadLong();     // 8
            entry.diskSize = stream.ReadInt();  // 4
            entry.flags = stream.ReadInt();     // 4
            entry.offset = stream.ReadInt();    // 4
            entry.size = stream.ReadInt();      // 4
            entry.decryptedSize = stream.ReadInt(); // 4

            // Register file
            a.RegisterFile(entry);
        }
        return true;
    }
}
