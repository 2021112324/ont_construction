package org.example.Ind_equ.ai.SimCSE.impl;

import org.example.Ind_equ.ai.SimCSE.SimCSEMethod;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SimCSEMethodImpl implements SimCSEMethod {
    public static double calculateSimilarity(String text1, String text2) {
        try {
            String root = "src/main/python/indEqu/AI/SimCSE/";
            ProcessBuilder processBuilder = new ProcessBuilder("python", root + "simcse_similarity.py", text1, text2);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }

            return Double.parseDouble(output.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error calculating similarity", e);
        }
    }
}
