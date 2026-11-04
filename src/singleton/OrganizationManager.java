package singleton;

public class OrganizationManager {

    private String organizationName;
    private String organizationEmail;
    private String organizationPhone;

    // The single shared instance
    private static OrganizationManager uniqueInstance;

    // Private constructor prevents creating objects from outside
    private OrganizationManager() {
        organizationName = "Hope Charity Organization";
        organizationEmail = "info@hopecharity.org";
        organizationPhone = "01000000000";
    }

    // Returns the same instance every time
    public static synchronized OrganizationManager getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new OrganizationManager();
        }

        return uniqueInstance;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationEmail() {
        return organizationEmail;
    }

    public String getOrganizationPhone() {
        return organizationPhone;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void displayInfo() {
        System.out.println("Organization Name: " + organizationName);
        System.out.println("Organization Email: " + organizationEmail);
        System.out.println("Organization Phone: " + organizationPhone);
    }
}
