package veloxio;

import veloxio.utils.XXHash;
import veloxio.utils.XXTEA;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;

public class ArchiveWriter {
    /**
     * Path to the archive output
     */
    private String path;

    /**
     * ArchiveHeader
     */
    private ArchiveHeader header;

    /**
     * Files
     */
    private HashMap<Long, ArchiveEntry> files;

    /**
     * Hasher
     */
    private XXHash hasher;

    /**
     * Output file writer
     */
    private FileOutputStream output;

    /**
     * Current offset
     */
    private int currentOffset;

    /**
     * Constructor
     *
     * @param path String to the output
     */
    public ArchiveWriter(String path) throws FileNotFoundException {
        this.files = new HashMap();
        this.path = path;
        this.hasher = new XXHash();
        this.header = new ArchiveHeader();
        this.output = new FileOutputStream(path);

        // Initial position after skipping the archive header padding
        try {
            this.output.getChannel().position(GetArchiveHeaderLength());
        } catch (IOException e) {
            throw new FileNotFoundException("Invalid file stream: " + this.path);
        }
    }

    /**
     * This is the initi
     *
     * @return
     */
    public int GetArchiveHeaderLength() {
        return (2 * 4) + VeloxConfig.magic.getBytes().length;
    }

    /**
     * Write file
     */
    public boolean WriteFile(String path, String diskPath, int flags) throws IOException {
        // Read file into buffer
        byte[] buffer = Files.readAllBytes(new File(diskPath).toPath());

        ArchiveEntry entry = new ArchiveEntry();
        entry.path = hasher.GetPath(path);
        entry.offset = (int)output.getChannel().position();
        entry.flags = flags;
        entry.diskSize = buffer.length;
        entry.size = entry.diskSize;

        //if ((flags & VeloxConfig.CRYPT) == VeloxConfig.RAW) {
        //    // RAW
        //}

        if ((entry.flags & VeloxConfig.COMPRESS) == VeloxConfig.COMPRESS) {
            // COMPRESSED
            LZ4Factory factory = LZ4Factory.fastestInstance();
            LZ4Compressor compressor = factory.fastCompressor();

            int maxCompressedLength = compressor.maxCompressedLength(entry.diskSize);
            byte[] compressed = new byte[maxCompressedLength];
            entry.size = compressor.compress(buffer, 0, entry.diskSize, compressed, 0,
                                                       maxCompressedLength);

            // Get compressed buffer
            ByteBuffer wCompressed = ByteBuffer.wrap(compressed);
            buffer = new byte[entry.size];
            wCompressed.get(buffer, 0, entry.size);
        }

        if ((entry.flags & VeloxConfig.CRYPT) == VeloxConfig.CRYPT) {
            int test = buffer.length;
            buffer = XXTEA.encrypt(buffer, path.getBytes());
            entry.decryptedSize = entry.size;
            entry.size = buffer.length;
        }

        // Write to file
        output.write(buffer);

        // Add file to archive index
        files.put(entry.path, entry);
        return true;
    }

    /**
     * Write index
     *
     * @return boolean true if success
     */
    public boolean WriteIndex() {
        ByteBuffer buffer;

        try {
            header.offset = (int) output.getChannel().position();

            for (HashMap.Entry<Long, ArchiveEntry> set : files.entrySet()) {
                ArchiveEntry entry = set.getValue();

                // Path
                buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.putLong(entry.path);
                output.write(buffer.array());
                buffer.clear();

                // We need an int buffer from now on
                buffer = ByteBuffer.allocate(4);

                // Disk size
                buffer.putInt(entry.diskSize);
                output.write(buffer.array());
                buffer.clear();

                // Flags
                buffer.putInt(entry.flags);
                output.write(buffer.array());
                buffer.clear();

                // Offset
                buffer.putInt(entry.offset);
                output.write(buffer.array());
                buffer.clear();

                // Size
                buffer.putInt(entry.size);
                output.write(buffer.array());
                buffer.clear();

                // Decrypted size
                buffer.putInt(entry.decryptedSize);
                output.write(buffer.array());
                buffer.clear();
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Write header
     *
     * @return boolean true if success
     */
    public boolean WriteHeader() {
        header.count = files.size();
        header.magic = VeloxConfig.magic;

        // Int buffer
        ByteBuffer buffer = ByteBuffer.allocate(4);
        try {
            this.output.getChannel().position(0);

            // Count
            buffer.putInt(header.count);
            output.write(buffer.array());
            buffer.clear();

            // Offset
            buffer.putInt(header.offset);
            output.write(buffer.array());
            buffer.clear();

            // Magic
            output.write(header.magic.getBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
