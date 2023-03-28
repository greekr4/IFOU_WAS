package com.gaon.nifou.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
	
	public List<String> make_query(HashMap<String, String> whereqry) {
		
		String orgcd = whereqry.get("orgcd");
		String depcd_where = "";
		String set_where = "";
		
		if(!Objects.equals(whereqry.get("depcd"),null )) {
			depcd_where += " AND DEP_CD = '" + whereqry.get("depcd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("sappdd"),null )) {
			set_where += " AND APPDD >= '" + whereqry.get("sappdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("eappdd"),null )) {
			set_where += " AND APPDD <= '" + whereqry.get("eappdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("appno"),null )) {
			set_where += " AND APPNO = '" + whereqry.get("appno") + "'";
		}
		
		if(!Objects.equals(whereqry.get("pid"),null )) {
			set_where += " AND ADD_CID = '" + whereqry.get("pid") + "'";
		}
		
		if(!Objects.equals(whereqry.get("cid"),null )) {
			set_where += " AND ADD_CASHER = '" + whereqry.get("cid") + "'";
		}
		
		if(!Objects.equals(whereqry.get("pgb"),null )) {
			set_where += " AND ADD_GB = '" + whereqry.get("pgb") + "'";
		}
		
		if(!Objects.equals(whereqry.get("rhk"),null )) {
			set_where += " AND ADD_CD = '" + whereqry.get("rhk") + "'";
		}
		
		if(!Objects.equals(whereqry.get("samt"),null )) {
			set_where += " AND AMOUNT >= '" + whereqry.get("samt") + "'";
		}
		
		if(!Objects.equals(whereqry.get("eamt"),null )) {
			set_where += " AND AMOUNT <= '" + whereqry.get("eamt") + "'";
		}
		
		if(!Objects.equals(whereqry.get("acqcd"),null )) {
			set_where += " AND ACQ_CD = '" + whereqry.get("acqcd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("cardtp"),null )) {
			set_where += " AND NVL(CHECK_CARD,'N') = '" + whereqry.get("cardtp") + "'";
		}
		
		if(!Objects.equals(whereqry.get("cardno"),null )) {
			set_where += " AND CARDNO = '" + whereqry.get("cardno") + "'";
		}
		
		if(!Objects.equals(whereqry.get("ovseacard"),null )) {
			set_where += " AND OVSEA_CARD = '" + whereqry.get("ovseacard") + "'";
		}
		
		if(!Objects.equals(whereqry.get("tid"),null )) {
			set_where += " AND TID = '" + whereqry.get("tid") + "'";
		}
		
		if(!Objects.equals(whereqry.get("authstat"),null )) {
			set_where += " AND APP_GB = '" + whereqry.get("authstat") + "'";
		}
		
//		미작업
//		if(!Objects.equals(whereqry.get("transtat"),null )) {
//			set_where += " AND APP_GB = '" + whereqry.get("transtat") + "'";
//		}
		
		
//		미작업
//		if(!Objects.equals(whereqry.get("depstat"),null )) {
//			set_where += " AND APP_GB = '" + whereqry.get("depstat") + "'";
//		}
		
		HashMap<String, String> inputHashMap= new HashMap<>();
		inputHashMap.put("orgcd", orgcd);
		inputHashMap.put("depcd_where", depcd_where);
		inputHashMap.put("set_where", set_where);
		
	    List<String> resultList = new ArrayList<>();
        resultList.add(inputHashMap.get("orgcd"));
        resultList.add(inputHashMap.get("depcd_where"));
        resultList.add(inputHashMap.get("set_where"));
		
		///WHERE QRY END///		
		return resultList;
	}
	
}
