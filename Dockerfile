ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
FROM hmctspublic.azurecr.io/base/java:11-distroless

COPY build/libs/sscs-hearings-api.jar /opt/app/

EXPOSE 8083
CMD [ "sscs-hearings-api.jar" ]
