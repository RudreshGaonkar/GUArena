package com.example.guarena.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for image operations
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 1024; // KB
    private static final int THUMBNAIL_SIZE = 150; // pixels

    /**
     * Save bitmap to internal storage
     * @param context Application context
     * @param bitmap Bitmap to save
     * @param folder Folder name within images directory
     * @param fileName File name without extension
     * @return Saved file path or null if failed
     */
    public static String saveImageToInternalStorage(Context context, Bitmap bitmap,
                                                    String folder, String fileName) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(context.getFilesDir(), Constants.IMAGE_DIRECTORY + "/" + folder);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file
            File file = new File(directory, fileName + ".jpg");

            // Compress and save
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            Log.d(TAG, "Image saved successfully: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load bitmap from file path
     * @param imagePath Path to image file
     * @return Bitmap or null if failed
     */
    public static Bitmap loadImageFromInternalStorage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                return null;
            }

            File file = new File(imagePath);
            if (!file.exists()) {
                Log.w(TAG, "Image file does not exist: " + imagePath);
                return null;
            }

            return BitmapFactory.decodeFile(imagePath);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete image file
     * @param imagePath Path to image file
     * @return true if deleted successfully
     */
    public static boolean deleteImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                return false;
            }

            File file = new File(imagePath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image: " + e.getMessage());
            return false;
        }
    }

    /**
     * Compress bitmap to specified dimensions
     * @param bitmap Original bitmap
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @return Compressed bitmap
     */
    public static Bitmap compressBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate scale ratio
        float ratio = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        if (ratio >= 1.0f) {
            return bitmap; // No need to compress
        }

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Create circular bitmap
     * @param bitmap Original bitmap
     * @return Circular bitmap
     */
    public static Bitmap createCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap squaredBitmap = Bitmap.createBitmap(bitmap,
                (bitmap.getWidth() - size) / 2,
                (bitmap.getHeight() - size) / 2,
                size, size);

        Bitmap circleBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(circleBitmap);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setShader(new android.graphics.BitmapShader(squaredBitmap,
                android.graphics.Shader.TileMode.CLAMP,
                android.graphics.Shader.TileMode.CLAMP));

        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        return circleBitmap;
    }

    /**
     * Get bitmap from URI
     * @param context Application context
     * @param uri Image URI
     * @return Bitmap or null if failed
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error getting bitmap from URI: " + e.getMessage());
            return null;
        }
    }

    /**
     * Rotate bitmap based on EXIF data
     * @param bitmap Original bitmap
     * @param imagePath Path to original image
     * @return Rotated bitmap
     */
    public static Bitmap rotateBitmapIfRequired(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateBitmap(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data: " + e.getMessage());
            return bitmap;
        }
    }

    /**
     * Rotate bitmap by degrees
     * @param bitmap Original bitmap
     * @param degrees Degrees to rotate
     * @return Rotated bitmap
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Create thumbnail bitmap
     * @param bitmap Original bitmap
     * @param size Thumbnail size (both width and height)
     * @return Thumbnail bitmap
     */
    public static Bitmap createThumbnail(Bitmap bitmap, int size) {
        if (bitmap == null) return null;
        return compressBitmap(bitmap, size, size);
    }

    /**
     * Get optimized bitmap from file path
     * @param imagePath Path to image
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Optimized bitmap
     */
    public static Bitmap decodeSampledBitmapFromFile(String imagePath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    /**
     * Calculate sample size for bitmap decoding
     * @param options BitmapFactory options
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Sample size
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Check if file is a valid image
     * @param filePath Path to file
     * @return true if valid image
     */
    public static boolean isValidImageFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        String extension = filePath.toLowerCase();
        return extension.endsWith(".jpg") ||
                extension.endsWith(".jpeg") ||
                extension.endsWith(".png") ||
                extension.endsWith(".bmp");
    }

    /**
     * Get file size in KB
     * @param filePath Path to file
     * @return File size in KB
     */
    public static long getFileSizeKB(String filePath) {
        try {
            File file = new File(filePath);
            return file.length() / 1024; // Convert bytes to KB
        } catch (Exception e) {
            Log.e(TAG, "Error getting file size: " + e.getMessage());
            return 0;
        }
    }
}
