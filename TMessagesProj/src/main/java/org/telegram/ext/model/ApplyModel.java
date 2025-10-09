package org.telegram.ext.model;

import org.telegram.tgnet.TLRPC;

public class ApplyModel {

    public static final int typeEmpty = 0x001;
    public static final int typeData = 0x002;

    private int itemType;

    private TLRPC.TL_friendContact apply;
    private TLRPC.User user;

    public static ApplyModel empty() {
        ApplyModel model = new ApplyModel();
        model.setItemType(typeEmpty);
        return model;
    }

    public ApplyModel() {
    }

    public ApplyModel(int itemType, TLRPC.TL_friendContact apply, TLRPC.User user) {
        this.itemType = itemType;
        this.apply = apply;
        this.user = user;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public TLRPC.TL_friendContact getApply() {
        return apply;
    }

    public void setApply(TLRPC.TL_friendContact apply) {
        this.apply = apply;
    }

    public TLRPC.User getUser() {
        return user;
    }

    public void setUser(TLRPC.User user) {
        this.user = user;
    }
}
