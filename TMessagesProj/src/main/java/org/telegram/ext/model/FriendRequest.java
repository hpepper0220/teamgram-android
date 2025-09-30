package org.telegram.ext.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friend_requests")
public class FriendRequest {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "from_user_id")
    public String fromUserId;   // 发送者ID

    @ColumnInfo(name = "to_user_id")
    public String toUserId;     // 接收者ID（当前用户）

    @ColumnInfo(name = "message")
    public String message;      // 申请附言

    @ColumnInfo(name = "status")
    public int status;          // 状态：0=待处理, 1=已同意, 2=已拒绝

    @ColumnInfo(name = "timestamp")
    public long timestamp;      // 申请时间戳
}
