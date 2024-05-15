package org.sumdu.helpers;

import org.sumdu.models.DatabaseInstance;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnvHelper {
    public static String[] getEnvVars(DatabaseInstance database) {
        List<String> envVars = new ArrayList<>();
        Class<?> clazz = database.getClass();
        for (Map.Entry<String, String> entry : database.getEnvs().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            try {
                Method method = clazz.getDeclaredMethod(value);
                Object methodResult = method.invoke(database);
                envVars.add(key + methodResult);
            } catch (NoSuchMethodException e) {
                envVars.add(key + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return envVars.toArray(new String[0]);
    }
}
