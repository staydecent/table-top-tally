package apps.staydecent.com.tabletoptally.ui.mainscreen;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import apps.staydecent.com.tabletoptally.ui.gamescreen.GameActivity;
import apps.staydecent.com.tabletoptally.MainActivity;
import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.GameModel;
import apps.staydecent.com.tabletoptally.models.ScoreModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

public class GameRealmAdapter
        extends RealmBasedRecyclerViewAdapter<GameModel, GameRealmAdapter.ViewHolder> {

    private Context context;

    public List<Integer> colors = Arrays.asList(
            R.color.colorPrimaryDark,
            R.color.colorPrimaryAlt1,
            R.color.colorPrimaryAlt2,
            R.color.colorPrimaryAlt3,
            R.color.colorPrimaryAlt4);

    public GameRealmAdapter(
            Context context,
            RealmResults<GameModel> realmResults,
            boolean automaticUpdate,
            boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
        View v = inflater.inflate(R.layout.game_item_view, viewGroup, false);
        return new ViewHolder((FrameLayout) v);
    }

    @Override
    public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
        final GameModel game = realmResults.get(position);
        viewHolder.bind(position);
        viewHolder.gameTextView.setText(game.getName());
        viewHolder.gameTextView.setBackgroundColor(getColorFromPosition(position));
    }

    private void buildAndShowDeleteDialog(final GameModel game) {
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
                GameModel game =
                        instance.where(GameModel.class).equalTo("id", id).findFirst();

                if (game != null) {
                    RealmResults<ScoreModel> scores =
                        instance.where(ScoreModel.class).equalTo("game.id", game.getId()).findAll();
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

    private int getColorFromPosition(int position) {
        int index = 0;
        int max = colors.size(); // 5

        if (position < max) {
            index = position;
        } else {
            index = position - max;

            // if position is >= max*2
            if (index >= max) {
                int factor = index / max;
                index = index - (factor * max);
            }
        }

        return ContextCompat.getColor(context, colors.get(index));
    }


    public class ViewHolder extends RealmViewHolder {
        public GameModel game;

        @Bind(R.id.game_text_view)
        TextView gameTextView;

        @Bind(R.id.game_list_card)
        CardView cardView;

        public ViewHolder(FrameLayout container) {
            super(container);
            ButterKnife.bind(this, container);

            final ViewHolder vh = this;

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = vh.getAdapterPosition();

                    game = realmResults.get(position);
                    if (game == null || !game.isValid()) {
                        return;
                    }

                    Intent intent = new Intent(context, GameActivity.class);
                    intent.putExtra(context.getResources().getString(R.string.extra_starting_game_position), position);
                    intent.putExtra("game_id", game.getId());
                    intent.putExtra("color", getColorFromPosition(position));

                    MainActivity mainActivity = (MainActivity) context;

                    if (!mainActivity.isGameActivityStarted) {
                        mainActivity.isGameActivityStarted = true;

                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                                mainActivity,
                                gameTextView,
                                gameTextView.getTransitionName());

                        Log.d("TTT", String.format("start GameActivity for %s", game.getName()));
                        mainActivity.startActivity(intent, options.toBundle());
                    }
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

        public void bind(int position) {
            String tagName = String.format(
                    context.getResources().getString(R.string.tag_name_tpl),
                    position);
            gameTextView.setTag(tagName);
            gameTextView.setTransitionName(tagName);
        }
    }

}

