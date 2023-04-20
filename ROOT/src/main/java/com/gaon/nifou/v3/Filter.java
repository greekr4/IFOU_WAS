package com.gaon.nifou.v3;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class Filter implements javax.servlet.Filter{
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		 
	HttpServletResponse response = (HttpServletResponse) res;

	response.setHeader("Access-Control-Allow-Origin", "*");
	response.setHeader("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept");
	response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
	response.setHeader("Access-Control-Max-Age", "3600");

	chain.doFilter(req, response);
}

public void init(FilterConfig arg0) throws ServletException {}

public void destroy() {}
}
