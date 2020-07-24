package com.example.squadbuilderrepository;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.squadbuilderrepository.Adapters.PlayerAdapter;
import com.example.squadbuilderrepository.Database.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String [] DIVISIONS = {"GK", "DEF", "MID", "FW"};

    private FloatingActionButton mFabAddPlayer;
    private RecyclerView mRecyclerViewPlayerList;

    private PlayerAdapter mAdapter;
    private List<Player> mPlayers;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStorage = FirebaseStorage.getInstance();

        mFabAddPlayer = findViewById(R.id.fab_add_player);
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
                FragmentManager fragmentManager = getSupportFragmentManager();
                addPlayerFragment.show(fragmentManager, "Add Player");
            }
        });

        for (final String div : DIVISIONS) {
            FirebaseFirestore.getInstance().collection("players")
                    .document(div).collection("info")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Player player = document.toObject(Player.class);
                                player.setPid(document.getId());
                                mPlayers.add(player);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}