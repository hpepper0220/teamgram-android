package org.telegram.ext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.blankj.utilcode.util.ArrayUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.telegram.ext.model.DiscoveryModel;
import org.telegram.ext.respository.SimpleCallback;
import org.telegram.ext.respository.SkRepository;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;

public class SkDiscoveryFragment extends BaseFragment {

    private AnimatorSet animatorSet;
    private SwipeRefreshLayout refreshLayout;
    private FrameLayout frameContainerView;
    private RecyclerView listview;
    private RadialProgressView progressView;
    private List<DiscoveryModel> dataList = new ArrayList<>();
    private DiscoveryAdapter discoveryAdapter;
    private LaunchActivity mParentActivity;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        frameContainerView = new FrameLayout(context);
        frameContainerView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        refreshLayout = new SwipeRefreshLayout(context);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData(currentAccount, classGuid, false, true);
            }
        });
        frameContainerView.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listview = new RecyclerView(context);
        listview.setLayoutManager(new LinearLayoutManager(context));

        discoveryAdapter = new DiscoveryAdapter(context);
        listview.setAdapter(discoveryAdapter);

        refreshLayout.addView(listview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createProgressView(context);

        String userAgent = WebSettings.getDefaultUserAgent(context);
        Log.e("UA", "Default UA: " + userAgent);

        return frameContainerView;
    }

    public void fetchData(int currentCount, int classGuid, boolean showLoading, boolean forceRefresh) {
        if (!forceRefresh && !dataList.isEmpty()) {
            return;
        }
        if (showLoading) {
            showLoading();
        }
        SkRepository.getInstance().getDiscovery(currentCount, classGuid, result -> {
            AndroidUtilities.runOnUIThread(() -> {
                if (showLoading) {
                    hideLoading();
                }
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
                dataList.clear();
                if (result.isEmpty()) {
                    DiscoveryModel model = new DiscoveryModel();
                    model.setItemType(DiscoveryModel.typeEmpty);
                    dataList.add(model);
                } else {
                    for (int i = 0; i < result.size(); i++) {
                        DiscoveryModel model = new DiscoveryModel();
                        model.setItemType(DiscoveryModel.typeData);
                        model.setData(result.get(i));
                        dataList.add(model);
                    }
                }
                discoveryAdapter.notifyDataSetChanged();
            });
        });
    }

    private void createProgressView(Context context) {
        progressView = new RadialProgressView(context);
        progressView.setSize(AndroidUtilities.dp(22));
        progressView.setAlpha(0.0f);
        progressView.setScaleX(0.1f);
        progressView.setScaleY(0.1f);
        progressView.setVisibility(View.INVISIBLE);
        progressView.setProgressColor(Color.BLACK);
        frameContainerView.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    public void showLoading() {
        progressView.setVisibility(View.VISIBLE);
        if (null != animatorSet) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                progressView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animatorSet != null && animatorSet.equals(animation)) {
                    animatorSet = null;
                }
            }
        });
        animator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();

            float scale = 0.1f + 0.9f * val;
            progressView.setScaleX(scale);
            progressView.setScaleY(scale);
            progressView.setAlpha(val);
        });
        animatorSet.playTogether(animator);
        animatorSet.setDuration(150);
        animatorSet.start();
    }

    public void hideLoading() {
        if (null != animatorSet) {
            animatorSet.cancel();
        }
        progressView.setVisibility(View.GONE);
    }

    private class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.ViewHolder> {
        public DiscoveryAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == DiscoveryModel.typeEmpty) {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_empty, parent, false);
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_discovery, parent, false);
            }
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DiscoveryModel model = dataList.get(position);
            if (model.getItemType() == DiscoveryModel.typeData) {
                TLRPC.TL_discoverPage itemData = model.getData();
                holder.atvContent.setText(itemData.title);
                holder.atvContent.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                holder.containerView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, 0xFFFFFFFF, 0xFFF0F0F0));
                holder.containerView.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong("web_id", itemData.id);
                    bundle.putString("web_url", itemData.url);
                    bundle.putString("web_title", itemData.title);
                    bundle.putString("web_img_url", itemData.logo);

                    WebFragment webFragment = new WebFragment(bundle);
                    webFragment.setParentActivity(mParentActivity);

                    mParentActivity.presentFragment(webFragment);
                });

                Glide.with(context).load(itemData.logo).diskCacheStrategy(DiskCacheStrategy.ALL).transform(new RoundedCorners(30)).transition(DrawableTransitionOptions.withCrossFade()).into(holder.imageView);
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return dataList.get(position).getItemType();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout containerView;
            AppCompatTextView atvContent;
            LinearLayout tagContainer;
            AppCompatImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                containerView = itemView.findViewById(R.id.rl_container);
                atvContent = itemView.findViewById(R.id.atv_content);
                tagContainer = itemView.findViewById(R.id.ll_tag_layout);
                imageView = itemView.findViewById(R.id.image_view);
            }
        }
    }
}
