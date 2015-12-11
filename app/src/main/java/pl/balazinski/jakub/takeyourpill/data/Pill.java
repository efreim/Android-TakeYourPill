/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.balazinski.jakub.takeyourpill.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Random;

import pl.balazinski.jakub.takeyourpill.R;

public class Pill implements Serializable {

    private static int mID = 0;
    private String mName;
    private String mDescription;
    private int mPillsCount;
    //pills taken at once
    private int mPillsTaken;
    private int mPillsRemaining;
    private boolean isActive = true;
    private static final Random RANDOM = new Random();

    public Pill(String name, String desc, int count, int taken) {
        this.mID++;
        this.mName = name;
        this.mDescription = desc;
        this.mPillsCount = count;
        this.mPillsTaken = taken;
    }

    public static int getRandomCheeseDrawable() {
        switch (RANDOM.nextInt(5)) {
            default:
            case 0:
                return R.drawable.cheese_1;
            case 1:
                return R.drawable.cheese_2;
            case 2:
                return R.drawable.cheese_3;
            case 3:
                return R.drawable.cheese_4;
            case 4:
                return R.drawable.cheese_5;
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getPillsTaken() {
        return mPillsTaken;
    }

    public void setPillsTaken(int mPillsTaken) {
        this.mPillsTaken = mPillsTaken;
    }

    public int getID() {
        return mID;
    }

    public void setID(int mID) {
        this.mID = mID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public int getPillsCount() {
        return mPillsCount;
    }

    public void setPillsCount(int mPillsCount) {
        this.mPillsCount = mPillsCount;
    }

    public int getPillsRemaining() {
        return mPillsRemaining;
    }

    public void setPillsRemaining(int mPillsRemaining) {
        this.mPillsRemaining = mPillsRemaining;
    }

}
