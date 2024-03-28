package algonquin.cst2335.li000543;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.li000543.databinding.RecipeDetailsBinding;
/**
 * Author: Shanghao Li 040903008
 * Section: 031
 * Date: March 21
 * Description: This class represents a Fragment for displaying and managing Recipe objects. It holds a selected Recipe object and
 * provides functionalities such as saving a recipe, handling back press, and making API requests to fetch recipe details.
 * The class uses Room database for storing and retrieving recipes, and Volley library for making network requests.
 * It also uses Data Binding for reducing boilerplate code and improving UI performance.
 */
public class RecipeFragment extends Fragment {

    private final String API_KEY = "fd88a53bac094038b76412c4e400a3fe"; // Spoonacular API key for fetching recipe details.

    Recipe selected; // The selected Recipe object.

    public RecipeFragment() {
        // Default constructor.
    }

    public RecipeFragment(Recipe selected) {
        this.selected = selected; // Initialize the selected Recipe object.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        RecipeDetailsBinding binding = RecipeDetailsBinding.inflate(inflater); // Inflate the layout using Data Binding.

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack(); // Handle back press.
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback); // Add the callback to the back press dispatcher.

        if (selected != null) {
            Executor thread = Executors.newSingleThreadExecutor(); // Create a single-thread executor for database operations.
            RecipeDatabase db = Room.databaseBuilder(requireContext(), RecipeDatabase.class, "recipes").build(); // Build the Room database.
            RecipeDAO rDAO = db.recipeDAO(); // Get the DAO for performing database operations.
            RequestQueue queue = Volley.newRequestQueue(requireContext()); // Create a RequestQueue for making network requests.

            RecipeMain mainActivity = (RecipeMain) requireActivity(); // Get the main activity.

            binding.saveButton.setOnClickListener(v -> {
                String requestURL = "https://api.spoonacular.com/recipes/" + selected.getId() + "/information?apiKey=" + API_KEY; // The URL for the API request.

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null, response -> {
                    try {
                        int id = response.getInt("id"); // Get the recipe ID from the response.
                        String title = response.getString("title"); // Get the recipe title from the response.
                        String summary = response.getString("summary"); // Get the recipe summary from the response.
                        String sourceURL = response.getString("sourceUrl"); // Get the source URL from the response.
                        String imageType = response.getString("imageType"); // Get the image type from the response.
                        String fileName = title.toLowerCase().replaceAll("[^a-zA-Z0-9]+", "_") + "." + imageType; // Generate the file name for the recipe image.

                        ImageRequest imgReq = new ImageRequest(
                                response.getString("image"),
                                image -> {
                                    try (FileOutputStream fOut = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)) {
                                        // Choose the compression format based on the image type
                                        if (imageType.equals("jpg")) {
                                            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                                        } else {
                                            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                        }

                                        // Insert a new RecipeObject in a new thread
                                        thread.execute(() -> rDAO.insertRecipe(new RecipeObject(id, title, summary, sourceURL, fileName)));

                                        // Update the visibility of the buttons and notify the adapter that the data has changed on the UI thread
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
                                error -> {
                                    try (FileOutputStream fOut = requireContext().openFileOutput("recipe_placeholder.png", Context.MODE_PRIVATE)) {
                                        // Use a placeholder image in case of an error
                                        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
                                        image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                    // Insert a new RecipeObject in a new thread
                                    thread.execute(() -> rDAO.insertRecipe(new RecipeObject(id, title, summary, sourceURL, "recipe_placeholder.png")));

                                    // Update the visibility of the buttons and notify the adapter that the data has changed on the UI thread
                                    requireActivity().runOnUiThread(() -> {
                                        binding.saveButton.setVisibility(View.GONE);
                                        binding.deleteButton.setVisibility(View.VISIBLE);

                                        mainActivity.myAdapter.notifyDataSetChanged();
                                    });
                                });
                        queue.add(imgReq);// Add the ImageRequest to the request queue
                    } catch (JSONException e) {
                        Log.w("JSON", e);
                    }
                }, error -> {
                });
                queue.add(request);
            });

            binding.deleteButton.setOnClickListener(v -> {
                // Create and show an AlertDialog for confirming deletion
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm_delete_title) // 设置标题
                        .setMessage(R.string.confirm_delete_message) // 设置消息内容
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            // User confirms deletion
                            thread.execute(() -> {
                                RecipeObject deletedRecipe = rDAO.getRecipeById(selected.getId()).get(0);
                                File imageFile = new File(requireContext().getFilesDir(), deletedRecipe.image);

                                rDAO.deleteRecipe(deletedRecipe);
                                imageFile.delete();

                                requireActivity().runOnUiThread(() -> {
                                    mainActivity.myAdapter.notifyDataSetChanged();
                                });
                            });
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });


            thread.execute(() -> {
                // Check if the selected recipe is already in the database
                List<RecipeObject> isFromDatabase = rDAO.getRecipeById(selected.getId());

                if (!isFromDatabase.isEmpty()) {
                    // Get the selected recipe from the database
                    RecipeObject selectedItem = rDAO.getRecipe(selected.getId());

                    requireActivity().runOnUiThread(() -> {
                        // Update the UI with the details of the selected recipe
                        binding.title.setText(selectedItem.title);
                        binding.summary.loadDataWithBaseURL(null, selectedItem.summary, "text/html", "UTF-8", null);
                        binding.sourceURL.setText(selectedItem.sourceURL);

                        // Load the image of the selected recipe
                        Bitmap bitmap = BitmapFactory.decodeFile(requireContext().getFilesDir() + File.separator + selectedItem.image);
                        binding.imageView.setImageBitmap(bitmap);

                        // Show the delete button
                        binding.deleteButton.setVisibility(View.VISIBLE);
                    });
                } else {
                    String requestURL = "https://api.spoonacular.com/recipes/" + selected.getId() + "/information?apiKey=" + API_KEY;

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null, response -> {
                        try {
                            // Get the details of the recipe from the API response
                            String title = response.getString("title");
                            String summary = response.getString("summary");
                            String sourceURL = response.getString("sourceUrl");

                            // Update the UI with the details of the recipe
                            requireActivity().runOnUiThread(() -> {
                                binding.title.setText(title);
                                binding.summary.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);
                                binding.sourceURL.setText(sourceURL);
                            });

                            // Request for the image of the recipe
                            ImageRequest imgReq = new ImageRequest(
                                    response.getString("image"),
                                    image -> requireActivity().runOnUiThread(() -> {
                                        binding.imageView.setImageBitmap(image);
                                    }),
                                    1024,
                                    1024,
                                    ImageView.ScaleType.CENTER,
                                    null,
                                    error -> binding.imageView.setImageResource(R.drawable.placeholder_image));
                            queue.add(imgReq);

                            // Show the save button
                            binding.saveButton.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                        }
                    }, error -> {
                    });
                    queue.add(request);
                }
            });
        }
// Return the root view of the binding
        return binding.getRoot();
    }
}
