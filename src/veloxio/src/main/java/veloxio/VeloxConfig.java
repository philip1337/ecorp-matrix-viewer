package veloxio;

public class VeloxConfig {
    public static final int seed = 0x9747b28c;

    /**
     * Velox-io version
     */
    public static int version = 1;

    /**
     * Velox-io magic
     */
    public static String magic = "VeloxVFS";

    /**
     * #define VELOXIO_DEFAULT_MAGIC "VeloxVFS"
     * #define VELOXIO_DEFAULT_MAGIC_SIZE 8
     */

    /**
     * Bit flags
     * 1 << 0 | RAW
     * 1 << 1 | COMPRESSED
     * 1 << 2 | CRYPT
     */
    public static final int RAW = 1;            // 0001
    public static final int COMPRESS = 2;   // 0010
    public static final int CRYPT = 4;          // 0100
}
