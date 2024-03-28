package algonquin.cst2335.li000543;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import algonquin.cst2335.li000543.databinding.ActivityMainBinding;
import algonquin.cst2335.li000543.databinding.SearchViewBinding;
/**
 * Author: Shanghao Li 040903008
 * Section: 031
 * Date: March 21
 * Description: This is the main activity for a recipe application. It handles the creation and management of recipes.
 * */
public class RecipeMain extends AppCompatActivity {
    // The API key for accessing the recipe service.
    private final String API_KEY = "c8a0a4b53cf14c12b0022eaa246542f7";
    // The queue for managing network requests.
    protected RequestQueue queue;
    // The binding for accessing the layout's views.
    ActivityMainBinding binding;
    // The shared preferences for storing user settings.
    SharedPreferences sharedPreferences;
    // The model for managing the recipes.
    RecipeModel recipeModel;
    // The list of recipes.
    ArrayList<Recipe> recipes= new ArrayList<>();
    // The executor for running tasks in the background.
    Executor thread;
    // The data access object for interacting with the recipe database.
    RecipeDAO rDAO;
    // The adapter for managing the items in the RecyclerView.
    RecyclerView.Adapter<MyRowHolder> myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Set the content view to the root view of the layout.
        setContentView(binding.getRoot());

        recipeModel = new ViewModelProvider(this).get(RecipeModel.class);
        // Get the current list of recipes.
        recipes = recipeModel.recipes.getValue();

        if (recipes == null) {
            // If the list of recipes is null, initialize it.
            recipeModel.recipes.postValue(recipes = new ArrayList<>());
        }

        // Set the toolbar.
        setSupportActionBar(binding.Toolbar);

        // Build the recipe database.
        RecipeDatabase db = Room.databaseBuilder(getApplicationContext(), RecipeDatabase.class, "recipes").build();
        rDAO = db.recipeDAO();

        queue = Volley.newRequestQueue(this);
        thread = Executors.newSingleThreadExecutor();

        // Observe the selected recipe and add a new RecipeFragment when it changes.
        recipeModel.selectedRecipe.observe(this, (newRecipeValue) -> getSupportFragmentManager().beginTransaction().add(R.id.fragmentLocation, new RecipeFragment(newRecipeValue)).addToBackStack(null).commit());

        // Set the adapter for the RecyclerView.
        binding.RecyclerView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MyRowHolder(SearchViewBinding.inflate(getLayoutInflater(),parent,false).getRoot());
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                // Get the recipe at the current position.
                Recipe obj = recipes.get(position);

