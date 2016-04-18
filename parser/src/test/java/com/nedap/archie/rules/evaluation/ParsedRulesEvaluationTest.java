package com.nedap.archie.rules.evaluation;

import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.rm.archetypes.Pathable;
import com.nedap.archie.rm.composition.Observation;
import com.nedap.archie.rm.datavalues.quantity.DvQuantity;
import com.nedap.archie.testutil.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by pieter.bos on 01/04/16.
 */
public class ParsedRulesEvaluationTest {

    ADLParser parser;
    Archetype archetype;

    TestUtil testUtil;

    @Before
    public void setup() {
        testUtil = new TestUtil();
        parser = ADLParser.withRMConstraintsImposer();
    }

    @Test
    public void simpleArithmetic() throws Exception {
        archetype = parser.parse(ParsedRulesEvaluationTest.class.getResourceAsStream("simplearithmetic.adls"));
        RuleEvaluation ruleEvaluation = new RuleEvaluation(archetype);
        Observation root = new Observation();
        ruleEvaluation.evaluate(root, archetype.getRules().getRules());
        VariableMap variables = ruleEvaluation.getVariableMap();
        assertEquals(19l, variables.get("arithmetic_test").getObject(0));
        assertTrue(variables.get("arithmetic_test").getPaths(0).isEmpty());
        assertEquals(false, variables.get("boolean_false_test").getObject(0));
        assertTrue(variables.get("boolean_false_test").getPaths(0).isEmpty());
        assertEquals(true, variables.get("boolean_true_test").getObject(0));
        assertTrue(variables.get("boolean_true_test").getPaths(0).isEmpty());
        assertEquals(true, variables.get("boolean_extended_test").getObject(0));
        assertTrue(variables.get("boolean_extended_test").getPaths(0).isEmpty());
        assertEquals(true, variables.get("not_false").getObject(0));
        assertTrue(variables.get("not_false").getPaths(0).isEmpty());
        assertEquals(false, variables.get("not_not_not_true").getObject(0));
        assertTrue(variables.get("not_not_not_true").getPaths(0).isEmpty());
        assertEquals(4l, variables.get("variable_reference").getObject(0));
        assertTrue(variables.get("variable_reference").getPaths(0).isEmpty());
    }


    @Test
    public void modelReferences() throws Exception {
        archetype = parser.parse(ParsedRulesEvaluationTest.class.getResourceAsStream("modelreferences.adls"));
        RuleEvaluation ruleEvaluation = new RuleEvaluation(archetype);

        Pathable root = (Pathable) testUtil.constructEmptyRMObject(archetype.getDefinition());

        DvQuantity quantity = (DvQuantity) root.itemAtPath("/data[id2]/events[id3]/data[id4]/items[id5]/value[id13]");
        quantity.setMagnitude(65d);

        ruleEvaluation.evaluate(root, archetype.getRules().getRules());

        assertEquals(65d, (Double) ruleEvaluation.getVariableMap().get("arithmetic_test").getObject(0), 0.001d);

        List<AssertionResult> assertionResults = ruleEvaluation.getAssertionResults();
        assertEquals("one assertion should have been checked", 1, assertionResults.size());
        AssertionResult result = assertionResults.get(0);

        assertEquals("the assertion should have succeeded", true, result.getResult());
        assertEquals("the assertion tag should be correct", "blood_pressure_valid", result.getTag());
        assertEquals(1, result.getRawResult().getPaths(0).size());
        assertEquals("/data[id2]/events[id3, 1]/data[id4]/items[id5, 1]/value/magnitude", result.getRawResult().getPaths(0).get(0));

    }

