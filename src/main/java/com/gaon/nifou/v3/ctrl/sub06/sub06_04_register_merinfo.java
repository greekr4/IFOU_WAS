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

@WebServlet("/sub06/04_register_merinfo.gaon")
public class sub06_04_register_merinfo extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		String DEBUG = request.getParameter("DEBUG");
		String orgcd = request.getParameter("orgcd");
		String depcd = request.getParameter("depcd");
		String purcd = request.getParameter("purcd");
		String van = request.getParameter("van");
		String mid = request.getParameter("mid");

		
		
		String timestmap = String.valueOf(System.currentTimeMillis());
		String reg_mer_mercd = "MD" + timestmap;
		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("reg_mer_mercd", reg_mer_mercd);
		ParamMap.put("orgcd",orgcd);
		ParamMap.put("depcd",depcd);
		ParamMap.put("purcd",purcd);
		ParamMap.put("van",van);
		ParamMap.put("mid",mid);
		
		JSONObject res_json = new JSONObject();
		int res = oram.register_merinfo(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}

