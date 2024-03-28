package algonquin.cst2335.li000543;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * Author: Shanghao Li 040903008
 * Section: 031
 * Date: March 21
 * Description: Represents the ViewModel for managing Recipe objects. It holds MutableLiveData objects for
 * a list of recipes and a selected recipe.
 */
public class RecipeModel extends ViewModel {
    // MutableLiveData object for holding a list of recipes.
    public MutableLiveData<ArrayList<Recipe>> recipes= new MutableLiveData<>();

    // MutableLiveData object for holding the selected recipe.
    public MutableLiveData<Recipe> selectedRecipe = new MutableLiveData<>();
}