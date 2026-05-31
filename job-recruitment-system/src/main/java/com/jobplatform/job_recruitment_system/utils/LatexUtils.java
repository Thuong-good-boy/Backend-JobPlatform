package com.jobplatform.job_recruitment_system.utils;

public class LatexUtils {
    public static String escapeLatex(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("\\", "\\textbackslash{}")
                .replace("&", "\\&")
                .replace("%", "\\%")
                .replace("$", "\\$")
                .replace("#", "\\#")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("~", "\\textasciitilde{}")
                .replace("^", "\\textasciicircum{}");
    }
}
