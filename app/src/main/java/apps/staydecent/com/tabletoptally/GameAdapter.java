package apps.staydecent.com.tabletoptally;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> gameList;

    public GameAdapter(List<Game> gameList) {
        this.gameList = gameList;
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GameAdapter.GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_list_item, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GameViewHolder gameViewHolder, int idx) {
        Game game = gameList.get(idx);
        gameViewHolder.name.setText(game.getName());
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.name)
        TextView name;

        public GameViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
