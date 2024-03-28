package algonquin.cst2335.li000543;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class Recipe {
    private int id;
    private String title;
    private String iconURL;

    //constructor of RecipeObject
    public Recipe(int id, String title, String iconURL){
        this.id=id;
        this.title=title;
        this.iconURL=iconURL;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIconURL() {
        return iconURL;
    }

    // transfer the recipes to the Objects
    public static Recipe covertFromItemToRecipe(RecipeObject recipeObject) {
        return new Recipe(recipeObject.id, recipeObject.title, recipeObject.image);
    }
}