package org.telegram.ext.model;

import org.telegram.tgnet.TLRPC;

public class ContactModel {

    public static final int typeEmpty = 0x001;
    public static final int typeData = 0x002;
    public static final int typeHeader = 0x003;

    private int itemType;
    private TLRPC.User data;
    private int count;

    public ContactModel() {
    }

    public ContactModel(int itemType, TLRPC.User data, int count) {
        this.itemType = itemType;
        this.data = data;
        this.count = count;
    }

    public static ContactModel header() {
        ContactModel model = new ContactModel();
        model.setItemType(typeHeader);
        return model;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public TLRPC.User getData() {
        return data;
    }

    public void setData(TLRPC.User data) {
        this.data = data;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
