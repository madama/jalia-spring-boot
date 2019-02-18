package net.etalia.jalia.boot;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;

public class FluxSequenceInputStreamTest {

    private static String SAMPLE = "abcdefghijklmnopqrstuvwxyz";

    private List<DataBuffer> buffers = new ArrayList<>();
    private String fullString = "";
    private Flux<DataBuffer> flux;

    @Before
    public void setupBuffer() {
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        for (int i = 0; i < 10; i++) {
            buffers.add(factory.wrap(SAMPLE.getBytes(StandardCharsets.UTF_8)));
            fullString += SAMPLE;
        }
        flux = Flux.create(subscriber -> {
            buffers.forEach(buffer -> {
                subscriber.next(buffer);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            subscriber.complete();
        });
    }

    @Test
    public void testReadBlocks() throws IOException {
        FluxSequenceInputStream fluxin = new FluxSequenceInputStream();
        new Thread(() -> flux.subscribe(fluxin)).start();
        String string = IOUtils.toString(fluxin, StandardCharsets.UTF_8);
        assertThat(string, equalTo(fullString));
    }

    @Test
    public void testReadSingle() throws IOException {
        FluxSequenceInputStream fluxin = new FluxSequenceInputStream();
        new Thread(() -> flux.subscribe(fluxin)).start();
        int v = -1;
        StringBuffer buffer = new StringBuffer();
        while ((v = fluxin.read()) != -1) {
            buffer.append((char) v);
        }
        assertThat(buffer.toString(), equalTo(fullString));
    }
}