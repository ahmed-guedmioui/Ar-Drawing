package com.med.drawing.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.med.drawing.R;
import java.util.ArrayList;


public abstract class DrawingListAdapter extends RecyclerView.Adapter<DrawingListAdapter.DrawingListHolder> {
    private Context mContext;
    int screenHeight;
    int screenWidth;
    ArrayList<String> videoList;

    @Override 
    public int getItemViewType(int i) {
        return i;
    }

    public abstract void onDrawingListClickItem(int i, View view);

    public abstract void onGalleryClickItem(int i, View view);

    public DrawingListAdapter(Context context, ArrayList<String> arrayList) {
        this.videoList = arrayList;
        this.mContext = context;
        this.screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override 
    public DrawingListHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new DrawingListHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_folder, (ViewGroup) null));
    }

    @Override 
    public void onBindViewHolder(DrawingListHolder drawingListHolder, final int i) {
        try {
            if (i == 0) {
                drawingListHolder.card_layout2.setVisibility(View.VISIBLE);
                drawingListHolder.card_layout.setVisibility(View.GONE);
            } else {
                drawingListHolder.card_layout2.setVisibility(View.GONE);
                drawingListHolder.card_layout.setVisibility(View.VISIBLE);
                RequestManager with = Glide.with(this.mContext);
                with.load("file:///android_asset/" + this.videoList.get(i)).apply((BaseRequestOptions<?>) new RequestOptions().placeholder(R.drawable.ic_img_loader).error(R.drawable.ic_img_loader)).into(drawingListHolder.img_bg);
            }
            drawingListHolder.card_layout.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view) {
                    DrawingListAdapter.this.onDrawingListClickItem(i, view);
                }
            });
            drawingListHolder.card_layout2.setOnClickListener(new View.OnClickListener() {
                @Override 
                public void onClick(View view) {
                    DrawingListAdapter.this.onGalleryClickItem(i, view);
                }
            });
        } catch (Exception e) {
            e.toString();
        }
    }

    @Override 
    public int getItemCount() {
        return this.videoList.size();
    }

    
    public class DrawingListHolder extends RecyclerView.ViewHolder {
        LinearLayout card_layout;
        LinearLayout card_layout2;
        public ImageView img_bg;

        public DrawingListHolder(View view) {
            super(view);
            try {
                view.setClickable(false);
                view.setFocusable(false);
                this.img_bg = (ImageView) view.findViewById(R.id.img_bg);
                this.card_layout = (LinearLayout) view.findViewById(R.id.card_layout);
                this.card_layout2 = (LinearLayout) view.findViewById(R.id.card_layout2);
            } catch (Exception e) {
                e.toString();
            }
        }
    }
}
