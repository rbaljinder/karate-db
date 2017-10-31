package com.rbaljinder.automation.karate.db;

import com.intuit.karate.Script;
import com.intuit.karate.ScriptContext;
import com.intuit.karate.StepDefs;
import cucumber.api.java.en.When;

import static com.rbaljinder.automation.karate.db.StepExtensionUtils.evalExpression;

public class StepDefsExtension {
    private final StepDefs karateStepDefs;
    private final ScriptContext context;

    public StepDefsExtension(StepDefs karateStepDefs) {
        this.karateStepDefs = karateStepDefs;
        this.context = karateStepDefs.getContext();
    }

    @When("^eval def (.+) = (.+)")
    public void defEval(String name, String expression) {
        Script.assign(name, evalExpression(expression, context), context);
    }
}
