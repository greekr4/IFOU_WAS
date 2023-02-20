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

/**
 * Servlet implementation class test
 */
@WebServlet("/common/insert_favorite.gaon")
public class insert_favorite extends HttpServlet {

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
        JSONArray jsonary = new JSONArray();
		PrintWriter out = response.getWriter();
		String userid = request.getParameter("userid");
		String program_seq = request.getParameter("program_seq");
		String sort = request.getParameter("sort");

		
		out.print(oram.insert_favorite(userid,program_seq,sort));
		
		
		
	}

}
