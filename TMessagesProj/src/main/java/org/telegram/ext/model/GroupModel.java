package org.telegram.ext.model;

import org.telegram.tgnet.TLRPC;

public class GroupModel {

    public static final int typeEmpty = 0x001;
    public static final int typeData = 0x002;
    public static final int typeHeader = 0x003;

    private int itemType;
    private TLRPC.Chat data;

    public GroupModel() {
    }

    public GroupModel(int itemType, TLRPC.Chat data) {
        this.itemType = itemType;
        this.data = data;
    }

    public static GroupModel header() {
        GroupModel model = new GroupModel();
        model.setItemType(typeHeader);
        return model;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public TLRPC.Chat getData() {
        return data;
    }

    public void setData(TLRPC.Chat data) {
        this.data = data;
    }
}
