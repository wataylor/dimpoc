# Show how annotations can be used to validate POJO fields

These are NOT ideal examples of annotations; they are intended to show how simple it can be to validate all fields in a POJO by annotating them to tell the validator what data are expected in each field as in @USSSN for an American social security number or @CANZIP for a Canadian zip code.

Batch jobs need to validate input and web apps SHOULD validate data read from disk or uploaded CSV or Excel objects.  All the word's a POJO, so validation should focus on POJOs without requiring anything else.
 
The validator must be passed the Java field object so the validator can normalize when data come in and mask or format for the locale when data go out.  Masking depends on the laws of the jurisdiction where the data originated and could also depend on where the data are being displayed.
 
Specifying the Deep Inner Meaning of a field supports many different validation-related functions:

* Sanitize - make sure the field contains no attack data.  This should be done whenever data are read into an application regardless of application type or data source.
 
* Normalize - take spaces out of credit card numbers, parentheses stripped out of phone numbers, etc.  Be able to do the opposite by putting format characters back in when sending to a person.
 
* Looks valid - US phone number has 10 digits, US SSN has 10 digits.  Credit cards have checksums, etc.  Bank account number formats depend on the account type and institution.  A phone number validator might expect a different specification for each country, or it could assume that a field with a DIM of COUNTRY would be found in the object to specify the phone number format.
 
* IS valid - is the phone number or SSN associated with a real person or telephone?  Is the credit card backed by an issuing financial organization?  Is the bank account number associated with an open account?
 
* Is the field fiscally capable - does it have the resources required to complete a proposed transaction?
 
These levels of validation have different costs.  We want to do different levels depending on the specific use case.  If the data are human-supplied, must validate a lot.  If reading old data from disk, mere sanitization may be enough.  

Have to specify information when calling the validate (object, user roles, user locale, depth) method.  Just because all the fields are individually OK doesn't mean the object is OK, but the business rules are simpler if they can assume that all required fields are validated automatically at least through "Look valid."  

User roles are needed for presenting individual fields to a human.  A given role may either have read/write permission, read permission, partial read permission, or no permission at all.  This implies masking or suppressing the data entirely.  The validator must persuade the JSON generator or the XML generator to SKIP null fields on output; that will keep the data the user may not see away from the client, and the client Java Script won't put null fields in the display or editing form.
 
# The unit test program shows how the programs work.

In this POC, the data type is specified as a string in the DIM annotation itself.  This may be OK, but it might be better th specify a different DIM annotation for each data type.  That could lead to a great many annotations, but on the other hand, if the data type is specified as a string, it would be easy to make errors which would not be found until runtime.
