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

public class RecipeFragment extends Fragment {

    private final String API_KEY = "fd88a53bac094038b76412c4e400a3fe";

    Recipe selected;

    public RecipeFragment() {
    }

    public RecipeFragment(Recipe selected) {
        this.selected = selected;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        RecipeDetailsBinding binding = RecipeDetailsBinding.inflate(inflater);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        if (selected != null) {
            Executor thread = Executors.newSingleThreadExecutor();
            RecipeDatabase db = Room.databaseBuilder(requireContext(), RecipeDatabase.class, "recipes").build();
            RecipeDAO rDAO = db.recipeDAO();
            RequestQueue queue = Volley.newRequestQueue(requireContext());

            RecipeMain mainActivity = (RecipeMain) requireActivity();

            binding.saveButton.setOnClickListener(v -> {
                String requestURL = "https://api.spoonacular.com/recipes/" + selected.getId() + "/information?apiKey=" + API_KEY;

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

                                        thread.execute(() -> rDAO.insertRecipe(new RecipeObject(id, title, summary, sourceURL, fileName)));

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
                                        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
                                        image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                    thread.execute(() -> rDAO.insertRecipe(new RecipeObject(id, title, summary, sourceURL, "recipe_placeholder.png")));

                                    requireActivity().runOnUiThread(() -> {
                                        binding.saveButton.setVisibility(View.GONE);
                                        binding.deleteButton.setVisibility(View.VISIBLE);

                                        mainActivity.myAdapter.notifyDataSetChanged();
                                    });
                                });
                        queue.add(imgReq);
                    } catch (JSONException e) {
                        Log.w("JSON", e);
                    }
                }, error -> {
                });
                queue.add(request);
            });

            binding.deleteButton.setOnClickListener(v -> {
                // 创建并显示确认删除的 AlertDialog
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm_delete_title) // 设置标题
                        .setMessage(R.string.confirm_delete_message) // 设置消息内容
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            // 用户确认删除
                            thread.execute(() -> {
                                RecipeObject deletedRecipe = rDAO.getRecipeById(selected.getId()).get(0);
                                File imageFile = new File(requireContext().getFilesDir(), deletedRecipe.image);

                                rDAO.deleteRecipe(deletedRecipe);
                                imageFile.delete();

                                requireActivity().runOnUiThread(() -> {
                                    // 按钮在删除后仍然可见
                                    mainActivity.myAdapter.notifyDataSetChanged();
                                });
                            });
                        })
                        .setNegativeButton(R.string.cancel, null) // 用户取消删除
                        .show();
            });


            thread.execute(() -> {
                List<RecipeObject> isFromDatabase = rDAO.getRecipeById(selected.getId());

                if (!isFromDatabase.isEmpty()) {
                    RecipeObject selectedItem = rDAO.getRecipe(selected.getId());

                    requireActivity().runOnUiThread(() -> {
                        binding.title.setText(selectedItem.title);
                        binding.summary.loadDataWithBaseURL(null, selectedItem.summary, "text/html", "UTF-8", null);
                        binding.sourceURL.setText(selectedItem.sourceURL);

                        Bitmap bitmap = BitmapFactory.decodeFile(requireContext().getFilesDir() + File.separator + selectedItem.image);
                        binding.imageView.setImageBitmap(bitmap);

                        binding.deleteButton.setVisibility(View.VISIBLE);
                    });
                } else {
                    String requestURL = "https://api.spoonacular.com/recipes/" + selected.getId() + "/information?apiKey=" + API_KEY;

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null, response -> {
                        try {
                            String title = response.getString("title");
                            String summary = response.getString("summary");
                            String sourceURL = response.getString("sourceUrl");

                            requireActivity().runOnUiThread(() -> {
                                binding.title.setText(title);
                                binding.summary.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);
                                binding.sourceURL.setText(sourceURL);
                            });

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

                            binding.saveButton.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            Toast.makeText(requireContext(), getString(R.string.recipe_display_error), Toast.LENGTH_LONG).show();
                        }
                    }, error -> {
                    });
                    queue.add(request);
                }
            });
        }

        return binding.getRoot();
    }
}
