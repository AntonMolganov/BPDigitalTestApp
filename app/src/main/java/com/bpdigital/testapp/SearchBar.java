package com.bpdigital.testapp;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Anton on 07.11.2015.
 */
public class SearchBar extends FrameLayout {
    EditText searchText;
    ImageView clearText;
    TextChangedListener textlistener;

    public SearchBar(Context context) {
        this(context, null);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View rootView = LayoutInflater.from(context).inflate(R.layout.search_bar, this);

        searchText = (EditText) rootView.findViewById(R.id.editText);
        clearText = (ImageView) rootView.findViewById(R.id.clearImg);
        clearText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
            }
        });

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textlistener != null) textlistener.onTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        searchText.addTextChangedListener(tw);
    }





    public void setTextChangedListener(TextChangedListener listener){
        textlistener = listener;
    }

    public interface TextChangedListener{
        void onTextChanged(CharSequence s);
    }
}
