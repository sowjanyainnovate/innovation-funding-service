*** Settings ***
Documentation     INFUND-736: As an applicant I want to be able to add all the finance details for all the sections so I can sent in all the info necessary to apply
...
...               INFUND-438: As an applicant and I am filling in the finance details I want a fully working Other funding section
Suite Setup       Guest user log-in    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Force Tags        HappyPath
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/User_actions.robot

*** Variables ***
${OTHER_FUNDING_SOURCE}    Alice
${OTHER_FUNDING_AMOUNT}    10000
${OTHER_FUNDING_DATE}    12-2008

*** Test Cases ***
Labour
    [Documentation]    INFUND-192
    ...
    ...    Acceptance tests for the Labour section calculations
    ...
    ...    INFUND-736
    ...
    ...    INFUND-1256
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    When the Applicant fills in the Labour costs for two rows
    Then Totals should be correct    css=#section-total-9    £ 104,348    css=[data-mirror="#section-total-9"]    £ 104,348
    And the user clicks the button/link    name=remove_cost
    And the user reloads the page
    Then Totals should be correct    css=#section-total-9    £ 52,174    css=[data-mirror="#section-total-9"]    £ 52,174
    And the applicant edits the working days field
    Then Totals should be correct    css=#section-total-9    £ 48,000    css=[data-mirror="#section-total-9"]    £ 48,000

Administration support costs
    [Documentation]    INFUND-192
    ...
    ...    Acceptance tests for the Administration support costs section calculations
    ...
    ...    INFUND-736
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    When the user clicks the button/link    css=[aria-controls="collapsible-2"]
    And user selects the admin costs    overheads-rateType-29-51    DEFAULT_PERCENTAGE
    Then admin costs total should be correct    id=section-total-10-default    £ 9,600
    And user selects the admin costs    overheads-rateType-29-51    CUSTOM_RATE
    And the user enters text to a text field    id=cost-overheads-51-customRate    30
    Then admin costs total should be correct    id=section-total-10-custom    £ 14,400
    And user selects the admin costs    overheads-rateType-29-51    SPECIAL_AGREED_RATE
    And the user enters text to a text field    id=cost-overheads-51-agreedRate    40
    Then admin costs total should be correct    id=section-total-10-special    £ 19,200
    And the user reloads the page
    And the user clicks the button/link    css=[aria-controls="collapsible-2"]
    Then admin costs total should be correct    id=section-total-10-special    £ 19,200

Materials
    [Documentation]    INFUND-192
    ...
    ...    INFUND-736
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    When the Applicant fills the Materials fields
    Then Totals should be correct    css=#section-total-11    £ 2,000    css=[data-mirror="#section-total-11"]    £ 2,000
    And the user clicks the button/link    css=#material-costs-table tbody tr:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-11    £ 1,000    css=[data-mirror="#section-total-11"]    £ 1,000

Capital usage
    [Documentation]    INFUND-736
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    When the applicant fills the 'capital usage' field
    Then Totals should be correct    css=#section-total-12    £ 200    css=[data-mirror="#section-total-12"]    £ 200
    And the user clicks the button/link    css=#capital_usage [data-repeatable-row]:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-12    £ 100    css=[data-mirror="#section-total-12"]    £ 100

Subcontracting costs
    [Documentation]    INFUND-192
    ...    INFUND-736
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    And the applicant edits the Subcontracting costs section
    Then Totals should be correct    css=#section-total-13    £ 200    css=[aria-controls="collapsible-5"] [data-mirror]    £ 200
    And the user clicks the button/link    css=#subcontracting_costs [data-repeatable-row]:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-13    £ 100    css=[aria-controls="collapsible-5"] [data-mirror]    £ 100

Travel and subsistence
    [Documentation]    INFUND-736
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    And the Applicant fills the Travel fields
    Then Totals should be correct    css=#section-total-14    £ 2,000    css=[data-mirror="#section-total-14"]    £ 2,000
    And the user clicks the button/link    css=#travel-costs-table [data-repeatable-row]:nth-child(1) button
    And the user reloads the page
    Then Totals should be correct    css=#section-total-14    £ 1,000    css=[data-mirror="#section-total-14"]    £ 1,000

