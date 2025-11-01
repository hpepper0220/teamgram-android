package org.telegram.ext.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.billy.android.swipe.SmartSwipeWrapper;
import com.billy.android.swipe.SwipeConsumer;
import com.billy.android.swipe.consumer.SlidingConsumer;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;

public class SimpleDialogCell extends FrameLayout {
    private CheckBox2 checkBox;
    private SmartSwipeWrapper smart_swipe_wrapper;
    private LinearLayout ll_check_box;
    private LinearLayout dialog_cell_container;
    public LinearLayout btn_delete;
    public LinearLayout btn_mark_read;
    public AppCompatTextView atv_pin;
    public AppCompatTextView atv_mute;
    public AppCompatTextView atv_mark_read;
    public AppCompatImageView aiv_pin;
    public AppCompatImageView aiv_mute;
    private DialogCell dialog;
    private SlidingConsumer slidingConsumer;

    public SimpleDialogCell(@NonNull Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.layout_dialog_cell, this, true);
        smart_swipe_wrapper = findViewById(R.id.smart_swipe_wrapper);
        dialog_cell_container = findViewById(R.id.dialog_cell_container);
        ll_check_box = findViewById(R.id.ll_check_box);

        btn_delete = findViewById(R.id.btn_delete);
        btn_mark_read = findViewById(R.id.btn_mark_read);
        aiv_mute = findViewById(R.id.aiv_mute);
        atv_mute = findViewById(R.id.atv_mute);
        aiv_pin = findViewById(R.id.aiv_pin);
        atv_pin = findViewById(R.id.atv_pin);
        atv_mark_read = findViewById(R.id.atv_mark_read);

        slidingConsumer = new SlidingConsumer();
        slidingConsumer.smoothClose();
        smart_swipe_wrapper.addConsumer(slidingConsumer).setRelativeMoveFactor(SlidingConsumer.FACTOR_FOLLOW);

        checkBox = new CheckBox2(context, 22);
        checkBox.setColor(-1, Theme.key_windowBackgroundWhiteGrayText, Theme.key_checkboxCheck);
        checkBox.setDrawUnchecked(true);
        checkBox.setDrawBackgroundAsArc(3);

        ll_check_box.addView(checkBox, LayoutHelper.createFrame(22, 22));
        ll_check_box.setVisibility(GONE);

//        btn_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e("DialogsActivity", "删除消息");
//            }
//        });
    }

    public SimpleDialogCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleDialogCell(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addDialog(DialogCell dialog, int currentAccount, boolean isEditModel, boolean isSelected) {
        if (isEditModel) {
            ll_check_box.setVisibility(View.VISIBLE);
        } else {
            ll_check_box.setVisibility(View.GONE);
        }

        checkBox.setChecked(isSelected, true);

        dialog_cell_container.removeAllViews();
        dialog.setId(R.id.dialog_cell);
        this.dialog = dialog;
        dialog_cell_container.addView(dialog, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        if (dialog.getHasUnread()) {
            atv_mark_read.setText(LocaleController.getString(R.string.MarkAsRead));
        } else {
            atv_mark_read.setText(LocaleController.getString(R.string.MarkAsUnread));
        }

//        int canMuteCount = MessagesController.getInstance(currentAccount).isDialogMuted(dialog.getDialogId(), 0) ? 0 : 1;
//        if (canMuteCount == 1) {
//            atv_mute.setText("关闭通知");
//            aiv_mute.setImageResource(R.drawable.sk_msg_mute);
//        } else {
//            atv_mute.setText("开启通知");
//            aiv_mute.setImageResource(R.drawable.sk_msg_unmute);
//        }

        if (dialog.getIsMuted()) {
            atv_mute.setText("开启通知");
            aiv_mute.setImageResource(R.drawable.sk_msg_unmute);
        } else {
            atv_mute.setText("关闭通知");
            aiv_mute.setImageResource(R.drawable.sk_msg_mute);
        }

        if (dialog.getIsPinned()) {
            aiv_pin.setImageResource(R.drawable.sk_msg_unpin);
            atv_pin.setText("取消置顶");
        } else {
            aiv_pin.setImageResource(R.drawable.sk_msg_pin);
            atv_pin.setText("置顶");
        }

        if (isEditModel) {
            smart_swipe_wrapper.enableDirection(SwipeConsumer.DIRECTION_LEFT, false);
            smart_swipe_wrapper.enableDirection(SwipeConsumer.DIRECTION_RIGHT, false);
        } else {
            smart_swipe_wrapper.enableDirection(SwipeConsumer.DIRECTION_LEFT, true);
            smart_swipe_wrapper.enableDirection(SwipeConsumer.DIRECTION_RIGHT, true);
        }
    }

    public void close(boolean smooth) {
        slidingConsumer.close(smooth);
    }
}
