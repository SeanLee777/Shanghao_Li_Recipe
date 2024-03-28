package algonquin.cst2335.li000543;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Author: Shanghao Li 040903008
 * Section: 031
 * Date: March 21
 * Description: Database class for the app, holds RecipeObjects.
 */
@Database(entities = {RecipeObject.class}, version = 1)
public abstract class RecipeDatabase extends RoomDatabase {
    // Provides DAO for Recipe operations.
    public abstract RecipeDAO recipeDAO();
}
