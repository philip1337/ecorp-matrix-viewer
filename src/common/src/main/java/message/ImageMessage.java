package message;

import java.util.ArrayList;
import java.util.List;

public class ImageMessage implements java.io.Serializable {
    public List<byte[]> image_ = new ArrayList<>();
    public String type_;

    public float brightness_;
    public long duration_;
    public boolean transpose_;

    public boolean processed_;
}
