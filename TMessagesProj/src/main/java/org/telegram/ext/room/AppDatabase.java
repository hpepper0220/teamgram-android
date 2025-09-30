package org.telegram.ext.room;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.telegram.ext.model.FriendRequest;
import org.telegram.messenger.ApplicationLoader;

@Database(entities = {FriendRequest.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract FriendRequestDao friendRequestDao();

    public static AppDatabase getInstance() {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    ApplicationLoader.applicationContext, AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
