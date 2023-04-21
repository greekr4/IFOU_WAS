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

@WebServlet("/sub06/01_modify_org.gaon")
public class sub06_01_modify_org extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		
		String org_nm = request.getParameter("org_nm");
		String org_no = request.getParameter("org_no");
		String org_ceo_nm = request.getParameter("org_ceo_nm");
		String org_tel1 = request.getParameter("org_tel1");
		String org_email = request.getParameter("org_email");
		String org_memo = request.getParameter("org_memo");
		String orgcd = request.getParameter("orgcd");
		String DEBUG = request.getParameter("DEBUG");
		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("org_nm", org_nm);
		ParamMap.put("org_no", org_no);
		ParamMap.put("org_ceo_nm", org_ceo_nm);
		ParamMap.put("org_tel1", org_tel1);
		ParamMap.put("org_email", org_email);
		ParamMap.put("org_memo", org_memo);
		ParamMap.put("orgcd", orgcd);
		
		JSONObject res_json = new JSONObject();
		int res = oram.modify_org(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}