                thread.execute(() ->{
                    // Get the recipe from the database.
                    List<RecipeObject> fromDatabase=rDAO.getRecipeById(obj.getId());

                    runOnUiThread(() ->{
                        // Set the details of the recipe.
                        if(!fromDatabase.isEmpty()){
                            RecipeObject dbobj = fromDatabase.get(0);
                            holder.title.setText(dbobj.title);

                            Bitmap bitmap = BitmapFactory.decodeFile(getFilesDir()+File.separator+dbobj.image);
                            holder.imageView.setImageBitmap(bitmap);

                            holder.delete.setVisibility(View.VISIBLE);
                            holder.save.setVisibility(View.GONE);
                        }else {
                            holder.title.setText(obj.getTitle());

                            // Create an ImageRequest for the recipe's image.
                            ImageRequest imageRequest = new ImageRequest(obj.getIconURL(), bitmap -> runOnUiThread(() -> holder.imageView.setImageBitmap(bitmap)), 1024, 1024, ImageView.ScaleType.CENTER, null, error ->{});

                            // Add the ImageRequest to the queue.
                            queue.add(imageRequest);
                            // Show the save button.
                            holder.save.setVisibility(View.VISIBLE);
                            // Hide the delete button.
                            holder.delete.setVisibility(View.GONE);
                        }
                    });
                });
            }

            @Override
            public int getItemCount() {
                // Return the size of the recipe list.
                return recipes.size();
            }
        });
        // Set the layout manager for the RecyclerView.
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences=getPreferences(Activity.MODE_PRIVATE);
        String savedText = sharedPreferences.getString("Search_Permission","");
        binding.EditText.setText(savedText);

        // Set the click listener for the search button.
        binding.SearchButton.setOnClickListener(cl -> {
            String recipeName = binding.EditText.getText().toString();

            // Clear the recipe list.
            recipes.clear();
            // Notify the adapter that the data has changed.
            myAdapter.notifyDataSetChanged();

            saveData(recipeName, "Search_Permission");

            String requestURL;

            try {
                requestURL = "https://api.spoonacular.com/recipes/complexSearch?query=" + URLEncoder.encode(recipeName, "UTF-8") + "&number=100&apiKey=" + API_KEY;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null, response -> {
                try {
                    JSONArray recipeArray = response.getJSONArray("results");

                    for (int i = 0; i < recipeArray.length(); i++) {
                        JSONObject recipe = recipeArray.getJSONObject(i);

                        int id = recipe.getInt("id");
                        String title = recipe.getString("title");
                        String imageURL = recipe.getString("image");

                        recipes.add(new Recipe(id, title, imageURL));
                        myAdapter.notifyItemInserted(recipes.size() - 1);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }, error -> {
            });
            queue.add(request);
        });

        // Set the click listener for the save button.
        binding.ViewSavedButton.setOnClickListener(c -> {
            recipes.clear();

            thread.execute(() -> {
                recipes.addAll(rDAO.getAllRecipes().stream().map(Recipe::covertFromItemToRecipe).collect(Collectors.toList()));

                runOnUiThread(() -> myAdapter.notifyDataSetChanged());
            });
        });
    }

    /**
     * This method is called to inflate the options menu.
     * @param menu The options menu in which you place your items.
     * @return boolean Return true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        return true;
    }

    /**
     * This method is called whenever an item in your options menu is selected.
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.Help) {
            showAlertDialog();
            return true;
        } else if (itemId == R.id.About) {
            showSnackbar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is used to show a Snackbar with the number of saved recipes.
     */
    private void showSnackbar() {
        thread.execute(() -> {
            List<RecipeObject> savedRecipes = rDAO.getAllRecipes();

            runOnUiThread(() -> {
                View rootView = findViewById(android.R.id.content);
                Snackbar.make(rootView, getString(R.string.About) + savedRecipes.size(), Snackbar.LENGTH_SHORT).show();
            });
        });
    }
    /**
     * This method is used to show an AlertDialog with help information.
     */
    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.help))
                .setMessage(getString(R.string.Help))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * This method is used to save data to SharedPreferences.
     * @param textToSave The text to be saved.
     * @param key The key for the text to be saved.
     */
    private void saveData(String textToSave, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, textToSave);
        editor.apply();
    }

    /**
     * This class provides a holder for the views in each row of the RecyclerView.
     */
    class MyRowHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView title;
        Button save;
        Button delete;

        /**
         * Constructor for the MyRowHolder class.
         * @param itemView The view for each row in the RecyclerView.
         */
        public MyRowHolder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.imageView);
            title=itemView.findViewById(R.id.textView);
            save=itemView.findViewById(R.id.saveButton);
            delete=itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(clk ->{
                int position = getAbsoluteAdapterPosition();
                Recipe selected=recipes.get(position);

                if (getSupportFragmentManager().getBackStackEntryCount()==0){
                    recipeModel.selectedRecipe.postValue(selected);
                }
            });

            save.setOnClickListener(cl ->{
                int position = getAbsoluteAdapterPosition();
                Recipe selected = recipes.get(position);

                String requestURL= "https://api.spoonacular.com/recipes/" + selected.getId() + "/information?apiKey=" + API_KEY;

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,requestURL,null,response ->{
                    try {
                        int id = response.getInt("id");
                        String title = response.getString("title");
                        String summary = response.getString("summary");
                        String sourceURL = response.getString("sourceUrl");
                        String imageType = response.getString("imageType");
                        String fileName = title.toLowerCase().replaceAll("[^a-zA-Z0-9]+", "_") + "." + imageType;

                        ImageRequest imgReq = new ImageRequest(
                                response.getString("image"),
                                image -> {
                                    try (FileOutputStream fOut = openFileOutput(fileName, Context.MODE_PRIVATE)) {
                                        if (imageType.equals("jpg")) {
                                            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                                        } else {
                                            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                1024,
                                1024,
                                ImageView.ScaleType.CENTER,
                                null,
                                error -> {
                                });
                        queue.add(imgReq);

                        thread.execute(() -> rDAO.insertRecipe(new RecipeObject(id, title, summary, sourceURL, fileName)));

                        runOnUiThread(() -> {
                            save.setVisibility(View.GONE);
                            delete.setVisibility(View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        Toast.makeText(RecipeMain.this, getString(R.string.recipe_save_error), Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                });
                queue.add(request);
            });

            delete.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                Recipe selected = recipes.get(position);

                thread.execute(() -> {
                    RecipeObject deletedRecipe = rDAO.getRecipeById(selected.getId()).get(0);
                    File imageFile = new File(getFilesDir(), deletedRecipe.image);

                    rDAO.deleteRecipe(deletedRecipe);
                    imageFile.delete();

                    runOnUiThread(() -> {
                        delete.setVisibility(View.GONE);
                        save.setVisibility(View.VISIBLE);
                    });

                });
            });
        }
    }
}