package com.zozot.OEM.cloudservice.exception;

/**
 * An exception condition indicating error when trying to make a request.
 * 
 * @author s0pau
 */
public class RequestUnsuccessfulException extends XClientException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7869561825106171310L;
	int statusCode;

	public RequestUnsuccessfulException(String msg, Throwable t)
	{
		super(msg, t);
	}
}