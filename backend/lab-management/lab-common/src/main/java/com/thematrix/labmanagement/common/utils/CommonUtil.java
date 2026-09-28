package com.thematrix.labmanagement.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求数据保存
 */
@Slf4j
public class CommonUtil {

    private static final String PARSED_BODY_KEY = "PARSED_BODY";

    @SuppressWarnings("unchecked")
    public static final Map<String, Object> getBodyParametersFromRequest(HttpServletRequest request) {
        Map<String, Object> map = null;

        if (request != null) {
            map = (Map<String, Object>) request.getAttribute(PARSED_BODY_KEY);

            if (map == null) {
                map = readParamsFromRequest(request);
                request.setAttribute(PARSED_BODY_KEY, map);
            }
        }

        if (map == null) {
            map = new HashMap<>(0);
        }

        return map;
    }

    public static final String getStringBodyParameterFromRequest(HttpServletRequest request, String key) {
        String result = Strings.EMPTY;
        Map<String, Object> map = getBodyParametersFromRequest(request);

        if (map.get(key) != null) {
            result = map.get(key).toString();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readParamsFromRequest(HttpServletRequest request) {
        Map<String, Object> map = null;

        try {
            ServletInputStream inputStream = request.getInputStream();
            StringBuilder content = new StringBuilder();
            byte[] b = new byte[1024];
            int lens;
            while ((lens = inputStream.read(b)) > 0) {
                content.append(new String(b, 0, lens));
            }

            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(content.toString(), Map.class);
        } catch (Exception ex) {
            log.error("从请求中读取参数时发生错误");
        }

        return map;
    }
}
