package com.med.drawing.main.presentaion.settings.adapter;

import static com.med.drawing.util.MonetizeKt.rateApp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.med.drawing.R;
import com.med.drawing.splash.data.DataManager;

public class RecommendedAppsAdapter extends RecyclerView.Adapter<RecommendedAppsAdapter.ListHolder> {
    private final Activity activity;

    @Override
    public int getItemViewType(int i) {
        return i;
    }

    public RecommendedAppsAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ListHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recommended_app, null));
    }

    @Override
    public void onBindViewHolder(ListHolder listHolder, final int i) {
        listHolder.bind(i);
    }

    @Override
    public int getItemCount() {
        return DataManager.appData.getRecommendedApps().size();
    }


    public class ListHolder extends RecyclerView.ViewHolder {
        private final View view;

        public ListHolder(View view) {
            super(view);
            this.view = view;
        }

        void bind(int position) {
            try {
                Glide.with(activity)
                        .load(DataManager.appData.getRecommendedApps().get(position).getImage())
                        .into(((ImageView) view.findViewById(R.id.app_cover)));

                Glide.with(activity)
                        .load(DataManager.appData.getRecommendedApps().get(position).getIcon())
                        .into(((ImageView) view.findViewById(R.id.app_icon)));

                ((TextView) view.findViewById(R.id.app_title))
                        .setText(DataManager.appData.getRecommendedApps().get(position).getName());

                ((TextView) view.findViewById(R.id.app_desc))
                        .setText(DataManager.appData.getRecommendedApps().get(position).getShortDescription());

                view.setOnClickListener(v -> {
                    rateApp(
                            activity,
                            DataManager.appData.getRecommendedApps().get(position).getUrlOrPackage()
                    );
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
