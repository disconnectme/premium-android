package pro.disconnect.me.feeds.alerts;

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


public class AlertsRecyclerViewAdapter extends RecyclerView.Adapter<AlertsRecyclerViewAdapter.ViewHolder> {
    private static final Pattern sLinkPattern = Pattern.compile("\\(([^)]+)\\)(?!.*\\d)");

    private static final int READ_LAYOUT = 0;
    private static final int UNREAD_LAYOUT = 1;

    private final TimeAgo mTimeAgo;
    private final RequestManager mGlideRequestManager;
    private final OnPostInteractionListener mOnPostInteractionListener;

    private List<Post> mValues;

    public AlertsRecyclerViewAdapter(RequestManager aGlideRequestManager, OnPostInteractionListener aOnPostInteractionListener, TimeAgo aTimeAgo ) {
        mGlideRequestManager = aGlideRequestManager;
        mOnPostInteractionListener = aOnPostInteractionListener;
        mTimeAgo = aTimeAgo;
    }

    public void setPosts(List<Post> aValues){
        mValues = aValues;
    }

    @Override
    public int getItemViewType(int position) {
        return mValues.get(position).getSeen() ? READ_LAYOUT : UNREAD_LAYOUT;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int resId = viewType == READ_LAYOUT ? R.layout.fragment_alerts_read_item : R.layout.fragment_alerts_unread_item;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlertsRecyclerViewAdapter.ViewHolder holder, int position) {
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
        public final TextView mDateView;
        public String mLink;
        public int mId;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mImageView = (ImageView) view.findViewById(R.id.image);
            mDateView = (TextView) view.findViewById(R.id.date_field);
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
