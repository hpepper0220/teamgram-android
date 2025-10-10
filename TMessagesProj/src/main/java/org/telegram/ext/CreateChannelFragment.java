package org.telegram.ext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.csdn.roundview.RoundLinearLayout;

import org.checkerframework.checker.units.qual.A;
import org.telegram.ext.model.ContactModel;
import org.telegram.ext.model.SelectContactModel;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.majiajie.pagerbottomtabstrip.internal.RoundMessageView;

public class CreateChannelFragment extends BaseFragment {

    private LinearLayout contentView;
    private RecyclerView listView;
    private RecyclerView selectedListView;
    private List<SelectContactModel> dataList = new ArrayList<>();
    private List<TLRPC.User> selectedDataList = new ArrayList<>();
    private SelectContactListAdapter listAdapter;
    private SelectedContactListAdapter selectedListAdapter;
    private LaunchActivity mParentActivity;

    private HashMap<Long, TLRPC.User> selectedMap = new HashMap<>();

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAllowOverlayTitle(true);
        actionBar.setBackgroundColor(Color.WHITE);

        contentView = new LinearLayout(context);
        contentView.setBackgroundColor(Color.parseColor("#F4F4F4"));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(10), AndroidUtilities.dp(12), AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        createTitleBar(context);

        RoundLinearLayout selected_layout = new RoundLinearLayout(context);
        selected_layout.setRadius(6f);
        selected_layout.setGravity(Gravity.CENTER_VERTICAL);
        selected_layout.setBackgroundColor(Color.WHITE);
        contentView.addView(selected_layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 70, 0, 2, 0, 0));

        selectedListView = new RecyclerView(context);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        selectedListView.setLayoutManager(layoutManager);
        selected_layout.addView(selectedListView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        selectedListAdapter = new SelectedContactListAdapter(context);
        selectedListView.setAdapter(selectedListAdapter);

        listView = new RecyclerView(context);
        listView.setLayoutManager(new LinearLayoutManager(context));
        contentView.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 10, 0, 0));

        listAdapter = new SelectContactListAdapter(context);
        listView.setAdapter(listAdapter);

        loadData();

        return contentView;
    }

    private void loadData() {
        dataList.clear();
        ArrayList<TLRPC.TL_contact> contacts = ContactsController.getInstance(currentAccount).contacts;
        for (int i = 0; i < contacts.size(); i++) {
            SelectContactModel model = new SelectContactModel();
            model.setItemType(ContactModel.typeData);
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(contacts.get(i).user_id);
            model.setData(user);
            dataList.add(model);
        }
        listAdapter.notifyDataSetChanged();
    }

    private class SelectedContactListAdapter extends RecyclerView.Adapter<SelectedContactListAdapter.ViewHolder> {

        public SelectedContactListAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public SelectedContactListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_contact_selected, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SelectedContactListAdapter.ViewHolder holder, int position) {
            BackupImageView avatarImageView = new BackupImageView(context);
            avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(24));

            TLRPC.User user = selectedDataList.get(position);

            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
            avatarImageView.setForUserOrChat(user, avatarDrawable);

            holder.ll_container.addView(avatarImageView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            holder.btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return selectedDataList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout ll_container;
            RoundLinearLayout btnClose;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_container = itemView.findViewById(R.id.ll_container);
                btnClose = itemView.findViewById(R.id.btn_close);
            }
        }
    }

    private class SelectContactListAdapter extends RecyclerView.Adapter<SelectContactListAdapter.ViewHolder> {

        public SelectContactListAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public SelectContactListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_contact_select, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SelectContactListAdapter.ViewHolder holder, int position) {
            UserCell userCell = new UserCell(context, 1, 1, false);
            holder.ll_container.removeAllViews();
            TLRPC.User itemData = dataList.get(position).getData();
            userCell.setData(itemData, itemData.first_name, LocaleController.formatUserStatus(currentAccount, itemData), 0);
            holder.ll_container.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            holder.checkBox.setChecked(selectedMap.containsKey(itemData.id));
            userCell.setOnClickListener(view -> {
                if (selectedMap.containsKey(itemData.id)) {
                    selectedMap.remove(itemData.id);
                } else {
                    selectedMap.put(itemData.id, dataList.get(position).getData());
                }
                notifyItemChanged(position);

                selectedDataList.clear();
                selectedDataList = new ArrayList<>(selectedMap.values());
                selectedListAdapter.notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout ll_container;
            CheckBox checkBox;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_container = itemView.findViewById(R.id.ll_container);
                checkBox = itemView.findViewById(R.id.checkbox);
            }
        }
    }

    private void createTitleBar(Context context) {
        LinearLayout layout = new LinearLayout(context);

        AppCompatTextView tv_cancel = new AppCompatTextView(context);
        tv_cancel.setText("取消");
        tv_cancel.setTextColor(Color.parseColor("#666666"));
        layout.addView(tv_cancel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        tv_cancel.setOnClickListener(view -> finishFragment());

        LinearLayout title_layout = new LinearLayout(context);
        title_layout.setOrientation(LinearLayout.VERTICAL);
        title_layout.setGravity(Gravity.CENTER);
        layout.addView(title_layout, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f));

        AppCompatTextView tv_title = new AppCompatTextView(context);
        tv_title.setText("新建群组");
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
        tv_title.setTextColor(Color.parseColor("#333333"));
        tv_title.setTypeface(Typeface.DEFAULT_BOLD);
        title_layout.addView(tv_title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        AppCompatTextView tv_next = new AppCompatTextView(context);
        tv_next.setText("下一步");
        tv_next.setTextColor(Color.parseColor("#666666"));
        layout.addView(tv_next, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        actionBar.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 12, 30, 12, 0));
    }
}
