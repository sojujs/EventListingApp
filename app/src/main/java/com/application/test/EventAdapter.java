package com.application.test;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.application.test.databinding.EventsListLayoutBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private ArrayList<EventListModel> events;
    private Context context;

    public EventAdapter(ArrayList<EventListModel> events, Context context) {
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventAdapter.ViewHolder viewHolder, int i) {
        viewHolder.binding.tvDate.setText(events.get(i).getDate());
        viewHolder.binding.tvDescription.setText(events.get(i).getDescription());
        viewHolder.binding.tvPlace.setText(events.get(i).getLocation());
        viewHolder.binding.tvPrice.setText(events.get(i).getPrice());
        viewHolder.binding.tvTitle.setText(events.get(i).getTitle());
        viewHolder.binding.tvTime.setText(events.get(i).getTime());
        if (events.get(i).getPicture() != null) {
            byte[] decodedString = Base64.decode(events.get(i).getPicture(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(context)
                    .asBitmap()
                    .load(decodedByte)
                    .into(viewHolder.binding.ivImage);
        }
        viewHolder.binding.tvDescription.setInterpolator(new OvershootInterpolator());
        viewHolder.binding.llLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.binding.tvDescription.isExpanded()) {
                    viewHolder.binding.tvDescription.collapse();
                    viewHolder.binding.ivArrow.setImageResource(R.mipmap.expand);
                } else {
                    viewHolder.binding.tvDescription.expand();
                    viewHolder.binding.ivArrow.setImageResource(R.mipmap.collapse);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private EventsListLayoutBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
