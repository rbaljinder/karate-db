package com.rbaljinder.automation.karate.db;

import com.intuit.karate.ScriptContext;
import com.intuit.karate.ScriptValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.karate.Script.evalInNashorn;
import static com.intuit.karate.Script.isOptionalMacro;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.*;

public class StepExtensionUtils {
    public static Pattern EMBEDDED_MACRO_EXPRESSION_PATTERN = Pattern.compile("(#\\(\\w*\\))");


    public static String resolve(String value) {
        if (startsWith(value, "_RESOLVE_ENV_")) {
            String propName = substringAfter(value, "_RESOLVE_ENV_");
            String envPropertyValue = getProperty(propName);
            if (isBlank(envPropertyValue))
                throw new RuntimeException("could not resolve property " + propName + " from environment variable");
            return envPropertyValue;
        }
        return value;
    }

    public static String evalExpression(String expression, ScriptContext context) {
        Matcher matcher = EMBEDDED_MACRO_EXPRESSION_PATTERN.matcher(expression);
        while (matcher.find()) {
            String value = matcher.group();
            boolean optional = isOptionalMacro(value);
            ScriptValue sv = evalInNashorn(value.substring(optional ? 2 : 1), context);
            expression = expression.replace(value, sv.getValue().toString());
        }
        return expression;
    }
}