Other costs
    [Documentation]    INFUND-736
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    And the applicant adds one row for the other costs
    Then Totals should be correct    id=section-total-15    £ 200    css=[data-mirror="#section-total-15"]    £ 200
    Then the user reloads the page
    Then Totals should be correct    id=section-total-15    £ 200    css=[data-mirror="#section-total-15"]    £ 200

Other Funding
    [Documentation]    INFUND-438
    [Tags]    Finances
    Given the user navigates to the page    ${YOUR_FINANCES_URL}
    When Applicant selects 'Yes' and fills two rows
    Then the total of the other funding should be correct
    And Applicant can leave the 'Your finances' page but the details are still saved
    And applicant selects 'No' for other funding
    And applicant cannot see the 'other funding' details

*** Keywords ***
the Applicant fills in the Labour costs for two rows
    Click Element    css=[aria-controls="collapsible-1"]
    Click Element    jQuery=button:contains('Add another role')
    Wait Until Page Contains Element    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input
    Clear Element Text    css=#cost-labour-1-workingDays
    Input Text    css=#cost-labour-1-workingDays    230
    Input Text    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input    120000
    Input Text    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(4) input    100
    Input Text    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(1) input    test
    Click Element    jQuery=button:contains('Add another role')
    Wait Until Page Contains Element    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(4) input
    Input Text    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(2) input    120000
    Input Text    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(4) input    100
    Input Text    css=.labour-costs-table tr:nth-of-type(3) td:nth-of-type(1) input    test

the applicant edits the working days field
    Click Element    css=[aria-controls="collapsible-1"]
    Wait Until Element Is Visible    css=#cost-labour-1-workingDays
    Clear Element Text    css=#cost-labour-1-workingDays
    Input Text    css=#cost-labour-1-workingDays    250
    Focus    css=.app-submit-btn
    Sleep    200ms

the Applicant fills the Materials fields
    Click Element    xpath=//*[@aria-controls="collapsible-3"]
    Click Element    jQuery=button:contains('Add another materials cost')
    Wait Until Page Contains Element    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Input Text    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    10
    Input Text    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    100
    input text    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    test
    Click Element    jQuery=button:contains('Add another materials cost')
    Wait Until Page Contains Element    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    Input Text    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    10
    Input Text    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(3) input    100
    Input Text    css=#material-costs-table tbody tr:nth-of-type(2) td:nth-of-type(1) input    test

the applicant edits the Subcontracting costs section
    Click Element    css=[aria-controls="collapsible-5"]
    Click Element    jQuery=button:contains('Add another subcontractor')
    Wait Until Page Contains Element    css=#collapsible-5 .form-row:nth-child(1)
    Input Text    css=#collapsible-5 .form-row:nth-child(1) input[id$=subcontractingCost]    100
    input text    css=#collapsible-5 .form-row:nth-child(1) input[id$=companyName]    test
    Click Element    jQuery=button:contains('Add another subcontractor')
    Wait Until Page Contains Element    css=#collapsible-5 .form-row:nth-child(2)
    Input Text    css=#collapsible-5 .form-row:nth-child(2) input[id$=subcontractingCost]    100
    input text    css=#collapsible-5 .form-row:nth-child(2) input[id$=companyName]    test
    focus    css=.app-submit-btn

the applicant fills the 'capital usage' field
    Click Element    css=[aria-controls="collapsible-4"]
    Click Element    jQuery=button:contains('Add another asset')
    Wait Until Element Is Visible    css=#capital_usage [name="remove_cost"]
    Input Text    css=.form-row:nth-child(1) .form-finances-capital-usage-npv    1000
    input text    css=.form-row:nth-child(1) .form-finances-capital-usage-residual-value    900
    input text    css=.form-finances-capital-usage-utilisation    100
    input text    css=.form-finances-capital-usage-depreciation    11
    sleep    200ms
    focus    jQuery=button:contains('Add another asset')
    Click Element    jQuery=button:contains('Add another asset')
    Wait Until Element Is Visible    css=.form-row:nth-child(2) .form-finances-capital-usage-npv
    Input Text    css=.form-row:nth-child(2) .form-finances-capital-usage-npv    1000
    input text    css=.form-row:nth-child(2) .form-finances-capital-usage-residual-value    900
    input text    css=.form-row:nth-child(2) .form-finances-capital-usage-utilisation    100
    Input Text    css=.form-row:nth-child(2) .form-finances-capital-usage-depreciation    10
    focus    css=.app-submit-btn

