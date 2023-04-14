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
		String set_where_dep ="";
		String set_where_req ="";
		
		if(!Objects.equals(whereqry.get("depcd"),null )) {
			depcd_where += " AND DEP_CD = '" + whereqry.get("depcd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("sappdd"),null )) {
			set_where += " AND APPDD >= '" + whereqry.get("sappdd") + "'";
			set_where_dep += " AND APP_DD >= '" + whereqry.get("sappdd") + "'";
			set_where_req += " AND SDATE >= '" + whereqry.get("sappdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("eappdd"),null )) {
			set_where += " AND APPDD <= '" + whereqry.get("eappdd") + "'";
			set_where_dep += " AND APP_DD <= '" + whereqry.get("eappdd") + "'";
			set_where_req += " AND EDATE <= '" + whereqry.get("eappdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("appno"),null )) {
			set_where += " AND APPNO = '" + whereqry.get("appno") + "'";
			set_where_dep += " AND APP_NO = '" + whereqry.get("appno") + "'";
		}
		
		if(!Objects.equals(whereqry.get("banstat"),null )) {
			if(Objects.equals(whereqry.get("banstat"),"ba" )){
				set_where_dep += " AND RTN_CD = '61'";
			}else {
				set_where_dep += " AND RTN_CD = '64'";
			}			
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
			set_where_dep += " AND SALE_AMT >= '" + whereqry.get("samt") + "'";
		}
		
		if(!Objects.equals(whereqry.get("eamt"),null )) {
			set_where += " AND AMOUNT <= '" + whereqry.get("eamt") + "'";
			set_where_dep += " AND SALE_AMT <= '" + whereqry.get("eamt") + "'";
		}
		
		if(!Objects.equals(whereqry.get("acqcd"),null )) {
			set_where += " AND ACQ_CD = '" + whereqry.get("acqcd") + "'";
			set_where_dep += " AND ACQ_CD = '" + whereqry.get("acqcd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("cashid"),null )) {
			set_where += " AND CARDNO = '" + whereqry.get("cashid") + "'";
		}
		
		if(!Objects.equals(whereqry.get("cashtp"),null )) {
			if(!Objects.equals(whereqry.get("cashtp"),"2" )){
				set_where += " AND DDCGB = '" + whereqry.get("cashtp") + "'";
			}else {
				set_where += " AND DDCGB IS NULL";
			}
			
		}
		
		if(!Objects.equals(whereqry.get("cardtp"),null )) {
			set_where += " AND NVL(CHECK_CARD,'N') = '" + whereqry.get("cardtp") + "'";
		}
		
		if(!Objects.equals(whereqry.get("cardno"),null )) {
			set_where += " AND CARDNO = '" + whereqry.get("cardno") + "'";
			set_where_dep += " AND CARD_NO = '" + whereqry.get("cardno") + "'";
		}
		
		if(!Objects.equals(whereqry.get("ovseacard"),null )) {
			set_where += " AND OVSEA_CARD = '" + whereqry.get("ovseacard") + "'";
		}
		
		if(!Objects.equals(whereqry.get("tid"),null )) {
			set_where += " AND TID = '" + whereqry.get("tid") + "'";
			set_where_dep += " AND TID = '" + whereqry.get("tid") + "'";
		}
		
		if(!Objects.equals(whereqry.get("mid"),null )) {
			set_where += " AND MID = '" + whereqry.get("mid") + "'";
			set_where_dep += " AND MID = '" + whereqry.get("mid") + "'";
		}
		
		if(!Objects.equals(whereqry.get("authstat"),null )) {
			set_where += " AND APPGB = '" + whereqry.get("authstat") + "'";
			//set_where_dep += " AND RTN_CD = '" + whereqry.get("authstat") + "'";  //0302에서 걸려서 죽임
		}
		
		if(!Objects.equals(whereqry.get("sexpdd"),null )) {
			set_where_dep += " AND EXP_DD >= '" + whereqry.get("sexpdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("eexpdd"),null )) {
			set_where_dep += " AND EXP_DD <= '" + whereqry.get("eexpdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("sreqdd"),null )) {
			set_where_dep += " AND REQ_DD >= '" + whereqry.get("sreqdd") + "'";
			set_where_req += " AND to_char(REGDATE, 'yyyyMMdd')  >= " + whereqry.get("sreqdd") + "";
		}
		
		if(!Objects.equals(whereqry.get("ereqdd"),null )) {
			set_where_dep += " AND REQ_DD <= '" + whereqry.get("ereqdd") + "'";
			set_where_req += " AND to_char(REGDATE, 'yyyyMMdd')  <= " + whereqry.get("sreqdd") + "";
		}
				
		if (!Objects.equals(whereqry.get("transtat"), null)) {

		    // transtat 값을 쉼표(,)로 분리하여 배열로 변환
		    String[] transtatValues = whereqry.get("transtat").split(",");

		    // transtat 배열의 값들을 순회하며 쿼리에 추가
		    StringBuilder conditionBuilder = new StringBuilder();
		    for (String value : transtatValues) {
		        value = value.trim(); // 공백 제거

		        if (value.equals("1")) {
		            conditionBuilder.append(" (APPGB = 'A' AND (OAPPDD IS NULL OR OAPP_AMT = 0))");
		        } else if (value.equals("2")) {
		            conditionBuilder.append(" ((APPGB = 'A' AND OAPPDD = APPDD) OR (APPGB = 'C' AND OAPPDD = APPDD))");
		        } else if (value.equals("3")) {
		            conditionBuilder.append(" ((APPGB = 'A' AND OAPPDD <> APPDD) OR (APPGB = 'C' AND OAPPDD <> APPDD))");
		        }

		        // 다음 조건을 위해 OR 추가
		        if (!value.equals(transtatValues[transtatValues.length - 1])) {
		            conditionBuilder.append(" OR");
		        }
		    }

		    // 완성된 조건을 set_where에 추가
		    if (conditionBuilder.length() > 0) {
		        set_where += " AND (" + conditionBuilder.toString().trim().replaceAll("\\s+OR$", "") + ")";
		    }
		}

		if (!Objects.equals(whereqry.get("depstat"), null)) {

		    // transtat 값을 쉼표(,)로 분리하여 배열로 변환
		    String[] transtatValues = whereqry.get("depstat").split(",");

		    // transtat 배열의 값들을 순회하며 쿼리에 추가
		    StringBuilder conditionBuilder = new StringBuilder();
		    for (String value : transtatValues) {
		        value = value.trim(); // 공백 제거

		        if (value.equals("1")) {
		            conditionBuilder.append(" T2.RTN_CD IN('60', '67')");
		        } else if (value.equals("2")) {
		            conditionBuilder.append(" T2.RTN_CD IN('61', '64')");
		        } else if (value.equals("3")) {
		            conditionBuilder.append(" T2.RTN_CD IS NULL");
		        }

		        // 다음 조건을 위해 OR 추가
		        if (!value.equals(transtatValues[transtatValues.length - 1])) {
		            conditionBuilder.append(" OR");
		        }
		    }

		    // 완성된 조건을 set_where에 추가
		    if (conditionBuilder.length() > 0) {
		        set_where += " AND (" + conditionBuilder.toString().trim().replaceAll("\\s+OR$", "") + ")";
		    }
		}

		
		
		
			
			
			
		
		
//		미작업
//		if(!Objects.equals(whereqry.get("depstat"),null )) {
//			set_where += " AND APP_GB = '" + whereqry.get("depstat") + "'";
//		}
		
		HashMap<String, String> inputHashMap= new HashMap<>();
		inputHashMap.put("orgcd", orgcd);
		inputHashMap.put("depcd_where", depcd_where);
		inputHashMap.put("set_where", set_where);
		inputHashMap.put("set_where_dep", set_where_dep);
		inputHashMap.put("set_where_req", set_where_req);
		
	    List<String> resultList = new ArrayList<>();
        resultList.add(inputHashMap.get("orgcd"));
        resultList.add(inputHashMap.get("depcd_where"));
        resultList.add(inputHashMap.get("set_where"));
        resultList.add(inputHashMap.get("set_where_dep"));
        resultList.add(inputHashMap.get("set_where_req"));
		
		///WHERE QRY END///		
		return resultList;
	}
	
	public String getString(String obj) {
		String res = (Objects.isNull(obj)) ? "" : obj;
		return res;
	}
	
}
