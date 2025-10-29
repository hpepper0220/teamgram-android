package org.telegram.ext.widgets;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.ext.model.MenuItem;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.R;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

public class ChatMenuPanel extends FrameLayout {

    public static final int CAMERA = 0x001;
    public static final int GALLERY = 0x002;
    public static final int CONTACT = 0x003;
    public static final int RED_PACKET = 0x004;

    private Adapter mAdapter;
    private RecyclerView recyclerView;

    public List<MenuItem> menuItems = new ArrayList<>();

    private RecyclerListView.OnItemClickListener onItemClickListener;

    public ChatMenuPanel(@NonNull Context context) {
        this(context, null);
    }

    public ChatMenuPanel(@NonNull Context context, ChatActivity parentFragment) {
        super(context);
        setBackgroundColor(0xFFEDEDED);
        if ((null != parentFragment && null != parentFragment.getCurrentChat() && ChatObject.hasAdminRights(parentFragment.getCurrentChat())) || (null != parentFragment && null != parentFragment.getCurrentUser() && parentFragment.getCurrentUser().premium == true)) {
            menuItems.add(new MenuItem(CAMERA, "相机", R.drawable.calls_video));
            menuItems.add(new MenuItem(GALLERY, "相册", R.drawable.files_gallery));
//            menuItems.add(new MenuItem(2, "文件", R.drawable.files_internal));
//            menuItems.add(new MenuItem(CONTACT, "联系人", R.drawable.msg_groups));
//            menuItems.add(new MenuItem(RED_PACKET, LocaleController.getString("RedEnvelope", R.string.RedEnvelope), R.drawable.msg_groups));
        } else {
            menuItems.add(new MenuItem(CAMERA, "相机", R.drawable.calls_video));
            menuItems.add(new MenuItem(GALLERY, "相册", R.drawable.files_gallery));
//            menuItems.add(new MenuItem(RED_PACKET, LocaleController.getString("RedEnvelope", R.string.RedEnvelope), R.drawable.msg_groups));
        }

        recyclerView = new RecyclerView(context);
        recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new Adapter(context);
        recyclerView.setAdapter(mAdapter);
        addView(recyclerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 16, 0, 16));
    }

    public void setOnItemClickListener(RecyclerListView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        public Adapter(Context context) {
            this.context = context;
        }

        private Context context;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_chat_menu_panel, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MenuItem menuItem = menuItems.get(position);
            holder.text.setText(menuItem.getTitle());
            holder.imageView.setImageResource(menuItem.getIconResource());
            holder.container.setOnClickListener(view -> {
                if (null != onItemClickListener) {
                    onItemClickListener.onItemClick(holder.container, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return menuItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            LinearLayout container;
            AppCompatImageView imageView;
            TextView text;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.container);
                text = itemView.findViewById(R.id.text);
                imageView = itemView.findViewById(R.id.image_view);
            }
        }
    }
}
