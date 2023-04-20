package com.gaon.nifou.v3.ctrl.sub03;

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


@WebServlet("/sub03/07T.gaon")
public class sub03_07T extends HttpServlet {

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
		String DEBUG = request.getParameter("DEBUG");
		
		util_manager um = new util_manager();
		HashMap<String, String> whereqry = um.get_where_qry(request);
		
		out.print(oram.get_sub0307T(oram.get_tb_sys_domain(orgcd, pages),DEBUG,whereqry));

	}

}
