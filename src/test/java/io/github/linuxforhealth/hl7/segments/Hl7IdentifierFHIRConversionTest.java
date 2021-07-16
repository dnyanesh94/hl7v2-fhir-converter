/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.linuxforhealth.hl7.segments;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.github.linuxforhealth.hl7.segments.util.PatientUtils;

public class Hl7IdentifierFHIRConversionTest {

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void patient_identifiers_test() {

    String patientIdentifiers =
    "MSH|^~\\&|MIICEHRApplication|MIIC|MIIC|MIIC|201705130822||VXU^V04^VXU_V04|test1100|P|2.5.1|||AL|AL|||||Z22^CDCPHINVS|^^^^^MIIC^SR^^^MIIC|MIIC\n"
    // Three ID's for testing, plus a SSN in field 19.
    + "PID|1||MRN12345678^^^ID-XYZ^MR~111223333^^^USA^SS~MN1234567^^^MNDOT^DL|ALTID|Moose^Mickey^J^III^^^||20060504|M|||||||||||444556666|||||||||||\n"
    ;

    Patient patient = PatientUtils.createPatientFromHl7Segment(patientIdentifiers);
    assertThat(patient.hasIdentifier()).isTrue();

    List<Identifier> identifiers = patient.getIdentifier();
    assertThat(identifiers.size()).isEqualTo(4);

    // First identifier (Medical Record) deep check
    Identifier identifier = identifiers.get(0);
    assertThat(identifier.hasSystem()).isTrue();
    assertThat(identifier.getSystem()).hasToString("urn:id:ID-XYZ");
    assertThat(identifier.getValue()).hasToString("MRN12345678");
    assertThat(identifier.hasType()).isTrue(); 
    CodeableConcept cc = identifier.getType();
    assertThat(cc.getText()).hasToString("MR");
    assertThat(cc.hasCoding()).isTrue(); 
    Coding coding = cc.getCodingFirstRep();
    assertThat(coding.getSystem()).hasToString("http://terminology.hl7.org/CodeSystem/v2-0203"); 
    assertThat(coding.getCode()).hasToString("MR");
    assertThat(coding.getDisplay()).hasToString("Medical record number");

    // Second identifier (SSN) medium check
    identifier = identifiers.get(1);
    assertThat(identifier.hasSystem()).isTrue();
    assertThat(identifier.getSystem()).hasToString("urn:id:USA");
    assertThat(identifier.getValue()).hasToString("111223333");
    assertThat(identifier.hasType()).isTrue(); 
    cc = identifier.getType();
    assertThat(cc.getText()).hasToString("SS");
    assertThat(cc.hasCoding()).isTrue(); 

    // Third identifier (Driver's license) medium check
    identifier = identifiers.get(2);
    assertThat(identifier.hasSystem()).isTrue();
    assertThat(identifier.getSystem()).hasToString("urn:id:MNDOT");
    assertThat(identifier.getValue()).hasToString("MN1234567");
    assertThat(identifier.hasType()).isTrue();
    cc = identifier.getType();
    assertThat(cc.getText()).hasToString("DL");
    assertThat(cc.hasCoding()).isTrue();  

    // Deep check for fourth identifier, which is assembled from PID.19 SSN
    identifier = identifiers.get(3);
    assertThat(identifier.hasSystem()).isTrue();
    assertThat(identifier.getSystem()).hasToString("urn:id:USA");
    // Using different SS than ID#2 to confirm coming from PID.19
    assertThat(identifier.getValue()).hasToString("444556666"); 
    assertThat(identifier.hasType()).isTrue(); 
    cc = identifier.getType();
    assertThat(cc.getText()).hasToString("SS");
    assertThat(cc.hasCoding()).isTrue();
    coding = cc.getCodingFirstRep();
    assertThat(coding.getSystem()).hasToString("http://terminology.hl7.org/CodeSystem/v2-0203"); 
    assertThat(coding.getCode()).hasToString("SS");
    assertThat(coding.getDisplay()).hasToString("Social Security number");
    
  }
  
}