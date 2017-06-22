package com.itc.service.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

public class IPAddressUtil {
	public static String getClientIp(HttpServletRequest request) {

//		String domainName = System.getenv("USERDOMAIN");
//		System.out.println(domainName);
//		InetAddress addr;
//		try {
//			addr = InetAddress.getLocalHost();
//			String hostname = addr.getHostName();
//			System.out.println(hostname);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return request.getRemoteAddr();
	}
}