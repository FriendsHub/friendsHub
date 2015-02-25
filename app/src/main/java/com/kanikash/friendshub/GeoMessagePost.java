package com.kanikash.friendshub;


import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Posts")
public class GeoMessagePost extends ParseObject {



    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String value) {
        put("title", value);
    }

    public String getSnippet() {
        return getString("snippet");
    }

    public void setSnippet(String value) {
        put("snippet", value);
    }



    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<GeoMessagePost> getQuery() {
        return ParseQuery.getQuery(GeoMessagePost.class);
    }
}

