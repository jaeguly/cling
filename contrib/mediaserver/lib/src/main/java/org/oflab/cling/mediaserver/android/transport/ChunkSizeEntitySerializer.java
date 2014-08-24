package org.oflab.cling.mediaserver.android.transport;

import android.util.Log;

import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.entity.EntitySerializer;
import org.apache.http.impl.io.ChunkedOutputStream;
import org.apache.http.impl.io.ContentLengthOutputStream;
import org.apache.http.impl.io.IdentityOutputStream;
import org.apache.http.io.SessionOutputBuffer;

import java.io.IOException;
import java.io.OutputStream;


public class ChunkSizeEntitySerializer extends EntitySerializer {

    private static final String CHUNK_SIZE_NAME = "ChunkSize";
    private static final int CHUNK_SIZE_VALUE = 2048;
    private final ContentLengthStrategy contentlengthStrategy;

    public ChunkSizeEntitySerializer(final ContentLengthStrategy length) {
        super(length);
        this.contentlengthStrategy = length;
    }

    @Override
    protected OutputStream doSerialize(final SessionOutputBuffer outBuffer, final HttpMessage message)
            throws HttpException, IOException {

        long len = this.contentlengthStrategy.determineLength(message);

        if (len == ContentLengthStrategy.CHUNKED) {

            int chunkSize = CHUNK_SIZE_VALUE;

            try {
                chunkSize = message.getParams().getIntParameter(CHUNK_SIZE_NAME, chunkSize);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ChunkedOutputStream(outBuffer, chunkSize);
        } else if (len == ContentLengthStrategy.IDENTITY) {
            return new IdentityOutputStream(outBuffer);
        } else {
            return new ContentLengthOutputStream(outBuffer, len);
        }
    }

}
