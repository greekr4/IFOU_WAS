package com.gaon.nifou.v3.ctrl.common;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.gaon.nifou.v3.trans_ora_manager;

/**
 * Servlet implementation class get_depcd
 */
@WebServlet("/common/get_main_dashboard3")
public class get_main_dashboard3 extends HttpServlet {
	//¿ù¸ÅÃâ
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        JSONObject jsonob = new JSONObject();
		PrintWriter out = response.getWriter();
		trans_ora_manager oram = new trans_ora_manager();
		String orgcd = request.getParameter("orgcd");
		String expdd = request.getParameter("expdd");
		out.print(oram.get_main_daliy_depdata(orgcd,expdd));
	}

}
