package org.telegram.ext.components;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import com.xujiaji.happybubble.BubbleDialog;

import org.telegram.ext.components.popup.BaseBubblePopup;
import org.telegram.ext.components.popup.PopupCell;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class PopupCreator {

    public static void createAddContactPopup(Context context, View anchor, final View.OnClickListener listener) {
        BaseBubblePopup popup = new BaseBubblePopup(context) {
            @Override
            protected View createContentView() {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                PopupCell popupCell = new PopupCell(context);
                popupCell.setId(R.id.popup_add_contact);
                popupCell.setData("添加好友", R.drawable.msg_contact_add);
                popupCell.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(3), Color.WHITE, 0xFF696969));
//                popupCell.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 46));
                popupCell.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
                popupCell.setOnClickListener(v -> {
                    dismiss();
                    if (null != listener) {
                        listener.onClick(v);
                    }
                });
                layout.addView(popupCell, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 46));

//                PopupCell popupCell1 = new PopupCell(context);
//                popupCell1.setId(R.id.popup_create_group);
//                popupCell1.setData("创建群组", R.drawable.msg_groups_create);
//                popupCell1.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(3), Color.WHITE, 0xFF696969));
//                popupCell1.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
//                popupCell1.setOnClickListener(v -> {
//                    dismiss();
//                    if (null != listener) {
//                        listener.onClick(v);
//                    }
//                });
//                layout.addView(popupCell1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 46));

                return layout;
            }
        };
        popup.getBubbleLayout().setLookWidth(AndroidUtilities.dp(10f));
        popup.getBubbleLayout().setLookLength(AndroidUtilities.dp(8f));
        popup.getBubbleLayout().setBubbleRadius(AndroidUtilities.dp(4));
        popup.setPosition(BubbleDialog.Position.BOTTOM);
        popup.setOffsetY(-10);
        popup.showAsDropDown(anchor);
    }

    public static void createProfileMorePopup(Context context, View anchor, final View.OnClickListener listener) {
        BaseBubblePopup popup = new BaseBubblePopup(context) {
            @Override
            protected View createContentView() {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                PopupCell popupCell = new PopupCell(context);
                popupCell.setId(R.id.popup_profile_edit_nick);
                popupCell.setData("设置备注", R.drawable.msg_edit);
                popupCell.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(3), Color.WHITE, 0xFF696969));
//                popupCell.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 46));
                popupCell.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
                popupCell.setOnClickListener(v -> {
                    dismiss();
                    if (null != listener) {
                        listener.onClick(v);
                    }
                });
                layout.addView(popupCell, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 46));

//                PopupCell popupCell1 = new PopupCell(context);
//                popupCell1.setId(R.id.popup_create_group);
//                popupCell1.setData("创建群组", R.drawable.msg_groups_create);
//                popupCell1.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(3), Color.WHITE, 0xFF696969));
//                popupCell1.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
//                popupCell1.setOnClickListener(v -> {
//                    dismiss();
//                    if (null != listener) {
//                        listener.onClick(v);
//                    }
//                });
//                layout.addView(popupCell1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 46));

                return layout;
            }
        };
        popup.getBubbleLayout().setLookWidth(AndroidUtilities.dp(10f));
        popup.getBubbleLayout().setLookLength(AndroidUtilities.dp(8f));
        popup.getBubbleLayout().setBubbleRadius(AndroidUtilities.dp(4));
        popup.setPosition(BubbleDialog.Position.BOTTOM);
        popup.setOffsetY(-10);
        popup.showAsDropDown(anchor);
    }
}
