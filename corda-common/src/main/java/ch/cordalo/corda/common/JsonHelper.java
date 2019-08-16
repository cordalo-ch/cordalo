package ch.cordalo.corda.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.corda.client.jackson.JacksonSupport;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonHelper {

    private static final TypeReference<Map<String, Object>> TYPE_REF_MAP = new TypeReference<Map<String, Object>>() {
    };

    public static String convertJsonToString(Map<String, Object> data) {
        ObjectMapper mapper = JacksonSupport.createNonRpcMapper();
        try {
            return mapper.writeValueAsString(data);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Map<String, Object> convertStringToJson(String dataString) {
        if (dataString != null && !dataString.isEmpty()) {
            ObjectMapper mapper = JacksonSupport.createNonRpcMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            try {
                return mapper.readValue(dataString, TYPE_REF_MAP);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    public static Map<String, Object> filterByGroupId(Map<String, Object> dataObject, String[] groupIDs) {
        Map<String, Object> sharedMap = new LinkedHashMap<>();
        for (String s: groupIDs) {
            if (dataObject.get(s) != null) {
                sharedMap.put(s, dataObject.get(s));
            }
        }
        return sharedMap;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> updateMapWithEntry(Map<String, Object> map, Map.Entry<String, Object> updateEntry) {
        Object oldValue = map.get(updateEntry.getKey());
        if (oldValue == null) {
            map.put(updateEntry.getKey(), updateEntry.getValue());
        } else {
            if (oldValue instanceof Map<?, ?>) {
                Map<String, Object> oldMap = (Map)oldValue;
                Map<String, Object> newMap = (Map)updateEntry.getValue();
                map.put(updateEntry.getKey(), updateMapWithMap(oldMap, newMap));
            } else {
                map.put(updateEntry.getKey(), updateEntry.getValue());
            }
        }
        return map;
    }
    private static Map<String, Object> updateMapWithMap(Map<String, Object> map, Map<String, Object> updateMap) {
        Map<String, Object> newMap = map;
        for ( Map.Entry<String, Object> entry : updateMap.entrySet()) {
            newMap = updateMapWithEntry(map, entry);
        }
        return newMap;
    }
    public static Map<String, Object> updateValues(Map<String, Object> map, String dataUpdateString) {
        Map<String, Object> oldMap = new LinkedHashMap<>(map);
        Map<String, Object> newMap = JsonHelper.convertStringToJson(dataUpdateString);
        return updateMapWithMap(oldMap, newMap);
    }

    public static String getDataValue(Map<String, Object> map, String attributesSeperatedByPoint) {
        String[] keys = attributesSeperatedByPoint.split("\\.");
        Object result = map;
        for (String k : keys) {
            Map<String, Object> tempMap = (Map<String, Object>)result;
            if (tempMap == null) {
                return null;
            }
            result = tempMap.get(k.trim());
        }
        return result == "" ? null : result.toString();
    }

}
