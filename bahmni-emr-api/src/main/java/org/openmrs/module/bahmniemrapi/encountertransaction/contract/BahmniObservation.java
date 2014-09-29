package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BahmniObservation{
    private ObsRelationship targetObsRelation;
    private EncounterTransaction.Observation encounterTransactionObservation;
    private List<BahmniObservation> groupMembers = new ArrayList<>();
    public Set<EncounterTransaction.Provider> providers;

    public BahmniObservation(EncounterTransaction.Observation encounterTransactionObservation) {
        for (EncounterTransaction.Observation groupMember : encounterTransactionObservation.getGroupMembers()) {
            addGroupMember(new BahmniObservation(groupMember));
        }
       this.encounterTransactionObservation = encounterTransactionObservation;
    }

    public BahmniObservation() {
        encounterTransactionObservation = new EncounterTransaction.Observation();
    }

    public EncounterTransaction.Concept getConcept() {
        return encounterTransactionObservation.getConcept();
    }

    public BahmniObservation setConcept(EncounterTransaction.Concept concept) {
        encounterTransactionObservation.setConcept(concept);
        return this;
    }

    public Object getValue() {
        return encounterTransactionObservation.getValue();
    }

    public BahmniObservation setValue(Object value) {
        encounterTransactionObservation.setValue(value);
        return this;
    }

    public String getComment() {
        return encounterTransactionObservation.getComment();
    }

    public BahmniObservation setComment(String comment) {
        encounterTransactionObservation.setComment(comment);
        return this;
    }

    public BahmniObservation setVoided(boolean voided) {
        encounterTransactionObservation.setVoided(voided);
        return this;
    }

    public boolean getVoided() {
        return encounterTransactionObservation.getVoided();
    }

    public String getVoidReason() {
        return encounterTransactionObservation.getVoidReason();
    }

    public BahmniObservation setVoidReason(String voidReason) {
        encounterTransactionObservation.setVoidReason(voidReason);
        return this;
    }

    public List<BahmniObservation> getGroupMembers() {
        return this.groupMembers;
    }

    public void setGroupMembers(List<BahmniObservation> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public void addGroupMember(BahmniObservation observation) {
        groupMembers.add(observation);
    }

    public String getOrderUuid() {
        return encounterTransactionObservation.getOrderUuid();
    }

    public BahmniObservation setOrderUuid(String orderUuid) {
        encounterTransactionObservation.setOrderUuid(orderUuid);
        return this;
    }

    public BahmniObservation setObservationDateTime(Date observationDateTime) {
        encounterTransactionObservation.setObservationDateTime(observationDateTime);
        return this;
    }

    public  String getUuid(){
        return encounterTransactionObservation.getUuid();
    }

    public BahmniObservation setUuid(String uuid){
        encounterTransactionObservation.setUuid(uuid);
        return this;
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getObservationDateTime() {
        return encounterTransactionObservation.getObservationDateTime();
    }

    public boolean isSameAs(EncounterTransaction.Observation encounterTransactionObservation){
        return this.getUuid().equals(encounterTransactionObservation.getUuid());
    }

    public ObsRelationship getTargetObsRelation() {
        return targetObsRelation;
    }

    public void setTargetObsRelation(ObsRelationship targetObsRelation) {
        this.targetObsRelation = targetObsRelation;
    }

    public EncounterTransaction.Observation toETObservation(){
       if (encounterTransactionObservation.getGroupMembers().size() == 0){
           for (BahmniObservation groupMember : this.groupMembers) {
               encounterTransactionObservation.addGroupMember(groupMember.toETObservation());
           }
       }
        return this.encounterTransactionObservation;
    }

    public String getConceptUuid() {
        return encounterTransactionObservation.getConceptUuid();
    }

    public boolean isSameAs(Obs obs) {
        return this.getUuid().equals(obs.getUuid());
    }

    public static List<BahmniObservation> toBahmniObsFromETObs(List<EncounterTransaction.Observation> allObservations) {
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        for (EncounterTransaction.Observation observation : allObservations) {
            bahmniObservations.add(new BahmniObservation(observation));
        }
        return bahmniObservations;
    }

    public static List<EncounterTransaction.Observation> toETObsFromBahmniObs(List<BahmniObservation> bahmniObservations) {
        List<EncounterTransaction.Observation> etObservations = new ArrayList<>();
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            etObservations.add(bahmniObservation.toETObservation());
        }
        return etObservations;
    }

    public boolean hasTargetObsRelation() {
        return targetObsRelation != null && targetObsRelation.getTargetObs() != null;
    }

    public Set<EncounterTransaction.Provider> getProviders() {
        return providers;
    }

    public void setProviders(Set<EncounterTransaction.Provider> providers) {
        this.providers = providers;
    }
}
