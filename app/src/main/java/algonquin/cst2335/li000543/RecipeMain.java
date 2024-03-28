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

public class RecipeMain extends AppCompatActivity {
    private final String API_KEY = "c8a0a4b53cf14c12b0022eaa246542f7";
    protected RequestQueue queue;
    ActivityMainBinding binding;
    SharedPreferences sharedPreferences;
    RecipeModel recipeModel;
    ArrayList<Recipe> recipes= new ArrayList<>();
    Executor thread;
    RecipeDAO rDAO;
    RecyclerView.Adapter<MyRowHolder> myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recipeModel = new ViewModelProvider(this).get(RecipeModel.class);
        recipes = recipeModel.recipes.getValue();

        if (recipes == null) {
            recipeModel.recipes.postValue(recipes = new ArrayList<>());
        }

        setSupportActionBar(binding.Toolbar);

        RecipeDatabase db = Room.databaseBuilder(getApplicationContext(), RecipeDatabase.class, "recipes").build();
        rDAO = db.recipeDAO();

        queue = Volley.newRequestQueue(this);
        thread = Executors.newSingleThreadExecutor();

        recipeModel.selectedRecipe.observe(this, (newRecipeValue) -> getSupportFragmentManager().beginTransaction().add(R.id.fragmentLocation, new RecipeFragment(newRecipeValue)).addToBackStack(null).commit());

        binding.RecyclerView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MyRowHolder(SearchViewBinding.inflate(getLayoutInflater(),parent,false).getRoot());
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                Recipe obj = recipes.get(position);

                thread.execute(() ->{
                    List<RecipeObject> fromDatabase=rDAO.getRecipeById(obj.getId());

                    runOnUiThread(() ->{
                        if(!fromDatabase.isEmpty()){
                            RecipeObject dbobj = fromDatabase.get(0);
                            holder.title.setText(dbobj.title);

                            Bitmap bitmap = BitmapFactory.decodeFile(getFilesDir()+File.separator+dbobj.image);
                            holder.imageView.setImageBitmap(bitmap);

                            holder.delete.setVisibility(View.VISIBLE);
                            holder.save.setVisibility(View.GONE);
                        }else {
                            holder.title.setText(obj.getTitle());

                            ImageRequest imageRequest = new ImageRequest(obj.getIconURL(), bitmap -> runOnUiThread(() -> holder.imageView.setImageBitmap(bitmap)), 1024, 1024, ImageView.ScaleType.CENTER, null, error ->{});

                            queue.add(imageRequest);
                            holder.save.setVisibility(View.VISIBLE);
                            holder.delete.setVisibility(View.GONE);
                        }
                    });
                });
            }

            @Override
            public int getItemCount() {
                return recipes.size();
            }
        });
        binding.RecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences=getPreferences(Activity.MODE_PRIVATE);
        String savedText = sharedPreferences.getString("Search_Permission","");
        binding.EditText.setText(savedText);

        binding.SearchButton.setOnClickListener(cl -> {
            String recipeName = binding.EditText.getText().toString();

            recipes.clear();
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

        binding.ViewSavedButton.setOnClickListener(c -> {
            recipes.clear();

            thread.execute(() -> {
                recipes.addAll(rDAO.getAllRecipes().stream().map(Recipe::covertFromItemToRecipe).collect(Collectors.toList()));

                runOnUiThread(() -> myAdapter.notifyDataSetChanged());
            });
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.Help) {
            showSnackbar();
            return true;
        } else if (itemId == R.id.About) {
            showAlertDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSnackbar() {
        thread.execute(() -> {
            List<RecipeObject> savedRecipes = rDAO.getAllRecipes();

            runOnUiThread(() -> {
                View rootView = findViewById(android.R.id.content);
                Snackbar.make(rootView, getString(R.string.Help) + savedRecipes.size(), Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.about))
                .setMessage(getString(R.string.About))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveData(String textToSave, String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, textToSave);
        editor.apply();
    }

    class MyRowHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView title;
        Button save;
        Button delete;

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