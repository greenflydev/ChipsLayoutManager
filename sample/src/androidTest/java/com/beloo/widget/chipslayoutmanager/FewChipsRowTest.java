package com.beloo.widget.chipslayoutmanager;

import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import androidx.appcompat.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.beloo.chipslayoutmanager.sample.R;
import com.beloo.chipslayoutmanager.sample.entity.ChipsEntity;
import com.beloo.chipslayoutmanager.sample.ui.FewChipsFacade;
import com.beloo.chipslayoutmanager.sample.ui.IItemsFacade;
import com.beloo.chipslayoutmanager.sample.ui.LayoutManagerFactory;
import com.beloo.chipslayoutmanager.sample.ui.TestActivity;
import com.beloo.widget.chipslayoutmanager.util.InstrumentalUtil;
import com.beloo.widget.chipslayoutmanager.util.testing.ISpy;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Locale;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.beloo.test.util.RecyclerViewEspressoFactory.actionDelegate;
import static com.beloo.test.util.RecyclerViewEspressoFactory.notifyItemRemovedAction;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test for {@link TestActivity}
 */
@RunWith(AndroidJUnit4.class)
public class FewChipsRowTest {

    static {
        TestActivity.isInitializeOutside = true;
    }

    @Rule
    public ActivityTestRule<TestActivity> activityTestRule = new ActivityTestRule<>(TestActivity.class);

    @Mock
    ISpy spy;

    @Mock
    LayoutManagerFactory layoutManagerFactory;

    private ChipsLayoutManager layoutManager;

    private List<ChipsEntity> items;

    private TestActivity activity;

    @Before
    public void setUp() throws Throwable {
        MockitoAnnotations.initMocks(this);
        activity = activityTestRule.getActivity();

        RecyclerView rvTest = (RecyclerView) activityTestRule.getActivity().findViewById(R.id.rvTest);
        //disable all animations
        rvTest.setItemAnimator(null);

        layoutManager = getLayoutManager();

        doReturn(layoutManager).when(layoutManagerFactory).layoutManager(any());

        IItemsFacade<ChipsEntity> chipsFacade = spy(new FewChipsFacade());
        items = chipsFacade.getItems();
        when(chipsFacade.getItems()).thenReturn(items);

        TestActivity.setItemsFactory(chipsFacade);
        TestActivity.setLmFactory(layoutManagerFactory);

    }

    protected ChipsLayoutManager getLayoutManager() {
        return ChipsLayoutManager.newBuilder(activityTestRule.getActivity())
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .build();
    }

    /**
     * test, that {@link androidx.appcompat.widget.LinearLayoutManager#onLayoutChildren} isn't called infinitely
     */
    @Test
    public void onLayoutChildren_afterActivityStarted_onLayoutCallLimited() throws Exception {
        //arrange
        activity.runOnUiThread(() -> activity.initialize());
        layoutManager.setSpy(spy);

        //act
        //we can't wait for idle, coz in case of error it won't be achieved. So just approximate time here.
        Thread.sleep(700);

        //assert
        verify(spy, atMost(6)).onLayoutChildren(any(RecyclerView.Recycler.class), any(RecyclerView.State.class));
    }

    @Test
    public void wrapContent_HeightIsWrapContent_DeletedLastItemInLastRowCauseHeightToDecrease() throws Exception {
        //arrange
        activity.runOnUiThread(() -> activity.initialize());
        final RecyclerView[] rvTest = new RecyclerView[1];

        ViewInteraction recyclerView = onView(withId(R.id.rvTest)).check(matches(isDisplayed()));
        ViewAction viewAction = actionDelegate((uiController, view) -> {
            rvTest[0] = view;
            view.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.requestLayout();
        });

        recyclerView.perform(viewAction);

        int startHeight = rvTest[0].getHeight();

        //act
        recyclerView.perform(
                actionDelegate(((uiController, view) -> items.remove(9))),
                notifyItemRemovedAction(9));

        //assert
        int endHeight = rvTest[0].getHeight();
        System.out.println(String.format(Locale.getDefault(), "start height = %d, end height = %d", startHeight, endHeight));
        assertTrue(endHeight < startHeight);
    }


    @Test
    public void deleteItemInTheFirstLine_ItemHasMaximumHeight_SameStartPadding() throws Exception {
        //arrange
        //just adapt input items list to required start values
        items.remove(1);
        activity.runOnUiThread(() -> activity.initialize());

        ViewInteraction recyclerView = onView(withId(R.id.rvTest)).check(matches(isDisplayed()));

        InstrumentalUtil.waitForIdle();

        View second = layoutManager.getChildAt(1);
        int startHeight = second.getHeight();
        double expectedY = second.getY();

        //act
        recyclerView.perform(
                actionDelegate(((uiController, view) -> items.remove(1))),
                notifyItemRemovedAction(1));

        InstrumentalUtil.waitForIdle();

        second = layoutManager.getChildAt(1);
        int endHeight = second.getHeight();
        double resultY = second.getY();

        //assert
        //check test behaviour
        assumeTrue(startHeight > endHeight);

        assertNotEquals(0, expectedY, 0.01);
        assertNotEquals(0, resultY, 0.01);
        assertEquals(resultY, expectedY, 0.01);
    }
}