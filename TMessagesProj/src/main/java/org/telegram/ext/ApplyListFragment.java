package org.telegram.ext;

import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.csdn.roundview.RoundTextView;

import org.checkerframework.checker.units.qual.A;
import org.telegram.ext.model.ApplyModel;
import org.telegram.ext.model.ContactModel;
import org.telegram.ext.respository.SimpleCallback;
import org.telegram.ext.respository.SkRepository;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;

import me.majiajie.pagerbottomtabstrip.internal.RoundMessageView;

public class ApplyListFragment extends BaseFragment {

    private LaunchActivity mParentActivity;

    private FrameLayout frameContainerView;
    private RecyclerView listview;
    private SwipeRefreshLayout refreshLayout;
    private RadialProgressView progressView;
    private AnimatorSet animatorSet;
    private int curPage = 1;
    private List<ApplyModel> dataList = new ArrayList<>();
    private ApplyListAdapter listAdapter;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("新的朋友");
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
        refreshLayout.setOnRefreshListener(() -> fetchData(false, true));
        frameContainerView.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listview = new RecyclerView(context);
        listview.setLayoutManager(new LinearLayoutManager(context));

        listAdapter = new ApplyListAdapter(context);
        listview.setAdapter(listAdapter);

        refreshLayout.addView(listview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createProgressView(context);

        fetchData(true, false);

        return frameContainerView;
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

    private void fetchData(boolean showLoading, boolean forceRefresh) {
        if (showLoading) {
            showLoading();
        }
        SkRepository.getInstance().getApplyList(currentAccount, classGuid, curPage, result -> {
            if (showLoading) {
                hideLoading();
            }
            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }

            dataList.clear();
            Log.e("ApplyListFragment", "list length ------> " + result.list.size() + " users length ------> " + result.users.size() + " -------> count: " + result.count);
            if (result.list.isEmpty()) {
                dataList.add(ApplyModel.empty());
            } else {
                for (int i = 0; i < result.list.size(); i++) {
                    if (i < result.users.size() && result.list.get(i).user_id != getUserConfig().getClientUserId() && result.users.get(i).id != getUserConfig().getClientUserId()) {
                        ApplyModel model = new ApplyModel();
                        model.setItemType(ApplyModel.typeData);
                        model.setApply(result.list.get(i));
                        model.setUser(result.users.get(i));
                        dataList.add(model);
                    }
                }
            }

            if (dataList.isEmpty()) {
                dataList.add(ApplyModel.empty());
            }

            listAdapter.notifyDataSetChanged();
        });
    }

    private void agreeApply(Context context, ApplyModel model) {
        final AlertDialog dialog = new AlertDialog(context, AlertDialog.ALERT_TYPE_SPINNER);
        dialog.setMessage(LocaleController.getString(R.string.Loading));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        AndroidUtilities.runOnUIThread(dialog::show, 250);

        TLRPC.TL_contacts_acceptContact req = new TLRPC.TL_contacts_acceptContact();
        req.id = getMessagesController().getInputUser(model.getUser());
        getConnectionsManager().sendRequest(req, (response, error) -> {
            AndroidUtilities.runOnUIThread(dialog::dismiss);
            if (error != null) {
                AndroidUtilities.runOnUIThread(() -> {
                    needShowAlert(getString(R.string.RestorePasswordNoEmailTitle), error.text);
                });
                return;
            }
            getMessagesController().processUpdates((TLRPC.Updates) response, false);
            AndroidUtilities.runOnUIThread(() -> {
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).getApply().user_id == model.getApply().user_id) {
                        model.getApply().mutual = true;
                    }
                }
                listAdapter.notifyDataSetChanged();
            });
        });
    }

    private void needShowAlert(String title, String text) {
        if (text == null || getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setPositiveButton(getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }

    private class ApplyListAdapter extends RecyclerView.Adapter<ApplyListAdapter.ViewHolder> {

        public ApplyListAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == ApplyModel.typeEmpty) {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_empty, parent, false);
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_apply, parent, false);
            }
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (dataList.get(position).getItemType() == ApplyModel.typeData) {
                UserCell userCell = new UserCell(context, 1, 1, false);
                holder.ll_container.removeAllViews();
                TLRPC.User itemData = dataList.get(position).getUser();
                userCell.setData(itemData, itemData.first_name, LocaleController.formatUserStatus(currentAccount, itemData), 0);
                holder.ll_container.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

                TLRPC.TL_friendContact apply = dataList.get(position).getApply();
                if (apply.mutual) {
                    holder.button.setText("已通过");
                } else {
                    holder.button.setText("同意申请");
                }
                holder.button.setOnClickListener(view -> agreeApply(context, dataList.get(position)));

                if (apply.message.isEmpty()) {
                    holder.tv_remark.setText(itemData.first_name + "申请添加你为好友");
                } else {
                    holder.tv_remark.setText(apply.message);
                }
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
            RoundTextView button;
            AppCompatTextView tv_remark;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_container = itemView.findViewById(R.id.ll_container);
                button = itemView.findViewById(R.id.button);
                tv_remark = itemView.findViewById(R.id.tv_remark);
            }
        }
    }
}
