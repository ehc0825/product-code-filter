package org.elasticsearch.plugin.analysis.util;

import java.util.regex.Pattern;

public class ProductCodeParseUtil {
    public String productCode(String inputString) {
        if(isProductCode(inputString))
        {
            inputString =  inputString.replaceAll("[0-9]*$","");
        }
        return inputString;
    }

    private boolean isProductCode(String inputString)
    {
        String pattern = "^[a-zA-Z]+[0-9]*$";
        String secondPattern = "^[0-9a-zA-Z]+\\-+[0-9]*$";
        return Pattern.matches(pattern,inputString) || Pattern.matches(secondPattern,inputString);
    }
}
