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

@WebServlet("/sub06/03_register_dep.gaon")
public class sub06_03_register_dep extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		
		String reg_dep_nm = request.getParameter("reg_dep_nm");
		String reg_dep_adm_user = request.getParameter("reg_dep_adm_user");
		String reg_dep_type = request.getParameter("reg_dep_type");
		String reg_dep_email = request.getParameter("reg_dep_email");
		String reg_dep_tel = request.getParameter("reg_dep_tel");
		String orgcd = request.getParameter("orgcd");
		String DEBUG = request.getParameter("DEBUG");
		
		String timestmap = String.valueOf(System.currentTimeMillis());
		String reg_dep_depcd = "MD" + timestmap;
		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("reg_dep_nm", reg_dep_nm);
		ParamMap.put("reg_dep_adm_user", reg_dep_adm_user);
		ParamMap.put("reg_dep_type", reg_dep_type);
		ParamMap.put("reg_dep_email", reg_dep_email);
		ParamMap.put("reg_dep_tel", reg_dep_tel);
		ParamMap.put("reg_dep_depcd", reg_dep_depcd);
		ParamMap.put("orgcd", orgcd);
		
		
		JSONObject res_json = new JSONObject();
		int res = oram.register_dep(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}
