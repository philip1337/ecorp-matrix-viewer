package veloxio;

class ArchiveEntry {
    /**
     * File path (xxhash)
     */
    public long path;

    /**
     * Bit flags
     */
    public int flags;

    /**
     * File size on disk
     */
    public int diskSize;

    /**
     * Offset in the archive
     */
    public int offset;

    /**
     * Size
     */
    public int size;

    /**
     * Decrypted size
     */
    public int decryptedSize;
}
