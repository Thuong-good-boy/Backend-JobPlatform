package com.jobplatform.job_recruitment_system.utils;

public class LatexUtils {
    public static String escapeLatex(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\': sb.append("\\textbackslash{}"); break;
                case '&':  sb.append("\\&"); break;
                case '%':  sb.append("\\%"); break;
                case '$':  sb.append("\\$"); break;
                case '#':  sb.append("\\#"); break;
                case '_':  sb.append("\\_"); break;
                case '{':  sb.append("\\{"); break;
                case '}':  sb.append("\\}"); break;
                case '~':  sb.append("\\textasciitilde{}"); break;
                case '^':  sb.append("\\textasciicircum{}"); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
}