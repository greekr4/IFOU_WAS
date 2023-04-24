package com.gaon.nifou.v3.ctrl.sub06;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gaon.nifou.v3.trans_ora_manager;
import com.gaon.nifou.v3.util_manager;

@WebServlet("/sub06/03_midmap_modify.gaon")
public class sub06_03_midmap_modify extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		
		
		String orgcd = request.getParameter("orgcd");
		String depcd = request.getParameter("depcd");
		String mids = request.getParameter("mids");
		String insuser = request.getParameter("insuser");
		String DEBUG = request.getParameter("DEBUG");
		
		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("orgcd", orgcd);
		ParamMap.put("depcd", depcd);
		ParamMap.put("mids",mids);
		ParamMap.put("insuser",insuser);
		
		JSONObject res_json = new JSONObject();
		
		
		int del_res = oram.delete_midmap(ParamMap, DEBUG);
		int res = oram.modify_midmap(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}

