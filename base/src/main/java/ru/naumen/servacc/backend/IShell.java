package ru.naumen.servacc.backend;

import java.io.IOException;

/**
 * @author Arkaev Andrei
 * @since 13.12.2019
 */
public interface IShell {
    void changeWindowDimensions(int cols, int rows, int width, int height) throws IOException;
}
