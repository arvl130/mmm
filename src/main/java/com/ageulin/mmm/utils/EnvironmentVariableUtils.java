package com.ageulin.mmm.utils;

import com.ageulin.mmm.exceptions.MissingOrInvalidEnvironmentVariableException;

public class EnvironmentVariableUtils {
    public static String getenvOrFail(String variableName) {
        var value = System.getenv(variableName);

        if (null == value || value.isEmpty()) {
            throw new MissingOrInvalidEnvironmentVariableException(variableName);
        }

        return value;
    }
}
