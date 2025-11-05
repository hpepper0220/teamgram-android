package org.telegram.ext.model;

import org.telegram.tgnet.TLRPC;

public class SearchModel {

    public static final int typeEmpty = 0x001;
    public static final int typeData = 0x002;

    private int itemType;

    private TLRPC.User user;

    public static SearchModel empty() {
        SearchModel model = new SearchModel();
        model.setItemType(typeEmpty);
        return model;
    }

    public SearchModel() {
    }

    public SearchModel(int itemType, TLRPC.User user) {
        this.itemType = itemType;
        this.user = user;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public TLRPC.User getUser() {
        return user;
    }

    public void setUser(TLRPC.User user) {
        this.user = user;
    }
}
