package Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Club {

    private final int clubId;
    private String clubName;
    private String description;

    private final Set<Integer> memberIds = new HashSet<>();
    private final List<ClubEvent> events = new ArrayList<>();

    public Club(int clubId, String clubName, String description) {
        this.clubId = clubId;
        this.clubName = clubName;
        this.description = description;
    }

    public int getClubId() {
        return clubId;
    }

    public String getClubName() {
        return clubName;
    }

    public String getDescription() {
        return description;
    }

    // setters
    public void setClubName(String name) {
        this.clubName = name;
    }

    public void setDescription(String overview) {
        this.description = overview;
    }

    // member methods
    public int getMemberCount() {
        return memberIds.size();
    }

    public boolean join(int userId) {
        return memberIds.add(userId);
    }

    public boolean leave(int userId) {
        return memberIds.remove(userId);
    }

    public boolean isMember(int userId) {
        return memberIds.contains(userId);
    }

    // event methods
    public List<ClubEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public void addEvent(ClubEvent event) {
        events.add(event);
    }

    public boolean removeEventById(int eventId) {
        return events.removeIf(e -> e.getEventId() == eventId);
    }
}