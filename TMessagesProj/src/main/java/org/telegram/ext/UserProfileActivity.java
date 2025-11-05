package org.telegram.ext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.csdn.roundview.RoundLinearLayout;
import net.csdn.roundview.RoundTextView;

import org.telegram.ext.components.PopupCreator;
import org.telegram.ext.config.SkMenuAction;
import org.telegram.ext.model.ContactModel;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;

public class UserProfileActivity extends BaseFragment {

    private LinearLayout contentView;

    private LaunchActivity mParentActivity;

    private long user_id;
    private TLRPC.UserFull userInfo;
    private TLRPC.User currentUser;
    private AppCompatTextView tv_remark_content;
    private AppCompatTextView tv_user_name;
    private ArrayList<TLRPC.Chat> chats = new ArrayList<>();
    private RecyclerView listview;
    private CommonGroupListAdapter listAdapter;
    private SwipeRefreshLayout refreshLayout;
    private int type;
    private TLRPC.User latestUser;

    public UserProfileActivity() {
    }

    public UserProfileActivity(Bundle args) {
        super(args);
        type = args.getInt("type");
    }

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    public void setUserInfo(long user_id, TLRPC.UserFull value) {
        this.user_id = user_id;
        this.userInfo = value;
        currentUser = getMessagesController().getUser(user_id);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("个人信息");
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

//        ActionBarMenu menu = actionBar.createMenu();
//        ActionBarMenuItem moreItem = menu.addItem(SkMenuAction.more, R.mipmap.ic_more);
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
//            @Override
//            public void onItemClick(int id) {
//                if (id == SkMenuAction.more) {
//                    PopupCreator.createProfileMorePopup(getParentActivity(), menu, v -> {
//
//                    });
//                }
//            }
//        });

        latestUser = getMessagesController().getUser(getUserConfig().getClientUserId());

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.setPadding(0, 0, 0, AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        View viewLine1 = new View(context);
        contentView.addView(viewLine1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(4)));

        RoundLinearLayout headerLayout = new RoundLinearLayout(context);
        headerLayout.setRadius(4f);
        headerLayout.setBackgroundColor(Color.WHITE);
        headerLayout.setPadding(8, 20, 8, 20);
        contentView.addView(headerLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 0, 12, 0));

