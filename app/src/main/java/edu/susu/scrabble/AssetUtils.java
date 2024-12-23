package edu.susu.scrabble;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Утилитный класс для копирования файлов из assets.
 */
public class AssetUtils {
    public static void copyAsset(Context context, String assetFileName, String destinationPath) {
        try (InputStream in = context.getAssets().open(assetFileName);
             OutputStream out = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            Log.e("AssetUtils", "Ошибка при копировании файла: " + e.getMessage());
        }
    }
}
