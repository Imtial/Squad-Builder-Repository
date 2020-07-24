package com.example.squadbuilderrepository.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.squadbuilderrepository.Database.Player;
import com.example.squadbuilderrepository.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private Context mContext;
    private List<Player> mPlayers;

    public PlayerAdapter(Context context, List<Player> players) {
        mContext = context;
        mPlayers = players;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player currentPlayer = mPlayers.get(position);

        holder.tvName.setText(currentPlayer.getName());
        holder.tvPosition.setText(currentPlayer.getPosition());
        String priceString = currentPlayer.getPrice() + " $";
        holder.tvPrice.setText(priceString);
        holder.tvRating.setText(String.valueOf(currentPlayer.getRating()));

        Picasso.get()
                .load(currentPlayer.getDownloadUrl())
                .placeholder(R.drawable.ic_baseline_person)
                .resize(80, 80)
                .centerCrop()
                .into(holder.ivPicture);
    }

    @Override
    public int getItemCount() {
        return mPlayers != null ? mPlayers.size() : 0;
    }

    public class PlayerViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPicture;
        TextView tvName;
        TextView tvPosition;
        TextView tvPrice;
        TextView tvRating;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPicture = itemView.findViewById(R.id.player_item_picture);
            tvName = itemView.findViewById(R.id.player_item_name);
            tvPosition = itemView.findViewById(R.id.player_item_position);
            tvPrice = itemView.findViewById(R.id.player_item_price);
            tvRating = itemView.findViewById(R.id.player_item_rating);
        }
    }
}