the Applicant fills the Travel fields
    Click Element    css=[aria-controls="collapsible-6"]
    Click Element    jQuery=button:contains('Add another travel cost')
    Wait Until Page Contains Element    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    10
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    100
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    test
    #focus    css=.app-submit-btn
    #sleep    1s
    Click Element    jQuery=button:contains('Add another travel cost')
    Wait Until Page Contains Element    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    10
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(3) input    100
    Input Text    css=#travel-costs-table tbody tr:nth-of-type(2) td:nth-of-type(1) input    test
    #focus    css=.app-submit-btn

the applicant adds one row for the other costs
    Click Element    css=[aria-controls="collapsible-7"]
    click Element    jQuery=button:contains('Add another cost')
    Wait Until Element Is Visible    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Input Text    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    100
    click Element    jQuery=button:contains('Add another cost')
    Wait Until Element Is Visible    css=#other-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    Input Text    css=#other-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    100
    Mouse Out    css=#other-costs-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    focus    css=.app-submit-btn

the user reloads the page
    Reload page
    sleep    800ms

the total of the other funding should be correct
    Textfield Value Should Be    id=other-funding-total    £ 20,000

Applicant cannot see the 'other funding' details
    Page Should Not Contain    ${OTHER_FUNDING_SOURCE}
    Page Should Not Contain    ${OTHER_FUNDING_DATE}
    Page Should Not Contain    ${OTHER_FUNDING_AMOUNT}
    Radio Button Should Be Set To    other_funding-otherPublicFunding-35-54    No

Applicant can leave the 'Your finances' page but the details are still saved
    Reload Page
    #Alert Should Be Present
    Wait Until Element Is Visible    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input
    Textfield Should Contain    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${OTHER_FUNDING_SOURCE}
    Textfield Should Contain    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    ${OTHER_FUNDING_DATE}

Applicant selects 'No' for other funding
    Select Radio button    other_funding-otherPublicFunding-35-54    No

Applicant selects 'Yes' and fills two rows
    Select Radio button    other_funding-otherPublicFunding-35-54    Yes
    Click Element    jQuery=button:contains('Add another source of funding')
    Wait Until Element Is Visible    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Element Should Be Visible    id=other-funding-table
    Input Text    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    ${OTHER_FUNDING_DATE}
    Input Text    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    ${OTHER_FUNDING_AMOUNT}
    Input Text    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${OTHER_FUNDING_SOURCE}
    Click Element    jQuery=button:contains('Add another source of funding')
    Wait Until Element Is Visible    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(2) input
    Click Element    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(1) input
    Input Text    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(2) input    ${OTHER_FUNDING_DATE}
    Input Text    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(3) input    ${OTHER_FUNDING_AMOUNT}
    Wait Until Element Is Visible    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(1) input
    Input Text    css=#other-funding-table tbody tr:nth-of-type(2) td:nth-of-type(1) input    ${OTHER_FUNDING_SOURCE}
    focus    css=.app-submit-btn

Totals should be correct
    [Arguments]    ${TOTAL_FIELD}    ${FIELD_VALUE}    ${TOTAL_COLLAPSIBLE}    ${COLLAPSIBLE_VALUE}
    Textfield Value Should Be    ${TOTAL_FIELD}    ${FIELD_VALUE}
    Element Should Contain    ${TOTAL_COLLAPSIBLE}    ${COLLAPSIBLE_VALUE}

User selects the admin costs
    [Arguments]    ${RADIO_BUTTON}    ${SELECTION}
    Select Radio Button    ${RADIO_BUTTON}    ${SELECTION}
    focus    css=.app-submit-btn

Admin costs total should be correct
    [Arguments]    ${ADMIN_TOTAL}    ${ADMIN_VALUE}
    focus    css=.app-submit-btn
    Wait Until Element Is Visible    ${ADMIN_TOTAL}
    Textfield Value Should Be    ${ADMIN_TOTAL}    ${ADMIN_VALUE}
    Element Should Contain    css=[aria-controls="collapsible-2"] [data-mirror]    ${ADMIN_VALUE}
