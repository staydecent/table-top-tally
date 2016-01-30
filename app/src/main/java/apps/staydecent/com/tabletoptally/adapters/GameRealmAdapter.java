package apps.staydecent.com.tabletoptally.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import apps.staydecent.com.tabletoptally.GameActivity;
import apps.staydecent.com.tabletoptally.MainActivity;
import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.Game;
import apps.staydecent.com.tabletoptally.models.Score;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

public class GameRealmAdapter
        extends RealmBasedRecyclerViewAdapter<Game, GameRealmAdapter.ViewHolder> {

    private Context context;

    public int colorIndex = 0;
    public List<Integer> colors = Arrays.asList(
            R.color.colorPrimaryDark,
            R.color.colorPrimaryAlt1,
            R.color.colorPrimaryAlt2,
            R.color.colorPrimaryAlt3,
            R.color.colorPrimaryAlt4);

    public GameRealmAdapter(
            Context context,
            RealmResults<Game> realmResults,
            boolean automaticUpdate,
            boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
        this.context = context;
    }

    public class ViewHolder extends RealmViewHolder {
        public Game game;

        @Bind(R.id.game_text_view)
        TextView gameTextView;

        @Bind(R.id.game_list_item)
        CardView cardView;

        public ViewHolder(FrameLayout container) {
            super(container);
            ButterKnife.bind(this, container);

            final ViewHolder vh = this;

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    game = realmResults.get(vh.getAdapterPosition());
                    if (game == null || !game.isValid()) {
                        return;
                    }

                    Intent intent = new Intent(context, GameActivity.class);
                    intent.putExtra("game_id", game.getId());

                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.startActivity(intent);
                    mainActivity.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    game = realmResults.get(vh.getAdapterPosition());
                    if (game == null || !game.isValid()) {
                        return false;
                    }
                    buildAndShowDeleteDialog(game);
                    return true;
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
        View v = inflater.inflate(R.layout.game_item_view, viewGroup, false);
        return new ViewHolder((FrameLayout) v);
    }

    @Override
    public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
        final Game game = realmResults.get(position);
        viewHolder.gameTextView.setText(game.getName());
        viewHolder.cardView.setCardBackgroundColor(getNextColor());
    }

    private void buildAndShowDeleteDialog(final Game game) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = context.getResources().getString(R.string.confirm_delete_game_tpl);
        builder.setTitle(String.format(title, game.getName()));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                asyncRemoveGame(game.getId());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void asyncRemoveGame(final long id) {
        AsyncTask<Void, Void, Void> remoteItem = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Realm instance = Realm.getInstance(context);
                Game game =
                        instance.where(Game.class).equalTo("id", id).findFirst();

                if (game != null) {
                    RealmResults<Score> scores =
                        instance.where(Score.class).equalTo("game.id", game.getId()).findAll();
                    instance.beginTransaction();
                    scores.clear();
                    game.removeFromRealm();
                    instance.commitTransaction();
                }

                instance.close();
                return null;
            }
        };
        remoteItem.execute();
    }

    private int getNextColor() {
        if (colorIndex >= colors.size() - 1) {
            colorIndex = 0;
        } else {
            colorIndex = colorIndex + 1;
        }

        return ContextCompat.getColor(context, colors.get(colorIndex));
    }
}

