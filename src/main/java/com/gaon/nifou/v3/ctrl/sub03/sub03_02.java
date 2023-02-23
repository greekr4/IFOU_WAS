package com.gaon.nifou.v3.ctrl.sub03;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gaon.nifou.v3.trans_ora_manager;

@WebServlet("/sub03/02.gaon")
public class sub03_02 extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
		JSONArray jsonary = new JSONArray();
		PrintWriter out = response.getWriter();

		String orgcd = request.getParameter("orgcd");
		String pages = request.getParameter("pages");

		out.print(oram.get_sub0302(oram.get_tb_sys_domain(orgcd, pages)));

	}

}
