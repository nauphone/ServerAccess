package ru.naumen.servacc;

import java.io.IOException;
import java.io.InputStream;

class SilentStreamReader implements Runnable
{
    private InputStream stream;

    public SilentStreamReader(InputStream stream)
    {
        super();
        this.stream = stream;
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                stream.read();
            }
        }
        catch (IOException e)
        {
        }
    }
}
