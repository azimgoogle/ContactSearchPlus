package com.letbyte.contact.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.letbyte.contact.R;
import com.letbyte.contact.data.provider.DataProvider;

import java.util.ArrayList;

/**
 * Created by Max on 05-Dec-15.
 */
public class SearchViewSuggestionAdapter extends SimpleCursorAdapter {

    private TextView textView;
    private final LayoutInflater inflater;
    private ArrayList<String> suggestionList;

    public SearchViewSuggestionAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        textView.setText((cursor.getString(cursor.getColumnIndex(DataProvider.Entry.KEYWORD))));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.query_suggestion, parent, false);
        textView = (TextView) view.findViewById(R.id.text1);
        return view;
    }

    public void setSuggestionList(ArrayList<String> suggestionList) {
        this.suggestionList = suggestionList;
    }

    /*@Override
    public String getItem(int position) {
        return suggestionList.get(position);
    }*/
}
