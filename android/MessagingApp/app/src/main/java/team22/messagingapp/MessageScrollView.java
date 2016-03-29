package team22.messagingapp;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class MessageScrollView extends ScrollView {
    onTopReachedListener mListener;
    public MessageScrollView(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
    }

    public MessageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageScrollView(Context context) {
        super(context);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = (View) getChildAt(getChildCount()-1);

        if(view.getTop()==t){
            mListener.onTopReached();
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }

    public onTopReachedListener getOnTopReachedListener() {
        return mListener;
    }

    public void setOnTopReachedListener(
            onTopReachedListener onTopReachedListener) {
        mListener = onTopReachedListener;
    }


    /**
     * Event listener.
     */
    public interface onTopReachedListener{
        public void onTopReached();
    }
}
