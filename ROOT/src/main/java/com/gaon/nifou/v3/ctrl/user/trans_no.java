package com.gaon.nifou.v3.ctrl.user;

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
 * Servlet implementation class trans_no
 */
@WebServlet("/user/trans_no.gaon")
public class trans_no extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        JSONObject jsonob = new JSONObject();
		PrintWriter out = response.getWriter();
		trans_ora_manager oram = new trans_ora_manager();
		String userid = request.getParameter("userid");
		
		jsonob.put("trans_no", oram.get_user_trans_no(userid));
		out.print(jsonob);
	}

}
