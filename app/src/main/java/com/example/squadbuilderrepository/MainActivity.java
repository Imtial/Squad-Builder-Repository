package com.example.squadbuilderrepository;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.squadbuilderrepository.Adapters.PlayerAdapter;
import com.example.squadbuilderrepository.Database.Player;
import com.example.squadbuilderrepository.Database.PlayerViewModel;
import com.example.squadbuilderrepository.Utils.DatabaseSyncUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AddPlayerDialog.DataSetChangeListener {

    private static String [] DIVISIONS = {"GK", "DEF", "MID", "FW"};

    private int recentlyDeletedPosition;
    private Player recentlyDeletedPlayer;

    private FloatingActionButton mFabAddPlayer;
    private FloatingActionButton mFabUndoDelete;
    private RecyclerView mRecyclerViewPlayerList;

    private PlayerViewModel mPlayerViewModel;
    private PlayerAdapter mAdapter;
    private List<Player> mPlayers;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStorage = FirebaseStorage.getInstance();

        mFabAddPlayer = findViewById(R.id.fab_add_player);
        mFabUndoDelete = findViewById(R.id.fab_undo);
        mRecyclerViewPlayerList = findViewById(R.id.rv_player_list);

        mRecyclerViewPlayerList.setHasFixedSize(true);
        mRecyclerViewPlayerList.setLayoutManager(new LinearLayoutManager(this));

        mPlayers = new ArrayList<>();
        mAdapter = new PlayerAdapter(this, mPlayers);
        mRecyclerViewPlayerList.setAdapter(mAdapter);

        mFabAddPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPlayerDialog addPlayerFragment = new AddPlayerDialog();
                addPlayerFragment.setDataSetChangeListener(MainActivity.this);
                FragmentManager fragmentManager = getSupportFragmentManager();
                addPlayerFragment.show(fragmentManager, "Add Player");
            }
        });

        mFabUndoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recentlyDeletedPlayer != null) {
                    mPlayerViewModel.insert(recentlyDeletedPlayer);
                    mFabUndoDelete.setVisibility(View.GONE);
                }
            }
        });

        mPlayerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        mPlayerViewModel.getPlayers().observe(this, new Observer<List<Player>>() {
            @Override
            public void onChanged(List<Player> players) {
                mAdapter.setPlayers(players);
            }
        });

        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                recentlyDeletedPosition = viewHolder.getAbsoluteAdapterPosition();
                recentlyDeletedPlayer = mPlayerViewModel.getPlayers().getValue().get(recentlyDeletedPosition);
                mAdapter.deleteItem(recentlyDeletedPosition);
                mPlayerViewModel.delete(recentlyDeletedPlayer.getPid());
                mFabUndoDelete.setVisibility(View.VISIBLE);
                Snackbar.make(mRecyclerViewPlayerList, recentlyDeletedPlayer.getName() + " is deleted.", Snackbar.LENGTH_LONG).show();
            }
        });
        touchHelper.attachToRecyclerView(mRecyclerViewPlayerList);

//        for (final String div : DIVISIONS) {
//            FirebaseFirestore.getInstance().collection("players")
//                    .document(div).collection("info")
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                                Player player = document.toObject(Player.class);
//                                player.setPid(document.getId());
//                                mPlayers.add(player);
//                            }
//                            mAdapter.notifyDataSetChanged();
//                        }
//                    });
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_push_local:
                DatabaseSyncUtil.syncCloudWithLocal(getApplication());
                return true;
            case R.id.menu_pull_cloud:
                DatabaseSyncUtil.syncLocalWithCloud(getApplication());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void insertItem(Player player) {
        mPlayerViewModel.insert(player);
    }
}