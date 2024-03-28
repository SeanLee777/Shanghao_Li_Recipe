package algonquin.cst2335.li000543;

import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RecipeTest3 {
    @Test
    public void testRecipeDeletion() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        RecipeDatabase db = Room.inMemoryDatabaseBuilder(appContext, RecipeDatabase.class).allowMainThreadQueries().build();
        RecipeDAO dao = db.recipeDAO();

        RecipeObject recipeToDelete = new RecipeObject(2, "Delete Me", "Summary", "http://source.url", "http://image.url");
        dao.insertRecipe(recipeToDelete);
        dao.deleteRecipe(recipeToDelete);

        RecipeObject result = dao.getRecipe(2);
        assertNull(result);

        db.close();
    }

}
