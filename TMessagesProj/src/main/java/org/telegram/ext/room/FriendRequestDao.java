package org.telegram.ext.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.telegram.ext.model.FriendRequest;

import java.util.List;

@Dao
public interface FriendRequestDao {

    // 插入一条好友申请
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRequest(FriendRequest request);

    // 查询所有好友申请
    @Query("SELECT * FROM friend_requests ORDER BY timestamp DESC")
    List<FriendRequest> getAllRequests();

    // 查询未处理的好友申请数
    @Query("SELECT COUNT(*) FROM friend_requests WHERE status = 0")
    int getPendingCount();

    // 更新状态（同意/拒绝）
    @Query("UPDATE friend_requests SET status = :status WHERE id = :id")
    void updateStatus(int id, int status);
}
