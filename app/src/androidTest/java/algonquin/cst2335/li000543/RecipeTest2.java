package algonquin.cst2335.li000543;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RecipeTest2 {
    @Test
    public void testRecipeSearch() {
        // Launch the RecipeMain activity
        ActivityScenario<RecipeMain> scenario = ActivityScenario.launch(RecipeMain.class);

        // Perform a search operation
        onView(withId(R.id.EditText)).perform(typeText("Chicken"), closeSoftKeyboard());
        onView(withId(R.id.SearchButton)).perform(click());

        // Check if RecyclerView is updated with search results
        onView(withId(R.id.RecyclerView)).check(matches(hasMinimumChildCount(1)));
    }
}