    @Test
    public void booleanConstraint() throws Exception {
        archetype = parser.parse(ParsedRulesEvaluationTest.class.getResourceAsStream("matches.adls"));

        RuleEvaluation ruleEvaluation = new RuleEvaluation(archetype);

        Pathable root = (Pathable) testUtil.constructEmptyRMObject(archetype.getDefinition());

        DvQuantity quantity = (DvQuantity) root.itemAtPath("/data[id2]/events[id3]/data[id4]/items[id5]/value[id13]");
        quantity.setMagnitude(40d);

        ruleEvaluation.evaluate(root, archetype.getRules().getRules());
        assertEquals(false, ruleEvaluation.getVariableMap().get("extended_validity").getObject(0));
        ValueList extendedValidity = ruleEvaluation.getVariableMap().get("extended_validity");
        assertEquals("/data[id2]/events[id3, 1]/data[id4]/items[id5, 1]/value/magnitude", extendedValidity.getPaths(0).get(0));
        quantity.setMagnitude(20d);

        ruleEvaluation.evaluate(root, archetype.getRules().getRules());
        extendedValidity = ruleEvaluation.getVariableMap().get("extended_validity");
        assertEquals(true, extendedValidity.getObject(0));
        assertEquals("/data[id2]/events[id3, 1]/data[id4]/items[id5, 1]/value/magnitude", extendedValidity.getPaths(0).get(0));

    }

    @Test
    public void multiValuedExpressions() throws Exception {
        archetype = parser.parse(ParsedRulesEvaluationTest.class.getResourceAsStream("multiplicity.adls"));
        RuleEvaluation ruleEvaluation = new RuleEvaluation(archetype);

        Pathable root = (Pathable) testUtil.constructEmptyRMObject(archetype.getDefinition());

        {
            DvQuantity systolic = (DvQuantity) root.itemAtPath("/data[id2]/events[id3]/data[id4]/items[id5]/value[id13]");
            systolic.setMagnitude(76d);
            DvQuantity diastolic = (DvQuantity) root.itemAtPath("/data[id2]/events[id3]/data[id4]/items[id6]/value[id14]");
            diastolic.setMagnitude(80d);
            //this is fine, because "blood_pressure_valid: $systolic > $diastolic - 5"
        }


        //add a second event
        {
            Pathable root2 = (Pathable) testUtil.constructEmptyRMObject(archetype.getDefinition());

            DvQuantity systolic = (DvQuantity) root2.itemAtPath("/data[id2]/events[id3]/data[id4]/items[id5]/value[id13]");
            systolic.setMagnitude(60d);
            DvQuantity diastolic = (DvQuantity) root2.itemAtPath("/data[id2]/events[id3]/data[id4]/items[id6]/value[id14]");
            diastolic.setMagnitude(80d);
            Observation observation = (Observation) root;
            Observation observation2 = (Observation) root2;
            observation.getData().addEvent(observation2.getData().getEvents().get(0));
            //a strange reading that will lead to a failure
        }

        ruleEvaluation.evaluate(root, archetype.getRules().getRules());

        List<AssertionResult> assertionResults = ruleEvaluation.getAssertionResults();
        assertEquals("one assertion should have been checked", 1, assertionResults.size());
        AssertionResult result = assertionResults.get(0);

        assertEquals("the assertion should not have succeeded", false, result.getResult());
        assertEquals("the assertion tag should be correct", "blood_pressure_valid", result.getTag());
        ValueList rawResult = result.getRawResult();
        assertEquals("two checks have been done", 2, rawResult.size());

        {
            Value<Boolean> value1 = rawResult.getValues().get(0);
            assertTrue("assertion 1 should be true", value1.getValue());
            //systolic and diastolic value 1. The original paths should be present, even if they have been computed through a variable
            assertTrue(value1.getPaths().contains("/data[id2]/events[id3, 1]/data[id4]/items[id5, 1]/value/magnitude"));
            assertTrue(value1.getPaths().contains("/data[id2]/events[id3, 1]/data[id4]/items[id6, 2]/value/magnitude"));
        }

        {
            Value<Boolean> value2 = rawResult.getValues().get(1);
            assertFalse("assertion 2 should be false", value2.getValue());
            //systolic and diastolic value 2. The original paths should be present, even if they have been computed through a variable
            assertTrue(value2.getPaths().contains("/data[id2]/events[id3, 2]/data[id4]/items[id5, 1]/value/magnitude"));
            assertTrue(value2.getPaths().contains("/data[id2]/events[id3, 2]/data[id4]/items[id6, 2]/value/magnitude"));
        }
    }




}
