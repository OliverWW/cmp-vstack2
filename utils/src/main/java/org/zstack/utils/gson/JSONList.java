package org.zstack.utils.gson;


import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;

public class JSONList<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -369558847578246550L;

    /**
     * 是否成功
     */
    private Boolean success;
    private String error;

    /**
     * 数据
     */
    private List<T> nodes;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
 
    public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public List<T> getNodes() {
		return nodes;
	}

	public void setNodes(List<T> nodes) {
		this.nodes = nodes;
	}

	public boolean isSuccess(){
    	return success;
    }
    public static JSONList fromJson(String json, Class clazz) {
        Gson gson = new Gson();
        Type objectType = type(JSONList.class, clazz);
        return gson.fromJson(json, objectType);
    }

    public String toJson(Class<T> clazz) {
        Gson gson = new Gson();
        Type objectType = type(JSONList.class, clazz);
        return gson.toJson(this, objectType);
    }

    static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }

}