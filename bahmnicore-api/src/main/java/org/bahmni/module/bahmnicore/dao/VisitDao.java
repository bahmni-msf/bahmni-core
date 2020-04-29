package org.bahmni.module.bahmnicore.dao;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.List;

public interface VisitDao {
    public Visit getLatestVisit(String patientUuid, String conceptName);

    Visit getVisitSummary(String visitUuid);

    List<Encounter> getAdmitAndDischargeEncounters(Integer visitId);

    List<Visit> getVisitsByPatient(Patient patient, int numberOfVisits);

    List<Integer> getVisitIdsFor(String patientUuid, Integer numberOfVisits);

    /**
     *
     * @param patientUuid
     * @param numberOfEncounters
     * @param numberOfVisits
     * @return Latest encounter ids limited(max) by numberOfEncounters where encounters do not go beyond latest numberOfVisits.
     * It also works when either numberOfEncounters or numberOfVisits are provided along with mandatory patientUuid
     */
    List<Integer> getEncounterIds(String patientUuid, Integer numberOfEncounters, Integer numberOfVisits);

}
