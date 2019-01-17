package message;

public class EhloMessage implements java.io.Serializable {
    public int version_;
    public String magic_;

    public String hostname_;
    public String vmVersion_;
    public String vmName_;

    public long serverTime_;
}
