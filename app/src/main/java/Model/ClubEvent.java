package Model;

import java.time.LocalDateTime;

public class ClubEvent {

    private final int eventId;
    private final int clubId;

    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    // clubevent

    public ClubEvent(int eventId, int clubId, String title,
                     String description, LocalDateTime dateTime,
                     String location) {

        this.eventId = eventId;
        this.clubId = clubId;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
    }

    public int getEventId() {
        return eventId;
    }

    public int getClubId() {
        return clubId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}