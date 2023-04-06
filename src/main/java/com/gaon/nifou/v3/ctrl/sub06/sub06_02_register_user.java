package com.gaon.nifou.v3.ctrl.sub06;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.gaon.nifou.v3.trans_ora_manager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/sub06/02_register_user.gaon")
public class sub06_02_register_user extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		String orgcd = request.getParameter("orgcd");
		String depcd = request.getParameter("depcd");
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		String user_name = request.getParameter("user_name");
		String user_lv = request.getParameter("user_lv");
		String user_tel1 = request.getParameter("user_tel1");
		String user_tel2 = request.getParameter("user_tel2");
		String user_email = request.getParameter("user_email");
		String ins_id = request.getParameter("ins_id");
		
		JSONObject res_json = new JSONObject();
		if (oram.isIdDuplicated(user_id) == 0) {
		int res = oram.register_user(depcd,orgcd, user_id, user_pw, user_lv, user_tel1, user_tel2,user_name,user_email,ins_id);
		res_json.put("res", res);
		}else {
		res_json.put("res",2);
		}
		out.print(res_json);

	}

}

