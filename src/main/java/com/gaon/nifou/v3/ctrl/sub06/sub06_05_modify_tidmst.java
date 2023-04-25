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

@WebServlet("/sub06/05_modify_tidmst.gaon")
public class sub06_05_modify_tidmst extends HttpServlet {
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
		String modify_tidnm = request.getParameter("modify_tidnm");
		String modify_tidno = request.getParameter("modify_tidno");
		String modify_van = request.getParameter("modify_van");
		String modify_model = request.getParameter("modify_model");
		String modify_tidcd = request.getParameter("modify_tidcd");

		
		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("orgcd",orgcd);
		ParamMap.put("modify_depcd",modify_depcd);
		ParamMap.put("modify_tidnm",modify_tidnm);
		ParamMap.put("modify_tidno",modify_tidno);
		ParamMap.put("modify_van",modify_van);
		ParamMap.put("modify_model",modify_model);
		ParamMap.put("modify_tidcd",modify_tidcd);
		
		JSONObject res_json = new JSONObject();
		int res = oram.modify_tidmst(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}

