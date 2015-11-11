package com.bpdigital.testapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.URL;

/**
 * Created by Anton on 08.11.2015.
 */
public class ImageDialog extends Dialog {
    private Bitmap bmp;

    public ImageDialog(Context context, String url) {
        this(context, 0, url);
    }

    public ImageDialog(Context context, int themeResId, String url) {
        this(context, true, null, url);
    }

    protected ImageDialog(final Context context, boolean cancelable, OnCancelListener cancelListener, String url) {
        super(context, cancelable, cancelListener);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        new AsyncTask<String,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(String... params) {
                URL url;
                try {
                    url = new URL(params[0]);
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;

            }

            @Override
            protected void onPostExecute(Boolean withouterror) {
                super.onPostExecute(withouterror);
                if (withouterror){
                    ImageView iv = (ImageView) findViewById(R.id.image);
                    iv.setImageBitmap(bmp);
                }else{
                    Toast.makeText(context, context.getResources().getString(R.string.loaderror), Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                ProgressBar pb = (ProgressBar) findViewById(R.id.progress);
                pb.setVisibility(View.GONE);
            }

        }.execute(url);
    }

    @Override
    public void dismiss() {
        if (bmp != null) bmp.recycle();
        super.dismiss();
    }
}
