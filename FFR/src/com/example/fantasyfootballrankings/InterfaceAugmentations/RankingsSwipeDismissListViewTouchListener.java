package com.example.fantasyfootballrankings.InterfaceAugmentations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.fantasyfootballrankings.ClassFiles.LittleStorage.Draft;
import com.example.fantasyfootballrankings.Pages.Home;
import com.example.fantasyfootballrankings.Pages.ImportLeague;
import com.example.fantasyfootballrankings.Pages.Rankings;
import com.ffr.fantasyfootballrankings.R;

public class RankingsSwipeDismissListViewTouchListener implements
		View.OnTouchListener {
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;

	// Fixed properties
	private ListView mListView;
	private OnDismissCallback mCallback;
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

	// Transient properties
	private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
	private int mDismissAnimationRefCount = 0;
	private float mDownX;
	private float mDownY;
	private boolean mSwiping;
	private VelocityTracker mVelocityTracker;
	private int mDownPosition;
	private View mDownView;
	private boolean mPaused;

	/**
	 * The callback interface used by {@link SwipeDismissListViewTouchListener}
	 * to inform its client about a successful dismissal of one or more list
	 * item positions.
	 */
	public interface OnDismissCallback {
		/**
		 * Called when the user has indicated they she would like to dismiss one
		 * or more list item positions.
		 * 
		 * @param listView
		 *            The originating {@link ListView}.
		 * @param reverseSortedPositions
		 *            An array of positions to dismiss, sorted in descending
		 *            order for convenience.
		 */
		void onDismiss(ListView listView, int[] reverseSortedPositions);
	}

	/**
	 * Constructs a new swipe-to-dismiss touch listener for the given list view.
	 * 
	 * @param listView
	 *            The list view whose items should be dismissable.
	 * @param callback
	 *            The callback to trigger when the user has indicated that she
	 *            would like to dismiss one or more list items.
	 */
	public RankingsSwipeDismissListViewTouchListener(boolean isSwipe,
			String source, ListView listView, OnDismissCallback callback) {
		ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = listView.getContext().getResources()
				.getInteger(android.R.integer.config_shortAnimTime);
		mListView = listView;
		mCallback = callback;
	}

	/**
	 * Enables or disables (pauses or resumes) watching for swipe-to-dismiss
	 * gestures.
	 * 
	 * @param enabled
	 *            Whether or not to watch for gestures.
	 */
	public void setEnabled(boolean enabled) {
		mPaused = !enabled;
	}

	/**
	 * Returns an {@link android.widget.AbsListView.OnScrollListener} to be
	 * added to the {@link ListView} using
	 * {@link ListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)}
	 * . If a scroll listener is already assigned, the caller should still pass
	 * scroll changes through to this listener. This will ensure that this
	 * {@link SwipeDismissListViewTouchListener} is paused during list view
	 * scrolling.</p>
	 * 
	 * @see {@link SwipeDismissListViewTouchListener}
	 */
	public AbsListView.OnScrollListener makeScrollListener() {
		return new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {
			}
		};
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (mViewWidth < 2) {
			mViewWidth = mListView.getWidth();
		}
		switch (motionEvent.getActionMasked()) {

		case MotionEvent.ACTION_DOWN: {
			if (mPaused) {
				return false;
			} else {

				Rect rect = new Rect();
				int childCount = mListView.getChildCount();
				int[] listViewCoords = new int[2];
				mListView.getLocationOnScreen(listViewCoords);
				int x = (int) motionEvent.getRawX() - listViewCoords[0];
				int y = (int) motionEvent.getRawY() - listViewCoords[1];
				View child;
				for (int i = 0; i < childCount; i++) {
					child = mListView.getChildAt(i);
					child.getHitRect(rect);
					if (rect.contains(x, y)) {
						mDownView = child;
						Rankings.swipedText = ((TextView) ((RelativeLayout) child)
								.findViewById(R.id.text1)).getText().toString();
						break;
					}
				}
				if (mDownView != null) {
					mDownX = motionEvent.getRawX();
					mDownY = motionEvent.getRawY();
					mDownPosition = mListView.getPositionForView(mDownView);
					mVelocityTracker = VelocityTracker.obtain();
					mVelocityTracker.addMovement(motionEvent);
					Rankings.isSwiping = true;
				}
				view.onTouchEvent(motionEvent);
			}
			return false;
		}

		case MotionEvent.ACTION_UP: {
			if (mVelocityTracker == null) {
				break;
			} else {
				float deltaX = motionEvent.getRawX() - mDownX;
				mVelocityTracker.addMovement(motionEvent);
				mVelocityTracker.computeCurrentVelocity(1000);
				float velocityX = Math.abs(mVelocityTracker.getXVelocity());
				float velocityY = Math.abs(mVelocityTracker.getYVelocity());
				boolean dismiss = false;
				boolean dismissRight = false;
				if (Math.abs(deltaX) > mViewWidth / 2) {
					dismiss = true;
					dismissRight = deltaX > 0;
				} else if (mMinFlingVelocity <= velocityX
						&& velocityX <= mMaxFlingVelocity
						&& velocityY < velocityX) {
					dismiss = true;
					dismissRight = mVelocityTracker.getXVelocity() > 0;
				}
				if (dismiss) {
					// dismiss
					final View downView = mDownView; // mDownView gets null'd
														// before animation ends
					final int downPosition = mDownPosition;
					++mDismissAnimationRefCount;
					mDownView
							.animate()
							.translationX(
									dismissRight ? mViewWidth : -mViewWidth)
							.alpha(0).setDuration(mAnimationTime)
							.setListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									performDismiss(downView, downPosition);
								}
							});

				} else {
					// cancel
					mDownView.animate().translationX(0).alpha(1)
							.setDuration(mAnimationTime).setListener(null);
					mDownView.invalidate();
					Rankings.isSwiping = false;
				}
				mVelocityTracker = null;
				mDownX = 0;
				mDownView = null;
				mDownPosition = ListView.INVALID_POSITION;
				mSwiping = false;
				break;
			}
		}

		case MotionEvent.ACTION_MOVE: {
			float deltaX = motionEvent.getRawX() - mDownX;
			float deltaY = -1 * (motionEvent.getRawY() - mDownY);
			if (mVelocityTracker == null || mPaused) {
				Rankings.isSwiping = false;
				Rankings.listview.invalidate();
				break;
			}
			mVelocityTracker.addMovement(motionEvent);

			if (Math.abs(deltaX) > mSlop) {
				mSwiping = true;
				mListView.requestDisallowInterceptTouchEvent(true);
				// Cancel ListView's touch (un-highlighting the item)
				MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
				cancelEvent
						.setAction(MotionEvent.ACTION_CANCEL
								| (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				mListView.onTouchEvent(cancelEvent);
			}

			if (mSwiping) {
				Rankings.isSwiping = true;
				mDownView.invalidate();
				mDownView.setTranslationX(deltaX);
				mDownView.setAlpha(Math.max(0f,
						Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
				return false;
			} else {
				if (deltaX < deltaY) {
					Rankings.isSwiping = false;
				}
			}
			break;
		}
		}
		return false;
	}

	class PendingDismissData implements Comparable<PendingDismissData> {
		public int position;
		public View view;

		public PendingDismissData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		@Override
		public int compareTo(PendingDismissData other) {
			// Sort by descending position
			return other.position - position;
		}
	}

	private void performDismiss(final View dismissView,
			final int dismissPosition) {
		// Animate the dismissed list item to zero-height and fire the dismiss
		// callback when
		// all dismissed list item animations have completed. This triggers
		// layout on each animation
		// frame; in the future we may want to do something smarter and more
		// performant.
		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
		final int originalHeight = dismissView.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1)
				.setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				--mDismissAnimationRefCount;
				if (mDismissAnimationRefCount == 0) {
					// No active animations, process all pending dismisses.
					// Sort by descending position
					Collections.sort(mPendingDismisses);

					int[] dismissPositions = new int[mPendingDismisses.size()];
					for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
						dismissPositions[i] = mPendingDismisses.get(i).position;
					}
					mCallback.onDismiss(mListView, dismissPositions);

					ViewGroup.LayoutParams lp;
					for (PendingDismissData pendingDismiss : mPendingDismisses) {
						// Reset view presentation
						pendingDismiss.view.setAlpha(1f);
						pendingDismiss.view.setTranslationX(0);
						lp = pendingDismiss.view.getLayoutParams();
						lp.height = originalHeight;
						pendingDismiss.view.setLayoutParams(lp);
					}

					mPendingDismisses.clear();
				}
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

		mPendingDismisses.add(new PendingDismissData(dismissPosition,
				dismissView));
		animator.start();
	}
}