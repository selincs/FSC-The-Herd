package Model;

import androidx.annotation.Nullable;

//Will be refined more once we have the GUI created and I can visualize things
public class Guide {
    private String guideTitle;
    private String description;
    private String contents;
    private String id;
    private String title;
    private String category;
    private boolean isVerified;
    private boolean isUserSuggested;


    public Guide() {

    }

    public Guide (String id, String title) {
        this.id = id;
        this.title = title;
    }

    public Guide (String id, String title, String description, Boolean isVerified, Boolean isUserSuggested, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isVerified = isVerified;
        this.isUserSuggested = isUserSuggested;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public String getContents() {
        return contents;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isUserSuggested() {
        return isUserSuggested;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Guide guide = (Guide) obj;
        return java.util.Objects.equals(id, guide.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}
