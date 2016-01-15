package apps.staydecent.com.tabletoptally;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private GameCollection mGameCollection;

    public GameAdapter(GameCollection gameCollection) {
        this.mGameCollection = gameCollection;
    }

    @Override
    public int getItemCount() {
        return mGameCollection.size();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GameAdapter.GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_list_item, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GameViewHolder gameViewHolder, int position) {
        Game game = mGameCollection.get(position);
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
