package org.telegram.ext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.R;

import me.majiajie.pagerbottomtabstrip.internal.RoundMessageView;
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem;

public class BottomItemView extends BaseTabItem {
    private Context context;
    private AppCompatImageView icon;
    private AppCompatTextView textView;
    private RoundMessageView msgView;
    private Drawable iconDrawable;
    private boolean isSelected;
    private @DrawableRes int imgResId;

    public BottomItemView(@NonNull Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.sk_item_bottom_tab, this, true);

        icon = findViewById(R.id.icon);
        textView = findViewById(R.id.title);
        msgView = findViewById(R.id.msg_view);
    }

    public BottomItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(String title, @DrawableRes int resId) {
        this.imgResId = resId;
        iconDrawable = ContextCompat.getDrawable(context, resId);
        setTitle(title);
        icon.setImageDrawable(iconDrawable);
        setHasMessage(false);
    }

    @Override
    public void setChecked(boolean checked) {
        isSelected = checked;
        icon.setImageDrawable(iconDrawable);
        if (checked) {
            icon.setColorFilter(Color.BLACK);
            textView.setTextColor(Color.BLACK);
        } else {
            icon.setColorFilter(Color.parseColor("#999999"));
            textView.setTextColor(Color.parseColor("#999999"));
        }
    }

    @Override
    public void setMessageNumber(int number) {
        msgView.setMessageNumber(number);
        msgView.setMessageNumberColor(Color.WHITE);
    }

    @Override
    public void setHasMessage(boolean hasMessage) {
        if (hasMessage) {
            msgView.setVisibility(View.VISIBLE);
        } else {
            msgView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setTitle(String title) {
        textView.setText(title);
    }

    @Override
    public void setDefaultDrawable(Drawable drawable) {
        icon.setImageResource(R.mipmap.sk_tab_message);
    }

    @Override
    public void setSelectedDrawable(Drawable drawable) {

    }

    @Override
    public String getTitle() {
        return textView.getText().toString();
    }
}
