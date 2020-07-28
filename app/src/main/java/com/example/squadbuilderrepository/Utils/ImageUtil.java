package com.example.squadbuilderrepository.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
    private static int REQ_WIDTH = 256;
    private static int REQ_HEIGHT = 256;

    public static String getExtensionFromUri(Context context, Uri imageUri) {
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.getContentResolver().getType(imageUri));
    }
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static byte [] convertToBytes(Context context, Uri uri) throws IOException {
        String ext = getExtensionFromUri(context, uri);
        InputStream input = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, REQ_WIDTH, REQ_HEIGHT);

        if (input != null) input.close();
        input = context.getContentResolver().openInputStream(uri);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap =  BitmapFactory.decodeStream(input, null, options);
        if (bitmap != null) {
            if (ext.equalsIgnoreCase("jpg")
                    || ext.equalsIgnoreCase("jpeg"))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteOut);

            else bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOut);
        } else {
            Log.d("SYNCING", "convertToPngBytes: null");
        }
        byte [] data = byteOut.toByteArray();

        if (input != null) input.close();
        byteOut.flush();
        byteOut.close();

        return data;
    }
}
