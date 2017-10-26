package com.jorisgmail.avezard.aje;

import android.os.Environment;

/**
 * Created by Mallaurie on 25/10/2017.
 */
public class CheckForSDCard {
    //Check If SD Card is present or not method
    public boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}