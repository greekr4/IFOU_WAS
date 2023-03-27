package com.gaon.nifou.v3;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

public class util_manager {

	
	public HashMap get_where_qry(HttpServletRequest request) {
		HashMap<String, String> qrymap = new HashMap<String, String>();
		qrymap.put("sappdd", request.getParameter("sappdd"));
		qrymap.put("eappdd", request.getParameter("eappdd"));
		qrymap.put("appno", request.getParameter("appno"));
		qrymap.put("pid", request.getParameter("pid"));
		qrymap.put("cid", request.getParameter("cid"));
		qrymap.put("samt", request.getParameter("samt"));
		qrymap.put("eamt", request.getParameter("eamt"));
		qrymap.put("acqcd", request.getParameter("acqcd"));
		qrymap.put("cardtp", request.getParameter("cardtp"));
		qrymap.put("cardno", request.getParameter("cardno"));
		qrymap.put("ovseacard", request.getParameter("ovseacard"));
		qrymap.put("depcd", request.getParameter("depcd"));
		qrymap.put("tid", request.getParameter("tid"));
		qrymap.put("mid", request.getParameter("mid"));
		qrymap.put("rhk", request.getParameter("rhk"));
		qrymap.put("pgb", request.getParameter("pgb"));
		qrymap.put("authstat", request.getParameter("authstat"));
		qrymap.put("transtat", request.getParameter("transtat"));
		qrymap.put("depstat", request.getParameter("depstat"));
		qrymap.put("cashid", request.getParameter("cashid"));
		qrymap.put("cashtp", request.getParameter("cashtp"));
		qrymap.put("appmsg", request.getParameter("appmsg"));
		qrymap.put("sexpdd", request.getParameter("sexpdd"));
		qrymap.put("eexpdd", request.getParameter("eexpdd"));
		qrymap.put("banstat", request.getParameter("banstat"));
		qrymap.put("reqstat", request.getParameter("reqstat"));
		qrymap.put("sreqdd", request.getParameter("sreqdd"));
		qrymap.put("ereqdd", request.getParameter("ereqdd"));
		qrymap.put("orgcd", request.getParameter("orgcd"));
		
		return qrymap;
	}
	
}
