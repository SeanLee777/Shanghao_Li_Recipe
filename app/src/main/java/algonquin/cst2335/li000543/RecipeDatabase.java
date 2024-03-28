package algonquin.cst2335.li000543;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RecipeObject.class}, version = 1)
public abstract class RecipeDatabase extends RoomDatabase {
    public abstract RecipeDAO recipeDAO();
}
