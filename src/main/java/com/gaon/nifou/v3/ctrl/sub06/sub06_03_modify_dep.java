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

@WebServlet("/sub06/03_modify_dep.gaon")
public class sub06_03_modify_dep extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		
		String modify_depnm = request.getParameter("modify_depnm");
		String modify_depcd = request.getParameter("modify_depcd");
		String modify_user = request.getParameter("modify_user");
		String modify_tel = request.getParameter("modify_tel");
		String modify_email = request.getParameter("modify_email");
		String modify_type = request.getParameter("modify_type");
		String orgcd = request.getParameter("orgcd");
		String DEBUG = request.getParameter("DEBUG");
		

		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("modify_depnm", modify_depnm);
		ParamMap.put("modify_depcd", modify_depcd);
		ParamMap.put("modify_user", modify_user);
		ParamMap.put("modify_tel", modify_tel);
		ParamMap.put("modify_email", modify_email);
		ParamMap.put("modify_type", modify_type);
		ParamMap.put("orgcd", orgcd);
		
		
		JSONObject res_json = new JSONObject();
		int res = oram.modify_dep(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}
