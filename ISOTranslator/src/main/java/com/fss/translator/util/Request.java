package com.fss.translator.util;

import lombok.Data;
import net.minidev.json.JSONObject;

/**
 * Request will map into this class
 * 
 * @author Harikrishna Agnikondu
 *
 */
@Data
public class Request {

	private String sourceMessageType;

	private String apiKey;

	private String requestData;


	public String getRequestBody() {
		JSONObject obj = new JSONObject();
		obj.put("sourceMessageType", this.sourceMessageType);
		obj.put("apiKey", this.apiKey);
		obj.put("requestData", this.requestData);
		return obj.toString();

	}

}
