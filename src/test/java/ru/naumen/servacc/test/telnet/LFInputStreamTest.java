package ru.naumen.servacc.test.telnet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import ru.naumen.servacc.telnet.LFInputStream;

/**
 * @author Andrey Hitrin
 * @since 09.01.14
 */
public class LFInputStreamTest
{

    private LFInputStream stream;

    @Test
    public void itCanReadSingleSymbol() throws IOException
    {
        stream = createStreamFromString("t");
        assertThat(stream.read(), is((int) 't'));
    }

    @Test
    public void itCanDetectEndOfString() throws IOException
    {
        stream = createStreamFromString("a");
        stream.read();
        assertThat(stream.read(), is(-1));
    }

    @Test
    public void readWholeString() throws IOException
    {
        byte[] buffer = new byte[6];
        stream = createStreamFromString("Woot");
        int length = stream.read(buffer);
        assertThat(new String(buffer, 0, length), is("Woot"));
    }

    @Test
    public void readStringWithGivenOffsetAndLength() throws IOException
    {
        byte[] buffer = new byte[6];
        stream = createStreamFromString("Woot");
        int length = stream.read(buffer, 1, 4);
        assertThat(new String(buffer, 1, length), is("Woot"));
    }

    private LFInputStream createStreamFromString(String source)
    {
        return new LFInputStream(new ByteArrayInputStream(source.getBytes()));
    }
}
