package algonquin.cst2335.li000543;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface RecipeDAO {
    @Insert
    void insertRecipe(RecipeObject recipeObject);

    @Query("Select * from RecipeObject")
    List<RecipeObject> getAllRecipes();

    @Query("SELECT * FROM RecipeObject where id = :id")
    RecipeObject getRecipe(int id);

    @Query("SELECT * FROM RecipeObject where id = :id")
    List<RecipeObject> getRecipeById(long id);

    @Delete
    void deleteRecipe(RecipeObject recipeObject);
}
