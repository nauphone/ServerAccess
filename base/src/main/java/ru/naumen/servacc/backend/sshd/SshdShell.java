package ru.naumen.servacc.backend.sshd;

import java.io.IOException;
import org.apache.sshd.client.channel.ChannelShell;
import ru.naumen.servacc.backend.IShell;

/**
 * @author Arkaev Andrei
 * @since 13.12.2019
 */
public class SshdShell implements IShell {

    private final ChannelShell shell;

    public SshdShell(ChannelShell shell) {
        this.shell = shell;
    }

    @Override
    public void changeWindowDimensions(int cols, int rows, int width, int height) throws IOException {
        shell.sendWindowChange(cols, rows, height, width);
    }
}
