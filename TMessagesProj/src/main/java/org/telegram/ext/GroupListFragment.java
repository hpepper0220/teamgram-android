package org.telegram.ext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.telegram.ext.model.ApplyModel;
import org.telegram.ext.model.ContactModel;
import org.telegram.ext.model.GroupModel;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;


public class GroupListFragment extends BaseFragment {
    private FrameLayout frameContainerView;
    private RecyclerView listview;
    private SwipeRefreshLayout refreshLayout;
    private RadialProgressView progressView;
    private AnimatorSet animatorSet;
    private int curPage = 1;
    private List<GroupModel> dataList = new ArrayList<>();
    private GroupListAdapter listAdapter;
    private LaunchActivity mParentActivity;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("群聊");
        actionBar.setTitleColor(Color.BLACK);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon), false);
        actionBar.setBackgroundColor(Color.WHITE);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        frameContainerView = new FrameLayout(context);
        frameContainerView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        refreshLayout = new SwipeRefreshLayout(context);
        refreshLayout.setOnRefreshListener(() -> fetchData(false));
        frameContainerView.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listview = new RecyclerView(context);
        listview.setLayoutManager(new LinearLayoutManager(context));

        listAdapter = new GroupListAdapter(context);
        listview.setAdapter(listAdapter);

        refreshLayout.addView(listview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createProgressView(context);

        fetchData(true);

        return frameContainerView;
    }

    private void fetchData(boolean showLoading) {
        if (showLoading) {
            showLoading();
        }
        TLRPC.TL_messages_getDialogs req = new TLRPC.TL_messages_getDialogs();
        req.limit = 100;
        req.exclude_pinned = true;
        req.offset_peer = new TLRPC.TL_inputPeerEmpty();
        getConnectionsManager().sendRequest(req, (response, error) -> {
            AndroidUtilities.runOnUIThread(() -> {
                if (showLoading) {
                    hideLoading();
                }
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
                if (error == null) {
                    dataList.clear();
                    TLRPC.TL_messages_dialogsSlice result = (TLRPC.TL_messages_dialogsSlice) response;
                    for (int i = 0; i < result.chats.size(); i++) {
                        if (result.chats.get(i) instanceof TLRPC.TL_chat) {
                            Log.e("GroupListFragment", "name ------> " + result.chats.get(i).title);
                            GroupModel model = new GroupModel();
                            model.setItemType(GroupModel.typeData);
                            model.setData(result.chats.get(i));
                            dataList.add(model);
                        }
                    }
                    if (dataList.isEmpty()) {
                        GroupModel model = new GroupModel();
                        model.setItemType(GroupModel.typeEmpty);
                        dataList.add(model);
                    }
                    listAdapter.notifyDataSetChanged();
                }
            });
        });
    }

    private class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

        public GroupListAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public GroupListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == ContactModel.typeEmpty) {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_empty, parent, false);
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_group, parent, false);
            }
            return new GroupListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupListAdapter.ViewHolder holder, int position) {
            if (dataList.get(position).getItemType() == ContactModel.typeData) {
                UserCell userCell = new UserCell(context, 1, 1, false);
                holder.ll_container.removeAllViews();
                TLRPC.Chat itemData = dataList.get(position).getData();
                userCell.setData(itemData, itemData.title, null, 0);
                holder.ll_container.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                userCell.setOnClickListener(view -> {
                    Bundle args = new Bundle();
                    args.putLong("chat_id", itemData.id);
                    mParentActivity.presentFragment(new ChatActivity(args));
                });
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
            LinearLayout ll_container;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_container = itemView.findViewById(R.id.ll_container);
            }
        }
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
}
