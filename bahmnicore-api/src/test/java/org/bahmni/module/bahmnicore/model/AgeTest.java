package org.bahmni.module.bahmnicore.model;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class AgeTest {

    @Test
    public void shouldConvertHashToAgeWhenOneOfThePropertiesDoNotExist() {
        assertEquals(new Age(2010, 0, 0), Age.fromHash(new SimpleObject().add("years", 2010)));
        assertEquals(new Age(0, 12, 0), Age.fromHash(new SimpleObject().add("months", 12)));
        assertEquals(new Age(0, 0, 31), Age.fromHash(new SimpleObject().add("days", 31)));
        assertEquals(new Age(0, 0, 0), Age.fromHash(new SimpleObject()));
    }

    @Test
    public void shouldCalculateAgeFromDateOfBirth() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM yyyy dd");

        String birthDate = "June 1990 15";
        String today = "Dec 2013 05";

        DateTime formattedBirthDate = formatter.parseDateTime(birthDate);
        DateTime formattedTodayDate = formatter.parseDateTime(today);

        Age age = Age.fromDateOfBirth(formattedBirthDate.toDate(), formattedTodayDate.toDate());

        assertEquals(new Age(23, 5, 20), age);
    }

    @Test
    public void shouldCalculateDateOfBirthFromAge() {
        Age age = new Age(20, 5, 21);
        Date today = new LocalDate(2013, 6, 20).toDate();

        Date dateOfBirth = age.getDateOfBirth(today);

        assertEquals(new LocalDate(1992, 12, 30).toDate(), dateOfBirth);
    }
}
