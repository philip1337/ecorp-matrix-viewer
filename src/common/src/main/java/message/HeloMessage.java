package message;

public class HeloMessage implements java.io.Serializable {
    public int version_;
    public String magic_;
    public int width;
    public int height;

    public long time_ = 0;
    public String hostname_;
    public String vmVersion_;
    public String vmName_;
}
