package org.telegram.ext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.ext.model.DiscoveryModel;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Objects;

public class SearchFriendFragment extends BaseFragment {

    private LinearLayout contentView;
    private RecyclerView listView;
    private LaunchActivity mParentActivity;
    private ArrayList<TLRPC.User> searchResult = new ArrayList<>();
    private SearchListAdapter searchListAdapter;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAllowOverlayTitle(true);
        actionBar.setBackgroundColor(Color.WHITE);

        contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(10), AndroidUtilities.dp(12), AndroidUtilities.dp(0));
        contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout searchLayout = new LinearLayout(context);
        searchLayout.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
        Drawable drawable = Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Color.parseColor("#F4F4F4"));
        searchLayout.setBackground(drawable);
        actionBar.addView(searchLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40, 12, 40, 12, 0));
//        contentView.addView(searchLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 40));

        AppCompatImageView iconSearch = new AppCompatImageView(context);
        iconSearch.setImageResource(R.mipmap.ic_index_search);
        searchLayout.addView(iconSearch, LayoutHelper.createLinear(AndroidUtilities.dp(6), AndroidUtilities.dp(6), Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        AppCompatEditText editText = new AppCompatEditText(context);
        editText.setHint("搜索账号");
        editText.setTextSize(13f);
        editText.setSingleLine();
        editText.setTextColor(Color.parseColor("#666666"));
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        editText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchContact(Objects.requireNonNull(editText.getText()).toString());
                return true;
            }
            return false;
        });
        searchLayout.addView(editText, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

        AppCompatTextView cancelText = new AppCompatTextView(context);
        cancelText.setHint("取消");
        cancelText.setTextSize(13f);
        cancelText.setTextColor(Color.RED);
        cancelText.setOnClickListener(view -> finishFragment());
        searchLayout.addView(cancelText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        listView = new RecyclerView(context);
        listView.setLayoutManager(new LinearLayoutManager(context));
        contentView.addView(listView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        searchListAdapter = new SearchListAdapter(context);
        listView.setAdapter(searchListAdapter);

        return contentView;
    }

    private void searchContact(String text) {
        TLRPC.TL_contacts_search req = new TLRPC.TL_contacts_search();
        req.q = text;
        req.limit = 20;
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, error) -> AndroidUtilities.runOnUIThread(() -> {
            TLRPC.TL_contacts_found response = null;
            if (res instanceof TLRPC.TL_contacts_found) {
                response = (TLRPC.TL_contacts_found) res;
                MessagesStorage.getInstance(currentAccount).putUsersAndChats(response.users, response.chats, true, true);
                MessagesController.getInstance(currentAccount).putUsers(response.users, false);
                MessagesController.getInstance(currentAccount).putChats(response.chats, false);

                searchResult.clear();
                for (int i = 0; i < response.users.size(); i++) {
                    if (UserConfig.getInstance(currentAccount).getClientUserId() != response.users.get(i).id) {
                        searchResult.add(response.users.get(i));
                    }
                }
                searchListAdapter.notifyDataSetChanged();
            }
        }));
    }

    private class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {
        public SearchListAdapter(Context context) {
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
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_search, parent, false);
            }
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserCell userCell = new UserCell(context, 1, 1, false);
            holder.ll_container.removeAllViews();
            TLRPC.User itemData = searchResult.get(position);
            userCell.setData(itemData, itemData.first_name, LocaleController.formatUserStatus(currentAccount, itemData), 0);
            holder.ll_container.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            holder.ll_container.setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putLong("user_id", itemData.id);

                FriendInfoFragment friendInfoFragment = new FriendInfoFragment(bundle);
                if (mParentActivity != null) {
                    friendInfoFragment.setParentActivity(mParentActivity);
                    mParentActivity.presentFragment(friendInfoFragment);
                }
            });
        }

        @Override
        public int getItemCount() {
            return searchResult.size();
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
