package apps.staydecent.com.tabletoptally.fragments;

import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import apps.staydecent.com.tabletoptally.R;
import apps.staydecent.com.tabletoptally.models.Game;
import butterknife.Bind;
import butterknife.ButterKnife;

public class GameFragment extends Fragment {

    private static final String ARG_GAME_ID = "arg_game_id";
    private static final String ARG_ALBUM_IMAGE_POSITION = "arg_album_image_position";
    private static final String ARG_STARTING_ALBUM_IMAGE_POSITION = "arg_starting_album_image_position";

    @Bind(R.id.game_text_view)
    TextView mGameText;

    private int mStartingPosition;
    private int mGamePosition;
    private boolean mIsTransitioning;
    private long mBackgroundImageFadeMillis;

    public static GameFragment newInstance(Game game, int position, int startingPosition) {
        Bundle args = new Bundle();
        args.putLong(ARG_GAME_ID, game_id);
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
        mIsTransitioning = savedInstanceState == null && mStartingPosition == mGamePosition;
        mBackgroundImageFadeMillis = 1000;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        ButterKnife.bind(this, rootView);

        View textContainer = rootView.findViewById(R.id.game_text_container);
        TextView gameTitleText = (TextView) textContainer.findViewById(R.id.game_title);


        gameTitleText.setText(gameName);

        String transitionName = String.format(
                getResources().getString(R.string.tag_name_tpl),
                mGamePosition);
        mGameText.setTransitionName(transitionName);

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
}
