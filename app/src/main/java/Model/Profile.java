package Model;


import java.util.List;
import java.util.ArrayList;

//Social Content class for the User, behaviors etc.
public class Profile {
    private User user;
    private List<Hobby> hobbies;
    private List<Interest> interests;
    private List<Integer> friends;
    //profile picture
    //bio
    //related community boards?

    public Profile(User user ) {

        this.user = user;

        hobbies = new ArrayList<Hobby>();

        interests = new ArrayList<Interest>();

        friends = new ArrayList<Integer>();


    }

    public void addHobbies(Hobby hobby){
        if ((hobby != null ) && !hobbies.contains(hobby)){
            hobbies.add(hobby);
        }
    }

    public void addInterests(Interest interest){
        if ((interest != null ) && !interests.contains(interest)){
            interests.add(interest);
        }
    }

    public void addFriends(int friend){
        if ( !friends.contains(friend)){
            friends.add(friend);
        }
    }

    public User getUser() {
        return user;
    }

    public List<Hobby> getHobbies() {
        return hobbies;
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public List<Integer> getFriends() {
        return friends;
    }
}
