
package model;
import java.io.Serializable;

public class LookupItem implements Serializable{

    private int id;
    private String category;
    private String value;

    public LookupItem(int id, String category, String value) {
        this.id = id;
        this.category = category;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
