package apps.staydecent.com.tabletoptally.views.gamescreen;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.BinderThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.GameModel;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class GameFragment extends Fragment {

    private static final String ARG_ALBUM_IMAGE_POSITION = "arg_album_image_position";
    private static final String ARG_STARTING_ALBUM_IMAGE_POSITION = "arg_starting_album_image_position";

    private static final String ARG_GAME_ID = "arg_game_id";
    private static final String ARG_COLOR = "arg_color";

    private int mStartingPosition;
    private int mGamePosition;
    private int mColor;
    private GameModel mGameModel;
    private ScoreAdapter scoreAdapter;

    private static Context mContext;

    public Realm realm;

    @Bind(R.id.game_text_view)
    TextView mGameText;

    @Bind(R.id.scores_recycler_view)
    RecyclerView mScoresRecyclerView;

    public static GameFragment newInstance(Context context, long gameId, int color, int position, int startingPosition) {
        // the same context is used for all fragments thus does not need to be an arg
        mContext = context;

        // Fragment specific args
        Bundle args = new Bundle();
        args.putLong(ARG_GAME_ID, gameId);
        args.putInt(ARG_COLOR, color);
        args.putInt(ARG_ALBUM_IMAGE_POSITION, position);
        args.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);

        GameFragment fragment = new GameFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStartingPosition = getArguments().getInt(ARG_STARTING_ALBUM_IMAGE_POSITION);
        mGamePosition = getArguments().getInt(ARG_ALBUM_IMAGE_POSITION);
        mColor = getArguments().getInt(ARG_COLOR);
        long gameId = getArguments().getLong(ARG_GAME_ID);

        realm = Realm.getInstance(mContext);
        mGameModel = realm
                .where(GameModel.class)
                .equalTo("id", gameId)
                .findFirst();

        scoreAdapter = new ScoreAdapter(mContext, mGameModel.getId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        ButterKnife.bind(this, rootView);

        String transitionName = String.format(
                getResources().getString(R.string.tag_name_tpl),
                mGamePosition);

        mGameText.setTransitionName(transitionName);
        mGameText.setBackgroundColor(mColor);
        mGameText.setText(mGameModel.getName());

        mScoresRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mScoresRecyclerView.setAdapter(scoreAdapter);
        mScoresRecyclerView.setBackgroundColor(mColor);

        startPostponedEnterTransition();

        return rootView;
    }

    private void startPostponedEnterTransition() {
        if (mGamePosition == mStartingPosition) {
            mGameText.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mGameText.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    /**
     * Returns the shared element that should be transitioned back to the previous Activity,
     * or null if the view is not visible on the screen.
     */
    @Nullable
    public TextView getGameText() {
        if (isViewInBounds(getActivity().getWindow().getDecorView(), mGameText)) {
            return mGameText;
        }
        return null;
    }

    /**
     * Returns true if {@param view} is contained within {@param container}'s bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }

    /**
     * Expose the ScoreAdapter method so the GameActivity can call it as it holds a reference to
     * the fragment, and only this fragment holds a reference to the adapter.
     */
    public void updateScoresAndNotifyAdapter() {
        scoreAdapter.updateScores(true);
    }
}
