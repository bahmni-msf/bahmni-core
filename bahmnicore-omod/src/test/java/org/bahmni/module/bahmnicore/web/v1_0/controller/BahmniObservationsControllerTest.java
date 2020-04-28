package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls.BahmniObservationsController;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.ComplexDataMapper;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.builder.BahmniObservationBuilder;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.obs.ComplexData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class BahmniObservationsControllerTest {

    @Mock
    private BahmniObsService bahmniObsService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private VisitService visitService;
    @Mock
    private BahmniExtensions bahmniExtensions;
    @Mock
    private ComplexDataMapper complexDataMapper;

    private Visit visit;
    private Concept concept;
    private BahmniObservationsController bahmniObservationsController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        visit = new VisitBuilder().build();
        concept = new Concept();
        bahmniObservationsController = new BahmniObservationsController(bahmniObsService, conceptService, visitService, bahmniExtensions, complexDataMapper);
        when(visitService.getVisitByUuid("visitId")).thenReturn(visit);
        when(conceptService.getConceptByName("Weight")).thenReturn(concept);
    }

    @Test
    public void returnLatestObservationsWithComplexData() throws Exception {
        BahmniObservation latestObs = new BahmniObservation();
        latestObs.setUuid("initialId");
        ComplexData complexData = new ComplexData("title", "");
        latestObs.setComplexData(complexData);
        when(bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(concept), null, true)).thenReturn(Arrays.asList(latestObs));

        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", "latest", Arrays.asList("Weight"), null, true, null);

        verify(bahmniObsService, never()).getInitialObsByVisit(visit, Arrays.asList(concept), null, false);
        verify(complexDataMapper, never()).getNullifiedComplexData(complexData);
        assertEquals(1, bahmniObservations.size());
    }

    @Test
    public void returnInitialObservationWithComplexData() throws Exception {
        EncounterTransaction.Concept cpt = new EncounterTransaction.Concept();
        cpt.setShortName("Concept1");

        BahmniObservation initialObs = new BahmniObservation();
        initialObs.setUuid("initialId");
        initialObs.setConcept(cpt);

        when(bahmniObsService.getInitialObsByVisit(visit, Arrays.asList(this.concept), null, true)).thenReturn(Arrays.asList(initialObs));

        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", "initial", Arrays.asList("Weight"), null, true, true);

        verify(complexDataMapper, never()).getNullifiedComplexData(any(ComplexData.class));
        assertEquals(1, bahmniObservations.size());
    }

    @Test
    public void returnAllObservationsWithoutComplexData() throws Exception {
        BahmniObservation obs = new BahmniObservation();
        List<String> conceptNames = Arrays.asList("Weight");
        ArrayList<Concept> obsIgnoreList = new ArrayList<>();
        when(bahmniObsService.getObservationForVisit("visitId", conceptNames, obsIgnoreList, true, null)).thenReturn(Arrays.asList(obs));

        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", null, conceptNames, null, true, false);

        verify(bahmniObsService, never()).getLatestObsByVisit(visit, Arrays.asList(concept), null, false);
        verify(bahmniObsService, never()).getInitialObsByVisit(visit, Arrays.asList(concept), null, false);
        verify(bahmniObsService, times(1)).getObservationForVisit("visitId", conceptNames, obsIgnoreList, true, null);
        verify(complexDataMapper).getNullifiedComplexData(any(ComplexData.class));

        assertEquals(1, bahmniObservations.size());
    }

    @Test
    public void shouldMakeACallToGetObsForEncounterAndConceptsSpecified() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        String encounterUuid = "encounterUuid";
        String obsUuid = "ObsUuid";
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(new BahmniObservationBuilder().withUuid(obsUuid).build());
        when(bahmniObsService.getObservationsForEncounter(encounterUuid, conceptNames)).thenReturn(bahmniObservations);

        Collection<BahmniObservation> actualResult = bahmniObservationsController.get(encounterUuid, conceptNames, true);

        verify(bahmniObsService, times(1)).getObservationsForEncounter(encounterUuid, conceptNames);
        verify(complexDataMapper, never()).getNullifiedComplexData(any(ComplexData.class));
        assertEquals(1, actualResult.size());
        assertEquals(obsUuid, actualResult.iterator().next().getUuid());
    }

    @Test
    public void shouldGetObsForPatientProgramWhenPatientProgramUuidIsSpecified() throws Exception {
        BahmniObservation latestObs = new BahmniObservation();
        latestObs.setUuid("initialId");
        String patientProgramUuid = "patientProgramUuid";
        List<String> conceptNames = Arrays.asList("Weight");
        when(bahmniObsService.getObservationsForPatientProgram(patientProgramUuid, conceptNames, null)).thenReturn(Arrays.asList(latestObs));
        when(bahmniExtensions.getExtension("observationsAdder","CurrentMonthOfTreatment.groovy")).thenReturn(null);

        bahmniObservationsController.get(patientProgramUuid, conceptNames, null, null, false);

        verify(bahmniObsService, times(1)).getObservationsForPatientProgram(patientProgramUuid, conceptNames, null);
        verify(complexDataMapper).getNullifiedComplexData(any(ComplexData.class));
    }

    @Test
    public void shouldNotGetObsForPatientProgramWhenPatientProgramUuidIsSpecified() throws Exception {
        List<String> conceptNames = new ArrayList<String>();
        String patientProgramUuid = null;
        when(bahmniExtensions.getExtension("observationsAdder","CurrentMonthOfTreatment.groovy")).thenReturn(null);

        bahmniObservationsController.get(patientProgramUuid, null, null, null, true);

        verify(bahmniObsService, times(0)).getObservationsForPatientProgram(patientProgramUuid, conceptNames, null);
        verify(complexDataMapper, never()).getNullifiedComplexData(any(ComplexData.class));
    }

    @Test
    public void shouldGetLatestObsForPatientProgramWhenPatientProgramUuidAndScopeLatestIsSpecified() throws Exception {
        BahmniObservation latestObs = new BahmniObservation();
        latestObs.setUuid("initialId");
        List<String> conceptNames = new ArrayList<String>();
        List<String> ignoreObsList = new ArrayList<>();
        String patientProgramUuid = "patientProgramUuid";
        String scope = "latest";
        when(bahmniObsService.getLatestObservationsForPatientProgram(patientProgramUuid, conceptNames, ignoreObsList)).thenReturn(Arrays.asList(latestObs));

        bahmniObservationsController.get(patientProgramUuid, conceptNames, scope, ignoreObsList, false);

        verify(bahmniObsService, times(1)).getLatestObservationsForPatientProgram(patientProgramUuid, conceptNames, ignoreObsList);
        verify(complexDataMapper).getNullifiedComplexData(any(ComplexData.class));
    }

    @Test
    public void shouldGetInitialObsForPatientProgramWhenPatientProgramUuidAndScopeLatestIsSpecified() throws Exception {
        List<String> conceptNames = new ArrayList<String>();
        List<String> ignoreObsList = new ArrayList<String>();
        String patientProgramUuid = "patientProgramUuid";
        String scope = "initial";
        when(bahmniExtensions.getExtension("observationsAdder","CurrentMonthOfTreatment.groovy")).thenReturn(null);

        bahmniObservationsController.get(patientProgramUuid, conceptNames, scope, ignoreObsList, true);

        verify(bahmniObsService, times(1)).getInitialObservationsForPatientProgram(patientProgramUuid, conceptNames, ignoreObsList);
        verify(complexDataMapper, never()).getNullifiedComplexData(any(ComplexData.class));
    }

    @Test
    public void shouldGetBahmniObservationWithTheGivenObservationUuid() throws Exception {
        String observationUuid = "observationUuid";
        BahmniObservation expectedBahmniObservation = new BahmniObservation();
        when(bahmniObsService.getBahmniObservationByUuid(observationUuid)).thenReturn(expectedBahmniObservation);

        BahmniObservation actualBahmniObservation = bahmniObservationsController.get(observationUuid, "", false);

        verify(bahmniObsService, times(1)).getBahmniObservationByUuid("observationUuid");
        verify(complexDataMapper).getNullifiedComplexData(any(ComplexData.class));
        assertNotNull("BahmniObservation should not be null", actualBahmniObservation);
        assertEquals(expectedBahmniObservation, actualBahmniObservation);
    }

}