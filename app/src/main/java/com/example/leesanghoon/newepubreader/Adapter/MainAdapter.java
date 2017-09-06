package com.example.leesanghoon.newepubreader.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.leesanghoon.newepubreader.Model.Book;
import com.example.leesanghoon.newepubreader.R;

import java.util.List;

/**
 * Created by leesanghoon on 2017. 9. 6..
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder>{

    private List<Book> bookList;

    public MainAdapter(List<Book> items) {
        this.bookList = items;
    }

    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_booklist,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainAdapter.ViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.nameTv.setText("Name => "+book.name);
        holder.pathTv.setText("Path => "+book.path);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView pathTv, nameTv;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTv = (TextView)itemView.findViewById(R.id.nameTv);
            pathTv = (TextView)itemView.findViewById(R.id.pathTv);
        }
    }
}
