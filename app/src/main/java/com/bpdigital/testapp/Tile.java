package com.bpdigital.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.net.URL;

/**
 * Created by Anton on 08.11.2015.
 */
public class Tile extends FrameLayout {

    private boolean checked = false;
    private boolean loaded = false;
    private BingResult data;
    private Bitmap bmp;

    private View checkbox;
    private ImageView thumbnail;
    private View progress;

    public Tile(Context context) { this(context, null);}

    public Tile(Context context, AttributeSet attrs) {this(context, attrs, 0);    }

    public Tile(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View rootView = LayoutInflater.from(context).inflate(R.layout.tile, this);
        checkbox = rootView.findViewById(R.id.selected);
        thumbnail = (ImageView) rootView.findViewById(R.id.thumbnail);
        progress = rootView.findViewById(R.id.progress);
        refreshView();
    }


    public BingResult getData() {
        return data;
    }
    public void setData(BingResult data) {
        this.data = data;
        refreshView();
    }


    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
        refreshView();
    }


    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
        refreshView();
    }

    private void refreshView(){
        if (isChecked()) {
            checkbox.setVisibility(VISIBLE);
        }else{
            checkbox.setVisibility(GONE);
        }
        if (isLoaded()) {
            thumbnail.setVisibility(VISIBLE);
            progress.setVisibility(GONE);
        }else{
            thumbnail.setVisibility(GONE);
            progress.setVisibility(VISIBLE);
        }
    }

    public void startLoad(){
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(data.getThumbnailUrl());
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                thumbnail.setImageBitmap(bmp);
                setLoaded(true);
                refreshView();
            }
        }.execute();
    }

    public void recycleBitmap(){
        if (bmp!=null) bmp.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
