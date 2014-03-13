package com.zozot.OEM.cloudservice;

import android.net.Uri;

/**
 * Fluent UriBuilder for API service endpoints.
 * 
 * @author s0pau
 * 
 */
public class UriBuilder
{
	private String DEFAULT_BASE_URI = "http://emoncms.org/input/";

	
		/**
		 * @return url to all Feed API endpoints
		 */
		public Feed feeds()
		{
			return new Feed();
		}

		/**
		 * @param feedId
		 *            parent of the datastream
		 * @return url to all Datastream API endpoints
		 */
//		public Datastream datastreams()
//		{
//			return new Datastream();
//		}

		/**
		 * @param datastreamId
		 *            direct parent of the datapoint
		 * @return url to all Datapoint API endpoints
		 */
		public Datapoint datapoints(String datastreamId, String sValue)
		{
			return new Datapoint(datastreamId, sValue);
		}

		/**
		 * 
		 * @return url to all ApiKey API endpoints
		 */
		public ApiKey apiKeys()
		{
			return new ApiKey();
		}

		public class Feed
		{
			public static final String ROOTList = "list.json";

			private Feed()
			{

			}

			public String resource()
			{
				return resources();//.concat("/");
			}
			public String resources()
			{
				return DEFAULT_BASE_URI.concat(ROOTList);
			}
			public String resources(String apiKey)
			{
				return DEFAULT_BASE_URI.concat(ROOTList+apiKeys().resource(apiKey));
			}
		}

//		public class Datastream
//		{
//			public static final String ROOT = "post.json?json=";
//
//			private Datastream()
//			{
//				
//			}
//
//			public String resources()
//			{
//				return parent().concat(ROOT);
//			}
//
//			public String resource(String datastreamId, String sValue)
//			{
//				return resources().concat("{").concat(datastreamId).concat("=").concat(sValue).concat("}");
//			}
//
//			public String parent()
//			{
//				return DEFAULT_BASE_URI.concat("/");
//			}
//		}

		public class Datapoint
		{
			public static final String ROOTPush = "post.json&json=";
			//http://emoncms.org/input/post.json?json={power:200}
			
			private String datastreamId;
			private String value;

			private Datapoint(String datastreamId, String sValue)
			{
				
				this.datastreamId = datastreamId;
				this.value=sValue;
			}

			public String resources(String apiKey)
			{
				return parent().concat(ROOTPush).concat(resource(datastreamId, value)).concat(apiKeys().resource(apiKey));
			}

			public String resource(String datastreamId, String sValue)
			{
				return Uri.encode("{".concat(datastreamId).concat(":").concat(sValue).concat("}"));
			}

			public String parent()
			{
				return DEFAULT_BASE_URI;
			}
		}

		public class ApiKey
		{
			public static final String ROOT = "apikey";

			private ApiKey()
			{

			}

			public String resource(String keyId)
			{
				return "&".concat(resources().concat("=").concat(keyId));
			}

			public String resources()
			{
				return ROOT;
			}
		}

	}


