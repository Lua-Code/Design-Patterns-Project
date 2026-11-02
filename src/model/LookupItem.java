package model;
import java.io.Serializable;

public class LookupItem implements Serializable{
    private static final long serialVersionUID = -3715230956984801827L;

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

    public void DisplayInfo(){
        System.out.println("Id: "+id);
        System.out.println("Category: "+category);
        System.out.println("Value: "+value);
    }
}
