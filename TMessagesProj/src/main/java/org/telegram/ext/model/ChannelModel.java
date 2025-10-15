package org.telegram.ext.model;

import org.telegram.tgnet.TLRPC;

public class ChannelModel {

    public static final int typeEmpty = 0x001;
    public static final int typeData = 0x002;
    public static final int typeHeader = 0x003;

    private int itemType;
    private TLRPC.TL_channel data;

    public ChannelModel() {
    }

    public ChannelModel(int itemType, TLRPC.TL_channel data) {
        this.itemType = itemType;
        this.data = data;
    }

    public static ChannelModel header() {
        ChannelModel model = new ChannelModel();
        model.setItemType(typeHeader);
        return model;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public TLRPC.TL_channel getData() {
        return data;
    }

    public void setData(TLRPC.TL_channel data) {
        this.data = data;
    }
}
