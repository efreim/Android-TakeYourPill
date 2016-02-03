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
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class stores information about pills
 */
@DatabaseTable(tableName = "pill")
public class Pill {

    @DatabaseField(generatedId = true, columnName = "id")
    private Long mId;

    @DatabaseField(columnName = "name")
    private String mName;

    @DatabaseField
    private String mDescription;

    @DatabaseField
    private int mPillsCount;

    @DatabaseField
    private int mDosage;

    @DatabaseField
    private String mPhoto;

    @DatabaseField
    private String mActiveSubstance;

    @DatabaseField
    private long mBarcodeNumber;

    @DatabaseField
    private String mPrice;

    @DatabaseField
    private int mPillsRemaining;


    public Pill() {
    }

    public Pill(String name, String desc, int count, int dosage, String photo, String activeSubstance, String price, long barcodeNumber) {
        this.mName = name;
        this.mDescription = desc;
        this.mPillsCount = count;
        this.mDosage = dosage;
        this.mPhoto = photo;
        this.mActiveSubstance = activeSubstance;
        this.mPrice = price;
        this.mBarcodeNumber = barcodeNumber;
        this.mPillsRemaining = count;
    }

    public Pill(String name, String desc, int count, String photo, String activeSubstance, String price, long barcodeNumber) {
        this.mName = name;
        this.mDescription = desc;
        this.mPillsCount = count;
        this.mPhoto = "";
        this.mActiveSubstance = activeSubstance;
        this.mPrice = price;
        this.mBarcodeNumber = barcodeNumber;
        this.mPillsRemaining = count;
    }

    public Pill(String name, String desc, int dosage) {
        this.mName = name;
        this.mDescription = desc;
        this.mPhoto = "";
    }



    public int getDosage() {
        return mDosage;
    }

    public void setDosage(int mPillsTaken) {
        this.mDosage = mPillsTaken;
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

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String mPrice) {
        this.mPrice = mPrice;
    }

    public long getBarcodeNumber() {
        return mBarcodeNumber;
    }

    public void setBarcodeNumber(long mBarcodeNumber) {
        this.mBarcodeNumber = mBarcodeNumber;
    }

    public String getActiveSubstance() {
        return mActiveSubstance;
    }

    public void setActiveSubstance(String mActiveSubstance) {
        this.mActiveSubstance = mActiveSubstance;
    }

}
