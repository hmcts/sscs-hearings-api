package uk.gov.hmcts.reform.sscs.helper;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified.PartiesNotifiedBuilder;

import java.util.Optional;

import static java.util.Objects.nonNull;

public class HearingsMapping {

    private HearingsMapping() {
    }

    public static PartiesNotified buildUpdatePartiesNotifiedPayload(HearingWrapper wrapper){

        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        PartiesNotified.PartiesNotifiedBuilder partiesNotifiedBuilder = PartiesNotified.builder();
        partiesNotifiedBuilder.serviceData(getServiceData(caseData));
        partiesNotifiedBuilder.requestVersion(getRequestVersion(caseData));

   return partiesNotifiedBuilder.build();
    }


    private static String getRequestVersion(SscsCaseData caseData){
        if (nonNull(caseData.getSchedulingAndListingFields())
            && nonNull(caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber())
            && caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber() > 0) {
            return caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber().toString();
        }
        return null;
    }

    public static String getHearingId(HearingWrapper wrapper) {
        return Optional.of(wrapper)
            .map(HearingWrapper::getUpdatedCaseData)
            .map(SscsCaseData::getSchedulingAndListingFields)
            .map(SchedulingAndListingFields::getActiveHearingId)
            .map(Object::toString).orElse(null);
    }


    public static String getResponseVersionNumber(HearingWrapper wrapper) {
        return Optional.of(wrapper)
            .map(HearingWrapper::getUpdatedCaseData)
            .map(SscsCaseData::getSchedulingAndListingFields)
            .map(SchedulingAndListingFields::getActiveHearingVersionNumber)
            .map(Object::toString).orElse(null);
    }

    private static JsonNode getServiceData(SscsCaseData caseData){

        return Optional.of(caseData)
            .map(SscsCaseData::getServiveData())
            .map(SscsCaseData::getSchedulingAndListingFields)
            .map(SchedulingAndListingFields::getActiveHearingId)
            .map(Object::toString).orElse(null);;
    }
}
