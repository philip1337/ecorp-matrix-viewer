package types;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.util.ArrayList;
import java.util.List;

public class WebSession {
    /**
     * Request for this sessions
     */
    public HttpRequest request_ = null;

    /**
     * Response buffer
     */
    public StringBuilder buffer_ = new StringBuilder();

    /**
     * Route for this session
     */
    public WebRoute route_ = null;

    /**
     * Post body request decoder
     */
    public HttpPostRequestDecoder decoder_ = null;

    /**
     * Attributes (post)
     */
    private List<Attribute> attributes_ = null;

    /**
     * Attributes (post)
     */
    private List<FileUpload> files_ = null;

    /**
     * Constructor
     */
    public WebSession() {
        attributes_ = new ArrayList<Attribute>();
        files_ = new ArrayList<FileUpload>();
    }

    /**
     * Reset
     */
    public void Reset() {
        decoder_ = null;
        route_ = null;
        buffer_.setLength(0);
        attributes_.clear();
    }

    /**
     * Add attribute
     * @param attr data
     */
    public void AddAttribute(Attribute attr) {
        attributes_.add(attr);
    }

    /**
     * AddF file
     * @param file data
     */
    public void AddFile(FileUpload file) {
        files_.add(file);
    }
}
