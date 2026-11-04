package model;
import java.util.ArrayList;
import java.io.Serializable;

public class Beneficiary implements Serializable{

    private int id;
    private String name;
    private String email;
    private String phone;
    private ArrayList<String> needs;
    private String assistanceStatus;

    public Beneficiary(int id, String name, String email,
                       String phone, ArrayList<String> needs) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.needs = new ArrayList<>(needs);
        this.assistanceStatus = "Pending";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public ArrayList<String> getNeeds() {
        return new ArrayList<>(needs);
    }

    public String getAssistanceStatus() {
        return assistanceStatus;
    }

    public void updateAssistanceStatus(String status) {
        this.assistanceStatus = status;
    }

    public void addNeed(String need) {
        if (!needs.contains(need)) {
            needs.add(need);
        }
    }

    public void displayInfo() {
        System.out.println("Beneficiary ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Needs: " + needs);
        System.out.println("Assistance Status: " + assistanceStatus);
    }
}
