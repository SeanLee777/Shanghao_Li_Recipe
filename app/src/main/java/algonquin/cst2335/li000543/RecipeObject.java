package algonquin.cst2335.li000543;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Author: Shanghao Li 040903008
 * Section: 031
 * Date: March 21
 * Description: Represents a recipe object in the database. This class is marked as an Entity for Room,
 * indicating it's a table within the app's database. It includes details such as recipe ID, title, summary,
 * source URL, and image URL.
 */
@Entity
public class RecipeObject {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id; // Unique identifier for the recipe.

    @ColumnInfo(name = "title")
    protected String title; // Title of the recipe.

    @ColumnInfo(name = "summary")
    protected String summary; // Brief summary of the recipe.

    @ColumnInfo(name = "sourceURL")
    protected String sourceURL; // URL to the original recipe source.

    @ColumnInfo(name = "image")
    protected String image; // URL to the recipe's image.

    // Default constructor.
    public RecipeObject() {
    }

    /**
     * Constructs a RecipeObject with detailed information.
     * @param id Unique identifier for the recipe.
     * @param title Title of the recipe.
     * @param summary Brief summary of the recipe.
     * @param sourceURL URL to the original recipe source.
     * @param image URL to the recipe's image.
     */
    @Ignore // Tells Room to ignore this constructor for the database schema.
    public RecipeObject(int id, String title, String summary, String sourceURL, String image) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.sourceURL = sourceURL;
        this.image = image;
    }
}
