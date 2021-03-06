package com.lanou3g.library.tools;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Risky on 15/10/30.
 */
public class CloseHelper {
    public static final void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
                closeable = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
