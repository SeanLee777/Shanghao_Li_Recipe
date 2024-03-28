package algonquin.cst2335.li000543;

import androidx.room.ColumnInfo;
        import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
@Entity
public class RecipeObject {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "title")
    protected String title;
    @ColumnInfo(name = "summary")
    protected String summary;
    @ColumnInfo(name = "sourceURL")
    protected String sourceURL;
    @ColumnInfo(name = "image")
    protected String image;

    public RecipeObject() {
    }

    /**override the constructor for the detail information
     * @param id
     * @param title
     * @param summary
     * @param sourceURL
     * @param image
     * **/
    @Ignore
    public RecipeObject(int id, String title, String summary, String sourceURL, String image) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.sourceURL = sourceURL;
        this.image = image;
    }
}

