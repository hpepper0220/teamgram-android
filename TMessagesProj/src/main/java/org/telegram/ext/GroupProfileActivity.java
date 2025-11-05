package org.telegram.ext;

import static org.telegram.messenger.LocaleController.getString;

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
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.csdn.roundview.RoundLinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;

public class GroupProfileActivity extends BaseFragment {

    private LinearLayout contentView;
    private LaunchActivity mParentActivity;
    private TLRPC.ChatFull chatInfo;
    private long chat_id;
    private TLRPC.Chat currentChat;
    private RecyclerView listview;
    private GroupMemberListAdapter listAdapter;
    private boolean loadingUsers;
    private boolean usersEndReached;
    private LongSparseArray<TLRPC.ChatParticipant> participantsMap = new LongSparseArray<>();

    private TLRPC.User latestUser;

    public void setParentActivity(LaunchActivity parentActivity) {
        this.mParentActivity = parentActivity;
    }

    public GroupProfileActivity() {
    }

    public GroupProfileActivity(Bundle args) {
        super(args);
        chat_id = args.getLong("chat_id");
    }

    public void setChatInfo(TLRPC.ChatFull value) {
        this.chatInfo = value;
    }

    @Override
    public boolean onFragmentCreate() {
        currentChat = getMessagesController().getChat(chat_id);

        if (null != chatInfo && null != chatInfo.participants) {
            Log.e("GroupProfile", "participants -----> " + chatInfo.participants.participants.size());
        }

        if (currentChat == null) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            getMessagesStorage().getStorageQueue().postRunnable(() -> {
                currentChat = getMessagesStorage().getChat(chat_id);
                countDownLatch.countDown();
            });
            try {
                countDownLatch.await();
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (currentChat != null) {
                getMessagesController().putChat(currentChat, true);
            } else {
                return false;
            }
        }

        if (chatInfo == null) {
            chatInfo = getMessagesController().getChatFull(chat_id);
        }
        if (ChatObject.isChannel(currentChat)) {
            getMessagesController().loadFullChat(chat_id, classGuid, true);
        } else if (chatInfo == null) {
            chatInfo = getMessagesStorage().loadChatInfo(chat_id, false, null, false, false);
        }

        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        if (ChatObject.isChannel(currentChat)) {
            actionBar.setTitle("频道信息");
        } else {
            actionBar.setTitle("群聊信息");
        }

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

        ProfileSearchCell cell = new ProfileSearchCell(context);
        cell.setData(currentChat, null, null, null, false, false);
        headerLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout ll_group_member = new LinearLayout(context);
        contentView.addView(ll_group_member, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        AppCompatTextView atv_group_member = new AppCompatTextView(context);
        atv_group_member.setText("群聊成员");
        atv_group_member.setPadding(34, 0, 12, 0);
        atv_group_member.setBackgroundColor(Color.WHITE);
        atv_group_member.setGravity(Gravity.CENTER_VERTICAL);
        ll_group_member.addView(atv_group_member, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(16), 12, 12, 12, 0));

        listview = new RecyclerView(context);
        listview.setLayoutManager(new LinearLayoutManager(context));
        contentView.addView(listview, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1, Gravity.TOP, 12, 0, 12, 0));

        listAdapter = new GroupMemberListAdapter(context);
        listview.setAdapter(listAdapter);

        if (ChatObject.isChannel(currentChat)) {
            fetchUsersFromChannelInfo();

            getChannelParticipants(true);
        }

