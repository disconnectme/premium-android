package pro.disconnect.me.feeds.news;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import org.w3c.dom.Text;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.models.Tracker;
import pro.disconnect.me.feeds.OnPostInteractionListener;
import pro.disconnect.me.feeds.TimeAgo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {

    private static final Pattern sSummaryPattern = Pattern.compile("\\[([^\\]]+)");
    private static final Pattern sLinkPattern = Pattern.compile("\\(([^)]+)\\)(?!.*\\d)");

    private final TimeAgo mTimeAgo;
    private final RequestManager mGlideRequestManager;
    private final OnPostInteractionListener mOnPostInteractionListener;

    private List<Post> mValues;

    public NewsRecyclerViewAdapter(RequestManager aGlideRequestManager, OnPostInteractionListener aOnPostInteractionListener, TimeAgo aTimeAgo ) {
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
                .inflate(R.layout.fragment_news_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NewsRecyclerViewAdapter.ViewHolder holder, int position) {
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
        public final TextView mSummaryView;
        public final ImageView mImageView;
        public final TextView mDateView;
        public String mLink;
        public int mId;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mSummaryView = (TextView) view.findViewById(R.id.summary);
            mImageView = (ImageView) view.findViewById(R.id.image);
            mDateView = (TextView) view.findViewById(R.id.date_field);
        }

        public void bind(Post aPost){
            mId = aPost.getId();

            mTitleView.setText(aPost.getTitle());

            String markDown = aPost.getMarkdown();
            Matcher summaryMatcher = sSummaryPattern.matcher(markDown);
            if ( summaryMatcher.find() ){
                mSummaryView.setText(summaryMatcher.group(1));
            }

            Matcher linkMatcher = sLinkPattern.matcher(markDown);
            if ( linkMatcher.find() ){
               mLink = linkMatcher.group(1);
            } else {
                mLink = null;
            }

            try {
                String publishedAt = mTimeAgo.fromDateString(aPost.getPublishedAt());
                mDateView.setText(publishedAt);
            } catch (Exception exception){
                mDateView.setText(null);
            }

            // Load image
            mGlideRequestManager
                    .load(aPost.getImage())
                    .into(mImageView);
        }
    }
}
