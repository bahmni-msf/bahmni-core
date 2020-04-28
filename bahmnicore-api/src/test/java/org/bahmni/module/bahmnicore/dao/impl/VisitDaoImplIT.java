package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VisitDaoImplIT extends BaseIntegrationTest {
    
    @Autowired
    VisitDao visitDao;

    @Autowired
    PatientDao patientDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("visitTestData.xml");
    }

    @Test
    public void shouldGetLatestObsForConceptSetByVisit() {
        Visit latestVisit = visitDao.getLatestVisit("86526ed5-3c11-11de-a0ba-001e378eb67a", "Weight");
        assertEquals(901, latestVisit.getVisitId().intValue());
    }

    @Test
    public void shouldGetVisitsByPatient(){
        Patient patient = patientDao.getPatient("GAN200000");
        List<Visit> visits = visitDao.getVisitsByPatient(patient, 1);
        assertEquals(1, visits.size());
        assertEquals(901, visits.get(0).getVisitId().intValue());
    }

    @Test
    public void shouldNotGetVoidedEncounter() throws Exception {
        List<Encounter> admitAndDischargeEncounters = visitDao.getAdmitAndDischargeEncounters(902);
        assertEquals(1, admitAndDischargeEncounters.size());
    }

    @Test
    public void shouldReturnAllNonVoidedEncountersWhenNumberOfVisitsAndEncountersAreNotProvided() {
        final List<Integer> encounterIds = visitDao.getEncounterIds("86526ed5-3c11-11de-a0ba-001e378eb67a", null, null);

        assertEquals(4, encounterIds.size());
        assertTrue(encounterIds.containsAll(asList(38, 39, 40, 42)));
        assertEquals(39, encounterIds.get(0).intValue());
        assertEquals(38, encounterIds.get(1).intValue());
        assertEquals(42, encounterIds.get(2).intValue());
        assertEquals(40, encounterIds.get(3).intValue());
    }

    @Test
    public void shouldReturnGivenNumberOfLatestNonVoidedEncountersWhenNumberOfVisitsIsNotGiven() {
        List<Integer> encounterIds = visitDao.getEncounterIds("86526ed5-3c11-11de-a0ba-001e378eb67a", 3, null);

        assertEquals(3, encounterIds.size());
        assertEquals(39, encounterIds.get(0).intValue());
        assertEquals(38, encounterIds.get(1).intValue());
        assertEquals(42, encounterIds.get(2).intValue());
    }

    @Test
    public void shouldReturnAllNonVoidedEncountersForGivenVisits() {
        List<Integer> encounterIds = visitDao.getEncounterIds("86526ed5-3c11-11de-a0ba-001e378eb67a", null, 1);

        assertEquals(2, encounterIds.size());
        assertEquals(39, encounterIds.get(0).intValue());
        assertEquals(38, encounterIds.get(1).intValue());
    }

    @Test
    public void shouldReturnGivenNumberOfLatestEncountersWithInGivenNumberOfVisits() {
        List<Integer> encounterIds = visitDao.getEncounterIds("86526ed5-3c11-11de-a0ba-001e378eb67a", 3, 1);

        assertEquals(2, encounterIds.size());
        assertEquals(39, encounterIds.get(0).intValue());
        assertEquals(38, encounterIds.get(1).intValue());
    }

    @Test
    public void shouldNotReturnAnyEncounterIdsWhenInvalidPatientUUIDIsGiven() {
        assertEquals(0, visitDao.getEncounterIds("invalid-uuid", 3, 1).size());
    }
}