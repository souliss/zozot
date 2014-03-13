package com.zozot.OEM.cloudservice.exception;

/**
 * An exception condition indicating error when trying to make a request.
 * 
 * @author s0pau
 */
public class RequestUnsuccessfulException extends XClientException
{
	int statusCode;

	public RequestUnsuccessfulException(String msg, Throwable t)
	{
		super(msg, t);
	}
}