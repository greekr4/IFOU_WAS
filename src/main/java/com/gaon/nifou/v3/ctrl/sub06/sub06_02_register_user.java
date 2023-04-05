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
		String user_lv = request.getParameter("user_lv");
		String user_tel = request.getParameter("user_tel");
		String user_tel2 = request.getParameter("user_tel2");
		
		int res = oram.register_user(depcd,orgcd, user_id, user_pw, user_lv, user_tel, user_tel2);
		JSONObject res_json = new JSONObject();
		res_json.put("res", res);
		out.print(res_json);

	}

}

