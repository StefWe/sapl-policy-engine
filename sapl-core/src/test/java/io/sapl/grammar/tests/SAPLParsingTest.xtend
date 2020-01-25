/**
 * Copyright © 2020 Dominic Heutelbeck (dominic@heutelbeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sapl.grammar.tests

import com.google.inject.Inject
import org.eclipse.xtext.diagnostics.Diagnostic

import org.junit.Assert
import org.junit.Test

import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.InjectWith
import org.junit.runner.RunWith
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import io.sapl.grammar.sapl.SAPL
import io.sapl.grammar.sapl.SaplPackage

@RunWith(XtextRunner)
@InjectWith(SAPLInjectorProvider)
class SAPLParsingTest {

	@Inject extension ParseHelper<SAPL> parseHelper
	@Inject extension ValidationTestHelper validator

	@Test
	def void policySet() {
		'''
			set "A policy set" deny-overrides
			policy "test policy"
			permit 
			where { "a" : ^subject.id, "b" : [ resource, action, environment.id ] };
		'''.parse.assertNoErrors
	}

	@Test
	def void targetExperiment() {
		'''
			policy "test policy"
			permit { "a" : subject.id, "b" : [ resource, action, environment.id ] } 
		'''.parse.assertNoErrors
	}

	@Test
	def void targetEmptyPermit() {
		'''
			policy "test policy"
			permit
		'''.parse.assertNoErrors
	}

	@Test
	def void targetEmptyDeny() {
		'''
			policy "test policy"
			deny
		'''.parse.assertNoErrors
	}

	@Test
	def void targetOneSimpleMatchA() {
		'''
			policy "test policy"
			permit subject == "some:subject"
		'''.parse.assertNoErrors
	}

	@Test
	def void targetCompareJSONObjects1() {
		'''
			policy "test policy"
			permit { "key" : "value" } == { "key": "value", "id" : 1234, "active" : false }
		'''.parse.assertNoErrors
	}

	@Test
	def void targetCompareJSONObjects2() {
		'''
			policy "test policy"
			permit subject.index[4].rules == { "key": "value", "id" : 1234, "active" : false }
		'''.parse.assertNoErrors
	}

	@Test
	def void targetOneSimpleMatchB() {
		'''
			policy "test policy"
			deny action =~ "some regex"
		'''.parse.assertNoErrors
	}

    // the null pointer exception is caused by a bug in the testing framework occurring when you have
	// the abstract root class and use expectError. 
	// See also: https://www.eclipse.org/forums/index.php/t/1071631/ expecting it is a workaround 
	@Test	
	def void emptyPolicy() {
		''' '''.parse.assertError(SaplPackage::eINSTANCE.SAPL, Diagnostic.SYNTAX_DIAGNOSTIC,
			"no viable alternative at input '<EOF>'");
	}

	@Test
	def void malformedHeader01() {
		'''
			policy "test policy"
			permit test
		'''.parse.assertNoErrors // .parse.assertError(SAPLPackage::eINSTANCE.policy, Diagnostic.SYNTAX_DIAGNOSTIC, "no viable alternative at input '<EOF>'")
	}

	@Test
	def void malformedHeader02() {
		'''
			policy "test policy"
			permit false
		'''.parse.assertNoErrors // .parse.assertError(SAPLPackage::eINSTANCE.policy, Diagnostic.SYNTAX_DIAGNOSTIC, "no viable alternative at input '<EOF>'")
	}

	@Test
	def void malformedHeader03() {
		'''
			policy "test policy"
			permit { "test" : 0.12 }
		'''.parse.assertNoErrors // .parse.assertError(SAPLPackage::eINSTANCE.policy, Diagnostic.SYNTAX_DIAGNOSTIC, "no viable alternative at input '<EOF>'")
	}

	@Test
	def void headerWithSubjectAttributeMatcher() {
		val policy = '''
			policy "test policy"
			permit subject.id =~ "^(?U)[\\p{Alpha}\\-'. [^=\\[\\]$()<>;]]*$"
		'''.parse
		Assert.assertNotNull(policy)
	}

	@Test
	def void headerWithComplexSubjectAttributeMatcher() {
		val policy = '''
			policy "test policy"
			permit subject.patterns[7].foo.bar == "something"
		'''.parse
		Assert.assertNotNull(policy)
	}

	@Test
	def void headerWithMatcherConjuctionA() {
		'''
			policy "test policy"
			permit subject == "aSubject" & target == "aTarget" 
		'''.parse.assertNoErrors
	}

	@Test
	def void headerWithMatcherConjuctionB() {
		'''
			policy "test policy"
			permit (subject == "aSubject" & target == "aTarget") 
		'''.parse.assertNoErrors
	}

	@Test
	def void headerWithMatcherConjuctionC() {
		'''
			policy "test policy"
			permit ((subject == "aSubject") & (target == "aTarget")) 
		'''.parse.assertNoErrors
	}

	@Test
	def void headerWithMatcherDisjuctionA() {
		'''
			policy "test policy"
			permit subject == "aSubject" | target == "aTarget" 
		'''.parse.assertNoErrors
	}

	@Test
	def void headerWithMatcherDisjuctionB() {
		'''
			policy "test policy"
			permit (subject == "aSubject" | target == "aTarget") 
		'''.parse.assertNoErrors
	}

	@Test
	def void headerWithMatcherDisjuctionC() {
		'''
			policy "test policy"
			permit ((subject == "aSubject") | (target == "aTarget")) 
		'''.parse.assertNoErrors
	}

	@Test
	def void headersWithNegationsA() {
		'''
			policy "test policy"
			permit !subject == "aSubject" | target == "aTarget" 
		'''.parse.assertNoErrors
	}

	@Test
	def void headersWithNegationsB() {
		'''
			policy "test policy"
			permit !(subject == "aSubject" | target == "aTarget") 
		'''.parse.assertNoErrors
	}

	@Test
	def void headersWithNegationsC() {
		'''
			policy "test policy"
			permit ((subject == { "id" : "x27", "name": "willi" }) | !target == "aTarget") 
		'''.parse.assertNoErrors
	}

	@Test
	def void headersComplexNestedExpression() {
		'''
			policy "test policy"
			permit (
			          (
			             (
			               !subject == "aSubject" | target == "aTarget"
			             )
			             &
			             !environment.data[2].errors =~ "regex"
			          )
			          |
			          false == true
			       ) 
			       &
			       (
			          action.volume == "some" | action.name == "bar"
			       ) 
		'''.parse.assertNoErrors
	}

	// the null pointer exception is caused by a bug in the testing framework occurring when you have
	// the abstract root class and use expectError. 
	// See also: https://www.eclipse.org/forums/index.php/t/1071631/ expecting it is a workaround 
	@Test
	def void rulesEmpty() {
		'''
			policy "test policy" permit
			deny subject.id =~ "http://*"
			where
		'''.parse.assertError(SaplPackage::eINSTANCE.SAPL, Diagnostic.SYNTAX_DIAGNOSTIC)
	}

	@Test
	def void rulesAssignment1() {
		'''
			policy "test policy"
			permit 
			where 
			  var something = { "key" : "value"}.key.<external.attribute> ;
		'''.parse.assertNoErrors
	}

	@Test
	def void rulesAssignment2() {
		'''
			policy "test policy"
			permit 
			where 
			  var something = { "key" : "value"}.key.<external.attribute>[7].other_key ;
		'''.parse.assertNoErrors
	}

	@Test
	def void rulesAssignment3() {
		'''
			policy "test policy"
			permit 
			where 
			  var something1 = { "key" : "value"}.key.<external.attribute>[7].other_key ;
			  var something2 = action.http.method;
			  var something3 = subject.id;
			  var something3 = ressource.path.elements[4].<extern.other>;
			  var something3 = !( environment.time.current == "2010-01-01T12:00:00+01:00" );
		'''.parse.assertNoErrors
	}

	@Test
	def void rulesAssignmentAndExpression() {
		'''
			policy "test policy"
			permit 
			where 
			  var subject_id = subject.metadata.id;
			  !("a" == "b");
			  action =~ "HTTP.GET";
		'''.parse.assertNoErrors
	}

	@Test
	def void rulesExpression() {
		'''
			policy "test policy"
			permit 
			where 
			  var subject_id = subject.metadata.id;
			  !("a" == "b");
			  action =~ "HTTP.GET";
		'''.parse.assertNoErrors
	}

	@Test
	def void rulesExpressionAndImport() {
		'''
			policy "test policy"
			permit 
			where 
			  var subject_id = subject.metadata.id;
			  !("a" == "b");
			  action =~ "HTTP.GET";
		'''.parse.assertNoErrors
	}

	@Test
	def void namedPolicy() {
		'''
			policy "test policy"
			permit 
			where 
			  var subject_id = subject.metadata.id;
			  !("a" == "b");
			  action =~ "HTTP.GET";
		'''.parse.assertNoErrors
	}

	@Test
	def void commentedPolicy() {
		'''
			policy "test policy"
			/*
			   this is a comment
			*/
			permit 
			where 
			  var subject_id = subject.metadata.id;
			  !("a" == "b");
			  action =~ "HTTP.GET";
		'''.parse.assertNoErrors
	}

	@Test
	def void namedAndCommentedPolicy() {
		'''
			/*
			   this is a comment
			*/
			policy "test policy"
			permit 
			where 
			  var subject_id = subject.metadata.id;
			  !("a" == "b");
			  action =~ "HTTP.GET";
		'''.parse.assertNoErrors
	}
	
	@Test
	def void simpleTransformPolicy() {
		'''
			policy "policy" 
				permit 
				transform
					true
		'''.parse.assertNoErrors
	}
	
	@Test
	def void ourPuppetDoctorTransformPolicy() {
		'''
			policy "doctors_hide_icd10" 
				permit 
					subject.role == "doctor" &
					action.verb == "show_patientdata"
				transform
					resource |- {
						@.address : remove,
						@.medicalhistory_icd10 : blacken(1,0,"")
					}
		'''.parse.assertNoErrors
	}
	
	@Test
	def void ourPuppetFamilymemberTransformPolicy() {
		'''
			policy "familymembers_truncate_contexthistory" 
				permit 
					subject.role == "familymember" &
					action.verb == "show_patient_contexthistory"
				transform
					{
						"patientid" : resource.patientid,
						"contexthistory" : resource.contexthistory[0:-1] :: {
							"datetime" : @.datetime,
							"captureid" : @.captureid,
							"status" : @.status
						}
					}
		'''.parse.assertNoErrors
	}
	
	@Test
	def void ourPuppetIntroducerTransformPolicy() {
		'''
			policy "puppetintroducers_truncate_contexthistory" 
				permit 
					subject.role == "puppetintroducer" &
					action.verb == "show_patient_contexthistory"
				transform
					{
						"patientid" : resource.patientid,
						"detected_situations" : resource.detected_situations[?(isOfToday(@.datetime))] :: {
							"datetime" : @.datetime,
							"captureid" : @.captureid,
							"status" : @.status
						}
					}
		'''.parse.assertNoErrors
	}

}
