package algonquin.cst2335.li000543;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class RecipeModel extends ViewModel {
    public MutableLiveData<ArrayList<Recipe>> recipes= new MutableLiveData<>();

    public MutableLiveData<Recipe> selectedRecipe = new MutableLiveData<>();
}
