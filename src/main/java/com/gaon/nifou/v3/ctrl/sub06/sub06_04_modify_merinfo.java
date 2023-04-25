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

@WebServlet("/sub06/04_modify_merinfo.gaon")
public class sub06_04_modify_merinfo extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		String DEBUG = request.getParameter("DEBUG");

		String orgcd = request.getParameter("orgcd");
		String modify_depcd = request.getParameter("modify_depcd");
		String modify_mercd = request.getParameter("modify_mercd");
		String modify_mid = request.getParameter("modify_mid");
		String modify_purcd = request.getParameter("modify_purcd");
		String modify_van = request.getParameter("modify_van");

		
		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("orgcd",orgcd);
		ParamMap.put("modify_depcd",modify_depcd);
		ParamMap.put("modify_mercd",modify_mercd);
		ParamMap.put("modify_purcd",modify_purcd);
		ParamMap.put("modify_mid",modify_mid);
		ParamMap.put("modify_van",modify_van);
		
		JSONObject res_json = new JSONObject();
		int res = oram.modify_merinfo(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}

