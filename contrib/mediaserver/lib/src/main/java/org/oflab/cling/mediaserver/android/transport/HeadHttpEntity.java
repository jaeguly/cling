package org.oflab.cling.mediaserver.android.transport;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Entity that can be sent or received with an HEAD Message
 */
public class HeadHttpEntity implements HttpEntity {

    private Header encoding = null;
    private Header contentType = null;
    private boolean isChunked = false;
    private long length = 0;

    public HeadHttpEntity() {

    }

    public HeadHttpEntity(String contentType, long length) {
        setContentType(contentType);
        setContentLength(length);
    }

    @Override
    public void consumeContent() throws IOException {
        // do nothing
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return null;
    }

    @Override
    public Header getContentEncoding() {
        return encoding;
    }

    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public Header getContentType() {
        return contentType;
    }

    @Override
    public boolean isChunked() {
        return isChunked;
    }

    @Override
    public boolean isRepeatable() {
        // do nothing
        return false;
    }

    @Override
    public boolean isStreaming() {
        // do nothing
        return false;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        // do nothing
    }

    /**
     * Sets ContentEncoding
     */
    public void setContentEncoding(String encoding) {
        setContentEncoding(new BasicHeader(HTTP.CONTENT_ENCODING, encoding));
    }

    /**
     * Sets ContentEncoding
     */
    public void setContentEncoding(Header encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets Content Length
     */
    public void setContentLength(long length) {
        this.length = length;
    }

    /**
     * Sets Content Type
     */
    public void setContentType(String type) {
        setContentType(new BasicHeader(HTTP.CONTENT_TYPE, type));
    }

    /**
     * Sets Content Type
     */
    public void setContentType(Header type) {
        this.contentType = type;
    }

    public void setChunked(boolean _isChunked) {
        isChunked = _isChunked;
    }
}