        UserCell userCell = new UserCell(context, 1, 1, false);
        userCell.setData(currentUser, currentUser.first_name, LocaleController.formatUserStatus(currentAccount, currentUser), 0);
        headerLayout.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        RoundLinearLayout bottomLayout = new RoundLinearLayout(context);
        bottomLayout.setRadius(4f);
        bottomLayout.setBackgroundColor(Color.WHITE);
        bottomLayout.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(bottomLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 12, 12, 12, 0));

        LinearLayout ll_user_name = new LinearLayout(context);
        ll_user_name.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.addView(ll_user_name, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16)));

        AppCompatTextView tv_user_name_title = new AppCompatTextView(context);
        tv_user_name_title.setText("用户名");
        tv_user_name_title.setGravity(Gravity.CENTER_VERTICAL);
        tv_user_name_title.setPadding(12, 0, 12, 0);
        ll_user_name.addView(tv_user_name_title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        tv_user_name = new AppCompatTextView(context);
        tv_user_name.setMaxLines(1);
        tv_user_name.setTextColor(Color.BLACK);
        tv_user_name.setEllipsize(TextUtils.TruncateAt.END);
        tv_user_name.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        tv_user_name.setPadding(0, 0, 12, 0);
        ll_user_name.addView(tv_user_name, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 8, 0));

        ll_user_name.setVisibility(View.GONE);

        LinearLayout remarkLayout = new LinearLayout(context);
        remarkLayout.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.addView(remarkLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16)));

        AppCompatTextView tv_remark = new AppCompatTextView(context);
        tv_remark.setText("个人简介");
        tv_remark.setGravity(Gravity.CENTER_VERTICAL);
        tv_remark.setPadding(12, 0, 12, 0);
        remarkLayout.addView(tv_remark, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 8, 0, 8, 0));

        tv_remark_content = new AppCompatTextView(context);
        tv_remark_content.setMaxLines(1);
        tv_remark_content.setTextColor(Color.BLACK);
        tv_remark_content.setEllipsize(TextUtils.TruncateAt.END);
        tv_remark_content.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        tv_remark_content.setPadding(0, 0, 12, 0);
        remarkLayout.addView(tv_remark_content, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, 1f, 0, 0, 8, 0));

        LinearLayout ll_common_group = new LinearLayout(context);
        contentView.addView(ll_common_group, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        AppCompatTextView atv_common_group = new AppCompatTextView(context);
        atv_common_group.setText("共同群聊");
        atv_common_group.setPadding(34, 0, 12, 0);
        atv_common_group.setBackgroundColor(Color.WHITE);
        atv_common_group.setGravity(Gravity.CENTER_VERTICAL);
        ll_common_group.addView(atv_common_group, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16), 12, 12, 12, 0));

        refreshLayout = new SwipeRefreshLayout(context);
        refreshLayout.setOnRefreshListener(this::getCommonChats);
        refreshLayout.setPadding(0, 0, 0, 12);
        contentView.addView(refreshLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1, Gravity.TOP, 12, 0, 12, 0));

        listview = new RecyclerView(context);
        listview.setLayoutManager(new LinearLayoutManager(context));

        listAdapter = new CommonGroupListAdapter(context);
        listview.setAdapter(listAdapter);

        refreshLayout.addView(listview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        if (latestUser.premium && currentUser.id != latestUser.id) {
            RoundTextView button = new RoundTextView(context);
            button.setGravity(Gravity.CENTER);
            button.setText("开始聊天");
            button.setTextColor(Color.WHITE);
            button.setRadius(6);
            button.setTextSize(15);
            button.setBackgroundColor(Theme.getColor(Theme.key_chats_actionBackground));
            contentView.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 12, 12, 12, 12));
            button.setOnClickListener(view -> {
                if (type == 0) {
                    finishFragment();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putLong("user_id", user_id);
                    ChatActivity chatActivity = new ChatActivity(bundle);
                    mParentActivity.presentFragment(chatActivity);
                }
            });
        }

        loadUserFullInfo();
        getCommonChats();

        return contentView;
    }

    private void getCommonChats() {
        TLRPC.TL_messages_getCommonChats req = new TLRPC.TL_messages_getCommonChats();
        req.user_id = MessagesController.getInstance(currentAccount).getInputUser(user_id);
        req.max_id = 0;
        req.limit = 100;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
            refreshLayout.setRefreshing(false);

            if (res instanceof TLRPC.messages_Chats) {
                final TLRPC.messages_Chats rez = (TLRPC.messages_Chats) res;
                MessagesController.getInstance(currentAccount).putChats(rez.chats, false);
                chats.clear();
                chats.addAll(rez.chats);

                listAdapter.notifyDataSetChanged();
            }
        }));
    }

    @SuppressLint("SetTextI18n")
    private void loadUserFullInfo() {
        getMessagesController().loadFullUser(currentUser, classGuid, true, arg -> AndroidUtilities.runOnUIThread(() -> {
            if (null == arg || null == arg.about || TextUtils.isEmpty(arg.about)) {
                tv_remark_content.setText("补充几句话介绍一下你自己");
            } else {
                tv_remark_content.setText(arg.about);
            }
            if (null != arg) {
                String username = UserObject.getPublicUsername(arg.user);
                if (null != username && !username.isEmpty()) {
                    tv_user_name.setText("@" + username);
                }
            }
        }));
    }

    private class CommonGroupListAdapter extends RecyclerView.Adapter<CommonGroupListAdapter.ViewHolder> {
        public CommonGroupListAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_common_group, parent, false);
            return new CommonGroupListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.ll_container.removeAllViews();
            ProfileSearchCell cell = new ProfileSearchCell(context);
            TLRPC.Chat chat = chats.get(position);
            cell.setData(chat, null, null, null, false, false);
            cell.useSeparator = position != chats.size() - 1;
            holder.ll_container.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            holder.ll_container.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putLong("chat_id", chat.id);
                mParentActivity.presentFragment(new ChatActivity(args));
            });
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout ll_container;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_container = itemView.findViewById(R.id.ll_container);
            }
        }
    }
}
