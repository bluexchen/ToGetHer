package com.exfantasy.together.register;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.exfantasy.together.R;

import java.io.FileNotFoundException;

import info.hoang8f.widget.FButton;

/**
 * Created by user on 2015/11/1.
 */
public class UploadImgDialog extends DialogFragment implements OnClickListener{

    private final String TAG = this.getClass().getSimpleName();

    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK_FROM_CAMERA = 2;

    private DisplayMetrics mPhone;

    private Activity mActivity;
    private ImageView mImgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_upload_img, null);

        mPhone = new DisplayMetrics();
        mActivity.getWindow().getWindowManager().getDefaultDisplay().getMetrics(mPhone);

        FButton btnPickFromGallery = (FButton) view.findViewById(R.id.btn_pick_from_gallery_at_upload_img_dlg);
        btnPickFromGallery.setOnClickListener(this);

        FButton btnPickFromCamera = (FButton) view.findViewById(R.id.btn_pick_from_carema_at_upload_img_dlg);
        btnPickFromCamera.setOnClickListener(this);

        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    public void setImgView(ImageView mImg) {
        this.mImgView = mImg;
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_pick_from_gallery_at_upload_img_dlg == v.getId()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(intent, PICK_FROM_GALLERY);
        }
        else if (R.id.btn_pick_from_carema_at_upload_img_dlg == v.getId()) {
            ContentValues value = new ContentValues();
            value.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            Uri uri =  mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value);
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri.getPath());

            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult --> requestCode: " + requestCode);
        Log.d(TAG, "onActivityResult --> Intent: " + String.valueOf((data != null)));

        // 藉由requestCode判斷是否為開啟相機或開啟相簿而呼叫的，且data不為null
        if ((requestCode == PICK_FROM_CAMERA || requestCode == PICK_FROM_GALLERY) && data != null) {
            Bitmap bitmap = null;
            // 讀取照片，型態為Bitmap
            if (requestCode == PICK_FROM_GALLERY) {
                // 取得照片路徑uri
                Uri uri = data.getData();
                Log.d(TAG, "onActivityResult --> File uri" + uri.toString());

                ContentResolver cr = mActivity.getContentResolver();
                try {
                    bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "onActivityResult --> File not found exception raised, file uri: " + uri.toString());
                }
            }
            else if (requestCode == PICK_FROM_CAMERA) {
                Log.d(TAG, "onActivityResult --> from camera");

                bitmap = (Bitmap) data.getExtras().get("data");
            }
            // 判斷照片為橫向或者為直向，並進入ScalePic判斷圖片是否要進行縮放
            if (bitmap != null) {
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    ScalePic(bitmap, mPhone.heightPixels);
                } else {
                    ScalePic(bitmap, mPhone.widthPixels);
                }
            }
            closeDialog();
        }
    }

    private void ScalePic(Bitmap bitmap, int phone) {
        // 縮放比例預設為1
        float mScale = 1 ;

        // 如果圖片寬度大於手機寬度則進行縮放，否則直接將圖片放入ImageView內
        if (bitmap.getWidth() > phone) {
            // 判斷縮放比例
            mScale = (float)phone/(float)bitmap.getWidth();

            Matrix mMat = new Matrix() ;
            mMat.setScale(mScale, mScale);

            Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    mMat,
                    false);

            mImgView.setImageBitmap(mScaleBitmap);
        }
        else {
            mImgView.setImageBitmap(bitmap);
        }
    }

    private void closeDialog(){
        this.dismiss();
    }
}