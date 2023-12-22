package gl.linpeng;


import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSON;
import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.Credentials;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.FunctionParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import gl.linpeng.gf.annotation.Auth;
import gl.linpeng.gf.utils.TokenUtil;
import gl.linpeng.serverless.advisor.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;


/**
 * Alibaba function compute proxy controller
 *
 * @author lin.peng
 * @since 1.0
 **/
@RestController
@RequestMapping("/gateway")
public class FunctionComputeProxyController {
    private static final Logger logger = LoggerFactory.getLogger(FunctionComputeProxyController.class);

    private Cache<String, String> lfuCache = CacheUtil.newLFUCache(1000);
    private String[] cacheFunctions = new String[]{"disease", "food", "ingredient", "foodMaterialQuery"};

    @RequestMapping(value = "/proxy/{groupName}/{functionName}", method = RequestMethod.POST)
    public Object proxy(@PathVariable String functionName, @PathVariable String groupName, @RequestBody String postData, @RequestHeader Map header) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
        String serverlessPackage = "gl.linpeng.serverless.advisor.controller";
        if (groupName != null && groupName.equalsIgnoreCase("aengine")) {
            serverlessPackage = "gl.linpeng.serverless.aengine.controller";
        }
        postData = postData.replaceAll("\\r", "");
        postData = postData.replaceAll("\\n", "");

        logger.info("===================begin:");
        logger.info("Request    Function:" + functionName);
        logger.info("Request       Group:" + groupName);
        logger.info("Request      Header:" + header);
        logger.info("Request RequestBody:" + postData);

        boolean isCacheable = Arrays.asList(cacheFunctions).contains(functionName);
        String result = null;
        Map<String, Object> requestBody = JSON.parseObject(postData, Map.class);
        String cacheKey = "group:" + groupName + "-function:" + functionName + "-id:" + requestBody.get("id") + "-page:" + requestBody.get("page") + "-pageSize:" + requestBody.get("pageSize");
        if (isCacheable && lfuCache.containsKey(cacheKey)) {
            result = lfuCache.get(cacheKey);
        } else {
            // get all Serverless
            String className = serverlessPackage + "." + toCamelName(functionName) + "Controller";
            Class clz = getClass().getClassLoader().loadClass(className);
            Object instance = clz.newInstance();
            Method[] methods = clz.getMethods();
            Method handle = null;
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase("handleRequest")) {
                    handle = method;
                    break;
                }
            }
            if (handle == null) {
                throw new RuntimeException("Can't find serverless handle method.");
            }

            Context ctx = new Context() {
                @Override
                public String getRequestId() {
                    return String.valueOf(System.currentTimeMillis());
                }

                @Override
                public Credentials getExecutionCredentials() {
                    Credentials credentials = new Credentials() {
                        @Override
                        public String getAccessKeyId() {
                            Object accessKey = header.get("FC-ACCESS-KEY");
                            return accessKey == null ? null : accessKey.toString();
                        }

                        @Override
                        public String getAccessKeySecret() {
                            Object accessSecret = header.get("FC-ACCESS-SECRET");
                            return accessSecret == null ? null : accessSecret.toString();
                        }

                        @Override
                        public String getSecurityToken() {
                            Object accessToken = header.get("FC-ACCESS-TOKEN");
                            if(accessToken == null){
                                accessToken = header.get("Authorization");
                            }
                            return accessToken == null ? null : accessToken.toString();
                        }
                    };
                    return credentials;
                }

                @Override
                public FunctionParam getFunctionParam() {
                    return null;
                }

                @Override
                public FunctionComputeLogger getLogger() {
                    return null;
                }
            };
            Class[] types = handle.getParameterTypes();
            ObjectMapper mapper = new ObjectMapper();
            Type type = clz.getGenericSuperclass();

            // 增加auth验证判断
            Annotation annotation = clz.getAnnotation(Auth.class);
            if(annotation != null){
                // 需要鉴权
                String token = ctx.getExecutionCredentials().getSecurityToken();
                if(token == null || token.length() == 0){
                    throw new RuntimeException("Can't invoke method,Check secret token please.");
                }
                boolean isVerify = TokenUtil.verifyToken(Constants.TOKEN_SECRET,token);
                if(!isVerify){
                    throw new RuntimeException("Verify token failed,Illegal credentials.");
                }
            }

            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class actualClass = (Class) parameterizedType.getActualTypeArguments()[0];
            Object dto = mapper.readValue(postData, actualClass);
            Object jsonObject = handle.invoke(instance, dto, ctx);
            result = JSON.toJSONString(jsonObject);
            // cache result
            if (isCacheable) {
                lfuCache.put(cacheKey, result, DateUnit.HOUR.getMillis() * 24);
            }
        }

        logger.info("Response       Body:" + result);
        logger.info("=====================end.");
        return result;
    }

    private String toCamelName(String functionName) {
        return functionName.substring(0, 1).toUpperCase() + functionName.substring(1);
    }


}
