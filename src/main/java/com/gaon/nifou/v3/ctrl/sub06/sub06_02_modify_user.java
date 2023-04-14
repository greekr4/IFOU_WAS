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

@WebServlet("/sub06/02_modify_user.gaon")
public class sub06_02_modify_user extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		PrintWriter out = response.getWriter();
		String memcd = request.getParameter("memcd");
		String user_pw = request.getParameter("user_pw");
		String user_nm = request.getParameter("user_nm");
		String user_email = request.getParameter("user_email");
		String user_tel1 = request.getParameter("user_tel1");
		String user_tel2 = request.getParameter("user_tel2");
		String user_lv = request.getParameter("user_lv");
		

		JSONObject res_json = new JSONObject();
		int res = oram.modify_user(memcd,  user_pw,  user_nm,  user_email,  user_tel1, user_tel2,  user_lv);
		res_json.put("res", res);
		out.print(res_json);

	}

}

