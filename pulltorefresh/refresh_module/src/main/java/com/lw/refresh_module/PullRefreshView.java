package com.lw.refresh_module;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author luowang8
 * @date 2020-08-21 10:54
 * @desc 下拉刷新控件
 */
public class PullRefreshView extends LinearLayout {
	
	
	/**
	 * 头部tag
	 */
	public static final String HEADER_TAG = "HEADER_TAG";
	
	/**
	 * 列表tag
	 */
	public static final String LIST_TAG   = "LIST_TAG";
	
	/**
	 * tag
	 */
	private static final String TAG = "PullRefreshView";
	
	/**
	 * 默认初始状态
	 */
	private @State
	int mState = State.INIT;
	
	/**
	 * 是否被拖拽
	 */
	private boolean mIsDragging = false;
	
	/**
	 * 上下文
	 */
	private Context mContext;
	
	
	/**
	 * RecyclerView
	 */
	private RecyclerView mRecyclerView;
	
	/**
	 * 顶部刷新头
	 */
	private View mHeaderView;
	
	/**
	 * 初始Y的坐标
	 */
	private int mInitMotionY;
	
	/**
	 * 上一次Y的坐标
	 */
	private int mLastMotionY;
	
	/**
	 * 手指触发滑动的临界距离
	 */
	private int mSlopTouch;
	
	/**
	 * 触发刷新的临界值
	 */
	private int mRefreshHeight = 200;
	
	/**
	 * 滑动时长
	 */
	private int mDuring = 300;
	
	/**
	 * 用户刷新监听器
	 */
	private OnRefreshListener mOnRefreshListener;
	
	/**
	 * 刷新文字提示
	 */
	private TextView mRefreshTip;
	
	/**
	 * 是否可拖拽, 因为在刷新头自由滑动和刷新状态的时候，
	 * 我们应该保持界面不被破坏
	 */
	private boolean mIsCanDrag = true;
	
	/**
	 * 头部布局
	 */
	private LayoutParams mHeaderLayoutParams;
	
	/**
	 * 列表布局
	 */
	private LayoutParams mListLayoutParams;
	
	/**
	 * 属性动画
	 */
	private ValueAnimator mValueAnimator;
	
	
	
	/////////////////////// 分割 ///////////////////////
	
	
	/**
	 * @param context
	 */
	public PullRefreshView(Context context) {
		this(context, null);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public PullRefreshView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	public PullRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		mContext = context;
		
		initView();
	}
	
	public RecyclerView getRecyclerView() {
		return mRecyclerView;
	}
	
	/**
	 * 设置RecyclerView
	 *
	 * @param recyclerView
	 */
	public void addRecyclerView(RecyclerView recyclerView) {
		
		if (recyclerView == null) {
			return;
		}
		
		View view = findViewWithTag(LIST_TAG);
		if (view != null) {
			removeView(view);
		}
		
		this.mRecyclerView = recyclerView;
		this.mRecyclerView.setTag(LIST_TAG);
		addView(recyclerView, mListLayoutParams);
	}
	
	/**
	 * 设置自定义刷新头部
	 * @param headerView
	 */
	public void addHeaderView(View headerView) {
		
		if (headerView == null) {
			return;
		}
		
		View view = findViewWithTag(HEADER_TAG);
		if (view != null) {
			removeView(view);
		}
		
		this.mHeaderView = headerView;
		this.mHeaderView.setTag(HEADER_TAG);
		addView(mHeaderView, mHeaderLayoutParams);
	}
	
	/**
	 * @param onRefreshListener
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		mOnRefreshListener = onRefreshListener;
	}
	
	/**
	 * 初始化View
	 */
	private void initView() {
		
		setOrientation(LinearLayout.VERTICAL);
		
		Context context = getContext();
		/** 1、添加刷新头Header */
		mHeaderView = LayoutInflater.from(context).inflate(R.layout.layout_refresh_header, null);
		mHeaderView.setTag(HEADER_TAG);
		mRefreshTip = mHeaderView.findViewById(R.id.content);
		mHeaderLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				DensityUtil.dip2px(mContext, 500)
		);
		this.addView(mHeaderView, mHeaderLayoutParams);
		
