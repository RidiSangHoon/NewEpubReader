package com.example.leesanghoon.newepubreader.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.leesanghoon.newepubreader.Activity.ReaderViewActivity;
import com.example.leesanghoon.newepubreader.Model.BookItem;
import com.example.leesanghoon.newepubreader.R;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder>{

    private List<BookItem> bookList;

    public MainAdapter(List<BookItem> items) {
        this.bookList = items;
    }

    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_booklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainAdapter.ViewHolder holder, int position) {
        final BookItem book = bookList.get(position);
        holder.nameTv.setText(book.name);
        holder.pathTv.setText("Path => " + book.path);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context c = view.getContext();
                Intent intent = new Intent(c, ReaderViewActivity.class);
                intent.putExtra("bookItem", book);
                c.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView pathTv, nameTv;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.book_name);
            pathTv = itemView.findViewById(R.id.book_path);
        }
    }
}
