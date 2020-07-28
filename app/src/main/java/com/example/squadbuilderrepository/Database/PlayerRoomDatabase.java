package com.example.squadbuilderrepository.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Player.class}, version = 1,exportSchema = false)
public abstract class PlayerRoomDatabase extends RoomDatabase {
    abstract PlayerDao getDao();
    private static volatile PlayerRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 10;

    static final ExecutorService dbWriteExecutors = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static PlayerRoomDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (PlayerRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PlayerRoomDatabase.class,
                            "playerDatabase")
                            .addCallback(roomDbCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomDbCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            dbWriteExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    PlayerDao playerDao = INSTANCE.getDao();

                    // Initialization Operations
//                    playerDao.deleteAll();
                }
            });
        }
    };
}
