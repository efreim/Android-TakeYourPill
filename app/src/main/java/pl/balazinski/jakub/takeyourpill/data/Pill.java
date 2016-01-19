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

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class stores information about pills
 */
@DatabaseTable(tableName = "pill")
public class Pill {

    @DatabaseField(columnName = "id")
    private int mId = 0;

    @DatabaseField(columnName = "name")
    private String mName;

    @DatabaseField
    private String mDescription;

    @DatabaseField
    private int mPillsCount;

    @DatabaseField
    private int mPillsTaken;

    @DatabaseField
    private String mPhoto;

    private int mPillsRemaining;


    public Pill() {
    }

    public Pill(int id, String name, String desc, int count, int taken, String photo) {
        this.mId = id;
        this.mName = name;
        this.mDescription = desc;
        this.mPillsCount = count;
        this.mPillsTaken = taken;
        this.mPhoto = photo;
    }


    public int getPillsTaken() {
        return mPillsTaken;
    }

    public void setPillsTaken(int mPillsTaken) {
        this.mPillsTaken = mPillsTaken;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
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

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Uri photo) {
        this.mPhoto = photo.toString();
    }
}
