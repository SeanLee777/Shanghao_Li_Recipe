package algonquin.cst2335.li000543;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RecipeTest1 {
    @Test
    public void testRecipeSave() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        RecipeDatabase db = Room.inMemoryDatabaseBuilder(appContext, RecipeDatabase.class).allowMainThreadQueries().build();
        RecipeDAO dao = db.recipeDAO();

        RecipeObject testRecipe = new RecipeObject(1, "Test Recipe", "This is a test summary", "http://source.url", "http://image.url");
        dao.insertRecipe(testRecipe);

        RecipeObject retrievedRecipe = dao.getRecipe(1);
        assertNotNull(retrievedRecipe);
        assertEquals(testRecipe.title, retrievedRecipe.title);

        db.close();
    }
}