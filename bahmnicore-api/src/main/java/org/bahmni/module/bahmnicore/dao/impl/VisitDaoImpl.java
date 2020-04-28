package org.bahmni.module.bahmnicore.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Repository
public class VisitDaoImpl implements VisitDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Visit getLatestVisit(String patientUuid, String conceptName) {
        String queryString = "select v\n" +
                "from Obs obs join obs.encounter enc join enc.visit v, ConceptName cn \n" +
                "where cn.concept.conceptId = obs.concept.conceptId and cn.name=:conceptName and cn.conceptNameType='FULLY_SPECIFIED' and obs.person.uuid=:patientUuid\n" +
                "order by v.startDatetime desc";
        Query queryToGetVisitId = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetVisitId.setString("conceptName", conceptName);
        queryToGetVisitId.setString("patientUuid", patientUuid);
        queryToGetVisitId.setMaxResults(1);

        return (Visit) queryToGetVisitId.uniqueResult();
    }

    @Override
    public Visit getVisitSummary(String visitUuid) {
        String queryString = "select v from Visit v where v.uuid=:visitUuid and v.voided=false";
        Query queryToGetVisitInfo = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetVisitInfo.setString("visitUuid", visitUuid);
        return (Visit) queryToGetVisitInfo.uniqueResult();
    }


    @Override
    public List<Encounter> getAdmitAndDischargeEncounters(Integer visitId) {
        String queryString = "select e from Encounter e where e.visit.id = :visitId and e.voided=false and e.encounterType.name in ('ADMISSION', 'DISCHARGE')";
        Query queryToGetVisitInfo = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetVisitInfo.setInteger("visitId", visitId);
        return (List<Encounter>) queryToGetVisitInfo.list();
    }

    @Override
    public List<Visit> getVisitsByPatient(Patient patient, int numberOfVisits) {
        if (patient == null || numberOfVisits <= 0) {
            return new ArrayList<>();
        }

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(Order.desc("startDatetime"));
        criteria.addOrder(Order.desc("visitId"));
        criteria.setMaxResults(numberOfVisits);
        List<Visit> visits = criteria.list();

        return visits;
    }

    public List<Integer> getVisitIdsFor(String patientUuid, Integer numberOfVisits) {
        Query queryToGetVisitIds = sessionFactory.getCurrentSession().createQuery(
                "select v.visitId " +
                        " from Visit as v " +
                        " where v.patient.uuid = :patientUuid " +
                        " and v.voided = false " +
                        "order by v.startDatetime desc");
        queryToGetVisitIds.setString("patientUuid", patientUuid);
        if (numberOfVisits != null) {
            queryToGetVisitIds.setMaxResults(numberOfVisits);
        }
        return queryToGetVisitIds.list();
    }

    @Override
    public List<Integer> getEncounterIds(String patientUuid, Integer numberOfEncounters, Integer numberOfVisits) {

        String encountersAndVisitsQuery = "select e.encounter_id " +
                "from encounter e join (select visit_id from visit " +
                "where patient_id in (select person_id from person where uuid=:patientUuid) and voided=0 " +
                "order by date_started desc %s) as v on v.visit_id=e.visit_id " +
                "where e.voided=0 order by e.encounter_datetime desc %s;";

        encountersAndVisitsQuery = format(encountersAndVisitsQuery, limitItems(numberOfVisits),
                limitItems(numberOfEncounters));

        SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(encountersAndVisitsQuery);
        sqlQuery.setString("patientUuid", patientUuid);
        return sqlQuery.list();
    }

    private String limitItems(Integer numberOfItems) {
        if (numberOfItems != null && numberOfItems > 0) {
            return format("limit %s", numberOfItems);
        }
        return StringUtils.EMPTY;
    }
}
