package com.kanikash.friendshub.Models;

import com.kanikash.friendshub.GeoMessagePost;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by sjayanna on 2/21/15.
 * An extension of ParseObject that makes
 * it more convenient to access information
 * about a given Place
 */

@ParseClassName("Place")
public class Place extends ParseObject {

    public Place() {
        // A default constructor is required.
    }

    public String getCaption() {
        return getString("caption");
    }

    public void setCaption(String caption) {
        put("caption", caption);
    }

    /*public ParseUser getAuthor() {
        return getParseUser("author");
    }

    public void setAuthor(ParseUser user) {
        put("author", user);
    }*/

    public ParseFile getPhotoFile() {
        return getParseFile("photo");
    }

    public void setPhotoFile(ParseFile file) {
        put("photo", file);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<Place> getQuery() {
        return ParseQuery.getQuery(Place.class);
    }


}
