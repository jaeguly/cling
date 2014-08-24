package org.oflab.cling.mediaserver.android.transport;


import android.util.Log;

import org.apache.http.entity.AbstractHttpEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * An FileEntity with 'Range' header that it's content is retrieved from a file.
 */
public class RangeFileEntity extends AbstractHttpEntity implements Cloneable {

    protected final File file;
    protected final long startPos;
    protected final long end;
    protected final long length;
    protected final static int BUFFER_SIZE = 8192;

    //
    // Constructor
    //
    public RangeFileEntity(final File file, final String contentType, long startPos, long endPos) {
        super();

        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }

        this.file = file;
        this.startPos = startPos;
        this.end = endPos;
        this.length = (endPos - startPos) + 1;

        setContentType(contentType);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public InputStream getContent() throws IOException {
        try {
            InputStream inputStream = new FileInputStream(this.file);
            inputStream.skip(startPos);

            return inputStream;
        } catch (Exception e) {
            throw new IOException();
        }
    }

    @Override
    public void writeTo(final OutputStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }

        InputStream inStream = new FileInputStream(this.file);

        try {
            inStream.skip(startPos);

            byte[] tmp = new byte[BUFFER_SIZE];
            long readCount = length / BUFFER_SIZE;
            int endByte = (int) (length % BUFFER_SIZE);
            int readed;

            for (long i = 0; i < readCount; i++) {
                if (Thread.interrupted()) {
                    inStream.close();
                    throw new IOException();
                }

                readed = inStream.read(tmp);

                if (readed != -1 && tmp.length == BUFFER_SIZE) {
                    outStream.write(tmp, 0, readed);
                } else {
                    Log.e("RangeFileEntity", "Wrong Range" + tmp.length);
                    break;
                }
            }

            tmp = new byte[endByte];
            readed = inStream.read(tmp);

            if (readed != -1) {
                outStream.write(tmp, 0, readed);
            }

            // flushes this stream
            outStream.flush();
        } finally {
            // closes this stream for freeing any resource during close.
            inStream.close();
        }
    }

    /**
     * Tells that this entity is not streaming.
     */
    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        // File instance is considered immutable
        // No need to make a copy of it
        return super.clone();
    }

}
