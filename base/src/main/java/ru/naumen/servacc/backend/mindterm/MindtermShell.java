package ru.naumen.servacc.backend.mindterm;

import com.mindbright.ssh2.SSH2SessionChannel;
import java.io.IOException;
import ru.naumen.servacc.backend.IShell;

/**
 * @author Arkaev Andrei
 * @since 13.12.2019
 */
public class MindtermShell implements IShell {

    private final SSH2SessionChannel shell;

    public MindtermShell(SSH2SessionChannel shell) {
        this.shell = shell;
    }

    @Override
    public void changeWindowDimensions(int cols, int rows, int width, int height) throws IOException {
        shell.sendWindowChange(height * 256 + rows, width *256 + cols);
    }
}
