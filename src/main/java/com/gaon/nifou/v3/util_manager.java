package com.gaon.nifou.v3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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
		
		qrymap.put("tranidx", request.getParameter("tranidx"));
		qrymap.put("add_cid", request.getParameter("add_cid"));
		qrymap.put("add_cd", request.getParameter("add_cd"));
		qrymap.put("add_casher", request.getParameter("add_casher"));
		qrymap.put("appdd", request.getParameter("appdd"));
		qrymap.put("apptm", request.getParameter("apptm"));
		qrymap.put("appgb", request.getParameter("appgb"));
		qrymap.put("acq_cd", request.getParameter("acq_cd"));
		qrymap.put("amount", request.getParameter("amount"));
		qrymap.put("ovsea_card", request.getParameter("ovsea_card"));
		qrymap.put("check_card", request.getParameter("check_card"));
		qrymap.put("tid_update", request.getParameter("tid_update"));
		
		
		return qrymap;
	}
	
	public List<String> make_query(HashMap<String, String> whereqry) {
		
		String orgcd = whereqry.get("orgcd");
		String depcd_where = "";
		String set_where = "";
		String set_where_dep ="";
		String set_where_req ="";
		String set_where_daesa ="";
		
		if(!Objects.equals(whereqry.get("depcd"),null )) {
			depcd_where += " AND DEP_CD = '" + whereqry.get("depcd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("sappdd"),null )) {
			set_where += " AND APPDD >= '" + whereqry.get("sappdd") + "'";
			set_where_dep += " AND APP_DD >= '" + whereqry.get("sappdd") + "'";
			set_where_req += " AND SDATE >= '" + whereqry.get("sappdd") + "'";
			set_where_daesa += " AND T1.APPDD >= '" + whereqry.get("sappdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("eappdd"),null )) {
			set_where += " AND APPDD <= '" + whereqry.get("eappdd") + "'";
			set_where_dep += " AND APP_DD <= '" + whereqry.get("eappdd") + "'";
			set_where_req += " AND EDATE <= '" + whereqry.get("eappdd") + "'";
			set_where_daesa += " AND T1.APPDD <= '" + whereqry.get("eappdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("appno"),null )) {
			set_where += " AND APPNO = '" + whereqry.get("appno") + "'";
			set_where_dep += " AND APP_NO = '" + whereqry.get("appno") + "'";
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
		
		if(!Objects.equals(whereqry.get("cardtp"),null )) {
			set_where += " AND NVL(CHECK_CARD,'N') = '" + whereqry.get("cardtp") + "'";
		}
		
		if(!Objects.equals(whereqry.get("cardno"),null )) {
			set_where += " AND CARDNO = '" + whereqry.get("cardno") + "'";
			set_where_dep += " AND CARD_NO = '" + whereqry.get("cardno") + "'";
		}
		
		if(!Objects.equals(whereqry.get("ovseacard"),null )) {
			set_where += " AND OVSEA_CARD = '" + whereqry.get("ovseacard") + "'";
			set_where_dep += " AND OVSEA_CARD = '" + whereqry.get("ovseacard") + "'";
		}
		
		if(!Objects.equals(whereqry.get("tid"),null )) {
			set_where += " AND TID = '" + whereqry.get("tid") + "'";
			set_where_dep += " AND TID = '" + whereqry.get("tid") + "'";
		}
		
		if(!Objects.equals(whereqry.get("authstat"),null )) {
			set_where += " AND APPGB = '" + whereqry.get("authstat") + "'";
			set_where_dep += " AND RTN_CD = '" + whereqry.get("authstat") + "'";
		}
		
		if(!Objects.equals(whereqry.get("sexpdd"),null )) {
			set_where_dep += " AND EXP_DD >= '" + whereqry.get("sexpdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("eexpdd"),null )) {
			set_where_dep += " AND EXP_DD <= '" + whereqry.get("eexpdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("sreqdd"),null )) {
			set_where_dep += " AND REQ_DD >= '" + whereqry.get("sreqdd") + "'";
			set_where_req += " AND to_char(regdate, 'yyyyMMdd') >= '" + whereqry.get("sreqdd") + "'";
		}
		
		if(!Objects.equals(whereqry.get("ereqdd"),null )) {
			set_where_dep += " AND REQ_DD <= '" + whereqry.get("ereqdd") + "'";
			set_where_req += " AND to_char(regdate, 'yyyyMMdd') <= '" + whereqry.get("ereqdd") + "'";
		}
				
		if (!Objects.equals(whereqry.get("transtat"), null)) {

		    // transtat 값을 쉼표(,)로 분리하여 배열로 변환
		    String[] transtatValues = whereqry.get("transtat").split(",");

		    // transtat 배열의 값들을 순회하며 쿼리에 추가
		    StringBuilder conditionBuilder = new StringBuilder();
		    for (String value : transtatValues) {
		        value = value.trim(); // 공백 제거

		        if (value.equals("1")) {
		            conditionBuilder.append(" (APPGB = 'A' AND (OAPP_AMT IS NULL OR OAPP_AMT = 0))");
		        } else if (value.equals("2")) {
		            conditionBuilder.append(" ((APPGB = 'A' AND OAPP_AMT = APPDD) OR (APPGB = 'C' AND OAPPDD = APPDD))");
		        } else if (value.equals("3")) {
		            conditionBuilder.append(" ((APPGB = 'A' AND OAPP_AMT <> APPDD) OR (APPGB = 'C' AND OAPPDD <> APPDD))");
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
		            conditionBuilder.append(" (APPGB = 'A' AND (OAPP_AMT IS NULL OR OAPP_AMT = 0))");
		        } else if (value.equals("2")) {
		            conditionBuilder.append(" ((APPGB = 'A' AND OAPP_AMT = APPDD) OR (APPGB = 'C' AND OAPPDD = APPDD))");
		        } else if (value.equals("3")) {
		            conditionBuilder.append(" ((APPGB = 'A' AND OAPP_AMT <> APPDD) OR (APPGB = 'C' AND OAPPDD <> APPDD))");
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
		inputHashMap.put("set_where_daesa", set_where_daesa);
		
	    List<String> resultList = new ArrayList<>();
        resultList.add(inputHashMap.get("orgcd"));
        resultList.add(inputHashMap.get("depcd_where"));
        resultList.add(inputHashMap.get("set_where"));
        resultList.add(inputHashMap.get("set_where_dep"));
        resultList.add(inputHashMap.get("set_where_req"));
        resultList.add(inputHashMap.get("set_where_daesa"));
		///WHERE QRY END///		
		return resultList;
	}
	
	public String getString(String obj) {
		String res = (Objects.isNull(obj)) ? "" : obj;
		return res;
	}
	
	
public List<String> insert_query(HashMap<String, String> whereqry) {
		
	   LocalDateTime currentDateTime = LocalDateTime.now();
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
       String SEQ_DATE = currentDateTime.format(formatter);
		
       Random random = new Random();
       int randomNumber = random.nextInt(1000000) + 1;
       String SEQ_RANDOM_NO = String.format("%06d", randomNumber);
       
		String orgcd = whereqry.get("orgcd");
		String depcd_where = "";
		String set_icvan = "";
		String set_depdata ="";
		String update_icvan = "";


		
		//INSERT INTO GLOB_MNG_ICVAN
		//(SEQNO, BIZNO, TID, MID, VANGB, MDATE, SVCGB, TRANIDX, APPGB, ENTRYMD, APPDD, APPTM, APPNO, CARDNO, HALBU, CURRENCY, AMOUNT, AMT_UNIT, AMT_TIP,
		//AMT_TAX, ISS_CD, ISS_NM, ACQ_CD, ACQ_NM, AUTHCD, AUTHMSG, CARD_CODE, CHECK_CARD, OVSEA_CARD, TLINEGB, SIGNCHK, DDCGB, EXT_FIELD, OAPPNO, OAPPDD,
		//OAPPTM, OAPP_AMT, ADD_GB, ADD_CID, ADD_CD, ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, SECTION_NO, SERVID, DPFLAG, DEPOREQDD, REQDEPTH, 
		//TRAN_STAT, DEPOSEQ, CTR_RST, CTR_DT, ADD_DEPT, MEDI_GOODS)
		//VALUES('2018010300696A154528', '2128207728', '0001', '16111611', '02', '20180103', 'CC', NULL, 'A', 'K', '20230222', '154526', '147387567', 'f91dee7dd24cbc9c9db8dd83af3a92ddcd2ec7b5c8acefa7cf5c27e51152566f', NULL, '410', 2000000000, 0, 0, NULL, '0098', NULL, '1101', NULL, '0000', '정상승인', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '9', '7', NULL, '3', NULL, 'BBQ', '2', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '3', 'ABC');

		

		
		set_icvan += "'"+SEQ_DATE +"A" + SEQ_RANDOM_NO+"', " ;  //SEQNO
		set_icvan += "NULL, " ;  //BIZNO
		set_icvan += (whereqry.get("tid") != null) ? "'"+whereqry.get("tid") + "', " : "NULL, "; //TID
		set_icvan += (whereqry.get("mid") != null) ? "'"+whereqry.get("mid") + "', " : "NULL, "; //MID		
		set_icvan += (whereqry.get("vangb") != null) ? "'"+whereqry.get("vangb") + "', " : "NULL, "; // VANGB
		set_icvan += "NULL, " ;  //MDATE
		set_icvan += "NULL, " ;  //SVCGB
		set_icvan += (whereqry.get("tranidx") != null) ? "'"+whereqry.get("tranidx") + "', " : "NULL, "; //TRANIDX
		set_icvan += (whereqry.get("appgb") != null) ? "'"+whereqry.get("appgb") + "', " : "NULL, "; //APPGB
		set_icvan += (whereqry.get("entrymd") != null) ? "'"+whereqry.get("entrymd") + "', " : "NULL, "; //ENTRYMD
		set_icvan += (whereqry.get("appdd") != null) ? "'"+whereqry.get("appdd") + "', " : "NULL, ";  //APPDD
		set_icvan += (whereqry.get("apptm") != null) ? "'"+whereqry.get("apptm") + "', " : "NULL, "; //APPTM
		set_icvan += (whereqry.get("appno") != null) ? "'"+whereqry.get("appno") + "', " : "NULL, ";  //APPNO
		set_icvan += (whereqry.get("cardno") != null) ? "'"+whereqry.get("cardno") + "', " : "NULL, "; //CARDNO
		set_icvan += (whereqry.get("halbu") != null) ? "'"+whereqry.get("halbu") + "', " : "NULL, ";  //HALBU
		set_icvan += "NULL, " ;  //CURRENCY
		set_icvan += (whereqry.get("amount") != null) ? "'"+whereqry.get("amount") + "', " : "NULL, ";  //AMOUNT
		set_icvan += (whereqry.get("amt_unit") != null) ? "'"+whereqry.get("amt_unit") + "', " : "NULL, ";  //AMT_UNIT
		set_icvan += (whereqry.get("amt_tip") != null) ? "'"+whereqry.get("amt_tip") + "', " : "NULL, ";  //AMT_TIP		
		set_icvan += (whereqry.get("amt_tax") != null) ? "'"+whereqry.get("amt_tax") + "', " : "NULL, ";   //AMT_TAX
		set_icvan += "NULL, " ;  //ISS_CD
		set_icvan += "NULL, " ;  //ISS_NM
		set_icvan += (whereqry.get("acq_cd") != null) ? "'"+whereqry.get("acq_cd") + "', " : "NULL, "; //ACQ_CD
		set_icvan += "NULL, " ;  //ACQ_NM
		set_icvan += "NULL, " ;  //AUTHCD
		set_icvan += "NULL, " ;  //AUTHMSG
		set_icvan += "NULL, " ;  //CARD_COD
		set_icvan += (whereqry.get("check_card") != null) ? "'"+whereqry.get("check_card") + "', " : "NULL, "; //CHECK_CARD
		set_icvan += (whereqry.get("ovsea_card") != null) ? "'"+whereqry.get("ovsea_card") + "', " : "NULL, ";  //OVSEA_CARD
		set_icvan += "NULL, " ;  //TLINEGB
		set_icvan += "NULL, " ;  //SIGNCHK
		set_icvan += "NULL, " ;  //DDCGB
		set_icvan += "NULL, " ;  //EXT_FIELD
		set_icvan += (whereqry.get("oappno") != null) ? "'"+whereqry.get("oappno") + "', " : "NULL, "; //OAPPNO
		set_icvan += (whereqry.get("oappdd") != null) ? "'"+whereqry.get("oappdd") + "', " : "NULL, ";  //OAPPDD		
		set_icvan += (whereqry.get("oapptm") != null) ? "'"+whereqry.get("oapptm") + "', " : "NULL, "; //OAPPTM
		set_icvan += "NULL, " ;  //OAPP_AMT
		set_icvan += (whereqry.get("add_gb") != null) ? "'"+whereqry.get("add_gb") + "', " : "NULL, ";   //ADD_GB
		set_icvan += (whereqry.get("add_cid") != null) ? "'"+whereqry.get("add_cid") + "', " : "NULL, ";  //ADD_CID
		set_icvan += (whereqry.get("add_cd") != null) ? "'"+whereqry.get("add_cd") + "', " : "NULL, "; //ADD_CD
		set_icvan += "NULL, " ;  //ADD_RECP
		set_icvan += "NULL, " ;  //ADD_CNT
		set_icvan += (whereqry.get("add_casher") != null) ? "'"+whereqry.get("add_casher") + "', " : "NULL, ";   //ADD_CASHER		
		set_icvan += "NULL, " ;  //ADD_DATE
		set_icvan += "NULL, " ;  //SECTION_NO
		set_icvan += "NULL, " ;  //SERVID
		set_icvan += "NULL, " ;  //DPFLAG
		set_icvan += "NULL, " ;  //DEPOREQDD		
		set_icvan += "NULL, " ;  //REQDEPTH
		set_icvan += "NULL, " ;  //TRAN_STAT
		set_icvan += "NULL, " ;  //DEPOSEQ		
		set_icvan += "NULL, " ;  //CTR_RST
		set_icvan += "NULL, " ;  //CTR_DT
		set_icvan += "NULL, " ;  //ADD_DEPT
		set_icvan += "NULL " ;  //MEDI_GOODS
		
		//INSERT INTO IFOU.TB_MNG_DEPDATA
		//(DEP_SEQ, VAN_GB, DEP_CD, FILE_DD, EXP_DD, MID, CO_TYPE, REQ_DD, RTN_CD, APP_DD, REG_DD, CARD_NO, HALBU, SALE_AMT, RSC_CD, RSV_CD, FEE, 
		//RS_MSG, APP_NO, FILE_NM, TID, TRANIDX, ACQ_CD, ISS_CD, COM_NO, OAPP_DD, AMT_TIP, AMT_TAX, PROC_GB, PROC_DD, DP_CLID, CARDTP, ORGCD, SYSTYPE)
		//VALUES('test1234', '03', '00063216', '20200209', '20230316', '16111611', '410', '20230223', '60', '20230222', '20200207', '5200450571326739', '00', 8000, '00', '00', 164, '정상완료', '61680612', NULL, '0001', NULL, '1103', '1103', NULL, NULL, NULL, NULL, NULL, 'PROC_DD', NULL, NULL, 'OR026', NULL);

		set_depdata += "NULL, " ;  //DEP_SEQ
		set_depdata += "NULL, " ;  //VAN_GB
		set_depdata += "NULL, " ;  //DEP_CD
		set_depdata += "NULL, " ;  //FILE_DD
		set_depdata += "NULL, " ;  //EXP_DD
		set_depdata += "NULL, " ;  //MID
		set_depdata += "NULL, " ;  //CO_TYPE
		set_depdata += "NULL, " ;  //REQ_DD
		set_depdata += "NULL, " ;  //RTN_CD
		set_depdata += "NULL, " ;  //APP_DD
		set_depdata += "NULL, " ;  //REG_DD
		set_depdata += "NULL, " ;  //CARD_NO
		set_depdata += "NULL, " ;  //HALBU
		set_depdata += "NULL, " ;  //SALE_AMT
		set_depdata += "NULL, " ;  //RSC_CD
		set_depdata += "NULL, " ;  //RSV_CD
		set_depdata += "NULL, " ;  //FEE
		set_depdata += "NULL, " ;  //RS_MSG		
		set_depdata += "NULL, " ;  //APP_NO
		set_depdata += "NULL, " ;  //FILE_NM
		set_depdata += "NULL, " ;  //TID
		set_depdata += "NULL, " ;  //TRANIDX
		set_depdata += "NULL, " ;  //ACQ_CD
		set_depdata += "NULL, " ;  //ISS_CD
		set_depdata += "NULL, " ;  //COM_NO		
		set_depdata += "NULL, " ;  //OAPP_DD
		set_depdata += "NULL, " ;  //AMT_TIP
		set_depdata += "NULL, " ;  //AMT_TAX
		set_depdata += "NULL, " ;  //PROC_GB
		set_depdata += "NULL, " ;  //PROC_DD
		set_depdata += "NULL, " ;  //DP_CLID
		set_depdata += "NULL, " ;  //CARDTP
		set_depdata += "NULL, " ;  //ORGCD
		set_depdata += "NULL " ;  //SYSTYPE
		
		
		//UPDATE IFOU.GLOB_MNG_ICVAN
		//SET SEQNO='20230301164520A2311402', BIZNO='1208201328', TID='8360600001', MID='9153241617', VANGB='02', MDATE='20230301', SVCGB='CC', 
		//TRANIDX='011712436544', APPGB='A', ENTRYMD='S', APPDD='20230301', APPTM='170109', APPNO='43119988', 
		//CARDNO='4b9adbdf57ee925da79a66a7804501a57fafaf62c44d881b4834f68673e1eb88', HALBU='00', CURRENCY='410', AMOUNT=10000, AMT_UNIT=10000, 
		//AMT_TIP=0, AMT_TAX=0, ISS_CD='38', ISS_NM=NULL, ACQ_CD='33', ACQ_NM=NULL, AUTHCD='0000', AUTHMSG='�������', CARD_CODE='SP', 
		//CHECK_CARD='N', OVSEA_CARD='N', TLINEGB=NULL, SIGNCHK=NULL, DDCGB='1', 
		//EXT_FIELD='AA010001595835INS   04827                                                                 HANYSEOUL', 
		//OAPPNO='43119988', OAPPDD=NULL, OAPPTM='N', OAPP_AMT=0, ADD_GB='I', ADD_CID='0001595835', ADD_CD='NS', ADD_RECP=NULL, ADD_CNT=NULL, 
		//ADD_CASHER='04827', ADD_DATE=NULL, SECTION_NO=NULL, SERVID='A', DPFLAG=NULL, DEPOREQDD=NULL, REQDEPTH=NULL, TRAN_STAT=NULL, DEPOSEQ=NULL, 
		//CTR_RST=NULL, CTR_DT=NULL, ADD_DEPT=NULL, MEDI_GOODS=NULL;
		update_icvan += ""; // SEQNO
		update_icvan += ""; // BIZNO		
		update_icvan += (whereqry.get("tid_update") != null) ? "TID='"+whereqry.get("tid_update") + "'" : ""; //TID
		update_icvan += (whereqry.get("mid") != null) ? "TID='"+whereqry.get("mid") + "'" : ""; // MID
		update_icvan += ""; // VANGB
		update_icvan += ""; // MDATE
		update_icvan += ""; // SVCGB
		update_icvan += (whereqry.get("tranidx") != null) ? ", TRANIDX='"+whereqry.get("tranidx") + "'" : ""; // TRANIDX
		update_icvan += ""; // APPGB
		update_icvan += ""; // ENTRYMD
		update_icvan += (whereqry.get("appdd") != null) ? ", APPDD='"+whereqry.get("appdd") + "'" : ""; // APPDD
		update_icvan += (whereqry.get("apptm") != null) ? ", APPTM='"+whereqry.get("apptm") + "'" : ""; // APPTM
		update_icvan += (whereqry.get("appno") != null) ? ", APPNO='"+whereqry.get("appno")  + "'" : ""; // APPNO
		update_icvan += (whereqry.get("cardno") != null) ? ", CARDNO='"+whereqry.get("cardno")  + "'" : ""; // CARDNO
		update_icvan += ""; // HALBU
		update_icvan += ""; // CURRENCY
		//update_icvan += (whereqry.get("amount") != null) ? ", AMOUNT='"+whereqry.get("amount")  + "'" : ""; // AMOUNT
		update_icvan += ""; // AMT_UNIT
		update_icvan += ""; // AMT_TIP
		update_icvan += ""; // AMT_TAX
		update_icvan += ""; // ISS_CD
		update_icvan += ""; // ISS_NM
		//update_icvan += (whereqry.get("acq_cd") != null) ? ", ACQ_CD='"+whereqry.get("acq_cd") + "'" : ""; // ACQ_CD
		update_icvan += ""; // ACQ_NM
		update_icvan += ""; // AUTHCD
		update_icvan += ""; // AUTHMSG
		update_icvan += ""; // CARD_CODE
		//update_icvan += (whereqry.get("check_card") != null) ? ", CHECK_CARD='"+whereqry.get("check_card") + "'" : ""; // CHECK_CARD
		update_icvan += (whereqry.get("ovsea_card") != null) ? ", OVSEA_CARD='"+whereqry.get("ovsea_card") + "'" : ""; // OVSEA_CARD
		update_icvan += ""; // TLINEGB
		update_icvan += ""; // SIGNCHK
		update_icvan += ""; // DDCGB
		update_icvan += ""; // EXT_FIELD
		update_icvan += ""; // OAPPNO
		update_icvan += ""; // OAPPDD
		update_icvan += ""; // OAPPTM
		update_icvan += ""; // OAPP_AMT
		update_icvan += (whereqry.get("add_gb") != null) ? ", ADD_GB='"+whereqry.get("add_gb") + "'" : ""; // ADD_GB
		update_icvan += (whereqry.get("add_cid") != null) ? ", ADD_CID='"+whereqry.get("add_cid") + "'" : ""; // ADD_CID
		update_icvan += (whereqry.get("add_cd") != null) ? ", ADD_CD='"+whereqry.get("add_cd")  + "'" : ""; // ADD_CD
		update_icvan += ""; // ADD_RECP
		update_icvan += ""; // ADD_CNT
		update_icvan += (whereqry.get("add_casher") != null) ? ", ADD_CASHER='"+whereqry.get("add_casher") + "'" : ""; // ADD_CASHER
		update_icvan += ""; // ADD_DATE
		update_icvan += ""; // SECTION_NO
		update_icvan += ""; // SERVID
		update_icvan += ""; // DPFLAG
		update_icvan += ""; // DEPOREQDD
		update_icvan += ""; // REQDEPTH
		update_icvan += ""; // TRAN_STAT
		update_icvan += ""; // DEPOSEQ		
		update_icvan += ""; // CTR_RST
		update_icvan += ""; // CTR_DT
		update_icvan += ""; // APP_DEPT
		update_icvan += ""; // MEDI_GOODS
		
		update_icvan += "WHERE AUTHCD='0000' "; // WHERE		
		
		update_icvan += (whereqry.get("tranidx") != null) ? " AND TRANIDX='"+whereqry.get("tranidx")  + "'" : ""; // WHERE TRANIDX
		update_icvan += (whereqry.get("add_gb") != null) ? " AND ADD_GB='"+whereqry.get("add_gb")  + "'" : ""; // WHERE APPGB
		update_icvan += (whereqry.get("appdd") != null) ? " AND APPDD='"+whereqry.get("appdd")  + "'" : "";  // WHERE APPDD
		update_icvan += (whereqry.get("appno") != null) ? " AND APPNO='"+whereqry.get("appno")  + "'" : "";  // WHERE APPNO
		//update_icvan += (whereqry.get("amount") != null) ? " AND AMOUNT='"+whereqry.get("amount") + "'" : ""; // WHERE AMOUNT
		
		HashMap<String, String> inputHashMap= new HashMap<>();
		inputHashMap.put("orgcd", orgcd);
		inputHashMap.put("depcd_where", depcd_where);
		inputHashMap.put("set_icvan", set_icvan);
		inputHashMap.put("set_depdata", set_depdata);
		inputHashMap.put("update_icvan", update_icvan);
		
	    List<String> resultList = new ArrayList<>();
        resultList.add(inputHashMap.get("orgcd"));
        resultList.add(inputHashMap.get("depcd_where"));
        resultList.add(inputHashMap.get("set_icvan"));
        resultList.add(inputHashMap.get("set_depdata"));
        resultList.add(inputHashMap.get("update_icvan"));

		///WHERE QRY END///		
		return resultList;
	}
	
}
