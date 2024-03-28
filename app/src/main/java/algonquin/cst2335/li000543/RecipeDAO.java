package algonquin.cst2335.li000543;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Author: Shanghao Li 040903008
 * Section: 031
 * Date: March 21
 * Description: This interface serves as the Data Access Object (DAO) for managing RecipeObjects within
 * the application's database. It provides the necessary methods to insert, query, and delete RecipeObjects,
 * facilitating interaction with the app's underlying SQLite database through Room's abstraction layer.
 */
@Dao
public interface RecipeDAO {

    /**
     * Inserts a RecipeObject into the database. If the RecipeObject already exists, it replaces the old one.
     * This operation is useful for adding new recipes or updating existing ones.
     * @param recipeObject The RecipeObject to be inserted.
     */
    @Insert
    void insertRecipe(RecipeObject recipeObject);

    /**
     * Retrieves all RecipeObjects from the database. This method is useful for displaying a list of recipes
     * to the user.
     * @return A list of all RecipeObjects stored in the database.
     */
    @Query("SELECT * FROM RecipeObject")
    List<RecipeObject> getAllRecipes();

    /**
     * Retrieves a single RecipeObject from the database by its id. This method is useful for fetching
     * detailed information about a specific recipe.
     * @param id The unique identifier of the RecipeObject to retrieve.
     * @return The RecipeObject with the specified id.
     */
    @Query("SELECT * FROM RecipeObject WHERE id = :id")
    RecipeObject getRecipe(int id);

    /**
     * Overloaded version of getRecipe to demonstrate how different parameters can be used to achieve
     * varying functionality. This version returns a list of RecipeObjects that match the given id,
     * demonstrating flexibility in query results based on method signature.
     * @param id The unique identifier of the RecipeObject(s) to retrieve.
     * @return A list of RecipeObjects with the specified id.
     */
    @Query("SELECT * FROM RecipeObject WHERE id = :id")
    List<RecipeObject> getRecipeById(long id);

    /**
     * Deletes a RecipeObject from the database. This operation is useful for removing recipes
     * that are no longer needed or were added by mistake.
     * @param recipeObject The RecipeObject to be deleted.
     */
    @Delete
    void deleteRecipe(RecipeObject recipeObject);
}
