package com.example.theherd

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PreferencesManager {
    private const val PREFS_NAME = "the_herd_prefs"
    private const val JOINED_CLUBS_KEY = "joined_clubs"
    private const val USERNAME_KEY = "user_name"
    private const val FIRST_NAME_KEY = "first_name"
    private const val LAST_NAME_KEY = "last_name"
    private const val COMMUNITIES_KEY = "saved_communities"

    private const val BIO_KEY = "bio"
    private const val INTERESTS_KEY = "interests"
    private const val GRAD_YEAR_KEY = "grad_year"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // USERNAME
    fun saveUsername(context: Context, username: String) {
        getPrefs(context).edit().putString(USERNAME_KEY, username).apply()
    }

    fun getUsername(context: Context): String {
        return getPrefs(context).getString(USERNAME_KEY, "") ?: ""
    }

    // GRAD YEAR
    fun saveGradYear(context: Context, year: String) {
        getPrefs(context).edit().putString(GRAD_YEAR_KEY, year).apply()
    }

    fun getGradYear(context: Context): String {
        return getPrefs(context).getString(GRAD_YEAR_KEY, "") ?: ""
    }

    // Save Bio
    fun saveBio(context: Context, bio: String) {
        getPrefs(context).edit()
            .putString(BIO_KEY, bio)
            .apply()
    }

    // Get Bio
    fun getBio(context: Context): String {
        return getPrefs(context).getString(BIO_KEY, "") ?: ""
    }

    // Save Interests
    fun saveInterests(context: Context, interests: List<String>) {
        getPrefs(context).edit()
            .putStringSet(INTERESTS_KEY, interests.toSet())
            .apply()
    }

    // Get Interests
    fun getInterests(context: Context): List<String> {
        return getPrefs(context)
            .getStringSet(INTERESTS_KEY, emptySet())
            ?.toList() ?: emptyList()
    }

    fun saveAllCommunities(context: Context, communities: List<Community>) {
        val json = gson.toJson(communities)
        getPrefs(context).edit().putString(COMMUNITIES_KEY, json).apply()
    }

    fun loadAllCommunities(context: Context): ArrayList<Community> {
        val json = getPrefs(context).getString(COMMUNITIES_KEY, null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<Community>>() {}.type
        return gson.fromJson(json, type)
    }

    fun savePosts(context: Context, communityName: String, posts: List<Post>) {
        val json = gson.toJson(posts)
        getPrefs(context).edit().putString("posts_$communityName", json).apply()
    }

    fun loadPosts(context: Context, communityName: String): ArrayList<Post> {
        val json = getPrefs(context).getString("posts_$communityName", null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<Post>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveJoinedClubs(context: Context, joinedNames: Set<String>) {
        getPrefs(context).edit().putStringSet(JOINED_CLUBS_KEY, joinedNames).apply()
    }

    fun getJoinedClubs(context: Context): Set<String> {
        return getPrefs(context).getStringSet(JOINED_CLUBS_KEY, emptySet()) ?: emptySet()
    }

    fun saveFullName(context: Context, first: String, last: String) {
        getPrefs(context).edit()
            .putString(FIRST_NAME_KEY, first)
            .putString(LAST_NAME_KEY, last)
            .apply()
    }

    fun getFullName(context: Context): String {
        val first = getPrefs(context).getString(FIRST_NAME_KEY, "New") ?: "New"
        val last = getPrefs(context).getString(LAST_NAME_KEY, "User") ?: "User"
        return "$first $last"
    }

    fun saveCommunities(context: Context, communities: List<String>) {
        val json = gson.toJson(communities)
        getPrefs(context).edit().putString("simple_communities_key", json).apply()
    }

    fun loadCommunities(context: Context): ArrayList<String> {
        val json = getPrefs(context).getString("simple_communities_key", null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return gson.fromJson(json, type)
    }
}