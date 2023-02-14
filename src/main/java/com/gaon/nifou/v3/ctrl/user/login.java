package com.gaon.nifou.v3.ctrl.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gaon.nifou.v3.trans_ora_manager;


@WebServlet("/user/login.gaon")
public class login extends HttpServlet {



	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        JSONObject jsonob = new JSONObject();
		PrintWriter out = response.getWriter();
		trans_ora_manager oram = new trans_ora_manager();
		String userid = request.getParameter("userid");
		String userpw = request.getParameter("userpw");

		String org_cd = "";
		String dep_cd = "";
		
		String rtnmsg = "9999"; //비정상
		if(oram.get_user_cnt(userid) > 0) {
			String[] user_info = oram.get_user_info(userid);
			if (user_info[0].equals(userpw)) {
				rtnmsg = "0000"; //성공
				dep_cd = user_info[1];
				org_cd = user_info[2];
			}else rtnmsg = "0001"; //비밀번호 오류
			
		}else {
			rtnmsg = "0002"; //아이디 오류
		}
		
		jsonob.put("RTN_MSG",rtnmsg);
		if(rtnmsg.equals("0000")) {
			String[] uauth = oram.get_user_uauth(userid);
			String uauth_base64 = "";
			for(int i=0;i<uauth.length;i++) {
				if(i!=uauth.length-1) {
					uauth_base64 += uauth[i]+"|";
				}else uauth_base64 += uauth[i];
			}
			jsonob.put("uauth", uauth_base64);
		}
		
		out.print(jsonob);

	}

}


