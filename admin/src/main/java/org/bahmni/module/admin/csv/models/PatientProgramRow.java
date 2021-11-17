package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

public class PatientProgramRow extends CSVEntity {
    @CSVHeader(name = "Registration Number")
    public String patientIdentifier;

    @CSVRegexHeader(pattern = "Patient.*")
    public List<KeyValue> patientAttributes;

    @CSVHeader(name = "Program")
    public String programName;

    @CSVHeader(name = "EnrollmentDate")
    public String enrollmentDateTime;

    @CSVHeader(name = "ProgramOutcome", optional = true)
    public String programOutcome;

    @CSVHeader(name = "DateCompleted", optional = true)
    public String completedDateTime;

    public Date getEnrollmentDate() throws ParseException {
        return getDateFromString(enrollmentDateTime);
    }

    public Date getDateCompleted() throws ParseException {
        if (completedDateTime.isEmpty()) {
            return null;
        }
        return getDateFromString(completedDateTime);
    }
}