        return contentView;
    }

    private void fetchUsersFromChannelInfo() {
        if (currentChat == null || !currentChat.megagroup) {
            return;
        }
        if (chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants != null) {
            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                TLRPC.ChatParticipant chatParticipant = chatInfo.participants.participants.get(a);
                participantsMap.put(chatParticipant.user_id, chatParticipant);
            }
        }
    }

    private void getChannelParticipants(boolean reload) {
        if (loadingUsers || participantsMap == null || chatInfo == null) {
            return;
        }
        loadingUsers = true;
        final int delay = participantsMap.size() > 0 && reload ? 300 : 0;

        final TLRPC.TL_channels_getParticipants req = new TLRPC.TL_channels_getParticipants();
        req.channel = getMessagesController().getInputChannel(chat_id);
        req.filter = new TLRPC.TL_channelParticipantsRecent();
        req.offset = reload ? 0 : participantsMap.size();
        req.limit = 200;
        int reqId = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> getNotificationCenter().doOnIdle(() -> {
            if (error == null) {
                TLRPC.TL_channels_channelParticipants res = (TLRPC.TL_channels_channelParticipants) response;
                getMessagesController().putUsers(res.users, false);
                getMessagesController().putChats(res.chats, false);
                if (res.users.size() < 200) {
                    usersEndReached = true;
                }
                if (req.offset == 0) {
                    participantsMap.clear();
                    chatInfo.participants = new TLRPC.TL_chatParticipants();
                    getMessagesStorage().putUsersAndChats(res.users, res.chats, true, true);
                    getMessagesStorage().updateChannelUsers(chat_id, res.participants);
                }
                for (int a = 0; a < res.participants.size(); a++) {
                    TLRPC.TL_chatChannelParticipant participant = new TLRPC.TL_chatChannelParticipant();
                    participant.channelParticipant = res.participants.get(a);
                    participant.inviter_id = participant.channelParticipant.inviter_id;
                    participant.user_id = MessageObject.getPeerId(participant.channelParticipant.peer);
                    participant.date = participant.channelParticipant.date;
                    if (participantsMap.indexOfKey(participant.user_id) < 0) {
                        if (chatInfo.participants == null) {
                            chatInfo.participants = new TLRPC.TL_chatParticipants();
                        }
                        chatInfo.participants.participants.add(participant);
                        participantsMap.put(participant.user_id, participant);
                    }
                }

//                Collections.sort(chatInfo.participants.participants, (u1, u2) -> {
//                    TLRPC.User user1 = getMessagesController().getUser(u1.user_id);
//                    TLRPC.User user2 = getMessagesController().getUser(u2.user_id);
//                    boolean online1 = user1.status instanceof TLRPC.TL_userStatusOnline;
//                    boolean online2 = user2.status instanceof TLRPC.TL_userStatusOnline;
//                    return Boolean.compare(online2, online1);
//                });

                Comparator<TLRPC.ChatParticipant> comparator = Comparator
                        // 在线用户优先
                        .comparing((TLRPC.ChatParticipant u) -> {
                            TLRPC.User user = getMessagesController().getUser(u.user_id);
                            return !(user.status instanceof TLRPC.TL_userStatusOnline);
                        })
                        // 根据自定义优先级
                        .thenComparing(u -> {
                            TLRPC.User user = getMessagesController().getUser(u.user_id);
                            int exp = user.status != null ? user.status.expires : Integer.MIN_VALUE;
                            return getExpirePriority(exp);
                        })
                        // 相同优先级则按 expires 倒序
                        .thenComparing((TLRPC.ChatParticipant u) -> {
                            TLRPC.User user = getMessagesController().getUser(u.user_id);
                            return user.status != null ? -user.status.expires : Integer.MIN_VALUE;
                        });

                chatInfo.participants.participants.sort(comparator);


//                getMessagesController().getUser(part.user_id);

            }
            loadingUsers = false;
            listAdapter.notifyDataSetChanged();
//            saveScrollPosition();
//            updateListAnimated(true);
        }), delay));
        getConnectionsManager().bindRequestToGuid(reqId, classGuid);
    }

    private int getExpirePriority(int expires) {
        switch (expires) {
            case -1:
                return 1;
            case -100:
            case -1000:
                return 2;
            case -101:
            case -1001:
                return 3;
            case -102:
            case -1002:
                return 4;
            default:
                return 5; // 普通用户最后
        }
    }


    private class GroupMemberListAdapter extends RecyclerView.Adapter<GroupMemberListAdapter.ViewHolder> {
        public GroupMemberListAdapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public GroupMemberListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_common_group, parent, false);
            return new GroupMemberListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupMemberListAdapter.ViewHolder holder, int position) {
            holder.ll_container.removeAllViews();
            UserCell userCell = new UserCell(context, 9, 0, true, false, null);
            TLRPC.ChatParticipant part = chatInfo.participants.participants.get(position);
            if (part != null) {
                String role;
                if (part instanceof TLRPC.TL_chatChannelParticipant) {
                    TLRPC.ChannelParticipant channelParticipant = ((TLRPC.TL_chatChannelParticipant) part).channelParticipant;
                    if (!TextUtils.isEmpty(channelParticipant.rank)) {
                        role = channelParticipant.rank;
                    } else {
                        if (channelParticipant instanceof TLRPC.TL_channelParticipantCreator) {
                            role = getString("ChannelCreator", R.string.ChannelCreator);
                        } else if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin) {
                            role = getString("ChannelAdmin", R.string.ChannelAdmin);
                        } else {
                            role = null;
                        }
                    }
                } else {
                    if (part instanceof TLRPC.TL_chatParticipantCreator) {
                        role = getString("ChannelCreator", R.string.ChannelCreator);
                    } else if (part instanceof TLRPC.TL_chatParticipantAdmin) {
                        role = getString("ChannelAdmin", R.string.ChannelAdmin);
                    } else {
                        role = null;
                    }
                }
                userCell.setAdminRole(role);
                userCell.setData(getMessagesController().getUser(part.user_id), null, null, 0, position != chatInfo.participants.participants.size() - 1);
                userCell.setOnClickListener(view -> {
                    Log.e("GroupProfile", "chat_id ------> "+ part.user_id);
                    if (latestUser.premium) {
                        Bundle args = new Bundle();
                        args.putLong("user_id", part.user_id);
                        mParentActivity.presentFragment(new ChatActivity(args));
                    }
                });
            }
            holder.ll_container.addView(userCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        @Override
        public int getItemCount() {
            return chatInfo.participants.participants.size();
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
