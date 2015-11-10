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
import android.widget.ImageButton;
import android.widget.ImageView;

import com.exfantasy.together.R;

import java.io.FileNotFoundException;

/**
 * Created by user on 2015/11/1.
 */
public class UploadImgDialog extends DialogFragment implements OnClickListener{
    int id;
    private DisplayMetrics mPhone;
    static final int PHOTO = 1;
    static final int CAMERA = 2;
    private ImageView mImg;
    private Activity mActivity;

    public void setmImg(ImageView mImg) {
        this.mImg = mImg;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "requestCode: " + requestCode);
        Log.d("intent data", String.valueOf((data!= null)));
        //藉由requestCode判斷是否為開啟相機或開啟相簿而呼叫的，且data不為null
        if ((requestCode == CAMERA || requestCode == PHOTO ) && data != null)
        {
            Bitmap bitmap = null;
            //讀取照片，型態為Bitmap
            if(requestCode == PHOTO){
                //取得照片路徑uri
                Uri uri = data.getData();
                Log.d("File uri", uri.toString());
                ContentResolver cr = mActivity.getContentResolver();
                try {
                    bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                } catch (FileNotFoundException e) {
                    Log.d("File uri", uri.toString());
                    e.printStackTrace();
                }
            }
            else if(requestCode == CAMERA){
                Log.d("CAMERA","scalePic");
                bitmap = (Bitmap) data.getExtras().get("data");
            }
            else{
                Log.d("NOTHING","error");
            }
            //判斷照片為橫向或者為直向，並進入ScalePic判斷圖片是否要進行縮放
            if(bitmap != null){
                if(bitmap.getWidth() > bitmap.getHeight()){
                    ScalePic(bitmap, mPhone.heightPixels);
                }
                else{
                    ScalePic(bitmap, mPhone.widthPixels);
                }
            }
            CloseDialog();
        }
    }

    private void ScalePic(Bitmap bitmap, int phone) {
        //縮放比例預設為1
        float mScale = 1 ;

        //如果圖片寬度大於手機寬度則進行縮放，否則直接將圖片放入ImageView內
        if(bitmap.getWidth() > phone )
        {
            //判斷縮放比例
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
            mImg.setImageBitmap(mScaleBitmap);
        }
        else mImg.setImageBitmap(bitmap);
    }




    @Override
    public void onClick(View v) {
        if(R.id.picImg == v.getId()){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            Log.d("Photo", "intent");

            startActivityForResult(intent, PHOTO);
        }
        else if(R.id.cameraImg == v.getId()){
            ContentValues value = new ContentValues();
            value.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            Uri uri=  mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value);
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri.getPath());
            Log.d("Camera", "intent");

            startActivityForResult(intent, CAMERA);
        }
    }


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

        ImageButton btn1 = (ImageButton) view.findViewById(R.id.picImg);
        btn1.setOnClickListener(this);
        ImageButton btn2 = (ImageButton) view.findViewById(R.id.cameraImg);
        btn2.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    private void CloseDialog(){
        UploadImgDialog.this.getDialog().cancel();
    }
}