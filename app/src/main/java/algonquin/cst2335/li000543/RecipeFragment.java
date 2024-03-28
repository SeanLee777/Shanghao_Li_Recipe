package algonquin.cst2335.li000543;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.io.FileOutputStream;
import java.io.IOException;

import algonquin.cst2335.li000543.databinding.RecipeDetailsBinding;

public class RecipeFragment extends Fragment {
    private final String API_KEY = "c8a0a4b53cf14c12b0022eaa246542f7";

    // The selected recipe to be displayed
    Recipe selectedRecipe;

    // Default constructor
    public RecipeFragment() {
    }

    // Override the Constructor that takes a Recipe object as parameter
    public RecipeFragment(Recipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        RecipeDetailsBinding binding = RecipeDetailsBinding.inflate(inflater); // Binding for the fragment's layout

        if (selectedRecipe != null) {
            RecipeDatabase db = Room.databaseBuilder(requireContext(), RecipeDatabase.class, "recipes").build();
            RecipeDAO rDAO = db.recipeDAO();
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            RecipeMain mainActivity = (RecipeMain) requireActivity();

            binding.saveButton.setOnClickListener(cli -> {
                String requestURL = "https://api.spoonacular.com/recipes/" + selectedRecipe.getId() + "/information?apiKey=" + API_KEY;

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null, response -> {
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
                                    try (FileOutputStream fOut = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)) {
                                        if (imageType.equals("jpg")) {
                                            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                                        } else {
                                            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                        }

                                        new Thread(() -> rDAO.insertRecipe(new RecipeObject(id, title, summary, sourceURL, fileName))).start();

                                        requireActivity().runOnUiThread(() -> {
                                            binding.saveButton.setVisibility(View.GONE);
                                            binding.deleteButton.setVisibility(View.VISIBLE);

                                            mainActivity.myAdapter.notifyDataSetChanged();
                                        });
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                1024,
                                1024,
                                ImageView.ScaleType.CENTER,
                                null,
                                error -> binding.imageView.setImageResource(R.drawable.recipe));
                        queue.add(imgReq);

                        binding.saveButton.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), getString(R.string.recipe_display_error), Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                });
                queue.add(request);
            });
        }

        return binding.getRoot();
    }
}


