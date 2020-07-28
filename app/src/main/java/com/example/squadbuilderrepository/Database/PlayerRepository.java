package com.example.squadbuilderrepository.Database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PlayerRepository {
    private PlayerDao playerDao;
    private LiveData<List<Player>> mPlayers;
    private static PlayerRepository INSTANCE;

    private PlayerRepository (Application application) {
        PlayerRoomDatabase database = PlayerRoomDatabase.getDatabase(application);

        playerDao = database.getDao();
        mPlayers = playerDao.getAlphabetizedPlayer();
    }

    public static PlayerRepository getInstance(Application application) {
        if (INSTANCE == null) INSTANCE = new PlayerRepository(application);
        return INSTANCE;
    }

    public void insert (final Player player) {
        PlayerRoomDatabase.dbWriteExecutors.execute(new Runnable() {
            @Override
            public void run() {
                playerDao.insert(player);
            }
        });
    }

    public void delete (final String pid) {
        PlayerRoomDatabase.dbWriteExecutors.execute(new Runnable() {
            @Override
            public void run() {
                playerDao.delete(pid);
            }
        });
    }

    public LiveData<List<Player>> getPlayers () {
        return mPlayers;
    }

    public List<Player> getStaticPlayerList () {
        return playerDao.getStaticPlayerList();
    }

    public List<String> getStaticPlayerIDs () {
        return playerDao.getStaticPlayerIDs();
    }
}
