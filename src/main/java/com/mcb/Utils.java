package com.mcb;

/**
 * Created by matthew on 9/8/15.
 */
public class Utils {
    public static class StringUtils {
        /**
         * Evaluates if a String is null or empty
         *
         * @param s @link String to evaluate
         * @return @link Boolean representation s's blankness
         */
        public static final Boolean isBlank(String s){
            return s == null || s == "" ? true : false;
        }
    }
}
