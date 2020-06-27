package com.fss.translator.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fss.translator.constants.TranslatorConstants;
import com.fss.translator.exception.ServiceException;

import lombok.extern.log4j.Log4j2;

/**
 * Util class provides all utility methods which can be reused across the
 * service.
 * 
 * @author Harikrishna Agnikondu
 *
 */

@Log4j2
public class Util {

	
	/**
	 * Util class should not be instantiated.
	 */
	private Util() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Checks whether an input String is empty or not. Returns true is input string
	 * is null or has 0 length.
	 * 
	 * @param input
	 *            The input string to be checked.
	 * 
	 * @return boolean indicating whether input string is empty or not.
	 */
	public static boolean isEmpty(String input) {
		return (input == null || input.trim().isEmpty() || input.equalsIgnoreCase("null"));
	}

	/**
	 * 
	 * Converting a Map to JSON String
	 * 
	 * @throws JsonProcessingException
	 * 
	 * @throws Exception
	 */
	public static String mapToJson(Map<String, Object> productAttributes) throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		//String attributesJsonResp = null;
		/*attributesJsonResp =*/ 
		return objectMapper.writeValueAsString(productAttributes);
		//return attributesJsonResp;
	}

	public static String convertHashMapToJson(Map<String, Object> attributes) {
		String attributesJsonString = null;

		if (!attributes.isEmpty()) {
			ObjectMapper mapperObj = new ObjectMapper();
			try {
				attributesJsonString = mapperObj.writeValueAsString(attributes);

			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
		}

		return attributesJsonString;
	}

	public static Map<String, Object> convertJsonToHashMap(String attributesJsonString) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> productAttributes = null;

		if (!Util.isEmpty(attributesJsonString)) {
			try {
				mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
				//TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
				productAttributes = mapper.readValue(attributesJsonString,/* typeRef*/new TypeReference<Map<String, Object>>(){});

			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		return productAttributes;
	}

	/**
	 * 
	 * Converting a JSON String to Map
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * 
	 * @throws Exception
	 */
	public static Map<String, Object> jsonToMap(String productAttributesStr) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		//Map<String, Object> productAttributes = null;

		/*productAttributes =*/ return objectMapper.readValue(productAttributesStr, new TypeReference<Map<String, Object>>() {
		});

		//return productAttributes;

	}

	public static boolean useFilter(String filterName) {
		return (!isEmpty(filterName) && !"*".equals(filterName));

	}

	public static String getDateWithTimeZone(String input) {

		DateFormat pDfrmt = new SimpleDateFormat(input);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new java.util.Date());

		return pDfrmt.format(cal.getTime());
	}

	/**
	 * to left pad the string
	 * 
	 * @param asInput
	 *            a string that has to be padded
	 * @param aiLen
	 *            required length of the string
	 * @param aiPadChar
	 *            the character with which the string has to be padded
	 * @return a padded string
	 */
	public static String padLeft(String asInput, int aiLen, String aiPadChar) {
		
		int piLen = 0;
		
		String input = returnBlank(asInput);
		input = input.trim();
		piLen = input.length();
		StringBuilder psretval = new StringBuilder(input);

		for (int i = 0; i < (aiLen - piLen); i++)
			psretval = new StringBuilder(aiPadChar).append(psretval);

		return psretval.toString();
	}

	/**
	 * returns the balnk string whereever a null value is got or the input string is
	 * null
	 * 
	 * @param inputVal
	 *            a string which has to be compared
	 * @return the blank string
	 */
	public static String returnBlank(String inputVal) {
		if (inputVal == null || inputVal.equals("null")) {
			return "";
		}
		/*if (inputVal.equals("null")) {
			return "";
		}
*/
		return inputVal;
	}

	public static String convertAsString(Object dataObject) {
		return (dataObject != null) && !"null".equalsIgnoreCase(String.valueOf(dataObject).trim())
				&& !"".equalsIgnoreCase(String.valueOf(dataObject).trim()) ? String.valueOf(dataObject) : "";
	}

	/**
	 * 
	 * Masking the sensitive data
	 * 
	 * 
	 */
	public static String maskString(String data) {

		if (!isEmpty(data)) {
			return data.replace(data.substring(4, data.length() - 4), "****");
		}
		return data;

	}

	/**
	 * This method will check object has null or not
	 * 
	 * @param obj
	 * @return
	 */

	public static boolean isNotNull(Object obj) {

		Optional<?> optObj = Optional.ofNullable(obj);

		return optObj.isPresent() ? true : false;

	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, String> getTreeMap(Map<String, String> map) {

		return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				(oldValue, newValue) -> newValue, TreeMap::new));

	}

	/**
	 * 
	 * @param name
	 * @return
	 */

	public static String getPropertInstFileName(String name) {
		return TranslatorConstants.INST_PREFIX + name + TranslatorConstants.PEROPERTY_SUFFIX;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static String getPropertFileName(String name) {
		return name + TranslatorConstants.PEROPERTY_SUFFIX;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static String getPropertKey(String name) {
		return name.replace(TranslatorConstants.PEROPERTY_SUFFIX, "");
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static String getXSDKey(String name) {
		return name.replace(TranslatorConstants.XSD_SUFFIX, "");
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static String getXSDFileName(String name) {

		return name + TranslatorConstants.XSD_SUFFIX;
	}

	public static String doEnCode(String inputData) {
		byte[] encodeString = Base64.getEncoder().encode(inputData.getBytes());

		return new String(encodeString);

	}

	/**
	 * 
	 * @param inputData
	 * @return
	 */
	public static String doDeCode(String inputData) {

		return new String(Base64.getDecoder().decode(inputData.getBytes()));
	}

	public static Map<String, Object> stringToMap(String input) throws ServiceException {
		Map<String, Object> map = new HashMap<>();

		String[] nameValuePairs = input.split("\\,");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
			try {
				map.put(URLDecoder.decode(nameValue[0], "UTF-8"),
						nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new ServiceException("This method requires UTF-8 encoding support", e.getMessage());
			}
		}

		return map;
	}

}
