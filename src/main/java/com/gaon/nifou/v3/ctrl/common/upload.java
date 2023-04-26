package com.gaon.nifou.v3.ctrl.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.gaon.nifou.v3.trans_ora_manager;
import com.gaon.nifou.v3.util_manager;

/**
 * Servlet implementation class test
 */
@WebServlet("/common/upload.gaon")
public class upload extends HttpServlet {

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		trans_ora_manager oram = new trans_ora_manager();
        JSONArray jsonary = new JSONArray();
		PrintWriter out = response.getWriter();
		
		String[] inputArray = request.getParameter("inputArray").split(",");

		String exp_dd = inputArray[0].trim();
		String acc_txt = inputArray[1].trim();
		String mid = inputArray[2].trim();
		int    exp_amt = Integer.parseInt(inputArray[3].trim());
		String update_dd = inputArray[4].trim();
		
		String orgcd = request.getParameter("orgcd");
		String pages = request.getParameter("pages");		
		String DEBUG = request.getParameter("DEBUG");
		
		util_manager um = new util_manager();
		HashMap<String, String> whereqry = um.get_where_qry(request);		
		
		out.print(oram.insert_bankdata(DEBUG,whereqry,exp_dd,acc_txt,mid,exp_amt,update_dd));		
		
	}

}
