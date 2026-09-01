package com.ecommerce.automation.utils;

import com.ecommerce.automation.models.CategoryInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvTestDataReader {

    private CsvTestDataReader() {
    }

    public static List<CategoryInput> readCategories(String csvPath) {
        List<CategoryInput> categories = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of(csvPath));
            for (int i = 1; i < lines.size(); i++) { // skip header row
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }
                String category = parts[0].trim();
                String keyword = parts[1].trim();
                int quantity = Integer.parseInt(parts[2].trim());
                categories.add(new CategoryInput(category, keyword, quantity));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read test data file: " + csvPath, e);
        }
        return categories;
    }
}
