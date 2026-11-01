
package storage;

import model.*;

import java.io.Serializable;
import java.util.ArrayList;

public class AppData implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Donor> donors = new ArrayList<>();
    private ArrayList<Donation> donations = new ArrayList<>();
    private ArrayList<Volunteer> volunteers = new ArrayList<>();
    private ArrayList<Event> events = new ArrayList<>();
    private ArrayList<Beneficiary> beneficiaries = new ArrayList<>();
    private ArrayList<LookupItem> lookupItems = new ArrayList<>();
    private boolean lookupInitialized = false;
    private ArrayList<NotificationRecord> notifications =
            new ArrayList<>();

    public boolean isLookupInitialized() {
        return lookupInitialized;
    }

    public void setLookupInitialized(boolean lookupInitialized) {
        this.lookupInitialized = lookupInitialized;
    }
    public ArrayList<Donor> getDonors() {
        return donors;
    }

    public void setDonors(ArrayList<Donor> donors) {
        this.donors = new ArrayList<>(donors);
    }

    public ArrayList<Donation> getDonations() {
        return donations;
    }

    public void setDonations(ArrayList<Donation> donations) {
        this.donations = new ArrayList<>(donations);
    }

    public ArrayList<Volunteer> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(ArrayList<Volunteer> volunteers) {
        this.volunteers = new ArrayList<>(volunteers);
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = new ArrayList<>(events);
    }

    public ArrayList<Beneficiary> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(ArrayList<Beneficiary> beneficiaries) {
        this.beneficiaries = new ArrayList<>(beneficiaries);
    }

    public ArrayList<LookupItem> getLookupItems() {
        return lookupItems;
    }

    public void setLookupItems(ArrayList<LookupItem> lookupItems) {
        this.lookupItems = new ArrayList<>(lookupItems);
    }

    public ArrayList<NotificationRecord> getNotifications() {

        // Handles save files created before notifications were added
        if (notifications == null) {
            notifications = new ArrayList<>();
        }

        return notifications;
    }

    public void setNotifications(
            ArrayList<NotificationRecord> notifications) {

        this.notifications = new ArrayList<>(notifications);
    }

}
