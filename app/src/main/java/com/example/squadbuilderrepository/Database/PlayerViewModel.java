package com.example.squadbuilderrepository.Database;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PlayerViewModel extends AndroidViewModel {

    private PlayerRepository mRepository;
    private LiveData<List<Player>> mPlayers;

    public PlayerViewModel(@NonNull Application application) {
        super(application);

        mRepository = PlayerRepository.getInstance(application);
        mPlayers = mRepository.getPlayers();
    }

    public LiveData<List<Player>> getPlayers () {
        return mPlayers;
    }

    public void insert (Player player) {
        mRepository.insert(player);
    }

    public void delete (String pid) {
        mRepository.delete(pid);
    }
}
