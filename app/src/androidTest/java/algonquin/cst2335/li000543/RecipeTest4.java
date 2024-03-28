package algonquin.cst2335.li000543;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class RecipeTest4 {
    @Test
    public void testRecipeObjectInstantiation() {
        // Instantiate a RecipeObject with test values.
        int testId = 1;
        String testTitle = "Test Recipe";
        String testSummary = "This is a test summary";
        String testSourceURL = "http://source.url";
        String testImage = "http://image.url";

        RecipeObject testRecipeObject = new RecipeObject(testId, testTitle, testSummary, testSourceURL, testImage);

        // Assert that the fields are correctly assigned.
        assertEquals(testId, testRecipeObject.id);
        assertEquals(testTitle, testRecipeObject.title);
        assertEquals(testSummary, testRecipeObject.summary);
        assertEquals(testSourceURL, testRecipeObject.sourceURL);
        assertEquals(testImage, testRecipeObject.image);
    }
}
