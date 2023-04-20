package com.gaon.nifou.v3.ctrl.common;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.gaon.nifou.v3.trans_ora_manager;
import com.gaon.nifou.v3.trans_seed_manager;

/**
 * Servlet implementation class test
 */
@WebServlet("/common/seed.gaon")
public class seed extends HttpServlet {

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		trans_seed_manager sm = new trans_seed_manager();
		
		String req = request.getParameter("req");

		out.print("decode : " + sm.seed_dec_card(req));	
		out.print("<br>incode : " + sm.seed_enc_str(req));
		
	}

}
