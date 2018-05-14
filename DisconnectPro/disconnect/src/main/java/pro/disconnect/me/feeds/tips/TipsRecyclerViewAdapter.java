package pro.disconnect.me.feeds.tips;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.feeds.OnPostInteractionListener;
import pro.disconnect.me.feeds.TimeAgo;

public class TipsRecyclerViewAdapter extends RecyclerView.Adapter<TipsRecyclerViewAdapter.ViewHolder> {

    private static final Pattern sSummaryPattern = Pattern.compile("\\[([^\\]]+)");
    private static final Pattern sLinkPattern = Pattern.compile("\\(([^)]+)\\)(?!.*\\d)");
    private static final Pattern sDurationPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
    private static final Pattern sDifficultyPattern = Pattern.compile("==(.*?)==");

    private final TimeAgo mTimeAgo;
    private final RequestManager mGlideRequestManager;
    private final OnPostInteractionListener mOnPostInteractionListener;

    private List<Post> mValues;

    public TipsRecyclerViewAdapter(RequestManager aGlideRequestManager, OnPostInteractionListener aOnPostInteractionListener, TimeAgo aTimeAgo ) {
        mGlideRequestManager = aGlideRequestManager;
        mOnPostInteractionListener = aOnPostInteractionListener;
        mTimeAgo = aTimeAgo;
    }

    public void setPosts(List<Post> aValues){
        mValues = aValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tips_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TipsRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(mValues.get(position));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null !=  mOnPostInteractionListener && holder.mLink != null) {
                    mOnPostInteractionListener.onPostLink(holder.mId, holder.mLink);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final ImageView mImageView;
        public final TextView mDurationView;
        public final ImageView mDifficultyOneView;
        public final ImageView mDifficultyTwoView;
        public final ImageView mDifficultyThreeView;
        public String mLink;
        public int mId;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mImageView = (ImageView) view.findViewById(R.id.image);
            mDurationView = (TextView) view.findViewById(R.id.duration);
            mDifficultyOneView= (ImageView) view.findViewById(R.id.difficulty_1);
            mDifficultyTwoView= (ImageView) view.findViewById(R.id.difficulty_2);
            mDifficultyThreeView= (ImageView) view.findViewById(R.id.difficulty_3);
        }

        public void bind(Post aPost){
            mId = aPost.getId();

            mTitleView.setText(aPost.getTitle());

            String markDown = aPost.getMarkdown();

            Matcher linkMatcher = sLinkPattern.matcher(markDown);
            if ( linkMatcher.find() ){
               mLink = linkMatcher.group(1);
            } else {
                mLink = null;
            }

            String duration = "";
            Matcher durationMatcher = sDurationPattern.matcher(markDown);
            if ( durationMatcher.find() ){
                duration  = durationMatcher.group(1);
            }
            mDurationView.setText(duration);

            int difficulty = 0;
            Matcher difficultyMatcher = sDifficultyPattern.matcher(markDown);
            if ( difficultyMatcher.find() ){
                String difficultyStr  = difficultyMatcher.group(1);
                difficulty = Integer.parseInt(difficultyStr);
            }

            mDifficultyOneView.setSelected(difficulty == 1);
            mDifficultyTwoView.setSelected(difficulty == 2);
            mDifficultyThreeView.setSelected(difficulty == 3);

            // Load image
            mGlideRequestManager
                    .load(aPost.getImage())
                    .into(mImageView);
        }
    }
}
