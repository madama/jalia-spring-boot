package net.etalia.jalia.boot;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

public class FluxSequenceInputStream extends InputStream implements Subscriber<DataBuffer> {

    private Subscription subscription;
    private LinkedList<DataBuffer> buffers = new LinkedList<>();
    private volatile boolean done;
    private volatile Throwable error;

    private DataBuffer inBuffer;
    private InputStream in;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(DataBuffer dataBuffer) {
        buffers.add(dataBuffer);
    }

    @Override
    public void onError(Throwable t) {
        done = true;
        error = t;
    }

    @Override
    public void onComplete() {
        done = true;
    }

    void nextStream() throws IOException {
        if (in != null) {
            in.close();
        }
        if (inBuffer != null) {
            DataBufferUtils.release(inBuffer);
        }

        inBuffer = null;
        in = null;
        do {
            if (error != null) {
                throw new IOException("Error on incoming DataBuffers", error);
            }
            if (!buffers.isEmpty()) {
                inBuffer = buffers.removeFirst();
                in = inBuffer.asInputStream();
            } else if (done) {
                return;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    done = true;
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } while (inBuffer == null);
    }

    public int available() throws IOException {
        if (in == null) {
            return 0; // no way to signal EOF from available()
        }
        return in.available();
    }

    public int read() throws IOException {
        if (in == null) {
            nextStream();
        }
        while (in != null) {
            int c = in.read();
            if (c != -1) {
                return c;
            }
            nextStream();
        }
        return -1;
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (in == null) {
            nextStream();
            if (in == null && done) {
                return -1;
            }
        }
        if (in == null) {
            return -1;
        }
        do {
            int n = in.read(b, off, len);
            if (n > 0) {
                return n;
            }
            nextStream();
        } while (in != null);
        return -1;
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        if (inBuffer != null) {
            DataBufferUtils.release(inBuffer);
        }
        buffers.forEach(DataBufferUtils::release);
    }
}
