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
import org.hamcrest.core.IsInstanceOf;
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
public class SearchByNameThenVisualizeTest {

    @Rule
    public ActivityTestRule<Search> mActivityTestRule = new ActivityTestRule<>(Search.class);

    @Test
    public void searchByNameThenVisualizeTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.search_editText),
                        withParent(allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.container),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("guava"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.search_editText), withText("guava"),
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

        ViewInteraction textView = onView(
                allOf(withText("guava"),
                        childAtPosition(
                                allOf(ViewMatchers.withId(com.seo.wonwo.hotrepos.R.id.appbar_visualization),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.RelativeLayout.class),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("guava")));

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
