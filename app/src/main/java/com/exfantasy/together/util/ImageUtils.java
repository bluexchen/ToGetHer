package com.exfantasy.together.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sergii Nezdolii on 27/07/15.
 *
 * Copyright (c) 2015 Frost°. All rights reserved.
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Allows to fix issue for some phones when image processed with android-crop
     * is not rotated properly.
     * Based on https://github.com/jdamcd/android-crop/issues/140#issuecomment-125109892
     * @param context - context to use while saving file
     * @param uri - origin file uri
     */
    public static void normalizeImageAndSave(Context context, Uri uri) {
        Bitmap bitmap = null;
        Bitmap rotatedBitmap = null;
        try {
            ExifInterface exif = new ExifInterface(uri.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            rotatedBitmap = rotateBitmap(bitmap, orientation);
            if (rotatedBitmap != null) {
                if (!bitmap.equals(rotatedBitmap)) {
                    saveBitmapToUri(context, rotatedBitmap, uri);
                    Log.i(TAG, "Save rotated bitmap to url: <" + uri + "> done");
                }
                String resultFilePath = saveToExternalStorage(rotatedBitmap);
                if (resultFilePath != null) {
                    Log.i(TAG, "Save rotated bitmap to path: <" + resultFilePath + "> done");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException raised while normalize upload image, msg: " + e.toString());
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (rotatedBitmap != null) {
                rotatedBitmap.recycle();
            }
        }
    }

    private static String saveToExternalStorage(Bitmap bitmap) {
        String resultFilePath = null;

        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            File folder = new File(Environment.getExternalStorageDirectory(), "TogetherUploadPhotos");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
            File imgFile = new File(folder.getPath() + File.separator + timestamp + ".jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imgFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();

                resultFilePath = imgFile.getCanonicalPath();
            } catch (Exception e) {
                Log.e(TAG, "Save upload image failed, msg: " + e.toString(), e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Closing FileOutputStream failed, msg: " + e.toString(), e);
                    }
                }
            }
        }
        return resultFilePath;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError raised while trying to rotate bitmap, msg: " + e.toString());
            return null;
        }
    }

    private static void saveBitmapToUri(Context context, Bitmap rotatedImage, Uri saveUri) {
        if (saveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(saveUri);
                if (outputStream != null) {
                    rotatedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            } catch (IOException e) {
                Log.e(TAG, "Cannot open file: " + saveUri.toString() + ", msg: " + e.toString());
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "IOException raised while closing OutputStream, msg: " + e.toString());
                    }
                }
            }
        }
    }
}