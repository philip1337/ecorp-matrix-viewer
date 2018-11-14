package types;

public class Broadcast {
    public static final String HEADER_MAGIC = "9rj91u2sd1238";


    /**
     * NM = Node -> master
     */
    public static final byte HEADER_NM_HELO = 1;

    /**
     * MN = Master -> node
     */
    public static final byte HEADER_MN_EHLO = 2;
}
