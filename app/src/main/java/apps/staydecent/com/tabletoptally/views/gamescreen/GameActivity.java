package apps.staydecent.com.tabletoptally.views.gamescreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.helpers.ColorHelper;
import apps.staydecent.com.tabletoptally.models.GameModel;
import apps.staydecent.com.tabletoptally.models.ScoreModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class GameActivity extends Activity {

    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    private Realm realm;
    private long mGameId;
    private ColorHelper mColorHelper;
    private ScoreAdapter scoreAdapter;

    private GameFragment mCurrentGameFragment;
    private CoordinatorLayout.LayoutParams fabLayoutParams;
    private int mCurrentPosition;
    private int mStartingPosition;
    private boolean mIsReturning;

    @Bind(R.id.pager)
    ViewPager pager;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    @OnClick(R.id.fab)
    public void onFabClick() {
        buildAndShowPlayersDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        postponeEnterTransition();
        setEnterSharedElementCallback(mCallback);

        mColorHelper = new ColorHelper(this);
        realm = Realm.getDefaultInstance();

        mStartingPosition = getIntent().getIntExtra(
                getResources().getString(R.string.extra_starting_game_position), 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(
                    getResources().getString(R.string.extra_current_game_position));
        }

        mGameId = getIntent().getLongExtra("game_id", 0);

        pager.setAdapter(new GameFragmentPagerAdapter(getFragmentManager(), this));
        pager.setCurrentItem(mCurrentPosition);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("TTT", String.format("PAGER %d", position));
                // When user swipes to new page, load the GameModel for this screen
                mCurrentPosition = position;
                GameModel game = realm
                        .where(GameModel.class)
                        .findAllSorted("id", Sort.ASCENDING)
                        .get(position);
                mGameId = game.getId();
                // If the FAB is not visible, slide it in
                float currentY = fab.getTranslationY();
                if (currentY > 0) {
                    slideFabIn();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @Override
    public void onEnterAnimationComplete() {
        slideFabIn();
    }

    /**
     * Called when the back button is pressed and we are returning to MainActivity
     */
    @Override
    public void finishAfterTransition() {
        slideFabOut();
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(
                getResources().getString(R.string.extra_starting_game_position), mStartingPosition);
        data.putExtra(
                getResources().getString(R.string.extra_current_game_position), mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }


    protected void slideFabOut() {
        fab.setVisibility(View.INVISIBLE);
//        if (fabLayoutParams == null) {
//            fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//        }
//        int fabBottomMargin = fabLayoutParams.bottomMargin;
//        fab.animate()
//                .translationY(fabBottomMargin + fab.getHeight())
//                .setInterpolator(new LinearInterpolator())
//                .setDuration(200);
    }

    protected void slideFabIn() {
        if (fabLayoutParams == null) {
            fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        }
        int fabBottomMargin = fabLayoutParams.bottomMargin;
        fab.setTranslationY(fabBottomMargin + fab.getHeight());
        fab.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setStartDelay(300)
                .setDuration(200);
    }


    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            Log.d("TTT", String.format("SharedElementCallback %b", mIsReturning));
            if (mIsReturning) {
                slideFabOut();
                RelativeLayout sharedElement = mCurrentGameFragment.getGameContainer();
                if (sharedElement == null) {
                    // If shared element is null, then it has been scrolled off screen and
                    // no longer visible. In this case we cancel the shared element transition by
                    // removing the shared element from the shared elements map.
                    names.clear();
                    sharedElements.clear();
                } else if (mStartingPosition != mCurrentPosition) {
                    // If the user has swiped to a different ViewPager page, then we need to
                    // remove the old shared element and replace it with the new shared element
                    // that should be transitioned instead.
                    names.clear();
                    names.add(sharedElement.getTransitionName());
                    sharedElements.clear();
                    sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                }
            }
        }
    };


    // --- Fragments and dialogs

    private void buildAndShowPlayersDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getResources().getString(R.string.title_dialog_players));

        // Views
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_score_players_add, null);
        final AutoCompleteTextView input = (AutoCompleteTextView) dialogView.findViewById(R.id.input);
        final TextView tvNames = (TextView) dialogView.findViewById(R.id.names);
        final Button btnAdd = (Button) dialogView.findViewById(R.id.btn_add);

        // Get AutoComplete options from Realm
        RealmResults<ScoreModel> scores = realm
                .where(ScoreModel.class)
                .findAllSorted("id", Sort.ASCENDING);
        ArrayList<String> existingNames = new ArrayList<>(0);
        for (ScoreModel score : scores) {
            existingNames.addAll(splitPlayersFromScore(score));
        }
        // remove duplicate names
        Set<String> namesSet = new LinkedHashSet<>(existingNames);
        existingNames = new ArrayList<>(namesSet);

        // Store entered player names in list
        final ArrayList<String> playerNames = new ArrayList<>(0);

        // Create the AutoCompleteTextView adapter
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(GameActivity.this, android.R.layout.simple_list_item_1, existingNames);
        input.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If text is currently entered (not submitted) save it
                String newName = input.getText().toString();
                if (!Strings.isNullOrEmpty(newName)) {
                    playerNames.add(input.getText().toString());
                }
                // ... Then dismiss
                dialog.dismiss();
                buildAndShowWinnerDialog(playerNames);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String newName = input.getText().toString();
                playerNames.add(newName);
                joinTextViewString(tvNames, newName);
                input.setText("");
            }
        });

        final AlertDialog dialog = builder.show();
        input.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (isInputDone(actionId, event)) {
                            String newName = input.getText().toString();
                            playerNames.add(newName);
                            joinTextViewString(tvNames, newName);
                            input.setText("");
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void buildAndShowWinnerDialog(final ArrayList<String> playerNames) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getResources().getString(R.string.title_dialog_winner));

        // Views
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_score_winner_choose, null);
        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.winner_spinner);

        // Setup Spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                GameActivity.this,
                android.R.layout.simple_spinner_item,
                playerNames);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Input events
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                addScore(mGameId, playerNames, spinner.getSelectedItem().toString());
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

    private class GameFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private Context context;

        public GameFragmentPagerAdapter(FragmentManager fm, Context c) {
            super(fm);
            context = c;
        }

        @Override
        public Fragment getItem(int position) {
            RealmResults<GameModel> games = realm
                    .where(GameModel.class)
                    .findAllSorted("id", Sort.ASCENDING);

            GameModel offscreenGame = games.get(position);
            if (offscreenGame == null || !offscreenGame.isValid()) {
                return null;
            }
            int newColor = mColorHelper.getColorFromPosition(position);
            return GameFragment.newInstance(context, offscreenGame.getId(), newColor, position, mStartingPosition);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentGameFragment = (GameFragment) object;
        }

        @Override
        public int getCount() {
            return (int) realm
                .where(GameModel.class)
                .count();
        }
    }

    // --- Helpers

    @NonNull
    private ArrayList<String> splitPlayersFromScore(ScoreModel score) {
        Iterable<String> namesIterable = Splitter.on(", ")
                .trimResults()
                .omitEmptyStrings()
                .split(score.getPlayers());
        return Lists.newArrayList(namesIterable);
    }

    private void joinTextViewString(TextView tv, String str) {
        String prevStr = tv.getText().toString();
        if (Strings.isNullOrEmpty(prevStr)) {
            tv.setText(str);
        } else {
            String newStr = prevStr + ", " + str;
            tv.setText(newStr);
        }
    }

    private boolean isInputDone(int actionId, KeyEvent event) {
        return (actionId == EditorInfo.IME_ACTION_DONE ||
                (event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER));
    }

    private void addScore(long gameId, ArrayList<String> players, String winner) {
        GameModel game = realm
                .where(GameModel.class)
                .equalTo("id", gameId)
                .findFirst();
        realm.beginTransaction();
        ScoreModel score = realm.createObject(ScoreModel.class);
        score.setId(System.currentTimeMillis());
        score.setWinner(winner);
        score.setPlayers(Joiner.on(", ").join(players));
        score.setGame(game);
        realm.commitTransaction();
        mCurrentGameFragment.updateScoresAndNotifyAdapter();
    }

}
