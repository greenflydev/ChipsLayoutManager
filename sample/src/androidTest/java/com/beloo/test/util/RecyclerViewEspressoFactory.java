package com.beloo.test.util;

import android.support.annotation.NonNull;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.BoundedMatcher;
import androidx.appcompat.widget.RecyclerView;
import android.view.View;

import com.beloo.widget.chipslayoutmanager.ChildViewsIterable;
import com.beloo.widget.chipslayoutmanager.support.BiConsumer;
import com.beloo.widget.chipslayoutmanager.util.ActionDelegate;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Locale;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

public abstract class RecyclerViewEspressoFactory {
    ///////////////////////////////////////////////////////////////////////////
    // Actions factory
    ///////////////////////////////////////////////////////////////////////////

    public static ViewAction scrollBy(int x, int y) {
        return new ScrollByRecyclerViewAction(x, y);
    }

    public static ViewAction smoothScrollToPosition(int position) {
        return new SmoothScrollToPositionRecyclerViewAction(position);
    }

    public static ViewAction setAdapter(RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter) {
        return new SetAdapterAction(adapter);
    }

    public static ViewAction notifyItemRemovedAction(int removePosition) {
        return new NotifyItemRemovedAction(removePosition);
    }

    public static ViewAction notifyItemRangeRemovedAction(int removePosition, int itemCount) {
        return new NotifyItemRemovedAction(removePosition, itemCount);
    }

    public static ViewAction actionDelegate(BiConsumer<UiController, RecyclerView> performAction) {
        return new ActionDelegate<>(performAction);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Matcher factory
    ///////////////////////////////////////////////////////////////////////////

    public static Matcher<View> incrementOrder() {
        return orderMatcher();
    }

    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ":\n");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                return viewHolder != null && itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

    public static <T extends RecyclerView.ViewHolder> Matcher<View> atPosition(final int position, @NonNull final ViewHolderMatcher<T> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ":\n");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                return viewHolder != null && itemMatcher.matches(viewHolder);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Actions
    ///////////////////////////////////////////////////////////////////////////

    private static final class NotifyItemRemovedAction extends RecyclerViewAction {

        private final int removePosition;
        private final int itemCount;

        private NotifyItemRemovedAction(int removePosition, int itemCount) {
            this.removePosition = removePosition;
            this.itemCount = itemCount;
        }

        private NotifyItemRemovedAction(int removePosition) {
            this.removePosition = removePosition;
            this.itemCount = 1;
        }

        @Override
        public void performAction(UiController uiController, RecyclerView recyclerView) {
            recyclerView.getAdapter().notifyItemRangeRemoved(removePosition, itemCount);
        }
    }

    private static final class SetAdapterAction extends RecyclerViewAction {
        private final RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter;

        private SetAdapterAction(RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter) {
            this.adapter = adapter;
        }

        @Override
        public String getDescription() {
            return"set adapter to recycler view";
        }

        @Override
        public void performAction(UiController uiController, RecyclerView recyclerView) {
            recyclerView.setAdapter(adapter);
        }
    }

    private static final class SmoothScrollToPositionRecyclerViewAction extends RecyclerViewAction {
        private final int position;

        private SmoothScrollToPositionRecyclerViewAction(int position) {
            this.position = position;
        }

        @Override
        public String getDescription() {
            return String.format(Locale.getDefault(), "smooth scroll RecyclerView to position %d", position);
        }

        @Override
        public void performAction(UiController uiController, RecyclerView recyclerView) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    SmoothScrollToPositionRecyclerViewAction.this.onScrollStateChanged(recyclerView, newState);
                }
            });
            recyclerView.smoothScrollToPosition(position);
        }

        private synchronized void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                notify();
            }
        }
    }

    private static final class ScrollByRecyclerViewAction extends RecyclerViewAction {
        private final int x;
        private final int y;

        private ScrollByRecyclerViewAction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String getDescription() {
            return String.format(Locale.getDefault(), "scroll RecyclerView with offsets: x = %d, y = %d ", x, y);
        }

        @Override
        public void performAction(UiController uiController, RecyclerView recyclerView) {
            recyclerView.scrollBy(x, y);
        }
    }

    private abstract static class RecyclerViewAction implements ViewAction {

        @Override
        public Matcher<View> getConstraints() {
            return allOf(isAssignableFrom(RecyclerView.class), isDisplayed());
        }

        @Override
        public String getDescription() {
            return "RecyclerView action " + this.getClass().getSimpleName();
        }

        @Override
        public final void perform(UiController uiController, View view) {
            performAction(uiController, (RecyclerView) view);
        }

        public void performAction(UiController uiController, RecyclerView recyclerView) {

        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Matcher
    ///////////////////////////////////////////////////////////////////////////

    private static Matcher<View> orderMatcher() {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with correct position order");
            }

            @Override
            public boolean matchesSafely(View v) {
                RecyclerView view = (RecyclerView) v;
                if (view.getLayoutManager() == null) return false;
                ChildViewsIterable childViews = new ChildViewsIterable(view.getLayoutManager());
                int pos = view.getChildAdapterPosition(childViews.iterator().next());
                for (View child : childViews) {
                    if (pos != view.getChildAdapterPosition(child)) {
                        return false;
                    }
                    pos ++;
                }
                return true;
            }
        };
    }

    public abstract static class ViewHolderMatcher<VH extends RecyclerView.ViewHolder> extends BaseMatcher<VH> {

        @Override
        public boolean matches(Object item) {
            VH viewHolder = (VH) item;
            RecyclerView recyclerView = (RecyclerView) viewHolder.itemView.getParent();
            return matches(recyclerView, viewHolder.itemView, viewHolder);
        }

        @Override
        public void describeTo(Description description) {

        }

        public abstract boolean matches(RecyclerView parent, View itemView, RecyclerView.ViewHolder viewHolder);
    }
}
