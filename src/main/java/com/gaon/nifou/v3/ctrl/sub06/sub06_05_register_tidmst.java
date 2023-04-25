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

@WebServlet("/sub06/05_register_tidmst.gaon")
public class sub06_05_register_tidmst extends HttpServlet {
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
		String reg_model = request.getParameter("reg_model");
		String reg_van = request.getParameter("reg_van");
		String reg_tidno = request.getParameter("reg_tidno");
		String reg_tidnm = request.getParameter("reg_tidnm");


		
		String timestmap = String.valueOf(System.currentTimeMillis());
		String reg_tidcd = "TD" + timestmap;
		
		HashMap<String, String> ParamMap = new HashMap<String, String>();
		ParamMap.put("reg_tidcd", reg_tidcd);
		ParamMap.put("orgcd",orgcd);
		ParamMap.put("depcd",depcd);
		ParamMap.put("reg_model",reg_model);
		ParamMap.put("reg_van",reg_van);
		ParamMap.put("reg_tidno",reg_tidno);
		ParamMap.put("reg_tidnm",reg_tidnm);
		
		JSONObject res_json = new JSONObject();
		int res = oram.register_tidmst(ParamMap,DEBUG);
		res_json.put("res", res);
		out.print(res_json);

	}

}

