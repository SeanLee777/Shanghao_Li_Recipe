package algonquin.cst2335.li000543;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecipeTest5 {
    @Test
    public void testRecipeGetters() {
        // Define test values for the Recipe instance.
        int expectedId = 101;
        String expectedTitle = "Test Recipe Title";
        String expectedIconURL = "http://example.com/icon.png";

        // Create a new Recipe instance with the test values.
        Recipe testRecipe = new Recipe(expectedId, expectedTitle, expectedIconURL);

        // Assert that the getter methods return the expected values.
        assertEquals(expectedId, testRecipe.getId());
        assertEquals(expectedTitle, testRecipe.getTitle());
        assertEquals(expectedIconURL, testRecipe.getIconURL());
    }

}
