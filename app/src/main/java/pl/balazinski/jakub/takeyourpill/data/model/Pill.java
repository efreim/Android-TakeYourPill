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

package pl.balazinski.jakub.takeyourpill.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

/**
 * Class stores information about pills
 */
@DatabaseTable(tableName = "pill")
public @Data class Pill {

    @DatabaseField(generatedId = true, columnName = "id")
    private Long id;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField
    private String description;

    @DatabaseField
    private int pillsCount;

    @DatabaseField
    private int dosage;

    @DatabaseField
    private String photo;

    @DatabaseField
    private String activeSubstance;

    @DatabaseField
    private long barcodeNumber;

    @DatabaseField
    private String price;

    @DatabaseField
    private int pillsRemaining;


    public Pill() {
    }

    public Pill(String name, String desc, int count, int dosage, String photo, String activeSubstance, String price, long barcodeNumber) {
        this.name = name;
        this.description = desc;
        this.pillsCount = count;
        this.dosage = dosage;
        this.photo = photo;
        this.activeSubstance = activeSubstance;
        this.price = price;
        this.barcodeNumber = barcodeNumber;
        this.pillsRemaining = count;
        //if (photo.equals(""))
        //this.photo = getResources().getIdentifier(name,"drawable", getPackageName());
    }
}
