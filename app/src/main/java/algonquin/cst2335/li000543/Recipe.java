package algonquin.cst2335.li000543;

/**
 * Author: Shanghao Li 040903008
 * Section: 031
 * Date: March 21
 * Description: This class represents a Recipe, used to store and manage information about a single recipe.
 * It includes the recipe's ID, title, and a URL to its icon. This class serves as a fundamental data model
 * within the application for handling recipe information.
 */

public class Recipe {
    // The unique identifier for the recipe. Used to differentiate between recipes.
    private int id;

    // The title of the recipe. Provides a brief description of the recipe to the user.
    private String title;

    // The URL for the icon associated with the recipe. Used to visually represent the recipe in the UI.
    private String iconURL;

    /**
     * Constructs a new Recipe instance with specified id, title, and iconURL.
     * @param id The unique identifier for the recipe.
     * @param title The title of the recipe.
     * @param iconURL The URL of the icon associated with the recipe.
     */
    public Recipe(int id, String title, String iconURL){
        this.id = id;
        this.title = title;
        this.iconURL = iconURL;
    }

    /**
     * Returns the unique identifier of the recipe.
     * @return The recipe's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the title of the recipe.
     * @return The recipe's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the URL of the icon associated with the recipe.
     * @return The URL of the recipe's icon.
     */
    public String getIconURL() {
        return iconURL;
    }

    /**
     * Converts a RecipeObject instance into a Recipe instance. This method is useful for integrating with
     * other parts of the application that use RecipeObject instances, allowing for a seamless conversion
     * between different object models.
     *
     * @param recipeObject The RecipeObject to be converted into a Recipe.
     * @return A new Recipe instance with the id, title, and icon URL from the RecipeObject.
     */
    public static Recipe covertFromItemToRecipe(RecipeObject recipeObject) {
        return new Recipe(recipeObject.id, recipeObject.title, recipeObject.image);
    }
}
