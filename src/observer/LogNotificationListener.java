package observer;

public class LogNotificationListener implements EventListener {

    private String logName;

    public LogNotificationListener(String logName) {
        this.logName = logName;
    }

    @Override
    public void update(String eventType, String message) {
        System.out.println("LOG [" + logName + "] - "
                + eventType + ": " + message);
    }
}
