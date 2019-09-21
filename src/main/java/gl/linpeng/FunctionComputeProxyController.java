package gl.linpeng;


import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.Credentials;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.FunctionParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * Alibaba function compute proxy controller
 *
 * @author lin.peng
 * @since 1.0
 **/
@RestController
@RequestMapping("/gateway")
public class FunctionComputeProxyController {


    @RequestMapping(value = "/proxy/{groupName}/{functionName}", method = RequestMethod.POST)
    public Object proxy(@PathVariable String functionName,@PathVariable String groupName, @RequestBody String postData) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
        String serverlessPackage = "gl.linpeng.serverless.advisor.controller";
        if(groupName!=null && groupName.equalsIgnoreCase("aengine")){
            serverlessPackage = "gl.linpeng.serverless.aengine.controller";
        }
        postData = postData.replaceAll("\\r","");
        postData = postData.replaceAll("\\n","");

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
                return null;
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
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class actualClass = (Class) parameterizedType.getActualTypeArguments()[0];
        Object dto = mapper.readValue(postData, actualClass);
        Object result = handle.invoke(instance, dto, ctx);
        return result;
    }

    private String toCamelName(String functionName) {
        return functionName.substring(0, 1).toUpperCase() + functionName.substring(1);
    }


}
