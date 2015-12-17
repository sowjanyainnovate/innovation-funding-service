*** Variables ***
${BROWSER}        Firefox
${SERVER_BASE}    localhost:8085
${PROTOCOL}       http://
${SERVER}         ${PROTOCOL}${SERVER_BASE}
${LOGIN_URL}      ${SERVER}/login
${DASHBOARD_URL}    ${SERVER}/applicant/dashboard
${SUMMARY_URL}    ${SERVER}/application/1/summary
${QUESTION11_URL}    ${SERVER}/application-form/1/section/1/#question-11
${APPLICATION_OVERVIEW_URL}    ${SERVER}/application/1
${APPLICATION_SUBMITTED_URL}    ${SERVER}/application/1/submit
${applicant_dashboard_url}    ${SERVER}/applicant/dashboard
${assessor_dashboard_url}    ${SERVER}/assessor/dashboard
${COMPETITION_DETAILS_URL}    ${SERVER}/competition/1/details/
${LOG_OUT}        ${SERVER}/logout
${APPLICATION_QUESTIONS_SECTION_URL}    ${SERVER}/application-form/1/section/2/
${SEARCH_COMPANYHOUSE_URL}    ${SERVER}/application/create/find-business
${APPLICATION_DETAILS_URL}    ${SERVER}/application/1/form/question/9
${PROJECT_SUMMARY_URL}    ${SERVER}/application/1/form/question/11
${PUBLIC_DESCRIPTION_URL}    ${SERVER}/application/1/form/question/12
${SCOPE_URL}      ${SERVER}/application/1/form/question/13
${BUSINESS_OPPORTUNITY_URL}    ${SERVER}/application/1/form/question/1
${POTENTIAL_MARKET_URL}    ${SERVER}/application/1/form/question/2
${PROJECT_EXPLOITATION_URL}    ${SERVER}/application/1/form/question/3
${ECONOMIC_BENEFIT_URL}    ${SERVER}/application/1/form/question/4
${TECHNICAL_APPROACH_URL}    ${SERVER}/application/1/form/question/5
${INNOVATION_URL}    ${SERVER}/application/1/form/question/6
${RISKS_URL}      ${SERVER}/application/1/form/question/7
${PROJECT_TEAM_URL}    ${SERVER}/application/1/form/question/8
${FUNDING_URL}    ${SERVER}/application/1/form/question/15
${ADDING_VALUE_URL}    ${SERVER}/application/1/form/question/16
${YOUR_FINANCES_URL}    ${SERVER}/application/1/form/section/7
${FINANCES_OVERVIEW_URL}    ${SERVER}/application/1/form/section/8
${ACCOUNT_CREATION_FORM_URL}    ${SERVER}/registration/register?organisationId=1
${CHECK_ELIGIBILITY}    ${SERVER}/application/create/check-eligibility/1
${YOUR_DETAILS}    ${SERVER}/application/create/your-details
