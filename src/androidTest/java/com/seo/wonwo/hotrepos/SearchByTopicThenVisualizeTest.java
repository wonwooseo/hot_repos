package com.seo.wonwo.hotrepos;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SearchByTopicThenVisualizeTest {

    @Rule
    public ActivityTestRule<Search> mActivityTestRule = new ActivityTestRule<>(Search.class);

    @Test
    public void searchByTopicThenVisualizeTest() {
        ViewInteraction appCompatSpinner = onView(
                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.search_options),
                        withParent(allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.container),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        appCompatSpinner.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(android.R.id.text1), withText("Topic"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.search_editText),
                        withParent(allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.container),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("tetris"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.search_editText), withText("tetris"),
                        withParent(allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.container),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        appCompatEditText2.perform(pressImeActionButton());

        ViewInteraction appCompatButton = onView(
                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.search_gobutton), withText("GO!"),
                        withParent(allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.container),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction relativeLayout = onView(
                allOf(childAtPosition(
                        allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.search_result_list),
                                withParent(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.container))),
                        0),
                        isDisplayed()));
        relativeLayout.perform(click());

        ViewInteraction viewGroup = onView(
                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.visualization_barchart),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
