package types;

public class Master {
    public static final int VERSION = 1;
    public static final String HEADER_MAGIC = "u6hn8uANs8h58123h";

    /**
     * NM = Node -> master
     */
    public static final byte HEADER_NM_HELO = 1;

    /**
     * MN = Master -> node
     */
    public static final byte HEADER_MN_EHLO = 2;

    /**
     * MN = Master -> node
     */
    public static final byte HEADER_MN_RAW = 3;

    /**
     * MN = Master -> node
     */
    public static final byte HEADER_MN_IMAGE = 3;
}
