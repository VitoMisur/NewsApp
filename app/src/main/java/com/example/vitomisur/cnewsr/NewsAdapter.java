package com.example.vitomisur.cnewsr;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

public class NewsAdapter extends ArrayAdapter<News> {
    public NewsAdapter(Activity context, ArrayList<News> news) {
        super(context, 0, news);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_view, parent, false);
        }
        // get data from the current news
        // and put it in the list view
        News currentNews = getItem(position);
        String section = currentNews.getSection();
        TextView newsSource = listItemView.findViewById(R.id.section);
        newsSource.setText(section);
        String title = currentNews.getTitle();
        TextView newsTitle = listItemView.findViewById(R.id.title);
        newsTitle.setText(title);
        String contributor = currentNews.getContributor();
        TextView autorView = listItemView.findViewById(R.id.contributor);
        autorView.setText(contributor);
        TextView dateView = listItemView.findViewById(R.id.date);
        TextView timeView = listItemView.findViewById(R.id.time);

        // format the int (sec) type date to readable one
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String input = currentNews.getTime();
        Date date;
        try {
            date = parser.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        // also split the date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm ");
        String formattedTime = timeFormat.format(date);

        dateView.setText(formattedDate);
        timeView.setText(formattedTime);

        return listItemView;
    }
}