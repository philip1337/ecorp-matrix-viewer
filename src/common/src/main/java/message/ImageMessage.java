package message;

import types.ImageBuffer;

import java.util.ArrayList;
import java.util.List;

public class ImageMessage implements java.io.Serializable {
    public List<ImageBuffer> image_ = new ArrayList<>();
    public int pause_;
    public String type_;

    public float brightness_;
    public long duration_;
    public boolean transpose_;
    public boolean keepAspectRatio_;

    public boolean processed_;
}
