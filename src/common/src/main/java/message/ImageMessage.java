package message;

public class ImageMessage implements java.io.Serializable {
    public byte[] image_;
    public String type_;

    public float brightness_;
    public long duration_;
    public boolean transpose_;

    public boolean processed_;
}
