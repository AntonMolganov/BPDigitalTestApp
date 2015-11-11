package com.bpdigital.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private Activity mActivity;
    private AlertDialog mLoginDialog;
    private AlertDialog mLogoutDialog;
    private LoadTask mLoadTask;
    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    private Button mButton;
    private ProgressBar mProgress;


    private static final String[] sMyScope = new String[]{
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.PHOTOS,
            VKScope.NOHTTPS,
            VKScope.MESSAGES,
            VKScope.DOCS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_main);

        AlertDialog.Builder adb = new AlertDialog.Builder(mActivity);
        adb.setTitle(getResources().getString(R.string.warning_title))
            .setMessage(getResources().getString(R.string.warning_message))
            .setPositiveButton(getResources().getString(R.string.warning_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VKSdk.login(mActivity, sMyScope);
//                    VKSdk.login(mActivity, new String[]{VKScope.WALL, VKScope.PHOTOS, VKScope.OFFLINE, VKScope.NOHTTPS});
                }
            })
            .setNegativeButton(getResources().getString(R.string.warning_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mActivity.finish();
                }
            })
            .setCancelable(false);
        mLoginDialog = adb.create();


        adb.setTitle(getResources().getString(R.string.logout_title));
        adb.setMessage(getResources().getString(R.string.logout_message));
        adb.setPositiveButton(getResources().getString(R.string.logout_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VKSdk.logout();
                mLoginDialog.show();
            }
        });
        adb.setNegativeButton(getResources().getString(R.string.logout_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.setCancelable(false);
        mLogoutDialog = adb.create();

        SearchBar searchbar = (SearchBar) findViewById(R.id.searchBar);
        searchbar.setTextChangedListener(new SearchBar.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s) {
                mImageAdapter.clear();
                if (mLoadTask != null) mLoadTask.cancel(true);
                mLoadTask = new LoadTask();
                mLoadTask.execute(s.toString());
            }
        });

        mGridView = (GridView) findViewById(R.id.gridView);
        mImageAdapter = new ImageAdapter();
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mImageAdapter.click(position);
                return true;
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new ImageDialog(mActivity, mImageAdapter.getImageUrl(position)).show();
            }
        });

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostTask().execute();
            }
        });

        mProgress = (ProgressBar) findViewById(R.id.progress);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!VKSdk.isLoggedIn()){
            mLoginDialog.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            mLogoutDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class ImageAdapter extends BaseAdapter{
        private ArrayList<Tile> tiles;

        private void recycleBitmaps(){
            if (tiles!=null){
                for (int i = 0; i < tiles.size(); i++){
                    tiles.get(i).recycleBitmap();
                }
            }
        }

        public void setData(ArrayList<BingResult> data){

            recycleBitmaps();

            tiles = new ArrayList<>();
            for (int i = 0; i < data.size(); i++){
                Tile tile = new Tile(mActivity);
                tile.setData(data.get(i));
                tiles.add(tile);
            }
            mButton.setVisibility(View.GONE);
            notifyDataSetChanged();
        }

        public void clear(){
            recycleBitmaps();
            tiles = new ArrayList<>();
            mButton.setVisibility(View.GONE);
            notifyDataSetChanged();
        }

        public void click(int position){
            if (tiles.get(position).isLoaded()){
                boolean checked = tiles.get(position).isChecked();
                tiles.get(position).setChecked(!checked);
            }
            boolean hasCheckedItems = false;
            for (int i = 0 ; i < tiles.size(); i++){
                if (tiles.get(i).isChecked()){
                    hasCheckedItems = true;
                    break;
                }
            }
            if (hasCheckedItems){
                mButton.setVisibility(View.VISIBLE);
            }else{
                mButton.setVisibility(View.GONE);
            }
        }

        public ArrayList<String> getCheckedUrls(){
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0 ; i < tiles.size(); i++){
                if (tiles.get(i).isChecked()){
                    result.add(tiles.get(i).getData().getMediaUrl());
                }
            }
            return result;
        }

        public String getImageUrl(int position){
            return tiles.get(position).getData().getMediaUrl();
        }

        @Override
        public int getCount() {
            if (tiles == null) return 0;
            return tiles.size();
        }

        @Override
        public Object getItem(int position) {
            return tiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Tile tile = tiles.get(position);
            if (!tile.isLoaded()) tile.startLoad();
            return tile;
        }
    }

    private class LoadTask extends AsyncTask<String, Void, ArrayList<BingResult>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);

        }

        @Override
        protected ArrayList<BingResult> doInBackground(String... params) {
            return BingProvider.makeQuery(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<BingResult> result) {
            if (!isCancelled()) {
                mImageAdapter.setData(result);
                mGridView.smoothScrollToPosition(0);
            }
            mProgress.setVisibility(View.GONE);
        }

    }

    private class PostTask extends AsyncTask<Void,Void,Void>{
        ProgressDialog pd = new ProgressDialog(mActivity);

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pd.setTitle(getResources().getString(R.string.postingimages));
            pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<String> urls = mImageAdapter.getCheckedUrls();
            for (int i = 0; i < urls.size(); i++){
                if (isCancelled()) break;

                try {
                    URL url = new URL(urls.get(i));
                    final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(bmp, VKImageParameters.jpgImage(0.9f)), 0, 0);
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            if (bmp!=null) bmp.recycle();
                            VKAttachments attachments = new VKAttachments();
                            VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                            attachments.add(photoModel);

                            VKRequest post = VKApi.wall().post(VKParameters.from(VKApiConst.ATTACHMENTS, attachments, VKApiConst.MESSAGE, getResources().getString(R.string.VKpostmessage)));
                            post.setModelClass(VKWallPostResult.class);
                            post.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);
                                }

                                @Override
                                public void onError(VKError error) {
                                    super.onError(error);
                                }
                            });

                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
        }
    }
}
