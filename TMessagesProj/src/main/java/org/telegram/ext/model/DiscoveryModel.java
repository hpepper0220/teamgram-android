package org.telegram.ext.model;

import org.telegram.tgnet.TLRPC;

public class DiscoveryModel {

    public static final int typeEmpty = 0x001;
    public static final int typeData = 0x002;

    private int itemType;
    private TLRPC.TL_discoverPage data;

    public DiscoveryModel() {
    }

    public DiscoveryModel(int itemType, TLRPC.TL_discoverPage data) {
        this.itemType = itemType;
        this.data = data;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public TLRPC.TL_discoverPage getData() {
        return data;
    }

    public void setData(TLRPC.TL_discoverPage data) {
        this.data = data;
    }
}
