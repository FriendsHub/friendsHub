package com.kanikash.friendshub;

import android.app.Application;

import com.kanikash.friendshub.Models.Place;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by kanikash on 2/25/15.
 */
public class ParseBackendApp extends Application{
    static final String PARSE_APPLICATION_ID = "AjTb1C0eiBjw6DQYUTHQXAoGfRHuFrBMhiZdBZdn";
    static final String PARSE_CLIENT_KEY = "shoL9aMMS8luS8UkfaTvanpFJcXEcQniNKCHJwsB";
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Place.class);
        ParseObject.registerSubclass(GeoMessagePost.class);

		/*
		 * Parse credentials
		 */
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

		/*
		 * TODO: For now create anonymous user.
		 * Learn more about the ParseUser class:
		 * https://www.parse.com/docs/android_guide#users
		 */
        //ParseUser.enableAutomaticUser();

		/*
		 * For more information on app security and Parse ACL:
		 * https://www.parse.com/docs/android_guide#security-recommendations
		 */
        ParseACL defaultACL = new ParseACL();

		/*
		 * If you would like all objects to be private by default, remove this
		 * line
		 */
        defaultACL.setPublicReadAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);
    }
}
