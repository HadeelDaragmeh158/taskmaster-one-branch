package com.example.taskmaster;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);


    @Test
    public void changeUsername() {
        // go to settings activity
//        onView(withId(R.id.settings)).perform(click());

        // Type text and then press the button.
//        onView(withId(R.id.usernameInput)).perform(typeText("Maria"),
//                closeSoftKeyboard());
//        onView(withId(R.id.save_btn)).perform(click());
    }

    @Test
    public void testingHeaderText (){

        // go to settings activity
//        onView(withId(R.id.settings)).perform(click());
        // Type text and then press the button.
//        onView(withId(R.id.usernameInput)).perform(typeText("Hadeel"),
//                closeSoftKeyboard());
//        onView(withId(R.id.save_team_btn)).perform(click());
//        onView(isRoot()).perform(ViewActions.pressBack());

//        onView(withId(android.R.id.home)).perform(click());
//        ViewActions.pressBack();

        onView(withId(R.id.usernameHeader)).check(matches(withText("Maria Tasks")));
    }

    @Test
    public void addTask() {
        // go to addTask activity
        onView(withId(R.id.addTaskButton)).perform(click());

        // Type text and then press the button.
        onView(withId(R.id.title)).perform(typeText("testing task"),
                closeSoftKeyboard());
        onView(withId(R.id.body)).perform(typeText("testing body"),
                closeSoftKeyboard());

        onView(withId(R.id.button)).perform(click());
    }

    @Test
    public void TeamTesting (){
        onView(withId(R.id.settings)).perform(click());
        onView(withId(R.id.save_team_btn)).perform(click());
        onView(isRoot()).perform(ViewActions.pressBack());
        onView(withId(R.id.team_name)).check(matches(withText("Design Tasks")));
    }



}
