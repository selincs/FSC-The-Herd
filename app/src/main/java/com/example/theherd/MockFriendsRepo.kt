package com.example.theherd

object MockFriendsRepo {

    private val myFriends = mutableListOf(
        Friend("1", "Jane Doe", "Studying for CSCI 101 exam 🎉", true, isFriend = true),
        Friend("2", "John Smith", "At the RAM gym", false, isFriend = true),
        Friend("3", "Chloee-Gabrielle", "Working on The Herd", true, isFriend = true),
        Friend("4", "Alex Rivera", "In the library", false, isFriend = true)
    )

    private val blockedFriends = mutableListOf<Friend>()

    private val myRequests = mutableListOf(
        Friend("5", "Sam Wilson", "3 mutual friends", false, isFriend = false)
    )

    fun getMockFriends(): List<Friend> = myFriends

    fun getMockRequests(): List<Friend> = myRequests

    // Get the current list of blocked users
    fun getBlockedFriends(): List<Friend> = blockedFriends

    // Move a friend to the blocked list
    fun blockFriend(friend: Friend) {
        // Remove from friends list
        myFriends.removeAll { it.id == friend.id }
        // Add to blocked list if not already there
        if (!blockedFriends.any { it.id == friend.id }) {
            blockedFriends.add(friend)
        }
    }

    // Move a user back to the friends list
    fun unblockFriend(friend: Friend) {
        blockedFriends.removeAll { it.id == friend.id }
        if (!myFriends.any { it.id == friend.id }) {
            myFriends.add(friend)
        }
    }

    fun removeFriend(friend: Friend) {
        myFriends.removeAll { it.id == friend.id }
    }

    fun removeFriendByName(name: String) {
        val friendFound = myFriends.find { it.name == name }
        friendFound?.let {
            myFriends.remove(it)
        }
    }

    fun addFriendRequest(name: String) {
        val newId = (myRequests.size + 10).toString()
        myRequests.add(Friend(
            id = newId,
            name = name,
            statusText = "Pending Request",
            isOnline = false,
            isFriend = false
        ))
    }
}