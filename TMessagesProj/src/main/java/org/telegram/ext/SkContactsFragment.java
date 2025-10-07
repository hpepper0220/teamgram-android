/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ext;

import android.app.Activity;
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

import org.telegram.ext.model.ContactModel;
import org.telegram.ext.model.DiscoveryModel;
import org.telegram.ext.respository.SimpleCallback;
import org.telegram.ext.respository.SkRepository;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.majiajie.pagerbottomtabstrip.internal.RoundMessageView;

public class SkContactsFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private FrameLayout frameContainerView;
    private RecyclerView listview;

    private List<ContactModel> dataList = new ArrayList<>();
    private ContactListAdapter listAdapter;

    private LaunchActivity mParentActivity;

    public SkContactsFragment() {

    }

    public SkContactsFragment(Bundle args) {
        super(args);
    }

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public Activity getParentActivity() {
        return mParentActivity;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.contactsDidLoad);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.storiesUpdated);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.refreshApplyList);

        getContactsController().checkInviteText();
        getContactsController().reloadContactsStatusesMaybe(false);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.contactsDidLoad);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.storiesUpdated);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.closeChats);
//        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
    }

    @Override
    public View createView(Context context) {
        frameContainerView = new FrameLayout(context);
        frameContainerView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listview = new RecyclerView(context);
        listview.setLayoutManager(new LinearLayoutManager(context));
        frameContainerView.addView(listview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listAdapter = new ContactListAdapter(context);
        listview.setAdapter(listAdapter);

        loadData();

        return frameContainerView;
    }

    private void loadData() {
        dataList.clear();
        dataList.add(ContactModel.header());
        ArrayList<TLRPC.TL_contact> contacts = ContactsController.getInstance(currentAccount).contacts;
        if (contacts.isEmpty()) {
            ContactModel model = new ContactModel();
            model.setItemType(ContactModel.typeEmpty);
            dataList.add(model);
        } else {
            for (int i = 0; i < contacts.size(); i++) {
                Log.e("ContactFragment", "userId -------> " + contacts.get(i).user_id);
                ContactModel model = new ContactModel();
                model.setItemType(ContactModel.typeData);
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(contacts.get(i).user_id);
                if (null != user) {
                    Log.e("ContactFragment", "user info -------> " + user.first_name);
                }
                model.setData(user);
                dataList.add(model);
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    private class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

        public ContactListAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView;
            if (viewType == ContactModel.typeEmpty) {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_empty, parent, false);
            } else if (viewType == ContactModel.typeHeader) {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_contact_header, parent, false);
            } else {
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_contact, parent, false);
            }
            return new ContactListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (dataList.get(position).getItemType() == ContactModel.typeData) {
                UserCell userCell = new UserCell(context, 1, 1, false);
                holder.ll_container.removeAllViews();
                TLRPC.User itemData = dataList.get(position).getData();
                userCell.setData(itemData, itemData.first_name, LocaleController.formatUserStatus(currentAccount, itemData), 0);
                holder.ll_container.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            } else if (dataList.get(position).getItemType() == ContactModel.typeHeader) {
                if (dataList.get(position).getCount() > 0) {
                    holder.msgView.setVisibility(View.VISIBLE);
                    holder.msgView.setHasMessage(true);
                    holder.msgView.setMessageNumber(dataList.get(position).getCount());
                    holder.msgView.setMessageNumberColor(Color.WHITE);
                } else {
                    holder.msgView.setVisibility(View.GONE);
                }
                holder.ll_apply_list.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SkRepository.getInstance().getApplyList(currentAccount, classGuid, new SimpleCallback<TLRPC.TL_contacts_requestFriendContacts>() {
                            @Override
                            public void onResp(TLRPC.TL_contacts_requestFriendContacts result) {

                            }
                        });
                    }
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
            RoundMessageView msgView;
            LinearLayout ll_apply_list;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_container = itemView.findViewById(R.id.ll_container);
                msgView = itemView.findViewById(R.id.msg_view);
                ll_apply_list = itemView.findViewById(R.id.ll_apply_list);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();
//        if (actionBar.isActionModeShowed()) {
//            hideActionMode();
//            return false;
//        } else {
//            return super.onBackPressed();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (actionBar != null) {
            actionBar.closeSearchField();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.refreshApplyList) {
            SkRepository.getInstance().getApplyList(currentAccount, classGuid, result -> AndroidUtilities.runOnUIThread(() -> {
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).getItemType() == ContactModel.typeHeader) {
                        dataList.get(i).setCount(result.count);
                    }
                }
                listAdapter.notifyDataSetChanged();
            }));
        } else if (id == NotificationCenter.contactsDidLoad) {
            loadData();
        } else if (id == NotificationCenter.updateInterfaces) {
//            int mask = (Integer) args[0];
//            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
//                updateVisibleRows(mask);
//            }
//            if ((mask & MessagesController.UPDATE_MASK_STATUS) != 0 && !sortByName && listViewAdapter != null) {
//                scheduleSort();
//            }
        } else if (id == NotificationCenter.encryptedChatCreated) {
//            if (createSecretChat && creatingChat) {
//                TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
//                Bundle args2 = new Bundle();
//                args2.putInt("enc_id", encryptedChat.id);
//                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
////                presentFragment(new ChatActivity(args2), false);
//                mParentActivity.presentFragment(new ChatActivity(args2));
//            }
        } else if (id == NotificationCenter.closeChats) {
//            if (!creatingChat) {
//                removeSelfFromStack(true);
//            }
        }
    }
}
