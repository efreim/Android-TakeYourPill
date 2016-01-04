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

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Random;

import pl.balazinski.jakub.takeyourpill.R;

/**
 * Class stores information about pills
 */
@DatabaseTable(tableName = "pill")
public class Pill {

    @DatabaseField(generatedId = true)
    private Long mId;

    @DatabaseField//(columnName = "name")
    private String mName;

    @DatabaseField
    private String mDescription;

    @DatabaseField
    private int mPillsCount;

    //pills taken at once
    @DatabaseField
    private int mPillsTaken;

    @DatabaseField
    private String mPhoto;

    private int mPillsRemaining;


    public Pill() {
    }

    public Pill(String name, String desc, int count, int taken, String photo) {
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

    public Long getId() {
        return mId;
    }

    public void setId(Long mId) {
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
