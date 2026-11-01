
package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String method;
    private String recipient;
    private String message;
    private String status;
    private String dateTime;

    public NotificationRecord(
            String method,
            String recipient,
            String message,
            String status) {

        this.method = method;
        this.recipient = recipient;
        this.message = message;
        this.status = status;

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        this.dateTime = LocalDateTime.now().format(formatter);
    }

    public String getMethod() {
        return method;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getDateTime() {
        return dateTime;
    }
}