		/** 2、添加内容RecyclerView */
		mRecyclerView = new RecyclerView(context);
		mRecyclerView.setTag(LIST_TAG);
		mListLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(mRecyclerView, mListLayoutParams);
		
		/** 3、一开始的时候要让Header看不见，设置向上的负paddingTop */
		setPadding(0, -DensityUtil.dip2px(mContext, 500), 0, 0);
		
		ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
		mSlopTouch = viewConfiguration.getScaledTouchSlop();
		
		setState(State.INIT);
		
	}
	
	/**
	 * 设置状态，每个状态下，做不同的事情
	 *
	 * @param state 状态
	 */
	private void setState(@State int state) {
		
		switch (state) {
			case State.INIT:
				initState();
				break;
			
			case State.DRAGGING:
				dragState();
				break;
			
			case State.READY:
				readyState();
				break;
			
			case State.REFRESHING:
				refreshState();
				break;
			
			case State.FLING:
				flingState();
				break;
			
			default:
				break;
		}
		
		mState = state;
	}
	
	/**
	 * 处理初始化状态方法
	 */
	private void initState() {
		
		// 只有在初始状态时，恢复成可拖拽
		mIsCanDrag = true;
		mIsDragging = false;
		mRefreshTip.setText("下拉刷新");
	}
	
	/**
	 * 处理拖拽时方法
	 */
	private void dragState() {
		mIsDragging = true;
	}
	
	/**
	 * 拖拽距离超过header高度时，如何处理
	 */
	private void readyState() {
		mRefreshTip.setText("松手刷新");
	}
	
	/**
	 * 用户刷新时，如何处理
	 */
	private void refreshState() {
		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRefresh();
		}
		
		mIsCanDrag = false;
		mRefreshTip.setText("正在刷新,请稍后...");
	}
	
	/**
	 * 自由滚动时，如何处理
	 */
	private void flingState() {
		mIsDragging = false;
		mIsCanDrag = false;
		
		/** 自由滚动状态可以从两个状态进入：
		 *  1、READY状态。
		 *  2、其他状态。
		 *
		 *  ！滑动均需要平滑滑动
		 *  */
		if (mState == State.READY) {
			
			Log.e(TAG, "flingState: 从Ready状态开始自由滑动");
			// 从准备状态进入，刷新头滑到 200 的位置
			
			smoothScroll(getScrollY(), -mRefreshHeight);
		}
		else {
			
			Log.e(TAG, "flingState: 松手后，从其他状态开始自由滑动");
			// 从刷新状态进入，刷新头直接回到最初默认的位置
			// 即: 滑出界面，ScrollY 变成 0
			smoothScroll(getScrollY(), 0);
		}
		
	}
	
	/**
	 *  光滑滚动
	 * @param startPos 开始位置
	 * @param targetPos 结束位置
	 */
	private void smoothScroll(int startPos, final int targetPos) {
		
		// 如果有动画正在播放，先停止
		if (mValueAnimator != null && mValueAnimator.isRunning()) {
			mValueAnimator.cancel();
			mValueAnimator.end();
			mValueAnimator = null;
		}
		
		// 然后开启动画
		mValueAnimator = ValueAnimator.ofInt(getScrollY(), targetPos);
		mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				int value = (int) valueAnimator.getAnimatedValue();
				scrollTo(0, value);
				
				if (getScrollY() == targetPos) {
					if (targetPos != 0) {
						setState(State.REFRESHING);
					}
					else {
						setState(State.INIT);
					}
				}
			}
		});
		
		mValueAnimator.setDuration(mDuring);
		mValueAnimator.start();
	}
	
	/**
	 * 是否准备好触发下拉的状态了
	 */
	private boolean isReadyToPull() {
		
		if (mRecyclerView == null) {
			return false;
		}
		
		LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
		
		if (manager == null) {
			return false;
		}
		
		if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
			View child  = mRecyclerView.getChildAt(0);
			int  height = child.getHeight();
			if (height > mRecyclerView.getHeight()) {
				return child.getTop() == 0 && manager.findFirstVisibleItemPosition() == 0;
			}
			else {
				return manager.findFirstCompletelyVisibleItemPosition() == 0;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		int action = ev.getAction();
		
		Log.e(TAG, "onInterceptTouchEvent: action = " + action);
		
		if (!mIsCanDrag) {
			return true;
		}
		
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mIsDragging = false;
			return false;
		}
		
		if (mIsDragging && action == MotionEvent.ACTION_MOVE) {
			return true;
		}
		
		switch (action) {
			case MotionEvent.ACTION_MOVE:
				int diff = (int) (ev.getY() - mLastMotionY);
				if (Math.abs(diff) > mSlopTouch && diff > 1 && isReadyToPull()) {
					mLastMotionY = (int) ev.getY();
					mIsDragging = true;
				}
				break;
			
			case MotionEvent.ACTION_DOWN:
				if (isReadyToPull()) {
					setState(State.INIT);
					mInitMotionY = (int) ev.getY();
					mLastMotionY = (int) ev.getY();
				}
				break;
			
			default:
				break;
		}
		
		return mIsDragging;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int action = event.getAction();
		
		Log.e(TAG, "onTouchEvent: action = " + action);
		
		if (!mIsCanDrag) {
			return false;
		}
		
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (isReadyToPull()) {
					setState(State.INIT);
					mInitMotionY = (int) event.getY();
					mLastMotionY = (int) event.getY();
				}
				break;
			
			case MotionEvent.ACTION_MOVE:
				
				if (mIsDragging) {
					mLastMotionY = (int) event.getY();
					setState(State.DRAGGING);
					
					pullScroll();
					return true;
				}
				
				break;
			
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsDragging = false;
				setState(State.FLING);
				break;
			
			default:
				break;
			
		}
		
		return true;
	}
	
	/**
	 * 下拉移动界面，拉出刷新头
	 */
	private void pullScroll() {
		/** 滚动值 = 初始值 - 结尾值 */
		int scrollValue = (mInitMotionY - mLastMotionY) / 3;
		
		if (scrollValue > 0) {
			scrollTo(0, 0);
			return;
		}
		
		if (Math.abs(scrollValue) > mRefreshHeight
				&& mState == State.DRAGGING) {
			// 约定：如果偏移量超过 200(这个值，表示是否可以启动刷新的临界值，可任意定),
			// 那么状态变成 State.READY
			Log.e(TAG, "pullScroll: 超过了触发刷新的临界值");
			setState(State.READY);
		}
		
		scrollTo(0, scrollValue);
	}
	
	/**
	 * 刷新完成，需要调用方主动发起，才能完成将刷新头收起
	 */
	public void refreshComplete() {
		mRefreshTip.setText("刷新完成！");
		setState(State.FLING);
	}
	
	@IntDef({
			        State.INIT
			        , State.DRAGGING
			        , State.READY
			        , State.REFRESHING
			        , State.FLING,
	        })
	@Retention(RetentionPolicy.SOURCE)
	public @interface State {
		
		/**
		 * 初始状态
		 */
		int INIT = 1;
		
		/**
		 * 手指拖拽状态
		 */
		int DRAGGING = 2;
		
		/**
		 * 就绪状态，松开手指后，可以刷新
		 */
		int READY = 3;
		
		/**
		 * 刷新状态，这个状态下，用户用于发起刷新请求
		 */
		int REFRESHING = 4;
		
		/**
		 * 松开手指，顶部自然回弹的状态，有两种表现
		 * 1、手指释放时的高度大于刷新头的高度。
		 * 2、手指释放时的高度小于刷新头的高度。
		 */
		int FLING = 5;
	}
	
	/**
	 * 用户刷新状态的操作
	 */
	public interface OnRefreshListener {
		void onRefresh();
	}
	
}
