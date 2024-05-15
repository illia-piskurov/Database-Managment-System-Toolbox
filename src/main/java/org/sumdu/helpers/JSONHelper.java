package org.sumdu.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sumdu.models.DatabaseInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JSONHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeDatabaseInstancesToFile(List<DatabaseInstance> databaseInstances, String filePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), databaseInstances);
    }

    public static List<DatabaseInstance> readDatabaseInstancesFromFile(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), new TypeReference<List<DatabaseInstance>>() {});
    }
}
